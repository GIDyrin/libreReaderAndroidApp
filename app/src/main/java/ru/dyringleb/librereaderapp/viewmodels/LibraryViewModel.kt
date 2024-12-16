package ru.dyringleb.librereaderapp.viewmodels

import DatabaseHelper
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.activities.BookReadingActivity
import ru.dyringleb.librereaderapp.retrofitConf.BookmarkResponse
import ru.dyringleb.librereaderapp.retrofitConf.NewBookmarkRequest
import ru.dyringleb.librereaderapp.retrofitConf.PostBookmarkResponse
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import ru.dyringleb.librereaderapp.retrofitConf.UpdateBookmarkRequest

class LibraryViewModel : ViewModel() {
    private var dbHelper: DatabaseHelper? = null

    private val _bookmarks = MutableLiveData<List<BookmarkResponse>>()
    val bookmarks: LiveData<List<BookmarkResponse>> get() = _bookmarks

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _local = MutableLiveData<List<DatabaseHelper.Companion.BookWithBookmarks>>()
    val local: LiveData<List<DatabaseHelper.Companion.BookWithBookmarks>> get() = _local

    private val _updateLocal = MutableLiveData<Boolean>()
    val updateLocal: LiveData<Boolean> get() = _updateLocal


    init{
        getMyBookmarks()
    }

    fun getLocalBooks(){
        val list = dbHelper?.getAllBooksWithBookmarks()
        if (list != null) {
            _local.postValue(list)
        }
    }


    fun dbAddNewBook(title: String, uri: String){
        dbHelper?.addBook(title, uri)
        _updateLocal.postValue(true)
    }

    fun dbDeleteBook(bookId: Int){
        dbHelper?.deleteBook(bookId)
        _updateLocal.postValue(true)
    }

    fun getMyBookmarks(){
        RetrofitClient.apiService.getMyBookMarks().enqueue(object : Callback<List<BookmarkResponse>>{
            override fun onResponse(
                call: Call<List<BookmarkResponse>>,
                response: Response<List<BookmarkResponse>>
            ) {
                if(response.isSuccessful){
                    if(response.body() != null){
                        _bookmarks.postValue(response.body())
                    }
                }
                else{
                    _error.postValue(response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<List<BookmarkResponse>>, t: Throwable) {
                _error.postValue("Ошибка: ${t.message}")
            }
        })
    }

    fun newBookmark(bookId: Int, page: Int){
        RetrofitClient.apiService.postNewBookmark(NewBookmarkRequest(book = bookId, page_number = page)).enqueue(object : Callback<PostBookmarkResponse>{
            override fun onResponse(call: Call<PostBookmarkResponse>, response: Response<PostBookmarkResponse>) {
                if(!response.isSuccessful){
                    _error.postValue(response.errorBody().toString())
                }
            }
            override fun onFailure(call: Call<PostBookmarkResponse>, t: Throwable) {
                _error.postValue("Ошибка: ${t.message}")
            }
        })
    }

    fun updateBookmark(bookmark: Int, page: Int){
       RetrofitClient.apiService.updateBookmark(bookmarkId = bookmark, UpdateBookmarkRequest(page_number = page))
           .enqueue(object : Callback<Void>{
               override fun onResponse(call: Call<Void>, response: Response<Void>) {
                   if(!response.isSuccessful){
                       _error.postValue(response.errorBody().toString())
                   }
               }

               override fun onFailure(call: Call<Void>, t: Throwable) {
                   _error.postValue("Ошибка: ${t.message}")
               }
       })
    }

    fun deleteBookmark(bookmark: Int){
        RetrofitClient.apiService.deleteBookmark(bookmark).enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code() != 204){
                    _error.postValue(response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                _error.postValue("Ошибка: ${t.message}")
            }
        })
    }

    fun dbStartReadingActivity(item: DatabaseHelper.Companion.BookWithBookmarks, activity: Activity){
        val bundle: Bundle = Bundle()
        bundle.putInt("localBookmark", item.bookmarkId)
        bundle.putInt("localPage", item.pageNumber)
        bundle.putString("localFilePath", item.bookPath)
        bundle.putBoolean("isLocal", true)
        val intent = Intent(activity, BookReadingActivity::class.java)
        intent.putExtras(bundle)
        activity.startActivity(intent)
    }

    companion object {
        fun dbUpdateBookmark(bookmarkId: Int, newPageId: Int, dbHelper: DatabaseHelper) {
            dbHelper.updateBookmark(bookmarkId, newPageId)
        }
    }

    fun setDbHelper(dbHelper: DatabaseHelper){
        this.dbHelper = dbHelper
    }

    fun getDbHelper(): DatabaseHelper? {
        return this.dbHelper
    }

    fun resetLocal(){
        _updateLocal.postValue(false)
    }


    override fun onCleared() {
        super.onCleared()
    }
}