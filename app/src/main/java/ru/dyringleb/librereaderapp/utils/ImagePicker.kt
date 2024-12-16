package ru.dyringleb.librereaderapp.utils

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class ImagePicker(
    private val fragment: Fragment,
    private val imageView: ImageView,
    private val imageSelectedCallback: (Uri) -> Unit // Lambda для обратного вызова
) {
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    var selectedImageUri: Uri? = null
        private set

    init {
        // Инициализация ActivityResultLauncher
        imagePickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    displaySelectedImage(uri) // Устанавливаем изображение в ImageView
                    imageSelectedCallback(uri) // Вызываем колбек
                }
            }
        }
    }

    fun selectImage() {
        openImagePicker() // Просто открываем выбор изображения
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun displaySelectedImage(uri: Uri) {
        try {
            val inputStream = fragment.requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap) // Устанавливаем загруженное изображение
            inputStream?.close()
        } catch (e: Exception) {
            Toast.makeText(fragment.requireContext(), "Ошибка при загрузке изображения: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}
