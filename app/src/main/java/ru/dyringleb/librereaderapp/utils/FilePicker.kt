package ru.dyringleb.librereaderapp.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale

class FilePicker(
    private val fragment: Fragment,
    private var fileNameTextView: TextView? = null,
    private val fileSelectedCallback: (Uri) -> Unit,
    private var selectedUriForModal: Uri? = null
) {
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    init {
        // Инициализация файла для выбора
        filePickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val filePath = getPathFromUri(uri)
                    if (filePath != null) {
                        val extension = getFileExtension(filePath)
                        if (isValidFileType(extension)) {
                            // Устанавливаем имя файла в TextView
                            val fileName = uri.lastPathSegment?.substringAfterLast('/')
                            fileNameTextView?.text = fileName
                            // Вызываем колбек с URI выбранного файла
                            selectedUriForModal = uri
                            fileSelectedCallback(uri)
                        } else {
                            Toast.makeText(fragment.requireContext(), "Недопустимый формат файла", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    fun openFilePicker() {
        val mimeTypes = arrayOf(
            "application/pdf", // Для PDF
            "application/x-fictionbook", // Для FB2
        )

        // Создаём intent для выбора файла
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // Устанавливаем общий тип, чтобы показывать все файлы, но фактически фильтровать по MIME
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes) // Добавляем массив допустимых MIME-типов
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        // Запускаем выбор файла через ActivityResultLauncher
        filePickerLauncher.launch(Intent.createChooser(intent, "Выберите файл"))
    }

    private fun getPathFromUri(uri: Uri): String? {
        // Логика для получения пути к файлу из URI (можно использовать Cursor или другой подход)
        return uri.path // Совсем просто для демонстрации.
    }

    private fun getFileExtension(filePath: String): String? {
        return filePath.substringAfterLast('.', "").toLowerCase(Locale.getDefault())
    }

    private fun isValidFileType(extension: String?): Boolean {
        return when (extension) {
            "pdf", "fb2" -> true
            else -> false
        }
    }

    fun setTextView(newView: TextView){
        fileNameTextView = newView
    }

    fun getSelectedUri(): Uri? {
        return selectedUriForModal
    }

    fun setUriNull(){
        selectedUriForModal = null
    }
}
