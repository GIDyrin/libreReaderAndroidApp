package ru.dyringleb.librereaderapp.fragments.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.adapters.ReviewsAdapter
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment
import ru.dyringleb.librereaderapp.fragments.books.BookDetailsFragment
import ru.dyringleb.librereaderapp.retrofitConf.Review


class UserReviewsFragment : Fragment(), ReviewsAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewsAdapter: ReviewsAdapter
    private lateinit var reviews: List<Review>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)

        // Получение списка отзывов из аргументов
        arguments?.let {
            reviews = it.getParcelableArrayList("reviews") ?: emptyList()
        }

        // Инициализация RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        reviewsAdapter = ReviewsAdapter(reviews, this) // Передаем слушатель в адаптер
        recyclerView.adapter = reviewsAdapter

        return view
    }

    override fun onBookTitleClick(bookId: Int) {
        val bundle = Bundle()
        bundle.putInt("bookId", bookId) // Передаем book_id во фрагмент
        changeCurrentFragment(parentFragmentManager, BookDetailsFragment(), bundle)
    }

}