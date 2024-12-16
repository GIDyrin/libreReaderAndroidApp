package ru.dyringleb.librereaderapp.fragments.books

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.activities.BookReadingActivity
import ru.dyringleb.librereaderapp.adapters.BookReviewsAdapter
import ru.dyringleb.librereaderapp.retrofitConf.BookReview
import ru.dyringleb.librereaderapp.retrofitConf.BookmarkResponse
import ru.dyringleb.librereaderapp.retrofitConf.FullBooksInfo
import ru.dyringleb.librereaderapp.retrofitConf.NewBookmarkRequest
import ru.dyringleb.librereaderapp.retrofitConf.PostBookmarkResponse
import ru.dyringleb.librereaderapp.retrofitConf.RatingResponse
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import java.io.File
import java.io.FileOutputStream


class BookDetailsFragment : Fragment() {
    private lateinit var bookTitle: TextView
    private lateinit var bookAuthor: TextView
    private lateinit var bookYear: TextView
    private lateinit var bookDescription: TextView
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var toggleReviewsButton: Button
    private lateinit var reviewsAdapter: BookReviewsAdapter
    private lateinit var bookGenres: TextView
    private lateinit var reviewPost: Button
    private lateinit var readBook: Button
    private lateinit var bookRating: TextView
    private var reviewsList: List<BookReview> = emptyList()
    private var bookId: Int = 0
    private var bookmark: BookmarkResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация представлений
        bookTitle = view.findViewById(R.id.bookTitle)
        bookAuthor = view.findViewById(R.id.bookAuthor)
        bookYear = view.findViewById(R.id.bookYear)
        bookDescription = view.findViewById(R.id.bookDescription)
        reviewsRecyclerView = view.findViewById(R.id.recyclerViewReviews)
        toggleReviewsButton = view.findViewById(R.id.buttonToggleReviews)
        bookGenres = view.findViewById(R.id.bookGenres)
        reviewPost = view.findViewById(R.id.reviewButton)
        bookRating = view.findViewById(R.id.rating)
        readBook = view.findViewById(R.id.readBookButton)

        reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        // Получаем bookId из аргументов
        val bookIdArgument = arguments?.getInt("bookId")

        if (bookIdArgument != null) {
            bookId = bookIdArgument
            fetchData(bookId = bookId)
        }

        // Устанавливаем слушатель на кнопку для показа/скрытия отзывов
        toggleReviewsButton.setOnClickListener {
            if (reviewsRecyclerView.visibility == View.GONE) {
                reviewsRecyclerView.visibility = View.VISIBLE
                toggleReviewsButton.text = "Скрыть отзывы"
            } else {
                reviewsRecyclerView.visibility = View.GONE
                toggleReviewsButton.text = "Показать отзывы"
            }
        }


        reviewPost.setOnClickListener(){
            val bundle = Bundle()
            bundle.putInt("bookId", bookId)
            val reviewPostFragment = ReviewPostFragment()
            reviewPostFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainerView,
                    reviewPostFragment
                )
                .addToBackStack(null)
                .commit()
        }


        readBook.setOnClickListener{
            fetchBookFile(bookId, this@BookDetailsFragment)
        }
    }

    private fun fetchData(bookId: Int){
        fetchBookDetails(bookId)
        fetchBookReviews(bookId)
        fetchBookRating(bookId)
        fetchBookmark(bookId)
    }

    private fun fetchBookDetails(bookId: Int) {
        val call = RetrofitClient.apiService.getBookInfo(bookId = bookId)
        call.enqueue(object : Callback<FullBooksInfo> {
            override fun onResponse(call: Call<FullBooksInfo>, response: Response<FullBooksInfo>) {
                if (response.isSuccessful && response.body() != null) {
                    val book = response.body()!!
                    displayBookDetails(book)
                }
            }

            override fun onFailure(call: Call<FullBooksInfo>, t: Throwable) {
                Toast.makeText(requireContext(), "Проблема с подключением  к серверу", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun displayBookDetails(book: FullBooksInfo) {
        bookTitle.text = book.book_title
        bookAuthor.text = "Автор: ${book.author.author_name}"
        bookYear.text = "Год: ${book.book_year}"
        // Укажите значение по умолчанию, если описание пустое или равно null
        bookDescription.text = book.description.takeIf { !it.isNullOrEmpty() } ?: "Автор этого произведения не оставил описания"
        bookGenres.text = book.genres.joinToString(separator = ", ") { it.genre_name }
    }

    private fun fetchBookReviews(bookId: Int) {
        val call = RetrofitClient.apiService.getBooksReviews(bookId=bookId)
        call.enqueue(object : Callback<List<BookReview>> {
            override fun onResponse(call: Call<List<BookReview>>, response: Response<List<BookReview>>) {
                if (response.isSuccessful && response.body() != null) {
                    reviewsList = response.body()!!
                    reviewsAdapter = BookReviewsAdapter(reviewsList)
                    reviewsRecyclerView.adapter = reviewsAdapter
                } else {
                    // Обработка ошибок
                    // Пусто, нет времени на это
                }
            }

            override fun onFailure(call: Call<List<BookReview>>, t: Throwable) {
                Toast.makeText(requireContext(), "Проблема с подключением  к серверу", Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun fetchBookRating(bookId: Int){
        RetrofitClient.apiService.getBookRating(bookId=bookId).enqueue(object: Callback<RatingResponse>{
            override fun onResponse(
                call: Call<RatingResponse>,
                response: Response<RatingResponse>
            ) {
                if(response.isSuccessful){
                    val rate = response.body()
                    if (rate != null) {
                        bookRating.text = "Рейтинг: ${rate.avg_rate} (${rate.reviews_count})"
                    }
                }
            }

            override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Проблема с подключением  к серверу", Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun fetchBookmark(bookId: Int){
        RetrofitClient.apiService.getBookmarkByBookId(bookId).enqueue(object : Callback<BookmarkResponse>{
            override fun onResponse(
                call: Call<BookmarkResponse>,
                response: Response<BookmarkResponse>
            ) {
                if(response.isSuccessful){
                    bookmark = response.body()
                    Log.d("BOOKMARK", bookmark?.page_number.toString())
                }
            }

            override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Проблема с подключением  к серверу", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchBookFile(bookId: Int, fragment: Fragment) {
        // Генерируем имя файла на основе bookId
        val randomFileName = "book_$bookId" // Используем ID книги для уникальности
        val directory = fragment.requireContext().getExternalFilesDir(null)

        // Проверяем все файлы в директории на наличие файла с именем без учета расширения
        val files = directory?.listFiles()
        val fileExists = files?.any { it.nameWithoutExtension == randomFileName } == true

        if (fileExists) {
            // Находим файл с нужным именем без расширения
            val existingFile = files?.first { it.nameWithoutExtension == randomFileName }
            // Файл уже существует, переходим к чтению книги
            val bundle = Bundle()
            if (existingFile != null) {
                bundle.putString("filePath", existingFile.absolutePath)
            }
            startReadingActivity(fragment.requireActivity(), bundle)
        } else {
            // Файл не существует, выполняем загрузку
            RetrofitClient.apiService.getBookFile(bookId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        // Извлекаем расширение файла из заголовка "Content-Type"
                        val contentType = response.headers()["Content-Type"] ?: "application/octet-stream"
                        val fileExtension = when {
                            contentType.contains("x-fictionbook") -> ".fb2"
                            contentType.contains("pdf") -> ".pdf"
                            else -> null
                        }

                        Log.d("fetchingBookFile", fileExtension ?: "")
                        if (fileExtension != null) {
                            // Создаем полное имя файла с расширением
                            val fullFileName = "$randomFileName$fileExtension"
                            val fullPathFile = File(directory, fullFileName)

                            // Записываем данные в файл
                            response.body()?.byteStream()?.use { inputStream ->
                                FileOutputStream(fullPathFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }

                            // Передаем файл в активность
                            val bundle = Bundle()
                            bundle.putString("filePath", fullPathFile.absolutePath)
                            fragment.activity?.let {
                                startReadingActivity(it, bundle)
                            }
                        }

                    } else {
                        Log.d("fetchingBookFile", "ERROR FETCHING BOOK FILE? HOW TF IS IT POSSIBLE?")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(fragment.requireContext(), "Проблема с подключением к серверу", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun startReadingActivity(activity: Activity, bundle: Bundle) {
        Log.d("BOOKMARK", bookmark?.bookmark_id.toString())
        if(bookmark == null) {
            RetrofitClient.apiService.postNewBookmark(NewBookmarkRequest(book = bookId, page_number = 1))
                .enqueue(object : Callback<PostBookmarkResponse> {
                    override fun onResponse(
                        call: Call<PostBookmarkResponse>,
                        response: Response<PostBookmarkResponse>
                    ) {
                        if(response.isSuccessful) {
                            val intent = Intent(activity, BookReadingActivity::class.java)
                            response.body()?.let {
                                bundle.putInt("bookmarkId", it.bookmark_id)
                            }
                            intent.putExtras(bundle)
                            activity.startActivity(intent)
                        } else {
                            // Обработка ошибок, если ответ не успешный
                            Log.e("startReadingActivity", "Error response: ${response.code()}: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<PostBookmarkResponse>, t: Throwable) {
                        Log.e("startReadingActivity", "Network error: ${t.localizedMessage}")
                    }
                })
        } else {
            val intent = Intent(activity, BookReadingActivity::class.java)
            bundle.putInt("bookmarkId", bookmark!!.bookmark_id)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }

    override fun onStart() {

        Log.d("BOOKDETAILS", "REFETCHED")
        fetchData(bookId = bookId)
        super.onStart()
    }


}


