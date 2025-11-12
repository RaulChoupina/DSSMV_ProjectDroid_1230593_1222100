package com.example.projdroid.api;

import com.example.projdroid.models.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface LibraryApi {

    // Endpoint para obter a lista de bibliotecas
    @GET("library") // Definido para coincidir com a URL base completa
    Call<List<Library>> getLibraries();

    @GET("library/{id}/book")
    Call<List<LibraryBook>> getBooksByLibraryId(@Path("id") String libraryId);

    @POST("library/{libraryId}/book/{isbn}")
    Call<Void> addBook(@Path("libraryId") String libraryId,
                       @Path("isbn") String isbn,
                       @Body CreateLibraryBookRequest request);

    @GET("book/{isbn}")
    Call<Book> loadBook(@Path("isbn") String isbn, @Query("persist") boolean persist);


    @POST("library")
    Call<Library> addLibrary(@Body Library library);

    // --- CHECKOUT (emprestar) ---
    @POST("library/{libraryId}/book/{bookId}/checkout")
    Call<Library> checkOutBook(
            @Path("libraryId") String libraryId,
            @Path("bookId") String bookId,
            @Query("userId") String userName
    );

    // --- CHECKIN (devolver) ---
    @POST("library/{libraryId}/book/{bookId}/checkin")
    Call<Void> checkinBook(
            @Path("libraryId") String libraryId,
            @Path("bookId") String bookId,
            @Query("userId") String userId
    );

    // --- EXTENDER EMPRÉSTIMO ---
    @POST("checkout/{id}/extend")
    Call<Library> extendCheckout(
            @Path("id") String id
    );



    @PUT("library/{id}")
    Call<Library> updateLibrary(@Path("id") String id, @Body Library library);

    @PUT("library/{libraryId}/book/{isbn}")
    Call<Void> updateBook(
            @Path("libraryId") String libraryId,
            @Path("isbn") String isbn,
            @Body CreateLibraryBookRequest request
    );

    @DELETE("library/{id}")
    Call<Void> removeLibrary(@Path("id") String id);

  ;

    @GET("books/{id}")
    Call<Book> getBookById(@Path("id") String id);

    @GET("books/isbn/{isbn}")
    Call<Book> getBookByIsbn(@Path("isbn") String isbn);

    @GET("search/typeahead")
    Call<List<Book>> typeaheadBooks(@Query("query") String query);

    @GET("book/{isbn}/review/recommended-count")
    Call<RecommendedCountResponse> getRecommendedCount(@Path("isbn") String isbn);


    @GET("user/checked-out")
    Call<List<BookItem>> getCheckedOutBooks(@Query("username") String username);

    @GET("user/checked-out")
    Call<List<LibraryBook>> getBooksByUser(@Query("userId") String username);


    @GET("user/checkout-history")

    Call<List<BookItem>> getCheckoutHistory(@Query("username") String username);

}


