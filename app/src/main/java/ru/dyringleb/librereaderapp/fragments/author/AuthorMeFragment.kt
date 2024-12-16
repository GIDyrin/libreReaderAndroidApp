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
import androidx.lifecycle.ViewModelProvider
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.SharedPreferencesHelper
import ru.dyringleb.librereaderapp.viewmodels.AuthorViewModel
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment
import ru.dyringleb.librereaderapp.utils.isNetworkAvailable
import ru.dyringleb.librereaderapp.utils.loadImage

class AuthorMeFragment : Fragment() {
    private lateinit var authorViewModel: AuthorViewModel
    private lateinit var profileImage: ImageView
    private lateinit var username: TextView
    private lateinit var about: TextView
    private lateinit var editButton: Button
    private lateinit var authorBooks: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorViewModel = ViewModelProvider(this).get(AuthorViewModel::class.java)

        if (!isNetworkAvailable(requireContext())) {
            activity?.findViewById<ImageButton>(R.id.navButtonLibrary)?.performClick()
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_author, container, false)
        val sharedPreferences = SharedPreferencesHelper(requireContext())
        if (sharedPreferences.getAuthToken() == null) {
            Toast.makeText(requireContext(), "Войдите в систему", Toast.LENGTH_LONG).show()
            activity?.findViewById<ImageButton>(R.id.navButtonMe)?.performClick()
        }

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
        editButton = view.findViewById(R.id.updateAuthor)
        profileImage = view.findViewById(R.id.profilePortrait)

        authorViewModel.fetchMyAuthorPage()

        authorBooks.setOnClickListener {
            authorViewModel.author.value?.let { author ->
                val bundle = Bundle()
                bundle.putInt("author_id", author.author_id)
                changeCurrentFragment(parentFragmentManager, AuthorMeBooksFragment(), bundle)
            }
        }

        editButton.setOnClickListener {
            changeCurrentFragment(parentFragmentManager, EditAuthorFragment(), null)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        authorViewModel.fetchMyAuthorPage() // Повторно получить данные при возврате к фрагменту
    }
}
