package ru.dyringleb.librereaderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.retrofitConf.Author

class AuthorsSearchAdapter(
    private var authorList: List<Author>,
    private val itemClickListener: OnItemClickListener,
    private val loadMoreClickListener: OnButtonClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_AUTHOR = 1
        private const val VIEW_TYPE_LOAD_MORE = 2
        private const val VIEW_TYPE_EMPTY = 3 // Новый тип для пустого списка
    }

    interface OnItemClickListener {
        fun onAuthorClick(authorId: Int) // Метод для обработки клика по автору
    }

    interface OnButtonClick {
        fun onButtonClicked() // Метод для обработки клика по кнопке загрузки
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            authorList.isEmpty() -> VIEW_TYPE_EMPTY // Пустой список
            position == authorList.size -> VIEW_TYPE_LOAD_MORE // Кнопка загрузки следующей страницы
            else -> VIEW_TYPE_AUTHOR // Элемент автора
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOAD_MORE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_load_more, parent, false)
                LoadMoreViewHolder(view)
            }
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_empty, parent, false)
                EmptyViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_author, parent, false)
                AuthorViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AuthorViewHolder -> {
                val author = authorList[position]
                holder.authorName.text = author.author_name
                holder.biography.text = if (author.biography.length > 250) {
                    author.biography.substring(0, 250) + "..." // Добавляем многоточие
                } else {
                    author.biography // Возвращаем всю биографию
                }

                holder.authorName.setOnClickListener {
                    itemClickListener.onAuthorClick(author.author_id) // Передаем ID автора
                }
            }
            is LoadMoreViewHolder -> {
                holder.loadMoreButton.setOnClickListener {
                    loadMoreClickListener.onButtonClicked()
                }
            }
            is EmptyViewHolder -> {
                // Здесь вы можете установить текст или другие элементы для отображения пустого состояния
            }
        }
    }

    override fun getItemCount(): Int {
        return if (authorList.isEmpty()) {
            1
        } else {
            if (authorList.size < 50) {
                authorList.size
            } else {
                authorList.size + 1
            }
        }
    }

    fun updateAuthors(newAuthors: List<Author>) {
        authorList = newAuthors
        notifyDataSetChanged() // Уведомляем адаптер об изменении данных
    }

    // ViewHolder для автора
    inner class AuthorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorName: TextView = itemView.findViewById(R.id.authorName) // Имя автора
        val biography: TextView = itemView.findViewById(R.id.Biography) // Биография
    }

    // ViewHolder для кнопки загрузки
    inner class LoadMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val loadMoreButton: Button = itemView.findViewById(R.id.buttonLoadMore)
    }

    // ViewHolder для пустого состояния
    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Здесь можно добавить компоненты, если нужно
    }
}
