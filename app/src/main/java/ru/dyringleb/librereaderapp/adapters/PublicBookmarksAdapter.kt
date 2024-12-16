package ru.dyringleb.librereaderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.adapters.AuthorBooksAdapter.OnBookDeleteListener
import ru.dyringleb.librereaderapp.adapters.AuthorBooksAdapter.OnItemClickListener
import ru.dyringleb.librereaderapp.adapters.AuthorBooksAdapter.OnLoadMoreClickListener
import ru.dyringleb.librereaderapp.retrofitConf.Book
import ru.dyringleb.librereaderapp.retrofitConf.BookmarkResponse
import ru.dyringleb.librereaderapp.retrofitConf.FullBooksInfo

class PublicBookmarksAdapter(
    private var bookList: List<BookmarkResponse>,
    private val itemClickListener: PublicBookmarksAdapter.OnItemClickListener,
    private val onBookDeleteListener: PublicBookmarksAdapter.OnBookDeleteListener
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BOOK = 1
        private const val VIEW_TYPE_EMPTY = 2 // Новый тип для пустого списка
    }

    interface OnItemClickListener {
        fun onBookTitleClick(bookId: Int, bookmarkId: Int)
    }

    interface OnBookDeleteListener {
        fun onDeleteButtonClick(bookId: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            bookList.isEmpty() -> VIEW_TYPE_EMPTY // Пустой список
            else -> VIEW_TYPE_BOOK // Элемент книги
        }
    }

    override fun getItemCount(): Int {
        return if (bookList.isEmpty()) {
            1 // Показать только один элемент - сообщение о пустом списке
        } else {
            return  bookList.size
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false)
                EmptyViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark_public, parent, false)
                BookViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BookViewHolder -> {
                val bookmark = bookList[position]
                holder.bookTitle.text = bookmark.book.book_title
                holder.author.text = bookmark.book.author.author_name
                holder.date.text = bookmark.book.book_year.toString()

                holder.deleteButton.setOnClickListener(){
                    onBookDeleteListener.onDeleteButtonClick(bookmark.bookmark_id)
                }

                // Обработка клика по названию книги
                holder.bookTitle.setOnClickListener {
                    itemClickListener.onBookTitleClick(bookmark.book.book_id, bookmark.bookmark_id) // Передаем bookId
                }
            }
            is EmptyViewHolder -> {
            }
        }
    }


    fun updateBooks(newBooks: List<BookmarkResponse>) {
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


    // Новый ViewHolder для пустого списка
    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Здесь можно добавить ссылки на элементы UI, если это нужно
    }


}