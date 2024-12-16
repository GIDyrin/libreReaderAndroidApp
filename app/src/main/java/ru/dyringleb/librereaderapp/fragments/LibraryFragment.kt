package ru.dyringleb.librereaderapp.fragments

import DatabaseHelper
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.dyringleb.librereaderapp.R
import ru.dyringleb.librereaderapp.adapters.LocalBooksAdapter
import ru.dyringleb.librereaderapp.adapters.PublicBookmarksAdapter
import ru.dyringleb.librereaderapp.fragments.books.BookDetailsFragment
import ru.dyringleb.librereaderapp.utils.FilePicker
import ru.dyringleb.librereaderapp.utils.animateImageButtons
import ru.dyringleb.librereaderapp.utils.changeCurrentFragment

import ru.dyringleb.librereaderapp.viewmodels.LibraryViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class LibraryFragment : Fragment() {
    private lateinit var viewModel: LibraryViewModel
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var publicBooksCard: CardView
    private lateinit var localBooksCard: CardView
    private lateinit var publicBooksHeader: TextView
    private lateinit var localBooksHeader: TextView
    private lateinit var localBooksList: RecyclerView
    private lateinit var publicBooksList: RecyclerView
    private lateinit var publicBookmarksAdapter: PublicBookmarksAdapter
    private lateinit var localBooksAdapter: LocalBooksAdapter
    private lateinit var filePicker: FilePicker
    private var selectedUriFromModal: Uri? = null
    private val clicked = BooleanArray(2)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Подключаем локальную бдшку
        dbHelper = DatabaseHelper(requireContext())
        viewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]
        viewModel.setDbHelper(dbHelper)
        filePicker = FilePicker(this, fileSelectedCallback =  {uri -> selectedUriFromModal = uri})
        viewModel.getLocalBooks()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        clicked[0] = true
        clicked[0] = false

        localBooksCard = view.findViewById(R.id.local_books_card)
        publicBooksCard = view.findViewById(R.id.public_books_card)
        localBooksHeader = view.findViewById(R.id.local_books_header)
        publicBooksHeader = view.findViewById(R.id.public_books_header)
        localBooksList = view.findViewById(R.id.local_books_list)
        publicBooksList = view.findViewById(R.id.public_books_list)



        publicBooksList.layoutManager = LinearLayoutManager(requireContext())
        localBooksList.layoutManager = LinearLayoutManager(requireContext())

        localBooksAdapter = LocalBooksAdapter(
            emptyList(),
            onClickToAddBook = object : LocalBooksAdapter.OnClickToAddBook {
                override fun addLocalBook() {
                    filePicker.setUriNull()
                    showLocalBookAddingDialog(this@LibraryFragment, dbHelper, filePicker)
                }
            },
            onClickToDeleteBook = object : LocalBooksAdapter.OnClickToDeleteBook{
                override fun deleteBook(bookId: Int) {
                    viewModel.dbDeleteBook(bookId)
                }
            },
            onClickToReadBook = object : LocalBooksAdapter.OnClickToReadBook{
                override fun readLocalBook(item: DatabaseHelper.Companion.BookWithBookmarks) {
                    viewModel.dbStartReadingActivity(item, this@LibraryFragment.requireActivity())
                }
            }

        )


        publicBookmarksAdapter = PublicBookmarksAdapter(emptyList(), object : PublicBookmarksAdapter.OnItemClickListener {
            override fun onBookTitleClick(bookId: Int, bookmarkId: Int) {
                val bundle = Bundle()
                bundle.putInt("bookId", bookId)
                changeCurrentFragment(this@LibraryFragment.parentFragmentManager, BookDetailsFragment(), bundle)
            }
        }, object : PublicBookmarksAdapter.OnBookDeleteListener {
            override fun onDeleteButtonClick(bookId: Int) {
                viewModel.deleteBookmark(bookId)
            }
        })
        publicBooksList.adapter = publicBookmarksAdapter
        localBooksList.adapter = localBooksAdapter



        localBooksHeader.setOnClickListener{
            clicked[0] = clicked[0].not()
            animateCard(requireContext(), clicked[0], localBooksCard)
            if (localBooksList.visibility == View.VISIBLE) {
                localBooksList.visibility = View.GONE
            } else {
                localBooksList.visibility = View.VISIBLE
            }
        }

        publicBooksHeader.setOnClickListener {
            clicked[1] = clicked[1].not()
            animateCard(requireContext(), clicked[1], publicBooksCard)
            if (publicBooksList.visibility == View.VISIBLE) {
                publicBooksList.visibility = View.GONE
            } else {
                publicBooksList.visibility = View.VISIBLE
            }
        }


        viewModel.updateLocal.observe(viewLifecycleOwner) { update ->
            if(update){
                Log.d("LibraryFragment", "Получены книги: ${update.toString()}")
                viewModel.getLocalBooks()
                viewModel.local.value?.let { localBooksAdapter.updateBooks(it) }
                viewModel.resetLocal()
            }
        }

        // Наблюдаем за изменениями в списке закладок и обновляем адаптер
        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
            publicBookmarksAdapter.updateBooks(bookmarks)
        }

        viewModel.local.observe(viewLifecycleOwner) { books ->
            Log.d("LibraryFragment", "Получены книги: ${books.size}")
            localBooksAdapter.updateBooks(books)
        }


        return view
    }


    private fun animateCard(context: Context, boolean: Boolean,cardView: CardView){
        animateImageButtons(cardView, false)
        if(boolean){
            cardView.setCardBackgroundColor(Color.parseColor("#756EF3"))
            cardView.cardElevation = 10f
        }
        else{
            cardView.setCardBackgroundColor(Color.TRANSPARENT)
            cardView.cardElevation = 4f
        }
    }

    private fun copyFileFromUri(context: Context, uri: Uri, destinationFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream?.copyTo(outputStream)  // Копируем данные из входного потока в выходной поток
                }
            }
            true  // Если нет исключений, возвращаем true
        } catch (e: IOException) {
            e.printStackTrace()
            false  // Если возникла ошибка, возвращаем false
        }
    }


    fun showLocalBookAddingDialog(fragment: Fragment, dbHelper: DatabaseHelper, filePicker: FilePicker) {
        val dialog = Dialog(fragment.requireContext())
        dialog.setContentView(R.layout.modal_local_book_adding)

        val bookTitle = dialog.findViewById<EditText>(R.id.bookTitle)
        val attachFileButton = dialog.findViewById<ImageButton>(R.id.attachFileButton)
        val saveButton = dialog.findViewById<Button>(R.id.publish)
        val fileName = dialog.findViewById<TextView>(R.id.fileName)

        filePicker.setTextView(fileName)

        attachFileButton.setOnClickListener {
            filePicker.openFilePicker()
        }

        saveButton.setOnClickListener {
            val title = bookTitle.text.toString()
            val selectedUri = filePicker.getSelectedUri()

            if (selectedUri != null && title.isNotEmpty()) {
                // Путь к файлу, куда будет сохраняться копия
                val destinationFile = File(fragment.requireContext().getExternalFilesDir(null), "копия_${System.currentTimeMillis()}.pdf")

                if (copyFileFromUri(fragment.requireContext(), selectedUri, destinationFile)) {
                    viewModel.dbAddNewBook(title, destinationFile.absolutePath) // Сохранение полной ссылки на новый файл
                    dialog.dismiss()
                } else {
                    Toast.makeText(fragment.requireContext(), "Не удалось скопировать файл", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(fragment.requireContext(), "Прикрепите файл и введите название", Toast.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }


}