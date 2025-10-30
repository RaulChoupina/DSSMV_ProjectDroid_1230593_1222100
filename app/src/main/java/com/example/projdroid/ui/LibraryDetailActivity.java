package com.example.projdroid.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projdroid.R;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.api.RetrofitClient;
import com.example.projdroid.models.Book;
import com.example.projdroid.models.LibraryBook;
import com.example.projdroid.models.CreateLibraryBookRequest;
import java.util.List;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryDetailActivity extends AppCompatActivity {

    private String libraryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_detail);

        libraryId = getIntent().getStringExtra("library_id");
        if (libraryId == null) {
            showError("Library ID not found");
            return;
        }

        fetchBooks(libraryId);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavBooks);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_add) {
                // Este é o botão "+" no bottomNav
                showAddBookDialog();
                bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                return false;
            } else if (id == R.id.nav_home) {
                return true;
            }

            return false;
        });

    }

    /** ===================== 1. LISTAR LIVROS ===================== **/
    private void fetchBooks(String libraryId) {
        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/")
                .create(LibraryApi.class);

        api.getBooksByLibraryId(libraryId).enqueue(new Callback<List<LibraryBook>>() {
            @Override
            public void onResponse(Call<List<LibraryBook>> call, Response<List<LibraryBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayBooks(response.body());
                } else {
                    showError("Failed to load books (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<LibraryBook>> call, Throwable t) {
                showError("Error: " + t.getMessage());
            }
        });
    }

    private void displayBooks(List<LibraryBook> books) {
            LinearLayout container = findViewById(R.id.containerBooksData);
        container.removeAllViews();

        for (LibraryBook lb : books) {
            Book b = lb.getBook();

            TextView tv = new TextView(this);
            tv.setTextSize(16);
            tv.setPadding(8, 8, 8, 8);
            tv.setBackgroundResource(R.drawable.library_item_background);
            tv.setText(
                    "Title: " + (b.getTitle() != null ? b.getTitle() : "N/A") + "\n" +
                            "Author: " + ((b.getAuthors() != null && !b.getAuthors().isEmpty()) ?
                            b.getAuthors().get(0).getName() : "Unknown") + "\n" +
                            "Stock: " + lb.getStock()
            );

            // Long click = editar
            tv.setOnLongClickListener(v -> {
                showEditBookDialog(lb);
                return true;
            });

            container.addView(tv);
        }
    }

    /** ===================== 2. ADICIONAR LIVRO ===================== **/
    private void showAddBookDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText isbnInput = new EditText(this);
        isbnInput.setHint("Enter ISBN");
        layout.addView(isbnInput);

        final EditText stockInput = new EditText(this);
        stockInput.setHint("Enter Stock");
        stockInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(stockInput);

        new AlertDialog.Builder(this)
                .setTitle("Add New Book")
                .setView(layout)
                .setPositiveButton("Add", (d, w) -> {
                    String isbn = isbnInput.getText().toString().trim();
                    int stock = stockInput.getText().toString().isEmpty() ? 0 :
                            Integer.parseInt(stockInput.getText().toString().trim());
                    addBookToLibrary(isbn, stock);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addBookToLibrary(String isbn, int stock) {
        if (libraryId == null) { showError("Library ID missing"); return; }

        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/")
                .create(LibraryApi.class);

        CreateLibraryBookRequest req = new CreateLibraryBookRequest(stock);

        api.addBook(libraryId, isbn, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LibraryDetailActivity.this, "Book added!", Toast.LENGTH_SHORT).show();
                    fetchBooks(libraryId);
                } else {
                    showError("Failed to add book (HTTP " + response.code() + ")");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showError("Error: " + t.getMessage());
            }
        });
    }

    /** ===================== 3. ATUALIZAR LIVRO ===================== **/
    private void showEditBookDialog(LibraryBook lb) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText stockInput = new EditText(this);
        stockInput.setHint("New Stock");
        stockInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        stockInput.setText(String.valueOf(lb.getStock()));
        layout.addView(stockInput);

        new AlertDialog.Builder(this)
                .setTitle("Update Book Stock")
                .setView(layout)
                .setPositiveButton("Update", (d, w) -> {
                    int newStock = Integer.parseInt(stockInput.getText().toString().trim());
                    updateBookInLibrary(lb.getIsbn(), newStock);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBookInLibrary(String isbn, int newStock) {
        if (libraryId == null) { showError("Library ID missing"); return; }

        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/")
                .create(LibraryApi.class);

        CreateLibraryBookRequest req = new CreateLibraryBookRequest(newStock);

        api.updateBook(libraryId, isbn, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LibraryDetailActivity.this, "Book updated!", Toast.LENGTH_SHORT).show();
                    fetchBooks(libraryId);
                } else {
                    showError("Failed to update (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showError("Error: " + t.getMessage());
            }
        });
    }

    /** ===================== AUX ===================== **/
    private void showError(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
        Log.e("LibraryDetailActivity", msg);
    }
}
