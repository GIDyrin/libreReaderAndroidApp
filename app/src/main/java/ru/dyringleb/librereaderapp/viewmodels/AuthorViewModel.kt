package ru.dyringleb.librereaderapp.viewmodels

import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.dyringleb.librereaderapp.retrofitConf.Author
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import java.util.concurrent.Executors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment
import ru.dyringleb.librereaderapp.fragments.author.AuthorMeFragment
import ru.dyringleb.librereaderapp.retrofitConf.BookSearchResponse
import ru.dyringleb.librereaderapp.retrofitConf.FullBooksInfo
import ru.dyringleb.librereaderapp.utils.ImageUtils
import ru.dyringleb.librereaderapp.utils.handleErrorResponse

class AuthorViewModel : ViewModel() {
    private val _author = MutableLiveData<Author>()
    val author: LiveData<Author> get() = _author

    private val _books = MutableLiveData<List<FullBooksInfo>>()
    val books: LiveData<List<FullBooksInfo>> get() = _books

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> get() = _currentPage

    private val executor = Executors.newSingleThreadExecutor()

    // Метод для получения книг по автору с пагинацией
    fun fetchBooksByAuthor(authorId: Int, page: Int = 1, new: Boolean = false) {
        if(new){
            _books.postValue(emptyList())
        }
        executor.execute {
            RetrofitClient.apiService.getBooksByAuthor(authorId, page).enqueue(object : Callback<BookSearchResponse> {
                override fun onResponse(call: Call<BookSearchResponse>, response: Response<BookSearchResponse>) {
                    if (response.isSuccessful) {
                        // Сохраняем текущую страницу
                        _currentPage.postValue(page)


                        // Обновляем список книг
                        val newBooks = response.body()?.results ?: emptyList()
                        val updatedBooks = (_books.value ?: emptyList()) + newBooks
                        _books.postValue(updatedBooks)
                    } else {
                        _error.postValue("Ошибка: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                    _error.postValue("Ошибка: ${t.message}")
                }
            })
        }
    }

    // Метод для загрузки следующей страницы
    fun loadNextPage(authorId: Int) {
        val nextPage = (_currentPage.value ?: 0) + 1
        fetchBooksByAuthor(authorId, nextPage)
    }

    fun nulledBooks(){
        _books.postValue(emptyList())
    }

    fun fetchMyAuthorPage() {
        executor.execute {
            // Выполняем сетевой запрос
            RetrofitClient.apiService.getMyAuthorPage().enqueue(object : Callback<Author> {
                override fun onResponse(call: Call<Author>, response: Response<Author>) {
                    if (response.isSuccessful) {

                        _author.postValue(response.body()) // Используем postValue для обновления LiveData
                    } else {
                        _error.postValue("Ошибка: ${response.code()}") // Обновляем ошибки
                    }
                }

                override fun onFailure(call: Call<Author>, t: Throwable) {
                    _error.postValue("Ошибка: ${t.message}") // Обновляем ошибки
                }
            })
        }
    }

    fun updateAuthorInfo(fragment: Fragment, name: String, about: String, selectedImageUri: Uri?) {
        executor.execute {
            // Создаем необходимые части, если они не пустые
            val authorNamePart = if (name.isNotEmpty()) {
                val authorNameRequestBody = RequestBody.create(MediaType.parse("text/plain"), name)
                MultipartBody.Part.createFormData("author_name", null, authorNameRequestBody)
            } else null

            val aboutUserPart = if (about.isNotEmpty()) {
                val aboutUserRequestBody = RequestBody.create(MediaType.parse("text/plain"), about)
                MultipartBody.Part.createFormData("biography", null, aboutUserRequestBody)
            } else null

            // Создаем MultipartBody.Part для изображения (если есть)
            selectedImageUri?.let { uri ->
                ImageUtils.compressAndCropImage(fragment,  uri,"image",) { imagePart ->
                    // Отправка данных на сервер
                    RetrofitClient.apiService.updateAuthor(
                        authorNamePart,
                        aboutUserPart,
                        imagePart // Этот параметр теперь используется после обработки
                    ).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                changeCurrentFragment(fragment.parentFragmentManager, AuthorMeFragment(), null)
                            } else {
                                _error.postValue("Ошибка: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            _error.postValue("Ошибка: сервер недоступен")
                        }
                    })
                }
            } ?: run {
                // Если URI изображения не предоставлен, просто отправляем остальные данные
                RetrofitClient.apiService.updateAuthor(
                    authorNamePart,
                    aboutUserPart,
                    null // Отправляем null для изображения
                ).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            changeCurrentFragment(fragment.parentFragmentManager, AuthorMeFragment(), null)
                        } else {
                            _error.postValue("Ошибка: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        _error.postValue("Ошибка: сервер недоступен")
                    }
                })
            }
        }
    }

    fun registrate(fragment: Fragment, name: String, about: String, selectedImageUri: Uri?) {
        executor.execute {
            // Создаем MultipartBody.Part для текста "Обо мне"
            val aboutUserRequestBody = RequestBody.create(MediaType.parse("text/plain"), about)
            val aboutUserPart = MultipartBody.Part.createFormData("biography", null, aboutUserRequestBody)
            val authorNameRequestBody = RequestBody.create(MediaType.parse("text/plain"), name)
            val authorNamePart = MultipartBody.Part.createFormData("author_name", null, authorNameRequestBody)

            // Создаем MultipartBody.Part для изображения
            selectedImageUri?.let { uri ->
                ImageUtils.compressAndCropImage(fragment, uri, "image") { imagePart ->
                    // Отправка данных на сервер
                    RetrofitClient.apiService.regNewAuthor(
                        authorNamePart,
                        aboutUserPart,
                        imagePart // Используем обработанное изображение
                    ).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(fragment.requireContext(), "Успешно.", Toast.LENGTH_SHORT).show()
                                changeCurrentFragment(fragment.parentFragmentManager, AuthorMeFragment(), null)
                            } else {
                                Toast.makeText(fragment.requireContext(), "Ошибка: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(fragment.requireContext(), "Ошибка: сервер недоступен", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            } ?: run {
                // Если изображение не выбрано, отправляем только название и биографию
                RetrofitClient.apiService.regNewAuthor(
                    authorNamePart,
                    aboutUserPart,
                    null // Отправляем null, если изображение не выбрано
                ).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(fragment.requireContext(), "Успешно.", Toast.LENGTH_SHORT).show()
                            changeCurrentFragment(fragment.parentFragmentManager, AuthorMeFragment(), null)
                        } else {
                            Toast.makeText(fragment.requireContext(), "Ошибка: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(fragment.requireContext(), "Ошибка: сервер недоступен", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    fun postNewBook(fragment: Fragment, file: Uri, title: String, description: String?, year: String, genres: List<Int>) {
        // Получение ContentResolver и определение типа MIME файла
        val contentResolver = fragment.context?.contentResolver
        val mimeType = contentResolver?.getType(file) ?: "application/fb2" // Значение по умолчанию
        val inputStream = fragment.requireContext().contentResolver.openInputStream(file)
        val fileBytes = inputStream?.readBytes()

        // Подготовка файла к отправке
        val requestFile = RequestBody.create(MediaType.parse(mimeType), fileBytes ?: ByteArray(0))
        val filePart = MultipartBody.Part.createFormData("book_file", file.lastPathSegment ?: "book_file", requestFile)

        // Подготовка заголовка книги
        val titleRequestBody = RequestBody.create(MediaType.parse("text/plain"), title)
        val titlePart = MultipartBody.Part.createFormData("book_title", null, titleRequestBody)

        // Подготовка года книги
        val yearRequestBody = RequestBody.create(MediaType.parse("text/plain"), year)
        val yearPart = MultipartBody.Part.createFormData("book_year", null, yearRequestBody)

        // Подготовка описания книги (если оно есть)
        val descriptionPart = description?.let {
            val descriptionRequestBody = RequestBody.create(MediaType.parse("text/plain"), it)
            MultipartBody.Part.createFormData("description", null, descriptionRequestBody)
        }

        // Подготовка жанров
        val genresString = genres.joinToString(",") // Конвертируем Int в строку
        val genresRequestBody = RequestBody.create(MediaType.parse("text/plain"), genresString)
        val genresPart = MultipartBody.Part.createFormData("genres", null, genresRequestBody)

        // Выполнение сетевого запроса
        RetrofitClient.apiService.postNewBook(
            titlePart,
            yearPart,
            descriptionPart,
            genresPart,
            filePart
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(fragment.requireContext(), "Успешно!", Toast.LENGTH_LONG).show()
                    changeCurrentFragment(fragment.parentFragmentManager, AuthorMeFragment(), bundle = null)
                } else {
                    val errorMessage = handleErrorResponse(response.errorBody())
                    Toast.makeText(fragment.requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(fragment.requireContext(), t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    fun fetchAuthorById(authorId: Int){
        executor.execute {
            // Выполняем сетевой запрос
            RetrofitClient.apiService.getSomeAuthorInfo(authorId).enqueue(object : Callback<Author> {
                override fun onResponse(call: Call<Author>, response: Response<Author>) {
                    if (response.isSuccessful) {
                        _author.postValue(response.body()) // Используем postValue для обновления LiveData
                    } else {
                        _error.postValue("Ошибка: ${response.code()}") // Обновляем ошибки
                    }
                }

                override fun onFailure(call: Call<Author>, t: Throwable) {
                    _error.postValue("Ошибка: ${t.message}") // Обновляем ошибки
                }
            })
        }
    }

    override fun onCleared() {
        super.onCleared()
        executor.shutdown()
    }
}

