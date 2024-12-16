package ru.dyringleb.librereaderapp.fragments.author

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.adapters.AuthorBooksAdapter
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment
import ru.dyringleb.librereaderapp.fragments.books.BookDetailsFragment
import ru.dyringleb.librereaderapp.retrofitConf.RetrofitClient
import ru.dyringleb.librereaderapp.viewmodels.AuthorViewModel

class AuthorMeBooksFragment : Fragment(), AuthorBooksAdapter.OnLoadMoreClickListener,
    AuthorBooksAdapter.OnItemClickListener, AuthorBooksAdapter.OnBookDeleteListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var authorBooksAdapter: AuthorBooksAdapter
    private lateinit var authorViewModel: AuthorViewModel
    private lateinit var postBook: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorViewModel = AuthorViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_author_books, container, false)

        postBook = view.findViewById(R.id.postBook)

        // Инициализация RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Инициализация адаптера
        authorBooksAdapter = AuthorBooksAdapter(emptyList(), this, this, this)
        recyclerView.adapter = authorBooksAdapter

        // Получение данных о книгах автора
        arguments?.let {
            authorViewModel.fetchBooksByAuthor(it.getInt("author_id"), new = true)
        }

        // Наблюдение за изменениями списка книг
        authorViewModel.books.observe(viewLifecycleOwner, { bookList ->
            authorBooksAdapter.updateBooks(bookList)
        })

        postBook.setOnClickListener(){
            changeCurrentFragment(parentFragmentManager, PostBookFragment(), null)
        }

        return view
    }



    // Реализация интерфейса для обработки нажатия на кнопку загрузки следующей страницы
    override fun onLoadMoreClicked()  {
        // Получаем ID автора из аргументов
        arguments?.let {
            val authorId = it.getInt("author_id")
            authorViewModel.loadNextPage(authorId) // Загружаем следующую страницу
        }
    }

    override fun onBookTitleClick(bookId: Int) {
        val bundle = Bundle()
        bundle.putInt("bookId", bookId)
        changeCurrentFragment(parentFragmentManager, BookDetailsFragment(), bundle)
    }
    override fun onDeleteButtonClick(bookId: Int) {
        showDeleteConfirmationDialog(bookId)
    }

    private fun showDeleteConfirmationDialog(bookId: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Подтверждение удаления")
        builder.setMessage("Вы уверены, что хотите удалить эту книгу?")
        builder.setPositiveButton("Да") { dialog, which ->
            deleteBook(bookId)
        }
        builder.setNegativeButton("Нет") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteBook(bookId: Int) {
        // Удаляем книгу через API
        RetrofitClient.apiService.deleteMyBook(bookId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {

                    authorViewModel.nulledBooks()
                    authorViewModel.fetchBooksByAuthor(arguments?.getInt("author_id") ?: 0) // Перезагрузить данные
                } else {
                    Toast.makeText(requireContext(), response.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        authorViewModel.books.value?.let { authorBooksAdapter.updateBooks(it) }
    }
}
