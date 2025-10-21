package com.example.projdroid.repository;

import com.example.projdroid.api.ApiClient;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.models.Book;
import com.example.projdroid.models.CheckedOutBook;
import com.example.projdroid.models.CreateReviewRequest;
import com.example.projdroid.models.Library;
import com.example.projdroid.models.LibraryBook;
import com.example.projdroid.models.Review;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Repositório de acesso à API da Biblioteca.
 * - Pesquisa com fallback (query->q)
 * - smartSearch: se pesquisa vier vazia, mostra livros da 1ª biblioteca
 */
public class LibraryRepository {

    private final LibraryApi api = ApiClient.get().create(LibraryApi.class);

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(Throwable t);
    }

    // ---------- Libraries ----------
    public void getLibraries(Callback<List<Library>> cb) {
        api.getLibraries().enqueue(new retrofit2.Callback<List<Library>>() {
            @Override public void onResponse(Call<List<Library>> c, Response<List<Library>> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<Library>> c, Throwable t) { cb.onError(t); }
        });
    }

    public void getBooks(String libraryId, Callback<List<LibraryBook>> cb) {
        api.getBooks(libraryId).enqueue(new retrofit2.Callback<List<LibraryBook>>() {
            @Override public void onResponse(Call<List<LibraryBook>> c, Response<List<LibraryBook>> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<LibraryBook>> c, Throwable t) { cb.onError(t); }
        });
    }

    // (Opcional) obter livro por ISBN
    public void getBookByIsbn(String isbn, boolean persist, Callback<Book> cb) {
        api.getBookByIsbn(isbn, persist).enqueue(new retrofit2.Callback<Book>() {
            @Override public void onResponse(Call<Book> c, Response<Book> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<Book> c, Throwable t) { cb.onError(t); }
        });
    }

    // ---------- Search ----------
    /**
     * Pesquisa com fallback:
     * 1) tenta /v1/search?query=...
     * 2) se vazio, tenta /v1/search?q=...
     */
    public void searchBooks(String query, Integer page, Callback<List<Book>> cb) {
        if (page == null) page = 0;
        api.searchBooksQuery(query, page).enqueue(new retrofit2.Callback<List<Book>>() {
            @Override public void onResponse(Call<List<Book>> c, Response<List<Book>> r) {
                if (r.isSuccessful() && r.body() != null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<Book>> c, Throwable t) { cb.onError(t); }
        });
    }



    /**
     * smartSearch:
     * - tenta pesquisa (query->q)
     * - se vier vazia, carrega livros da 1ª biblioteca
     */
    public void smartSearch(String query, Integer page, Callback<List<Book>> cb) {
        searchBooks(query, page, new Callback<List<Book>>() {
            @Override public void onSuccess(List<Book> data) {
                if (data != null && !data.isEmpty()) { cb.onSuccess(data); return; }

                // Fallback: livros da 1ª biblioteca
                getLibraries(new Callback<List<Library>>() {
                    @Override public void onSuccess(List<Library> libs) {
                        if (libs == null || libs.isEmpty()) { cb.onError(new Exception("Sem bibliotecas")); return; }

                        Library first = libs.get(0);
                        if (first == null || first.id == null || first.id.isEmpty()) {
                            cb.onError(new Exception("Library.id inválido")); return;
                        }

                        getBooks(first.id, new Callback<List<LibraryBook>>() {
                            @Override public void onSuccess(List<LibraryBook> lbs) {
                                ArrayList<Book> out = new ArrayList<>();
                                if (lbs != null) for (LibraryBook lb : lbs) if (lb != null && lb.book != null) out.add(lb.book);
                                cb.onSuccess(out);
                            }
                            @Override public void onError(Throwable t) { cb.onError(t); }
                        });
                    }
                    @Override public void onError(Throwable t) { cb.onError(t); }
                });
            }
            @Override public void onError(Throwable t) { cb.onError(t); }
        });
    }


    // ---------- Reviews ----------
    public void getReviews(String isbn, Integer limit, Callback<List<Review>> cb) {
        api.getReviews(isbn, limit).enqueue(new retrofit2.Callback<List<Review>>() {
            @Override public void onResponse(Call<List<Review>> c, Response<List<Review>> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<Review>> c, Throwable t) { cb.onError(t); }
        });
    }

    public void createReview(String isbn, CreateReviewRequest req, String userId, Callback<Review> cb) {
        api.createReview(isbn, req, userId).enqueue(new retrofit2.Callback<Review>() {
            @Override public void onResponse(Call<Review> c, Response<Review> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<Review> c, Throwable t) { cb.onError(t); }
        });
    }

    // ---------- User loans ----------
    public void getCheckedOut(String userId, Callback<List<CheckedOutBook>> cb) {
        api.getCheckedOut(userId).enqueue(new retrofit2.Callback<List<CheckedOutBook>>() {
            @Override public void onResponse(Call<List<CheckedOutBook>> c, Response<List<CheckedOutBook>> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<CheckedOutBook>> c, Throwable t) { cb.onError(t); }
        });
    }

    public void getCheckoutHistory(String userId, Callback<List<CheckedOutBook>> cb) {
        api.getCheckoutHistory(userId).enqueue(new retrofit2.Callback<List<CheckedOutBook>>() {
            @Override public void onResponse(Call<List<CheckedOutBook>> c, Response<List<CheckedOutBook>> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<CheckedOutBook>> c, Throwable t) { cb.onError(t); }
        });
    }

    // ---------- Checkout / Checkin (já prontos a usar) ----------
    public void checkout(String libraryId, String bookId, String userId, Callback<Void> cb) {
        api.checkout(libraryId, bookId, userId).enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) cb.onSuccess(null);
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { cb.onError(t); }
        });
    }

    public void checkin(String libraryId, String bookId, String userId, Callback<Void> cb) {
        api.checkin(libraryId, bookId, userId).enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) cb.onSuccess(null);
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { cb.onError(t); }
        });
    }
}
