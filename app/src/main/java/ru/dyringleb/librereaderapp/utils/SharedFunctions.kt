package ru.dyringleb.librereaderapp.utils

import DatabaseHelper
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import java.io.ByteArrayOutputStream
import java.io.IOException

fun changeCurrentFragment(fragmentManager: FragmentManager, fragmentClass: Fragment, bundle: Bundle?) {
    fragmentClass.arguments = bundle
    fragmentManager.beginTransaction()
        .replace(R.id.fragmentContainerView, fragmentClass)
        .addToBackStack(null)
        .commit()
}


fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Проверяем, доступна ли сеть
    val networkCapabilities = connectivityManager.activeNetwork?.let { activeNetwork ->
        connectivityManager.getNetworkCapabilities(activeNetwork)
    }
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

fun loadImage(fragment: Fragment, imageUrl: String, where: ImageView) {
    Glide.with(fragment)
        .load(RetrofitClient.BASE_URL + imageUrl) // уникальный URL
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .error(R.drawable.unknown2)
        .into(where)
}

fun handleErrorResponse(responseBody: ResponseBody?): String {
    val gson = Gson()
    return try {
        // Пробуем прочитать JSON как Map
        val errorMap: Map<*, *>? = gson.fromJson(responseBody?.string(), Map::class.java)
        // Формируем строку с ошибками для вывода
        errorMap?.entries?.joinToString(separator = "\n") { "${it.key}: ${it.value}" }
            ?: "Нет доступных данных об ошибке" // Возвращаем сообщение по умолчанию, если errorMap == null
    } catch (e: JsonSyntaxException) {
        "Ошибка парсинга ответа"
    } catch (e: IOException) {
        "Ошибка ввода/вывода"
    } catch (e: Exception) {
        "Неизвестная ошибка: ${e.message ?: "нет сообщения об ошибке"}" // Предоставляем сообщение по умолчанию
    }
}


fun animateImageButtons(button: View, transparent:Boolean = true){
    if(transparent) {
        button.setBackgroundColor(Color.TRANSPARENT)
    }

    val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 0.9f, 1f)
    val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 0.9f, 1f)

    scaleX.duration = 150
    scaleY.duration = 150

    scaleX.interpolator = null
    scaleY.interpolator = null

    scaleX.start()
    scaleY.start()
}

object ImageUtils {

    fun compressAndCropImage(fragment: Fragment, uri: Uri, field: String, onImageReady: (MultipartBody.Part?) -> Unit) {
        Glide.with(fragment)
            .asBitmap()
            .load(uri)
            .override(400, 400)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val croppedBitmap = Bitmap.createBitmap(resource)

                    // Сжатие изображения
                    val outputStream = ByteArrayOutputStream()
                    croppedBitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)

                    val fileBytes = outputStream.toByteArray()
                    val contentType = fragment.requireContext().contentResolver.getType(uri) ?: "image/jpeg"
                    val fileExtension = when (contentType) {
                        "image/jpeg" -> "jpg"
                        "image/png" -> "png"
                        "image/webp" -> "webp"
                        else -> null
                    }

                    if (fileExtension.isNullOrEmpty()) {
                        onImageReady(null)
                        return
                    }

                    // Создание части изображения с правильным именем файла
                    val requestFile = RequestBody.create(MediaType.parse(contentType), fileBytes)
                    val imagePart = MultipartBody.Part.createFormData(field, "image.$fileExtension", requestFile)

                    onImageReady(imagePart) // Возвращаем сжатое изображение
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

            })
    }
}







