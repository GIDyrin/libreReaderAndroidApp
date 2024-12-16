package ru.dyringleb.librereaderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.retrofitConf.BookByAuthor
import ru.dyringleb.librereaderapp.retrofitConf.BookSearchResponse
import ru.dyringleb.librereaderapp.retrofitConf.FullBooksInfo

class AuthorBooksAdapter(
    private var bookList: List<FullBooksInfo>,
    private val loadMoreClickListener: OnLoadMoreClickListener,
    private val itemClickListener: OnItemClickListener,
    private val onBookDeleteListener: OnBookDeleteListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BOOK = 1
        private const val VIEW_TYPE_LOAD_MORE = 2
        private const val VIEW_TYPE_EMPTY = 3 // Новый тип для пустого списка
    }

    interface OnItemClickListener {
        fun onBookTitleClick(bookId: Int) // Метод для передачи bookId
    }

    interface OnBookDeleteListener {
        fun onDeleteButtonClick(bookId: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            bookList.isEmpty() -> VIEW_TYPE_EMPTY // Пустой список
            position >= bookList.size -> VIEW_TYPE_LOAD_MORE // Кнопка загрузки следующей страницы
            else -> VIEW_TYPE_BOOK // Элемент книги
        }
    }

    override fun getItemCount(): Int {
        return if (bookList.isEmpty()) {
            1 // Показать только один элемент - сообщение о пустом списке
        } else {
            if (bookList.size < 50) {
                bookList.size // Возвращаем только количество книг
            } else {
                bookList.size + 1 // Добавляем 1 для кнопки "Загрузить следующую страницу"
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOAD_MORE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_load_more, parent, false)
                LoadMoreViewHolder(view)
            }
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false)
                EmptyViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_mine, parent, false)
                BookViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BookViewHolder -> {
                val book = bookList[position]
                holder.bookTitle.text = book.book_title
                holder.author.text = book.author.author_name
                holder.date.text = book.book_year.toString()

                holder.deleteButton.setOnClickListener(){
                    onBookDeleteListener.onDeleteButtonClick(book.book_id)
                }

                // Обработка клика по названию книги
                holder.bookTitle.setOnClickListener {
                    itemClickListener.onBookTitleClick(book.book_id) // Передаем bookId
                }
            }
            is LoadMoreViewHolder -> {
                // Привязываем кнопку загрузки следующей страницы
                holder.loadMoreButton.setOnClickListener {
                    loadMoreClickListener.onLoadMoreClicked() // Вызываем метод нажатия на кнопку
                }
            }
            is EmptyViewHolder -> {
            }
        }
    }


    fun updateBooks(newBooks: List<FullBooksInfo>) {
        bookList = newBooks
        notifyDataSetChanged()  // Уведомляем адаптер об изменении данных
    }

    // ViewHolder для книги
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val author: TextView = itemView.findViewById(R.id.author)
        val date: TextView = itemView.findViewById(R.id.date)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteBook)
    }


    inner class LoadMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val loadMoreButton: Button = itemView.findViewById(R.id.buttonLoadMore)
    }

    // Новый ViewHolder для пустого списка
    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Здесь можно добавить ссылки на элементы UI, если это нужно
    }

    // Интерфейс для обработки нажатия на кнопку загрузки следующей страницы
    interface OnLoadMoreClickListener {
        fun onLoadMoreClicked()
    }
}
