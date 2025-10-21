package com.example.projdroid.api;

import com.example.projdroid.models.Book;
import com.example.projdroid.models.CheckedOutBook;
import com.example.projdroid.models.Library;
import com.example.projdroid.models.LibraryBook;
import com.example.projdroid.models.Review;
import com.example.projdroid.models.CreateReviewRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LibraryApi {

    // ---------- Libraries ----------
    @GET("v1/library")
    Call<List<Library>> getLibraries();

    @GET("v1/library/{libraryId}/book")
    Call<List<LibraryBook>> getBooks(@Path("libraryId") String libraryId);

    // ---------- Books ----------
    @GET("v1/book/{isbn}")
    Call<Book> getBookByIsbn(@Path("isbn") String isbn,
                             @Query("persist") boolean persist);

    // ---------- Search ----------
    // Algumas instalações usam ?query=, outras ?q=. Mantemos as duas.
    @GET("v1/search")
    Call<List<Book>> searchBooksQuery(@Query("query") String query,
                                      @Query("page") Integer page);

    @GET("v1/search/typeahead")
    Call<List<String>> typeahead(@Query("query") String query);

    // ---------- Reviews ----------
    @GET("v1/book/{isbn}/review")
    Call<List<Review>> getReviews(@Path("isbn") String isbn,
                                  @Query("limit") Integer limit);

    @POST("v1/book/{isbn}/review")
    Call<Review> createReview(@Path("isbn") String isbn,
                              @Body CreateReviewRequest body,
                              @Query("userId") String userId);

    // ---------- User loans ----------
    @GET("v1/user/checked-out")
    Call<List<CheckedOutBook>> getCheckedOut(@Query("userId") String userId);

    @GET("v1/user/checkout-history")
    Call<List<CheckedOutBook>> getCheckoutHistory(@Query("userId") String userId);

    // ---------- Checkout / Checkin (para usar depois) ----------
    @POST("v1/library/{libraryId}/book/{bookId}/checkout")
    Call<Void> checkout(@Path("libraryId") String libraryId,
                        @Path("bookId") String bookId,
                        @Query("userId") String userId);

    @POST("v1/library/{libraryId}/book/{bookId}/checkin")
    Call<Void> checkin(@Path("libraryId") String libraryId,
                       @Path("bookId") String bookId,
                       @Query("userId") String userId);
}
