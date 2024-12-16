package ru.dyringleb.librereaderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.retrofitConf.Review

class ReviewsAdapter(
    private val reviews: List<Review>,
    private val itemClickListener: OnItemClickListener // Добавляем слушателя
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    interface OnItemClickListener {
        fun onBookTitleClick(bookId: Int) // Метод для передачи bookId
    }

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reviewText: TextView = view.findViewById(R.id.reviewText)
        val reviewRate: TextView = view.findViewById(R.id.reviewRate)
        val reviewDate: TextView = view.findViewById(R.id.reviewDate)
        val bookTitle: TextView = view.findViewById(R.id.bookTitle) // Название книги
        val authorName: TextView = view.findViewById(R.id.authorName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reviews, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // Заполняем данными
        holder.reviewText.text = review.review_text
        holder.reviewRate.text = "${review.review_rate} из 10"
        holder.reviewDate.text = "Оставлен: ${review.review_date}"
        holder.bookTitle.text = review.book.book_title
        holder.authorName.text = review.book.author.author_name

        // Обработка клика по названию книги
        holder.bookTitle.setOnClickListener {
            itemClickListener.onBookTitleClick(review.book.book_id) // Передаем bookId
        }
    }

    override fun getItemCount() = reviews.size
}
