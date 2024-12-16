package ru.dyringleb.librereaderapp.fragments.author

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.adapters.AuthorBooksAdapter
import ru.dyringleb.librereaderapp.adapters.BooksAdapter
import ru.dyringleb.librereaderapp.fragments.books.BookDetailsFragment
import ru.dyringleb.librereaderapp.retrofitConf.Author
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment
import ru.dyringleb.librereaderapp.viewmodels.AuthorViewModel

class AuthorPageBooksFragment : Fragment(), BooksAdapter.OnLoadMoreClickListener,
    BooksAdapter.OnItemClickListener, BooksAdapter.AuthorNameClickInterface {
    private lateinit var recyclerView: RecyclerView
    private lateinit var authorBooksAdapter: BooksAdapter
    private lateinit var authorViewModel: AuthorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorViewModel = AuthorViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_author_page_books, container, false)


        // Инициализация RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Инициализация адаптера
        authorBooksAdapter = BooksAdapter(emptyList(), this, this, this)
        recyclerView.adapter = authorBooksAdapter

        // Получение данных о книгах автора
        arguments?.let {
            authorViewModel.fetchBooksByAuthor(it.getInt("author_id"))
        }

        // Наблюдение за изменениями списка книг
        authorViewModel.books.observe(viewLifecycleOwner, { bookList ->
            authorBooksAdapter.updateBooks(bookList)
        })


        return view
    }

    // Реализация интерфейса для обработки нажатия на кнопку загрузки следующей страницы
    override fun onLoadMoreClicked() {
        // Получаем ID автора из аргументов
        arguments?.let {
            val authorId = it.getInt("author_id")
            authorViewModel.loadNextPage(authorId) // Загружаем следующую страницу
        }
    }

    override fun onAuthorNameClick(author: Author) {
        TODO("Not yet implemented")
    }
    override fun onBookTitleClick(bookId: Int) {
        val bundle = Bundle()
        bundle.putInt("bookId", bookId)
        changeCurrentFragment(parentFragmentManager, BookDetailsFragment(), bundle)
    }
}