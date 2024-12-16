package ru.dyringleb.librereaderapp.retrofitConf

import android.os.Parcel
import android.os.Parcelable
import okhttp3.MultipartBody


data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val auth_token: String
)

data class RegistrationRequest(
    val email: String,
    val username: String,
    val password: String
)

data class RegistrationResponse(
    val userId: Int,
    val username: String,
    val email: String
)

data class UpdateUserRequest(
    val username: String?,
    val about_user: String?,
    val photo: MultipartBody.Part? // Для изображения
)

data class RatingResponse(
    val reviews_count: Int,
    val avg_rate: Float
)

data class UserInfo(
    val id: Int,
    val username: String,
    val about_user: String,
    val date_joined: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(username)
        parcel.writeString(about_user)
        parcel.writeString(date_joined)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserInfo> {
        override fun createFromParcel(parcel: Parcel): UserInfo {
            return UserInfo(parcel)
        }

        override fun newArray(size: Int): Array<UserInfo?> {
            return arrayOfNulls(size)
        }
    }
}



data class Author(
    val author_id: Int,
    val author_name: String,
    val biography: String,
    val image_path: String,
    val user: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(author_id)
        parcel.writeString(author_name)
        parcel.writeString(biography)
        parcel.writeString(image_path)
        parcel.writeInt(user)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Author> {
        override fun createFromParcel(parcel: Parcel): Author {
            return Author(parcel)
        }

        override fun newArray(size: Int): Array<Author?> {
            return arrayOfNulls(size)
        }
    }
}

data class Book(
    val book_id: Int,
    val book_title: String,
    val book_year: Int,
    val description: String,
    val author: Author,
    val book_path: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readParcelable(Author::class.java.classLoader) ?: Author(0, "", "", "", 0),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(book_id)
        parcel.writeString(book_title)
        parcel.writeInt(book_year)
        parcel.writeString(description)
        parcel.writeParcelable(author, flags)
        parcel.writeString(book_path)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Book> {
        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }
}

data class Review(
    val review_id: Int,
    val user: Int,
    val book: Book,
    val review_text: String,
    val review_rate: Int,
    val review_date: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(Book::class.java.classLoader) ?: Book(
            0,
            "",
            0,
            "",
            Author(0, "", "", "", 0),
            ""
        ),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(review_id)
        parcel.writeInt(user)
        parcel.writeParcelable(book, flags)
        parcel.writeString(review_text)
        parcel.writeInt(review_rate)
        parcel.writeString(review_date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Review> {
        override fun createFromParcel(parcel: Parcel): Review {
            return Review(parcel)
        }

        override fun newArray(size: Int): Array<Review?> {
            return arrayOfNulls(size)
        }
    }
}

data class BookReview(
    val review_id: Int,
    val user: UserInfo,
    val review_text: String,
    val review_rate: Int,
    val review_date: String
)

data class Genre(
    val genre_name: String
)

data class FullBooksInfo(
    val book_id: Int,
    val book_title: String,
    val book_year: Int,
    val description: String,
    val author: Author,
    val book_path: String,
    val genres: List<Genre>
)


data class ReviewPostRequest(
    val book: Int,
    val review_text: String,
    val review_rate: Int
)


data class BooksResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<BookByAuthor>
)

data class BookByAuthor(
    val book_id: Int,
    val book_title: String,
    val book_year: Int,
    val description: String?,
    val book_path: String,
    val author: Author
)

data class BookSearchResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<FullBooksInfo>
)

data class AuthorResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Author>
)

data class BookmarkResponse(
    val bookmark_id: Int,
    val page_number: Int,
    val user: Int,
    val book: BookByAuthor
)

data class NewBookmarkRequest(
    val book: Int,
    val page_number: Int
)

data class UpdateBookmarkRequest(
    val page_number: Int
)

data class PostBookmarkResponse(
    val bookmark_id: Int,
    val page_number: Int,
    val book: Int
)


data class OnlyBookmarkInfo(
    val bookmark_id: Int,
    val page_number: Int,
    val book: Int,
    val user: Int
)