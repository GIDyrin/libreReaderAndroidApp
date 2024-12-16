package ru.dyringleb.librereaderapp.fragments.author

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment

class NewAuthorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_author, container, false)

        val createAuthorButton: Button = view.findViewById(R.id.createAuthor)
        createAuthorButton.setOnClickListener {
        changeCurrentFragment(parentFragmentManager, RegistrateAuthorFragment(), null)
        }

        return view
    }
}
