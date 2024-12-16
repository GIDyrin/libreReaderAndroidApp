package ru.dyringleb.librereaderapp.retrofitConf

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/v1/registration/")
    fun register(@Body request: RegistrationRequest): Call<RegistrationResponse>

    @POST("api/v1/auth/token/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("api/v1/user/me/")
    fun loginMe(): Call<UserInfo>

    @POST("api/v1/auth/token/logout")
    fun logout(): Call<Void>

    @GET("api/v1/reviews/user/{user_id}/")
    fun getUserReviews(@Path("user_id") userId: Int): Call<List<Review>>

    @GET("api/v1/reviews/book/{book_id}/")
    fun getBooksReviews(@Path("book_id") bookId: Int): Call<List<BookReview>>

    @GET("api/v1/books/{book_id}/")
    fun getBookInfo(@Path("book_id") bookId: Int): Call<FullBooksInfo>

    @POST("api/v1/reviews/new/")
    fun postReview(@Body request: ReviewPostRequest): Call<Void>


    @Multipart
    @PUT("api/v1/user/update/{pk}/")
    fun updateProfile(
        @Path("pk") userId: Int,
        @Part about_user: MultipartBody.Part?,
        @Part profile_photo: MultipartBody.Part?
    ): Call<Void>

    @GET("api/v1/authors/me/")
    fun getMyAuthorPage(): Call<Author>

    @Multipart
    @POST("api/v1/authors/new/")
    fun regNewAuthor(
        @Part author_name: MultipartBody.Part,
        @Part biography: MultipartBody.Part,
        @Part image: MultipartBody.Part?
    ): Call<Void>


    @Multipart
    @PUT("api/v1/authors/me/")
    fun updateAuthor(
        @Part author_name: MultipartBody.Part?,
        @Part biography: MultipartBody.Part?,
        @Part image: MultipartBody.Part?
    ): Call<Void>

    @GET("api/v1/books/byauthor/{author_id}/")
    fun getBooksByAuthor(
        @Path("author_id") authorId: Int,
        @Query("page") page: Int
    ): Call<BookSearchResponse>


    @DELETE("api/v1/books/me/{bookId}/")
    fun deleteMyBook(
        @Path("bookId") bookId: Int
    ): Call<Void>

    @Multipart
    @POST("api/v1/books/new/")
    fun postNewBook(
        @Part book_title: MultipartBody.Part,
        @Part book_year: MultipartBody.Part,
        @Part description: MultipartBody.Part?,
        @Part genres: MultipartBody.Part,
        @Part book_file: MultipartBody.Part
    ): Call<Void>


    @DELETE("api/v1/user/delete/")
    fun deleteMe(): Call<Void>

    @GET("api/v1/books/")
    fun getBooksByGenres(
        @Query("genre_ids") genreIds: String,
        @Query("search") search: String,
        @Query("page") page: Int
    ): Call<BookSearchResponse>

    @GET("api/v1/authors/{author_id}/")
    fun getSomeAuthorInfo(
        @Path("author_id") authorId: Int,
    ): Call<Author>


    @GET("api/v1/authors/")
    fun getAuthors(
        @Query("filter") filter: String? = null,
        @Query("page") page: Int = 1,
    ): Call<AuthorResponse>

    @GET("api/v1/rating/{book_id}/")
    fun getBookRating(
        @Path("book_id") bookId: Int
    ): Call<RatingResponse>


    @GET("api/v1/bookmarks/me/")
    fun getMyBookMarks() : Call<List<BookmarkResponse>>


    @POST("api/v1/bookmarks/me/new/")
    fun postNewBookmark(@Body request: NewBookmarkRequest): Call<PostBookmarkResponse>


    @PUT("api/v1/bookmarks/me/{bookmark_id}/")
    fun updateBookmark(
        @Path("bookmark_id") bookmarkId: Int,
        @Body request: UpdateBookmarkRequest): Call<Void>

    @DELETE("api/v1/bookmarks/me/{bookmark_id}/")
    fun deleteBookmark(
        @Path("bookmark_id") bookmarkId: Int
    ): Call<Void>


    @GET("api/v1/bookmarks/{book_id}/")
    fun getBookmarkByBookId(
        @Path("book_id") bookId: Int
    ): Call<BookmarkResponse>


    @GET("api/v1/books/download/{book_id}/")
    fun getBookFile(
        @Path("book_id") bookId: Int
    ): Call<ResponseBody>


    @GET("api/v1/bookmarks/me/{pk}/")
    fun getBookmark(
        @Path("pk") bookmarkId: Int
    ): Call<OnlyBookmarkInfo>
}