import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "library.db"
        private const val DATABASE_VERSION = 2

        // Таблица books
        const val TABLE_BOOKS = "books"
        const val COLUMN_BOOK_ID = "book_id"
        const val COLUMN_BOOK_TITLE = "book_title" // Новое поле
        const val COLUMN_BOOK_PATH = "book_path"

        // Таблица bookmarks
        const val TABLE_BOOKMARKS = "bookmarks"
        const val COLUMN_BOOKMARK_ID = "bookmark_id"
        const val COLUMN_BOOKMARK_BOOK_ID = "book_id"
        const val COLUMN_BOOKMARK_PAGE_NUMBER = "page_number"

        data class Bookmark(val bookmarkId: Int, val bookId: Int, val pageNumber: Int)

        data class BookWithBookmarks(
            val bookId: Int,
            val bookPath: String,
            val bookTitle: String, // Обновлено
            val bookmarkId: Int,
            val pageNumber: Int
        )
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createBooksTable = ("CREATE TABLE $TABLE_BOOKS (" +
                "$COLUMN_BOOK_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_BOOK_TITLE TEXT NOT NULL, " + // Добавлено новое поле
                "$COLUMN_BOOK_PATH TEXT NOT NULL)")
        db.execSQL(createBooksTable)

        val createBookmarksTable = ("CREATE TABLE $TABLE_BOOKMARKS (" +
                "$COLUMN_BOOKMARK_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_BOOKMARK_BOOK_ID INTEGER NOT NULL UNIQUE, " + // Добавлено UNIQUE для ограничения на одну закладку на книгу
                "$COLUMN_BOOKMARK_PAGE_NUMBER INTEGER NOT NULL," +
                "FOREIGN KEY($COLUMN_BOOKMARK_BOOK_ID) REFERENCES $TABLE_BOOKS($COLUMN_BOOK_ID))")
        db.execSQL(createBookmarksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKMARKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS")

        onCreate(db)
    }

    fun recreateDatabase() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKMARKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS")
        onCreate(db) // Создание новых таблиц
        db.close()
    }

    fun addBook(bookTitle: String, bookPath: String) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_BOOK_TITLE, bookTitle) // Сохраняем заголовок книги
        values.put(COLUMN_BOOK_PATH, bookPath)

        // Вставка новой книги и получение ее ID
        val bookId = db.insert(TABLE_BOOKS, null, values)

        // Если добавление прошло успешно (bookId не равен -1), создаем закладку на первой странице
        if (bookId != -1L) {
            addBookmark(bookId.toInt(), 1) // Добавляем закладку на первую страницу
        }

        db.close()
    }

    fun addBookmark(bookId: Int, pageNumber: Int) {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_BOOKMARK_BOOK_ID, bookId)
        values.put(COLUMN_BOOKMARK_PAGE_NUMBER, pageNumber)

        // Попробуем обновить закладку, если она существует, или вставить новую, если нет
        val updatedRows = db.update(TABLE_BOOKMARKS, values, "$COLUMN_BOOKMARK_BOOK_ID = ?", arrayOf(bookId.toString()))
        if (updatedRows == 0) {
            db.insert(TABLE_BOOKMARKS, null, values)
        }

        db.close()
    }

    fun getAllBooksWithBookmarks(): List<BookWithBookmarks> {
        val bookWithBookmarksList = mutableListOf<BookWithBookmarks>()
        val db = this.readableDatabase

        val cursorBooks = db.rawQuery("SELECT * FROM $TABLE_BOOKS", null)

        // Получаем индексы колонок
        val bookIdIndex = cursorBooks.getColumnIndex(COLUMN_BOOK_ID)
        val bookTitleIndex = cursorBooks.getColumnIndex(COLUMN_BOOK_TITLE)
        val bookPathIndex = cursorBooks.getColumnIndex(COLUMN_BOOK_PATH)

        // Проверка наличия колонок
        if (bookIdIndex == -1 || bookTitleIndex == -1 || bookPathIndex == -1) {
            Log.e("DatabaseError", "One or more columns not found in the books table. " +
                    "bookIdIndex: $bookIdIndex, bookTitleIndex: $bookTitleIndex, bookPathIndex: $bookPathIndex.")
            cursorBooks.close()
            db.close()
            return bookWithBookmarksList
        }

        if (cursorBooks.moveToFirst()) {
            do {
                val bookId = cursorBooks.getInt(bookIdIndex)
                val bookTitle = cursorBooks.getString(bookTitleIndex) // Получение заголовка книги
                val bookPath = cursorBooks.getString(bookPathIndex) // Получение пути книги

                // Получаем единственную закладку для этой книги
                val bookmark = getBookmarkForBook(bookId)

                // Если закладка найдена, добавляем в список
                bookmark?.let {
                    bookWithBookmarksList.add(BookWithBookmarks(
                        bookId,
                        bookPath,
                        bookTitle,
                        it.bookmarkId,
                        it.pageNumber
                    ))
                }
            } while (cursorBooks.moveToNext())
        }

        cursorBooks.close()
        db.close()
        return bookWithBookmarksList
    }


    private fun getBookmarkForBook(bookId: Int): Bookmark? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_BOOKMARKS WHERE $COLUMN_BOOKMARK_BOOK_ID = ?",
            arrayOf(bookId.toString())
        )

        // Проверяем на наличие ошибок в колонках
        val bookmarkIdIndex = cursor.getColumnIndex(COLUMN_BOOKMARK_ID)
        val pageNumberIndex = cursor.getColumnIndex(COLUMN_BOOKMARK_PAGE_NUMBER)

        // Если у нас нет нужных колонок, возвращаем null
        if (bookmarkIdIndex == -1 || pageNumberIndex == -1) {
            Log.e("DatabaseError", "One or more columns not found in the bookmarks table.")
            cursor.close()
            return null
        }

        if (cursor.moveToFirst()) {
            val bookmarkId = cursor.getInt(bookmarkIdIndex)
            val pageNumber = cursor.getInt(pageNumberIndex)
            cursor.close()
            return Bookmark(bookmarkId, bookId, pageNumber)
        }

        cursor.close()
        return null // Если закладка не найдена
    }

    fun deleteBookmark(bookmarkId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_BOOKMARKS, "$COLUMN_BOOKMARK_ID = ?", arrayOf(bookmarkId.toString()))
        db.close()
    }

    fun updateBookmark(bookmarkId: Int, newPageNumber: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_BOOKMARK_PAGE_NUMBER, newPageNumber)
        }
        db.update(TABLE_BOOKMARKS, values, "$COLUMN_BOOKMARK_ID = ?", arrayOf(bookmarkId.toString()))
        db.close()
    }

    fun deleteBook(bookId: Int) {
        val db = this.writableDatabase

        // Получаем путь к файлу перед удалением записи о книге
        val bookPathCursor = db.rawQuery("SELECT $COLUMN_BOOK_PATH FROM $TABLE_BOOKS WHERE $COLUMN_BOOK_ID = ?", arrayOf(bookId.toString()))
        var filePath: String? = null

        val bookPathIndex = bookPathCursor.getColumnIndex(COLUMN_BOOK_PATH)
        // Проверка наличия колонок
        if (bookPathIndex == -1){
            Log.e("DatabaseError", "One or more columns not found in the books table. " +
                    "bookPathIndex: $bookPathIndex.")
            bookPathCursor.close()
            db.close()
            return
        }
        else {
            if (bookPathCursor.moveToFirst()) {
                filePath = bookPathCursor.getString(bookPathIndex)
            }
            bookPathCursor.close()


            // Удаляем закладку, связанную с книгой перед удалением самой книги
            db.delete(TABLE_BOOKMARKS, "$COLUMN_BOOKMARK_BOOK_ID = ?", arrayOf(bookId.toString()))
            db.delete(TABLE_BOOKS, "$COLUMN_BOOK_ID = ?", arrayOf(bookId.toString()))

            db.close()

            // Удаляем файл, если он существует
            filePath?.let {
                val file = File(it)
                if (file.exists()) {
                    if (file.delete()) {
                        Log.d("DatabaseHelper", "Файл успешно удален: ${file.absolutePath}")
                    } else {
                        Log.e("DatabaseHelper", "Не удалось удалить файл: ${file.absolutePath}")
                    }
                }
            }
        }
    }

}
