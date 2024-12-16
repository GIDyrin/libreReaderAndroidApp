package ru.dyringleb.librereaderapp.fragments.author

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.utils.ImagePicker
import ru.dyringleb.librereaderapp.viewmodels.AuthorViewModel


class RegistrateAuthorFragment : Fragment() {
    private lateinit var imagePicker: ImagePicker
    private lateinit var profileImage: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var aboutEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var authorViewModel: AuthorViewModel
    private var selectedImageUri: Uri? = null


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authorViewModel = AuthorViewModel()
        val view = inflater.inflate(R.layout.fragment_registrate_author, container, false)

        profileImage = view.findViewById(R.id.authorPortrait)
        usernameTextView = view.findViewById(R.id.Pseudo)
        aboutEditText = view.findViewById(R.id.aboutAuthor)
        saveButton = view.findViewById(R.id.edit_button)


        // Инициализация ImagePicker с лямбда-функцией для обработки выбранного URI
        imagePicker = ImagePicker(this, profileImage) { uri ->
            selectedImageUri = uri // Сохраняем выбранный URI
        }

        // Настройка выбора изображения
        profileImage.setOnClickListener {
            imagePicker.selectImage()
        }

        saveButton.setOnClickListener {
            val aboutUser = aboutEditText.text.toString()
            val pseudo = usernameTextView.text.toString()
            if (aboutUser.isNotEmpty() && pseudo.isNotEmpty()) {
                authorViewModel.registrate(this, name = pseudo, about = aboutUser, selectedImageUri)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Пожалуйста, заполните все поля.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }

}