package ru.dyringleb.librereaderapp.activities

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.SharedPreferencesHelper
import ru.dyringleb.librereaderapp.fragments.author.AuthorMeFragment
import ru.dyringleb.librereaderapp.fragments.LibraryFragment
import ru.dyringleb.librereaderapp.fragments.regauth.LoginFragment
import ru.dyringleb.librereaderapp.fragments.SearchFragment
import ru.dyringleb.librereaderapp.fragments.user.MyProfileFragment
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import ru.dyringleb.librereaderapp.utils.animateImageButtons
import ru.dyringleb.librereaderapp.viewmodels.LibraryViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var navButtonMe: ImageButton
    private lateinit var navButtonLibrary: ImageButton
    private lateinit var navButtonSearch: ImageButton
    private lateinit var navButtonAuthor: ImageButton
    lateinit var libraryViewModel: LibraryViewModel


    private lateinit var currentButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализируем SharedPreferencesHelper
        val sharedPreferencesHelper = SharedPreferencesHelper(this)
        // Инициализируем RetrofitClient
        RetrofitClient.initialize(sharedPreferencesHelper)



        // Проверка наличия токена
        val token = sharedPreferencesHelper.getAuthToken()

        // Установка начального фрагмента
        if (token != null) {
            // Токен найден, переходим к главному фрагменту
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, MyProfileFragment())
                .commit()
        } else {
            // Токен не найден, переходим на экран авторизации
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, LoginFragment()) // создайте LoginFragment
                .commit()
        }



        navButtonMe = findViewById(R.id.navButtonMe)
        navButtonLibrary = findViewById(R.id.navButtonLibrary)
        navButtonSearch = findViewById(R.id.navButtonSearch)
        navButtonAuthor = findViewById(R.id.navButtonAuthor)

        // Устанавливаем текущую кнопку
        currentButton = navButtonMe

        // Устанавливаем обработчики нажатий
        navButtonMe.setOnClickListener { onButtonClicked(navButtonMe,
            R.drawable.pressed_home, MyProfileFragment()) }
        navButtonLibrary.setOnClickListener { onButtonClicked(navButtonLibrary,
            R.drawable.pressed_library, LibraryFragment()) }
        navButtonSearch.setOnClickListener { onButtonClicked(navButtonSearch,
            R.drawable.pressed_search, SearchFragment()) }
        navButtonAuthor.setOnClickListener { onButtonClicked(navButtonAuthor,
            R.drawable.pressed_author, AuthorMeFragment()) }


        libraryViewModel = ViewModelProvider(this)[LibraryViewModel::class.java]


    }

    private fun onButtonClicked(button: ImageButton, pressedDrawableId: Int, fragment: Fragment) {
        // Убираем эффект с текущей кнопки
        currentButton.setImageResource(getUnpressedDrawableId(currentButton.id))

        // Меняем ресурс у нажатой кнопки
        button.setImageResource(pressedDrawableId)
        // Обновляем текущую кнопку
        currentButton = button

        animateImageButtons(button)

        // Заменяем фрагмент
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun getUnpressedDrawableId(buttonId: Int): Int {
        return when (buttonId) {
            R.id.navButtonMe -> R.drawable.home
            R.id.navButtonLibrary -> R.drawable.library
            R.id.navButtonSearch -> R.drawable.search
            R.id.navButtonAuthor -> R.drawable.author
            else -> R.drawable.home // Значение по умолчанию
        }
    }

}