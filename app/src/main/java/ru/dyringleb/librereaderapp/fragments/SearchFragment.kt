package ru.dyringleb.librereaderapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.adapters.AuthorsSearchAdapter
import ru.dyringleb.librereaderapp.adapters.BooksAdapter
import ru.dyringleb.librereaderapp.fragments.author.AuthorPageFragment
import ru.dyringleb.librereaderapp.fragments.books.BookDetailsFragment
import ru.dyringleb.librereaderapp.retrofitConf.Author
import ru.dyringleb.librereaderapp.utils.GenreSelectionModal
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment
import ru.dyringleb.librereaderapp.viewmodels.SearchingViewModel

class SearchFragment : Fragment(),
    BooksAdapter.OnLoadMoreClickListener,
    BooksAdapter.AuthorNameClickInterface,
    BooksAdapter.OnItemClickListener,
    AuthorsSearchAdapter.OnItemClickListener,
    AuthorsSearchAdapter.OnButtonClick
{

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: Button
    private lateinit var buttonSelectGenres: Button
    private lateinit var switchSearchType: Switch
    private lateinit var viewModel: SearchingViewModel
    private lateinit var genreSelectionModal: GenreSelectionModal
    private lateinit var adapter: BooksAdapter
    private lateinit var adapterAuthor: AuthorsSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = SearchingViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        genreSelectionModal = GenreSelectionModal(requireContext())


        recyclerView = view.findViewById(R.id.recyclerViewResults)
        editTextSearch = view.findViewById(R.id.editTextSearch)
        buttonSearch = view.findViewById(R.id.buttonSearch)
        buttonSelectGenres = view.findViewById(R.id.buttonSelectGenres)
        switchSearchType = view.findViewById(R.id.switchSearchType)

        if(switchSearchType.isChecked){
            viewModel.setSwtichState(true)
        }
        else{
            viewModel.setSwtichState(false)
        }

        // Установка адаптера для RecyclerView
        adapter = BooksAdapter(emptyList(), this, this, this) // Передаем пустой список
        adapterAuthor = AuthorsSearchAdapter(emptyList(), this, this)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Наблюдаем за изменениями списка книг
        viewModel.books.observe(viewLifecycleOwner, Observer { books ->
            if (books != null) {
                adapter.updateBooks(books)
            } else {
                adapter.updateBooks(emptyList())
            }
        })



        // Устанавливаем слушателя для переключателя
        switchSearchType.setOnCheckedChangeListener { _, isChecked ->
            buttonSelectGenres.visibility = if (isChecked) View.GONE else View.VISIBLE
            viewModel.changeSwitchState()
        }



        viewModel.by_author.observe(viewLifecycleOwner, Observer {state->
            if(state!=false) {
                recyclerView.adapter = adapterAuthor
                adapterAuthor.updateAuthors(emptyList())
            }
            else{
                recyclerView.adapter = adapter
                adapter.updateBooks(emptyList())
            }
        })


        viewModel.author.observe(viewLifecycleOwner, Observer { author ->
            if (author != null) {
                adapterAuthor.updateAuthors(author)
            } else {
                adapterAuthor.updateAuthors(emptyList())
            }
        })

        buttonSearch.setOnClickListener { performSearch() }

        buttonSelectGenres.setOnClickListener {
            genreSelectionModal.showDialog()
        }

        return view
    }

    private fun performSearch() {
        val query = editTextSearch.text.toString()
        val isByAuthor = switchSearchType.isChecked
        val genresId = genreSelectionModal.getSelectedGenres()

        // Выполняем поиск книг в зависимости от типа поиска
        if (isByAuthor) {
            viewModel.fetchFilteredAuthors(query, page=1, new=true)
        } else {
            viewModel.fetchBooksByGenres(genresId, query, new=true)
        }
    }

    override fun onLoadMoreClicked() {
        // Предполагается, что вызвать метод загрузки следующей страницы
        val genresId = genreSelectionModal.getSelectedGenres()
        val query = editTextSearch.text.toString()
        viewModel.loadNextPage(genresId, query)
    }

    override fun onAuthorNameClick(author: Author) {
        val bundle = Bundle()
        bundle.putInt("authorId", author.author_id)
        changeCurrentFragment(parentFragmentManager, AuthorPageFragment(), bundle)
    }

    override fun onBookTitleClick(bookId: Int) {
        val bundle = Bundle()
        bundle.putInt("bookId", bookId)
        changeCurrentFragment(parentFragmentManager, BookDetailsFragment(), bundle = bundle)
    }

    override fun onAuthorClick(authorId: Int) {
        val bundle = Bundle()
        bundle.putInt("authorId", authorId)
        changeCurrentFragment(parentFragmentManager, AuthorPageFragment(), bundle)
    }

    override fun onButtonClicked() {
        val query = editTextSearch.text.toString()
        viewModel.loadNextAuthorPage(query)
    }
}
