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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

// Importa o novo ficheiro que criámos
import com.example.projdroid.api.ApiConstants;


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

        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        int imgW = (int) (64 * getResources().getDisplayMetrics().density);
        int imgH = (int) (96 * getResources().getDisplayMetrics().density);

        for (LibraryBook lb : books) {
            Book b = lb.getBook();

            // ---- Card/linha horizontal
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundResource(R.drawable.library_item_background);
            row.setPadding(pad, pad, pad, pad);

            // margem inferior entre cards
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowLp.setMargins(0, 0, 0, pad);
            container.addView(row, rowLp);

            // ---- Cover
            ImageView cover = new ImageView(this);
            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(imgW, imgH);
            imgLp.setMargins(0, 0, pad, 0);
            cover.setLayoutParams(imgLp);
            cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            row.addView(cover);

            // ***** ALTERAÇÃO AQUI *****
            // Chamada correta ao método fetchBookCover
            fetchBookCover(lb.getIsbn(), b, cover, lb.getStock());

            // ---- Texto
            TextView tv = new TextView(this);
            tv.setTextSize(16);
            tv.setTextColor(getResources().getColor(android.R.color.black));

            // Usei o teu getAuthorDisplay() do Book.java para ser mais robusto
            tv.setText(
                    "Title: " + (b.getTitle() != null ? b.getTitle() : "N/A") + "\n" +
                            "Author: " + (b.getAuthorDisplay()) + "\n" +
                            "Stock: " + lb.getStock()
            );
            row.addView(tv);

            // Long click = editar
            row.setOnLongClickListener(v -> { showEditBookDialog(lb); return true; });
        }
    }
    private void fetchBookCover(String isbn, Book book, ImageView target, int stock) {
        // 1) Tenta primeiro o nome vindo no Book (assume que fizeste a alteração no Book.java)
        String imageName = null;
        if (book != null && book.getCover() != null) {
            imageName = book.getImage();
        }
        // 2) Fallback: tenta por ISBN caso o backend use nomes com ISBN
        if ((imageName == null || imageName.isEmpty()) && isbn != null && !isbn.isEmpty()) {
            imageName = isbn + ".jpg"; // ou .png, dependendo do teu servidor
        }

        // 3) Constroi o URL usando o novo ApiConstants
        String url = ApiConstants.coverUrl(imageName);

        Log.d("LibraryDetailActivity", "Loading cover from URL: " + url);

        Glide.with(this)
                .load(url)
                // Certifica-te que tens estes ficheiros em res/drawable
                .placeholder(R.drawable.cover_placeholder)
                .error(R.drawable.cover_error)
                .transform(new CenterCrop(), new RoundedCorners(12))
                .into(target);
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