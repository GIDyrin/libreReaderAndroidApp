package ru.dyringleb.librereaderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R

class LocalBooksAdapter(private var items: List<DatabaseHelper.Companion.BookWithBookmarks>,
                        private val onClickToAddBook: OnClickToAddBook,
                        private val onClickToDeleteBook: OnClickToDeleteBook,
                        private val onClickToReadBook: OnClickToReadBook
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ADD_BUTTON = 0
    private val VIEW_TYPE_BOOK = 1


    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) VIEW_TYPE_ADD_BUTTON else VIEW_TYPE_BOOK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_button, parent, false)
                AddButtonViewHolder(view)
            }
            VIEW_TYPE_BOOK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_local_book, parent, false)
                BookViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AddButtonViewHolder -> {
                holder.addBookButton.setOnClickListener() {
                    onClickToAddBook.addLocalBook()
                }
            }
            is BookViewHolder -> {
                val item = items[position]
                holder.bookTitle.text = item.bookTitle
                holder.deleteBook.setOnClickListener{
                    onClickToDeleteBook.deleteBook(item.bookId)
                }
                holder.bookTitle.setOnClickListener{
                    onClickToReadBook.readLocalBook(item)
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return items.size + 1 // Один элемент для кнопки
    }

    // ViewHolder для кнопки добавления
    inner class AddButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val addBookButton: Button = itemView.findViewById(R.id.addBookButton)
    }

    // ViewHolder для книги
    inner class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val deleteBook: ImageButton = itemView.findViewById(R.id.deleteBook)

    }

    fun updateBooks(newItems: List<DatabaseHelper.Companion.BookWithBookmarks>){
        items = newItems
        notifyDataSetChanged()  // Уведомляем адаптер об изменении данных
    }

    interface OnClickToAddBook{
        fun addLocalBook()
    }

    interface OnClickToDeleteBook{
        fun deleteBook(bookId: Int)
    }


    interface OnClickToReadBook{
        fun readLocalBook(item: DatabaseHelper.Companion.BookWithBookmarks)
    }
}
