package ru.dyringleb.librereaderapp.viewmodels

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.retrofitConf.Review
import ru.dyringleb.librereaderapp.retrofitConf.UserInfo
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import ru.dyringleb.librereaderapp.utils.ImageUtils

class MeAsUserViewModel : ViewModel() {

    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> get() = _userInfo

    private val _userReviews = MutableLiveData<List<Review>>()
    val userReviews: LiveData<List<Review>> get() = _userReviews

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _logoutStatus = MutableLiveData<Boolean>()
    val logoutStatus: LiveData<Boolean> get() = _logoutStatus

    private val _updateStatus = MutableLiveData<Boolean>()
    val updateStatus: LiveData<Boolean> get() = _updateStatus



    fun fetchUserData() {
        RetrofitClient.apiService.loginMe().enqueue(object : Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                if (response.isSuccessful) {
                    _userInfo.value = response.body()
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                _error.postValue("Ошибка: ${t.message}")
            }
        })
    }

    fun fetchUserReviews(userId: Int) {
        RetrofitClient.apiService.getUserReviews(userId).enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful) {
                    _userReviews.value = response.body()
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                _error.postValue("Ошибка: ${t.message}")
            }
        })
    }

    fun logout() {
        RetrofitClient.apiService.logout().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                _logoutStatus.value = response.isSuccessful
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                _logoutStatus.value = true
            }
        })


    }

    fun updateProfile(fragment: Fragment, userId: Int, aboutUser: String, selectedImageUri: Uri?) {
        val aboutUserRequestBody = RequestBody.create(MediaType.parse("text/plain"), aboutUser)
        val aboutUserPart = MultipartBody.Part.createFormData("about_user", null, aboutUserRequestBody)

        // Получаем изображение с помощью утилиты
        if (selectedImageUri != null) {
            ImageUtils.compressAndCropImage(fragment, selectedImageUri, "profile_photo") { imagePart ->
                RetrofitClient.apiService.updateProfile(userId, aboutUserPart, imagePart).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        _updateStatus.value = response.isSuccessful
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        _error.postValue("Ошибка: ${t.message}")
                        _updateStatus.value = false
                    }
                })
            }
        }
        else{
            RetrofitClient.apiService.updateProfile(userId, aboutUserPart, null).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    _updateStatus.value = response.isSuccessful
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    _error.postValue("Ошибка: ${t.message}")
                    _updateStatus.value = false
                }
            })
        }
    }


}
