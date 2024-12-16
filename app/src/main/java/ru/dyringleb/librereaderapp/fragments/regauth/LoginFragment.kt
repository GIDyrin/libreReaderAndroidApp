package ru.dyringleb.librereaderapp.fragments.regauth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.SharedPreferencesHelper
import ru.dyringleb.librereaderapp.fragments.user.MyProfileFragment
import ru.dyringleb.librereaderapp.retrofitConf.LoginRequest
import ru.dyringleb.librereaderapp.retrofitConf.LoginResponse
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient


class LoginFragment : Fragment() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var storeHelper: SharedPreferencesHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        // Находим текстовое поле
        val redirectionToRegistration: TextView = view.findViewById(R.id.toSignUp)
        usernameEditText = view.findViewById(R.id.auth_username)
        passwordEditText = view.findViewById(R.id.auth_password)

        // Устанавливаем обработчик нажатий
        redirectionToRegistration.setOnClickListener {
            // Создаем новый фрагмент
            val homeFragment = RegistrationFragment()

            // Получаем FragmentManager и начинаем транзакцию
            val fragmentManager = parentFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            // Заменяем текущий фрагмент на новый
            fragmentTransaction.replace(R.id.fragmentContainerView, homeFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        storeHelper = SharedPreferencesHelper(requireContext())
        val loginButton: Button = view.findViewById(R.id.auth_button)
        loginButton.setOnClickListener(){
            loginAction()
        }

        return view
    }

    private fun loginAction(){
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val loginRequest = LoginRequest(username=username, password=password)

        RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val message = "Успешный вход!"

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    // Здесь действия после успешного входа
                    storeHelper.saveAuthToken(loginResponse!!.auth_token)

                    // переброс на страницу пользователя
                    val homeFragment = MyProfileFragment()
                    val fragmentManager = parentFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragmentContainerView, homeFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(requireContext(), "Ошибка входа: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}