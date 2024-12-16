package ru.dyringleb.librereaderapp.fragments.books

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import ru.dyringleb.librereaderapp.retrofitConf.ReviewPostRequest

class ReviewPostFragment : Fragment() {
    private var selectedRating: Int = 0 // Для хранения выбранной оценки
    private lateinit var star1: ImageView
    private lateinit var star2: ImageView
    private lateinit var star3: ImageView
    private lateinit var star4: ImageView
    private lateinit var star5: ImageView
    private lateinit var star6: ImageView
    private lateinit var star7: ImageView
    private lateinit var star8: ImageView
    private lateinit var star9: ImageView
    private lateinit var star10: ImageView
    private lateinit var stars: Array<ImageView>
    private var bookId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Наполняем представление
        val view = inflater.inflate(R.layout.fragment_review_post, container, false)
        arguments?.let {
            bookId = it.getInt("bookId", 0)
        }

        // Получаем ссылки на звезды
        star1 = view.findViewById<ImageView>(R.id.star1)
        star2 = view.findViewById<ImageView>(R.id.star2)
        star3 = view.findViewById<ImageView>(R.id.star3)
        star4 = view.findViewById<ImageView>(R.id.star4)
        star5 = view.findViewById<ImageView>(R.id.star5)
        star6 = view.findViewById<ImageView>(R.id.star6)
        star7 = view.findViewById<ImageView>(R.id.star7)
        star8 = view.findViewById<ImageView>(R.id.star8)
        star9 = view.findViewById<ImageView>(R.id.star9)
        star10 = view.findViewById<ImageView>(R.id.star10)

        // Слушатели нажатий для звезд
        stars = arrayOf(star1, star2, star3, star4, star5, star6, star7, star8, star9, star10)
        for (i in stars.indices) {
            stars[i].setOnClickListener {
                selectRating(i + 1)
            }
        }

        // Обработка нажатия на кнопку отправки
        val buttonSubmitReview = view.findViewById<Button>(R.id.buttonSubmitReview)
        buttonSubmitReview.setOnClickListener {
            submitReview() // Здесь будет обработка отправки отзыва
        }

        return view
    }

    private fun selectRating(rating: Int) {
        selectedRating = rating // Сохраняем выбранную оценку

        // Обновляем визуальное представление звезд
        for (i in stars.indices) {
            stars[i].setImageResource(
                if (i < rating) R.drawable.ic_star else R.drawable.ic_star_border // Устанавливаем заполненные или пустые звезды
            )
        }
    }

    private fun submitReview() {
        val reviewText = view?.findViewById<EditText>(R.id.editTextReview)?.text.toString()

        // Проверка на пустую строку и ограничение по количеству символов
        if (reviewText.isBlank()) {
            Toast.makeText(requireContext(), "Пожалуйста, введите отзыв", Toast.LENGTH_SHORT).show()
        } else if (reviewText.length > 2000) {
            Toast.makeText(requireContext(), "Отзыв не может превышать 2000 символов", Toast.LENGTH_SHORT).show()
        } else if (selectedRating <= 0) {
            Toast.makeText(requireContext(), "Пожалуйста, выберите оценку", Toast.LENGTH_SHORT).show()
        } else {
            // Создаем запрос отзыва
            val reviewPostRequest = ReviewPostRequest(book = bookId, review_rate = selectedRating, review_text = reviewText)

            // Отправляем запрос на сервер
            RetrofitClient.apiService.postReview(reviewPostRequest).enqueue(object :
                Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Отзыв успешно отправлен", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        response.errorBody()?.string()?.let { errorBody ->
                            try {
                                // Пробуем разобрать тело ошибки для извлечения детали
                                val jsonObject = JSONObject(errorBody)
                                val errorMessage = jsonObject.getJSONObject("error").getString("detail")
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                            } catch (e: JSONException) {
                                // Если произошла ошибка разбора JSON, показываем общий сообщение
                                Toast.makeText(requireContext(), "Ошибка при отправке отзыва: ${response.message()}", Toast.LENGTH_SHORT).show()
                            }
                        } ?: run {
                            // Если нет тела ответа, показываем сообщение
                            Toast.makeText(requireContext(), "Ошибка при отправке отзыва: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}
