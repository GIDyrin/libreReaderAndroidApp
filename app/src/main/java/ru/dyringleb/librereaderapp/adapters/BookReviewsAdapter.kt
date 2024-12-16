package ru.dyringleb.librereaderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.retrofitConf.BookReview

class BookReviewsAdapter(private val reviews: List<BookReview>) : RecyclerView.Adapter<BookReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reviewText: TextView = view.findViewById(R.id.reviewText)
        val reviewRate: TextView = view.findViewById(R.id.reviewRate)
        val reviewDate: TextView = view.findViewById(R.id.reviewDate)
        val username: TextView = view.findViewById(R.id.username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.reviewText.text = review.review_text
        holder.reviewRate.text = "${review.review_rate} из 10"
        holder.reviewDate.text = "Оставлен: ${review.review_date}"
        holder.username.text = "Пользователь: ${review.user.username}"

    }

    override fun getItemCount(): Int {
        return reviews.size
    }
}
