package com.example.projdroid.api;

import com.example.projdroid.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface LibraryApi {
    // Libraries
    @GET("v1/library") Call<List<Library>> getLibraries();

    // Books in a library
    @GET("v1/library/{libraryId}/book")
    Call<List<LibraryBook>> getBooks(@Path("libraryId") String libraryId);

    // Single Book by ISBN
    @GET("v1/book/{isbn}")
    Call<Book> getBookByIsbn(@Path("isbn") String isbn, @Query("persist") boolean persist);

    // Reviews
    @GET("v1/book/{isbn}/review")
    Call<List<Review>> getReviews(@Path("isbn") String isbn, @Query("limit") Integer limit);
}
