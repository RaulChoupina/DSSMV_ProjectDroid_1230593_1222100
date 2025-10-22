package com.example.projdroid.repository;

import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.models.Book;
import com.example.projdroid.models.Library;
import com.example.projdroid.models.LibraryBook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Response;

public class LibraryRepository {

    private final LibraryApi api = ApiClient.get().create(LibraryApi.class);

    public interface Callback<T> { void onSuccess(T data); void onError(Throwable t); }

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

    // (opcional) obter livro por ISBN
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
     * Pesquisa para a UI:
     * 1) Usa /v1/search/typeahead (devolve strings — tipicamente ISBNs ou termos).
     * 2) Para cada item, tenta obter o Book completo via /v1/book/{isbn}.
     * 3) Devolve List<Book> à UI (evita o erro "Expected BEGIN_OBJECT but was STRING").
     */
    public void search(String query, Callback<List<Book>> cb) {
        final String q = (query == null) ? "" : query.trim();
        api.typeahead(q).enqueue(new retrofit2.Callback<List<String>>() {
            @Override public void onResponse(Call<List<String>> c, Response<List<String>> r) {
                if (!r.isSuccessful() || r.body() == null) {
                    cb.onError(new Exception("HTTP " + r.code()));
                    return;
                }

                List<String> hits = r.body();
                if (hits.isEmpty()) { cb.onSuccess(Collections.emptyList()); return; }

                // Limita para não saturar a UI/servidor (ajusta se quiseres)
                int max = Math.min(10, hits.size());
                List<Book> results = Collections.synchronizedList(new ArrayList<>());
                AtomicInteger remaining = new AtomicInteger(max);

                for (int i = 0; i < max; i++) {
                    String token = hits.get(i);
                    if (token == null || token.trim().isEmpty()) {
                        if (remaining.decrementAndGet() == 0) cb.onSuccess(results);
                        continue;
                    }

                    // Tentamos tratar "token" como ISBN diretamente:
                    getBookByIsbn(token.trim(), false, new Callback<Book>() {
                        @Override public void onSuccess(Book book) {
                            if (book != null) results.add(book);
                            if (remaining.decrementAndGet() == 0) cb.onSuccess(results);
                        }
                        @Override public void onError(Throwable t) {
                            // Se falhar por o token não ser um ISBN válido, simplesmente ignoramos esse item
                            if (remaining.decrementAndGet() == 0) cb.onSuccess(results);
                        }
                    });
                }
            }

            @Override public void onFailure(Call<List<String>> c, Throwable t) { cb.onError(t); }
        });
    }

    /** Se quiseres pesquisar só dentro de uma biblioteca (sem usar /v1/search): */
    public void searchBooksInLibrary(String libraryId, String query, Callback<List<Book>> cb) {
        final String q = query == null ? "" : query.trim().toLowerCase();

        getBooks(libraryId, new Callback<List<LibraryBook>>() {
            @Override public void onSuccess(List<LibraryBook> lbs) {
                List<Book> result = new ArrayList<>();
                if (lbs != null) {
                    for (LibraryBook lb : lbs) {
                        if (lb == null || lb.book == null) continue;
                        Book b = lb.book;

                        String title  = b.title == null ? "" : b.title.toLowerCase();
                        String isbn   = b.isbn  == null ? "" : b.isbn.toLowerCase();

                        String author = "";
                        if (b.authors != null && !b.authors.isEmpty() && b.authors.get(0) != null) {
                            // ajusta o campo conforme o teu modelo Author
                            author = b.authors.get(0).name == null ? "" : b.authors.get(0).name.toLowerCase();
                        }

                        boolean matches = q.isEmpty() || title.contains(q) || isbn.contains(q) || author.contains(q);
                        if (matches) result.add(b);
                    }
                }
                cb.onSuccess(result);
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
}
