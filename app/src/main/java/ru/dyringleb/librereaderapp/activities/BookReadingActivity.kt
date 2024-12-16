package ru.dyringleb.librereaderapp.activities

import DatabaseHelper
import PDFAdapter
import android.content.Context
import android.net.Uri
import ru.dyringleb.librereaderapp.FB2Parser
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.adapters.FB2PagerAdapter
import ru.dyringleb.librereaderapp.retrofitConf.OnlyBookmarkInfo
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import ru.dyringleb.librereaderapp.retrofitConf.UpdateBookmarkRequest
import ru.dyringleb.librereaderapp.viewmodels.LibraryViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class BookReadingActivity : AppCompatActivity() {
    private lateinit var parser: FB2Parser
    private lateinit var adapter: FB2PagerAdapter
    private var job: Job? = null
    private var currentPage: Int = 0 // Переменная для хранения текущей страницы
    private var bookmark: OnlyBookmarkInfo? = null
    private var pageNum: Int = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var pager2: ViewPager2
    private lateinit var pdfAdapter: PDFAdapter
    private var isLocal: Boolean = false
    private var filePath: String = ""
    private var bookmarkId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reading)

        // Настройка отступов для системы
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reader)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.reader)
        pager2 = findViewById(R.id.pdf_reader)


        // Получение данных из Intent
        isLocal = intent.extras?.getBoolean("isLocal") ?: false
        if(isLocal){
            filePath = intent.extras?.getString("localFilePath") ?: ""
            bookmarkId = intent.extras?.getInt("localBookmark") ?: 0
            pageNum = intent.extras?.getInt("localPage") ?: 0
            setView(filePath)
        }
        else {

            filePath = intent.extras?.getString("filePath") ?: ""
            bookmarkId = intent.extras?.getInt("bookmarkId") ?: 0

            getBookmark(bookmarkId, filePath)


            pager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPage = position // Сохраняем текущую страницу
                }
            })
        }

    }
    
    private fun getBookmark(id: Int, filePath: String){
        RetrofitClient.apiService.getBookmark(id).enqueue(object : Callback<OnlyBookmarkInfo>{
            override fun onResponse(
                call: Call<OnlyBookmarkInfo>,
                response: Response<OnlyBookmarkInfo>
            ) {
                if (response.isSuccessful){
                    bookmark = response.body()
                    pageNum = response.body()?.page_number ?: 1
                    setView(filePath)
                }
            }

            override fun onFailure(call: Call<OnlyBookmarkInfo>, t: Throwable) {
                Log.d("SERVER", "ERROR")
            }
        })
        
        
    }

    private fun setView(filePath: String){
        filePath.let {
            val file = File(it)
            when {
                file.extension.equals("fb2", ignoreCase = true) -> {
                    parseFB2File(file, pageNum) // Передаем pageNum в метод
                }

                file.extension.equals("pdf", ignoreCase = true) -> {
                    recyclerView.visibility = View.GONE
                    pager2.visibility = View.VISIBLE
                    setUpPDFView(filePath)
                }

                else -> {
                    Toast.makeText(this, "Не поддерживаемый формат файла", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun setUpPDFView(filePath: String) {
        pdfAdapter = PDFAdapter(this, filePath)
        pager2.adapter = pdfAdapter

        // Устанавливаем текущую страницу, если сохранена
        if (pageNum > 0) {
            pager2.setCurrentItem(pageNum, false)
        }

        Log.d("BookReadingActivity", "PDF view set up successfully.")
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun parseFB2File(file: File, pageNum: Int) {
        parser = FB2Parser(this)

        job = GlobalScope.launch(Dispatchers.IO) {
            val text = parser.loadFile(file)

            withContext(Dispatchers.Main) {
                if (text.isNotEmpty()) {
                    setUpRecyclerView(text, pageNum) // Передаем pageNum в метод
                } else {
                    Toast.makeText(this@BookReadingActivity, "Нет доступного текста для отображения", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun setUpRecyclerView(content: String, pageNum: Int) {
        val pages = content.split("\n") // Каждая строка будет отдельной страницей
        adapter = FB2PagerAdapter(pages.toMutableList(), 16f) // Устанавливаем размер текста
        val recyclerView = findViewById<RecyclerView>(R.id.reader)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Устанавливаем позицию RecyclerView на pageNum
        recyclerView.scrollToPosition(pageNum)

        currentPage = pageNum // Сохраняем текущую страницу
        Log.d("BookReadingActivity", "Parsed and set up pages from FB2 file.")

        // Устанавливаем ScrollListener для отслеживания текущей страницы
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Получаем текущую позицию первого видимого элемента
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                currentPage = layoutManager.findFirstVisibleItemPosition()
            }
        })
    }


    override fun onDestroy() {
        if(isLocal){
            LibraryViewModel.dbUpdateBookmark(bookmarkId, currentPage, DatabaseHelper(this))
        }
        else {
            // Создаем объект запроса с номером страницы
            val updateRequest = UpdateBookmarkRequest(page_number = currentPage)

            RetrofitClient.apiService.updateBookmark(bookmarkId, updateRequest)
                .enqueue(object : retrofit2.Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                        if (response.isSuccessful) {
                            Log.d(
                                "BookReadingActivity",
                                "Page position updated successfully: $currentPage"
                            )
                        } else {
                            Log.e(
                                "BookReadingActivity",
                                "Error updating page position: ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("BookReadingActivity", "Failed to update page position: ${t.message}")
                    }
                })
        }
        super.onDestroy()
        job?.cancel() // Отмена корутины, если активность уничтожена
        pdfAdapter.close() // Закрытие PDF адаптера
    }



}
