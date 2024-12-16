package ru.dyringleb.librereaderapp.fragments.author

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.utils.ImagePicker
import ru.dyringleb.librereaderapp.utils.loadImage
import ru.dyringleb.librereaderapp.viewmodels.AuthorViewModel

class EditAuthorFragment : Fragment() {

    private lateinit var authorViewModel: AuthorViewModel
    private lateinit var editAuthorName: EditText
    private lateinit var editAboutMe: EditText
    private lateinit var saveButton: Button
    private lateinit var profileImage: ImageView
    private lateinit var imagePicker: ImagePicker
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorViewModel = AuthorViewModel()

        // Подписка на изменения в данных автора
        authorViewModel.author.observe(this, Observer { author ->
            author?.let {
                editAuthorName.setText(it.author_name)
                editAboutMe.setText(it.biography)

                val imageUrl = "api/v1/authors/${it.author_id}/portrait/"
                loadImage(this, imageUrl, profileImage)
            }
        })

        authorViewModel.error.observe(this, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        // Получаем данные автора при создании фрагмента
        authorViewModel.fetchMyAuthorPage()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_author, container, false)

        // Инициализация элементов интерфейса
        editAuthorName = view.findViewById(R.id.AuthorName)
        editAboutMe = view.findViewById(R.id.aboutMe)
        saveButton = view.findViewById(R.id.updateAuthor)
        profileImage = view.findViewById(R.id.profilePortrait)

        saveButton.setOnClickListener {
            saveAuthorInfo()
        }

        imagePicker = ImagePicker(this, profileImage) { uri ->
            selectedImageUri = uri // Сохраняем выбранный URI
        }

        // Настройка выбора изображения
        profileImage.setOnClickListener {
            imagePicker.selectImage()
        }

        return view
    }

    private fun saveAuthorInfo() {
        val name = editAuthorName.text.toString()
        val about = editAboutMe.text.toString()


        authorViewModel.updateAuthorInfo(this, name, about, selectedImageUri)
        Toast.makeText(requireContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show()
    }
}
