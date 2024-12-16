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
import ru.dyringleb.librereaderapp.retrofitConf.RegistrationRequest
import ru.dyringleb.librereaderapp.retrofitConf.RegistrationResponse
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient


class RegistrationFragment : Fragment() {
    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        val registrationButton: Button = view.findViewById(R.id.reg_button)
        emailEditText = view.findViewById(R.id.reg_email)
        usernameEditText = view.findViewById(R.id.reg_username)
        passwordEditText = view.findViewById(R.id.reg_password)
        confirmPasswordEditText = view.findViewById(R.id.reg_conf_password)

        // Находим ваше текстовое поле
        val redirectionToLogin: TextView = view.findViewById(R.id.toSignIn)
        // Обработка нажатия на кнопку регистрации

        registrationButton.setOnClickListener {
            registerUser(redirectionLink=redirectionToLogin)
        }

        // Устанавливаем обработчик нажатий
        redirectionToLogin.setOnClickListener {
            // Создаем новый фрагмент
            val homeFragment = LoginFragment()

            // Получаем FragmentManager и начинаем транзакцию
            val fragmentManager = parentFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            // Заменяем текущий фрагмент на новый
            fragmentTransaction.replace(R.id.fragmentContainerView, homeFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        return view
    }

    private fun registerUser(redirectionLink: TextView) {
        val email = emailEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // Проверка на пустоту и совпадение паролей
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        // Создание объекта запроса
        val registrationRequest = RegistrationRequest(email=email, username=username, password=password)

        // Выполнение запроса регистрации
        RetrofitClient.apiService.register(registrationRequest).enqueue(object :
            Callback<RegistrationResponse> {
            override fun onResponse(
                call: Call<RegistrationResponse>,
                response: Response<RegistrationResponse>
            ) {
                if (response.isSuccessful) {
                    // Получаем ответ из тела
                    val registrationResponse = response.body()
                    val message = "Регистрация завершена успешно!"

                    // Выводим сообщение о успешной регистрации
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    // Здесь можно выполнить переход на другой экран, если это необходимо
                    redirectionLink.performClick()

                } else {
                    // Вывод сообщения об ошибке, если регистрация не удалась
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(requireContext(), "Ошибка регистрации: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                // Вывод сообщения об ошибке соединения
                Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
