package ru.dyringleb.librereaderapp.fragments.author

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.SharedPreferencesHelper
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment
import ru.dyringleb.librereaderapp.utils.loadImage
import ru.dyringleb.librereaderapp.viewmodels.AuthorViewModel


class AuthorPageFragment : Fragment() {
    private lateinit var authorViewModel: AuthorViewModel
    private lateinit var profileImage: ImageView
    private lateinit var username: TextView
    private lateinit var about: TextView
    private lateinit var authorBooks: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_author_page, container, false)
        authorViewModel = AuthorViewModel()
        // Подписка на изменения в данных автора
        authorViewModel.author.observe(viewLifecycleOwner, Observer { author ->
            author?.let {
                username.text = it.author_name
                about.text = it.biography

                val imageUrl = "api/v1/authors/${it.author_id}/portrait/"
                loadImage(this, imageUrl, profileImage)
            }
        })

        authorViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                changeCurrentFragment(parentFragmentManager, NewAuthorFragment(), null)
            }
        })


        username = view.findViewById(R.id.AuthorName)
        about = view.findViewById(R.id.aboutMe)
        authorBooks = view.findViewById(R.id.BookLink)
        profileImage = view.findViewById(R.id.profilePortrait)

        arguments?.let { authorViewModel.fetchAuthorById(it.getInt("authorId")) }

        authorBooks.setOnClickListener {
            authorViewModel.author.value?.let { author ->
                val bundle = Bundle()
                bundle.putInt("author_id", author.author_id)
                changeCurrentFragment(parentFragmentManager, AuthorPageBooksFragment(), bundle)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        arguments?.let { authorViewModel.fetchAuthorById(it.getInt("authorId")) }
    }


}