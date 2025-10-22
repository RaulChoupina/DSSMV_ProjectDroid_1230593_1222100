package com.example.projdroid.api;

import com.example.projdroid.models.Library;
import com.example.projdroid.models.LibraryBook;

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

    @GET("user/checked-out")
    Call<List<LibraryBook>> getBooksByUser(@Query("userId") String username);

    @POST("library")
    Call<Library> addLibrary(@Body Library library);

    @POST("library/{libraryId}/book/{isbn}/checkin")
    Call<Library> checkInBook(
            @Path("libraryId") String libraryId,
            @Path("isbn") String isbn,
            @Query("userId") String userId
    );

    @POST("library/{libraryId}/book/{isbn}/checkout")
    Call<Library> checkOutBook(
            @Path("libraryId") String libraryId,
            @Path("isbn") String isbn,
            @Query("userId") String userId
    );


    @PUT("library/{id}")
    Call<Library> updateLibrary(@Path("id") String id, @Body Library library);

    @DELETE("library/{id}")
    Call<Void> removeLibrary(@Path("id") String id);
}
