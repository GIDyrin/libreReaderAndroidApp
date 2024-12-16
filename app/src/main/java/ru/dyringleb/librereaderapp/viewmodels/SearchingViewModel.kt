package ru.dyringleb.librereaderapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.dyringleb.librereaderapp.retrofitConf.BookByAuthor
import ru.dyringleb.librereaderapp.retrofitConf.BooksResponse
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.retrofitConf.Author
import ru.dyringleb.librereaderapp.retrofitConf.AuthorResponse
import ru.dyringleb.librereaderapp.retrofitConf.BookSearchResponse
import ru.dyringleb.librereaderapp.retrofitConf.FullBooksInfo
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SearchingViewModel : ViewModel() {

    private val _books = MutableLiveData<List<FullBooksInfo>>()
    val books: LiveData<List<FullBooksInfo>> get() = _books

    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> get() = _currentPage

    private val _authors = MutableLiveData<List<Author>>()
    val author: LiveData<List<Author>> get() = _authors

    private val _currentPageAuthor = MutableLiveData<Int>()
    val currentPageAuthor: LiveData<Int> get() = _currentPageAuthor

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _by_author = MutableLiveData<Boolean>()
    val by_author: LiveData<Boolean> get() = _by_author

    // Создаем ExecutorService для выполнения потоковых задач
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()


    // Метод для получения книг по жанрам с пагинацией
    fun fetchBooksByGenres(genreIds: List<Int>, searchField: String,page: Int = 1, new:Boolean = false) {
        val genreIdString = genreIds.joinToString(",")  // Преобразуем список в строку
        if(new){
            _books.postValue(emptyList())
        }
        executor.execute {
            RetrofitClient.apiService.getBooksByGenres(genreIdString,searchField, page).enqueue(object : Callback<BookSearchResponse> {
                override fun onResponse(call: Call<BookSearchResponse>, response: Response<BookSearchResponse>) {
                    if (response.isSuccessful) {
                        _currentPage.postValue(page)
                        val newBooks = response.body()?.results ?: emptyList()
                        // Объединяем старые и новые книги
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

    fun setSwtichState(state: Boolean) {
        _by_author.postValue(state)
    }

    fun changeSwitchState(){
        _by_author.postValue(_by_author.value?.not())
    }

    fun fetchFilteredAuthors(filter: String, page: Int, new: Boolean = false) {
        executor.execute{
            if(new){
                _authors.postValue(emptyList())
            }
            RetrofitClient.apiService.getAuthors(filter = filter, page)
                .enqueue(object : Callback<AuthorResponse> {
                    override fun onResponse(
                        call: Call<AuthorResponse>,
                        response: Response<AuthorResponse>
                    ) {
                        if (response.isSuccessful) {
                            _currentPageAuthor.postValue(page)
                            val body = response.body()?.results ?: emptyList<Author>()

                            val updatedAuthors = (_authors.value ?: emptyList()) + body
                            _authors.postValue(updatedAuthors)

                        } else {
                            _error.postValue("Ошибка: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<AuthorResponse>, t: Throwable) {
                        _error.postValue("Ошибка: ${t.message}")
                    }
                })
        }
    }

     fun loadNextAuthorPage(filter: String){
         val nextPage = (_currentPageAuthor.value ?: 0) + 1
         fetchFilteredAuthors(filter, nextPage)
     }


    // Метод для загрузки следующей страницы при фильтрации по жанрам
    fun loadNextPage(genreIds: List<Int>, searchField: String) {
        val nextPage = (_currentPage.value ?: 0) + 1
        fetchBooksByGenres(genreIds, searchField, nextPage)
    }


    override fun onCleared() {
        super.onCleared()
        executor.shutdown()
    }
}
