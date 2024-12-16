package ru.dyringleb.librereaderapp.fragments.author

import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.utils.FilePicker
import android.icu.text.SimpleDateFormat
import ru.dyringleb.librereaderapp.utils.GenreSelectionModal
import ru.dyringleb.librereaderapp.viewmodels.AuthorViewModel

import java.util.Calendar
import java.util.Locale

class PostBookFragment : Fragment() {
    private var attachedFileUri: Uri? = null
    private lateinit var filePicker: FilePicker
    private lateinit var description: EditText
    private lateinit var title: EditText
    private lateinit var postButton: Button
    private lateinit var yearText: EditText
    private lateinit var genreDialog: GenreSelectionModal
    private lateinit var viewModel: AuthorViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_book, container, false)
        viewModel = AuthorViewModel()


        val fileNameTextView: TextView = view.findViewById(R.id.fileName)
        description = view.findViewById(R.id.description)
        title = view.findViewById(R.id.bookTitle)
        postButton = view.findViewById(R.id.publish)
        yearText = view.findViewById(R.id.bookYear)


        // Модалка для жанров
        genreDialog = GenreSelectionModal(requireContext())

        // Инициализация FilePicker и передача TextView
        filePicker = FilePicker(this, fileNameTextView, fileSelectedCallback = { uri -> attachedFileUri = uri })

        // Инициализация кнопки прикрепления файла
        val attachFileButton: ImageButton = view.findViewById(R.id.attachFileButton)
        attachFileButton.setOnClickListener {
            filePicker.openFilePicker()
        }

        val genreButton: Button = view.findViewById(R.id.genreButton)
        genreButton.setOnClickListener {
            genreDialog.showDialog()
        }

        // Обработка нажатия кнопки "Опубликовать"
        postButton.setOnClickListener {
            postBook()
        }

        return view
    }

    private fun postBook() {
        // Получение значений из полей ввода
        val bookTitle = title.text.toString().trim()
        val bookDescription = description.text.toString().trim()
        val currentYear = yearText.text.toString().trim()
        val genres: List<Int> = genreDialog.getSelectedGenres()

        // Проверка валидации
        when {
            bookTitle.isEmpty() -> {
                Toast.makeText(requireContext(), "Название книги обязательно", Toast.LENGTH_SHORT).show()
            }
            attachedFileUri == null -> {
                Toast.makeText(requireContext(), "Файл обязательно должен быть прикреплён", Toast.LENGTH_SHORT).show()
            }
            genres.isEmpty() || genres.size > 3 -> {
                Toast.makeText(requireContext(), "Обратите внимание на количество выбранных жанров", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val year = currentYear.ifEmpty { Calendar.getInstance().get(Calendar.YEAR) } // Установка года по умолчанию
                viewModel.postNewBook(this,
                    attachedFileUri!!, bookTitle, bookDescription, year.toString(), genres)
            }
        }
    }
}
