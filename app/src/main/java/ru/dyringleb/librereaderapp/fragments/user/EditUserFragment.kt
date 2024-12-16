package ru.dyringleb.librereaderapp.fragments.user

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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment
import ru.dyringleb.librereaderapp.utils.ImagePicker
import ru.dyringleb.librereaderapp.retrofitConf.UserInfo
import ru.dyringleb.librereaderapp.utils.loadImage
import ru.dyringleb.librereaderapp.viewmodels.MeAsUserViewModel

class EditUserFragment : Fragment() {
    private lateinit var imagePicker: ImagePicker
    private lateinit var profileImage: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var aboutEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var userInfo: UserInfo
    private var selectedImageUri: Uri? = null

    private lateinit var userViewModel: MeAsUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = MeAsUserViewModel()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userViewModel.fetchUserData()
        userViewModel.userInfo.value?.let { it -> userInfo = it }
        val view = inflater.inflate(R.layout.fragment_edit_user, container, false)

        profileImage = view.findViewById(R.id.profilePortrait)
        usernameTextView = view.findViewById(R.id.username)
        aboutEditText = view.findViewById(R.id.aboutMe)
        saveButton = view.findViewById(R.id.edit_button)

        userViewModel.userInfo.observe(viewLifecycleOwner) { it ->
            usernameTextView.text = it.username
            aboutEditText.setText(it.about_user)
            val imageUrl = "api/v1/user/${it.id}/photo/"
            loadImage(this, imageUrl, profileImage)
        }

        // Инициализация ImagePicker с лямбда-функцией для обработки выбранного URI
        imagePicker = ImagePicker(this, profileImage) { uri ->
            selectedImageUri = uri // Сохраняем выбранный URI
            uri.let {
                // Обновляем ImageView при выборе нового изображения
                Glide.with(this)
                    .load(it) // Загружаем новое изображение
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(profileImage) // Указываем ImageView для обновления
            }
        }

        // Настройка выбора изображения
        profileImage.setOnClickListener {
            imagePicker.selectImage()
        }

        saveButton.setOnClickListener {
            val aboutUser = aboutEditText.text.toString()
            if (aboutUser.isNotEmpty()) {
                userViewModel.userInfo.value?.let { it1 -> userViewModel.updateProfile(this, it1.id, aboutUser, selectedImageUri) }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Пожалуйста, заполните все поля.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Наблюдение за статусом обновления профиля
        userViewModel.updateStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Данные успешно обновлены.", Toast.LENGTH_SHORT)
                    .show()
                changeCurrentFragment(parentFragmentManager, MyProfileFragment(), null)
            } else {
                Toast.makeText(requireContext(), "Ошибка обновления профиля.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return view
    }
}

