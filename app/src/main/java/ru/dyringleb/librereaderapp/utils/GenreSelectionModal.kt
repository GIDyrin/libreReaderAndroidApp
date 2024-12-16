package ru.dyringleb.librereaderapp.utils
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class GenreSelectionModal(private val context: Context) {

    // Список жанров
    private val genres = arrayOf(
        "Проза", "Поэзия", "Драматургия", "Переводы", "Сказки", "Детская",
        "Мемуары", "История", "Публицистика", "Критика", "Философия", "Религия",
        "Политика", "Историческая проза", "Биографическая проза", "Юмор и сатира",
        "Путешествия", "Правоведение", "Этнография", "Приключения", "Педагогика",
        "Психология", "География", "Справочная", "Антропология", "Филология",
        "Зоология", "Эпистолярий", "Ботаника", "Фантастика", "Политэкономия"
    )

    private val selectedGenres = ArrayList<Int>()  // Список выбранных жанров

    // Метод для открытия диалогового окна
    fun showDialog() {
        val selectedItems = BooleanArray(genres.size)

        // Устанавливаем текущее состояние выбранных элементов
        for (genre in selectedGenres) {
            selectedItems[genre - 1] = true // Подключаемся к массиву по индексу
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Выберите жанры")

        builder.setMultiChoiceItems(genres, selectedItems) { _, which, isChecked ->
            if (isChecked) {
                selectedGenres.add(which + 1)  // Добавляем жанр
            } else {
                selectedGenres.remove(which + 1) // Удаляем жанр
            }
        }

        // Кнопки "OK" в диалоге
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Кнопка "Отмена"
        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }

        builder.show()
    }

    // Метод для получения выбранных жанров
    fun getSelectedGenres(): List<Int> {
        return selectedGenres
    }

}
