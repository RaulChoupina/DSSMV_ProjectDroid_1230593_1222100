package com.example.projdroid.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projdroid.R;
import com.example.projdroid.api.ApiConstants;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.api.RetrofitClient;
import com.example.projdroid.models.Book;
import com.example.projdroid.models.Library;
import com.example.projdroid.models.LibraryBook;
import com.example.projdroid.models.CreateLibraryBookRequest;
import com.example.projdroid.models.LibraryBook;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryDetailActivity extends AppCompatActivity {

    private String libraryId;
    // Lista para guardar os livros carregados, para o menu "Editar"
    private List<LibraryBook> currentBooksList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_detail);

        libraryId = getIntent().getStringExtra("library_id");
        if (libraryId == null) {
            showError("Library ID not found");
            return;
        }

        // Vai buscar os livros
        fetchBooks(libraryId);

        // Configura a barra de navegação inferior
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavBooks);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_add) {
                // Botão "Adicionar"
                showAddBookDialog();
                bottomNav.getMenu().findItem(R.id.nav_loans).setChecked(false);
                return false;

            } else if (id == R.id.nav_loans) {
                // Botão "Empréstimo"
                showLoanDialog(libraryId);
                bottomNav.getMenu().findItem(R.id.nav_loans).setChecked(false);
                return false;

            } else if (id == R.id.action_edit) {
                Toast.makeText(this, "Função Editar ainda não implementada", Toast.LENGTH_SHORT).show();
                return false;
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
                    // ATUALIZADO: Guarda a lista de livros
                    currentBooksList.clear();
                    currentBooksList.addAll(response.body());

                    // Mostra os livros (agora usa a lista guardada)
                    displayBooks(currentBooksList);
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

        for (LibraryBook libraryBook : books) {
            Book b = libraryBook.getBook();

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundResource(R.drawable.library_item_background);
            row.setPadding(pad, pad, pad, pad);

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowLp.setMargins(0, 0, 0, pad);
            container.addView(row, rowLp);

            ImageView cover = new ImageView(this);
            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(imgW, imgH);
            imgLp.setMargins(0, 0, pad, 0);
            cover.setLayoutParams(imgLp);
            cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            row.addView(cover);

            fetchBookCover(libraryBook.getIsbn(), b, cover, libraryBook.getStock());

            TextView tv = new TextView(this);
            tv.setTextSize(16);
            tv.setTextColor(getResources().getColor(android.R.color.black));

            String author = (b != null) ? b.getAuthorDisplay() : "Unknown";

            tv.setText(
                    "Title: " + (b != null && b.getTitle() != null ? b.getTitle() : "N/A") + "\n" +
                            "Author: " + author + "\n" +
                            "Stock: " + libraryBook.getStock()
            );
            row.addView(tv);

            // ----- LISTENERS -----
            // clique curto = ver descrição
            row.setOnClickListener(v -> showBookDescription(libraryBook));

            // (O clique longo foi removido, pois o botão "Editar" faz agora essa função)
        }
    }

    private void fetchBookCover(String isbn, Book book, ImageView target, int stock) {
        String imageName = null;

        // CORREÇÃO (de conversas anteriores): Usa o objeto Cover
        if (book != null && book.getCover() != null) {
            imageName = book.getCover().getImageName();
        }

        // Fallback: tenta por ISBN
        if ((imageName == null || imageName.isEmpty()) && isbn != null && !isbn.isEmpty()) {
            imageName = isbn + ".jpg"; // ou .png
        }

        String url = ApiConstants.coverUrl(imageName);
        Log.d("LibraryDetailActivity", "Loading cover from URL: " + url);

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.cover_placeholder)
                .error(R.drawable.cover_error)
                .transform(new CenterCrop(), new RoundedCorners(12))
                .into(target);
    }

    /** ===================== 2. VER DESCRIÇÃO ===================== **/

    private void showBookDescription(LibraryBook lb) {
        Book b = (lb != null) ? lb.getBook() : null;
        String title = (b != null && b.getTitle() != null) ? b.getTitle() : "Book";

        if (b != null && b.getDescription() != null && !b.getDescription().trim().isEmpty()) {
            showDescDialog(title, b.getDescription());
            return;
        }

        LibraryApi api = RetrofitClient
                .getClient("http://193.136.62.24/v1/")
                .create(LibraryApi.class);

        Call<Book> call = null;
        if (b != null && b.getIsbn() != null && !b.getIsbn().trim().isEmpty()) {
            call = api.getBookByIsbn(b.getIsbn().trim());
        } else if (lb != null && lb.getBookId() != null && !lb.getBookId().trim().isEmpty()) {
            call = api.getBookById(lb.getBookId().trim());
        }

        if (call == null) {
            showDescDialog(title, "No description available.");
            return;
        }

        AlertDialog loading = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Loading description…")
                .setCancelable(false)
                .create();
        loading.show();

        call.enqueue(new Callback<Book>() {
            @Override public void onResponse(Call<Book> c, Response<Book> r) {
                loading.dismiss();
                if (r.isSuccessful() && r.body() != null) {
                    String desc = r.body().getDescription();
                    if (desc != null && !desc.trim().isEmpty()) {
                        showDescDialog(title, desc);
                    } else {
                        showDescDialog(title, "No description available.");
                    }
                } else {
                    showDescDialog(title, "Failed to load description (HTTP " + r.code() + ").");
                }
            }
            @Override public void onFailure(Call<Book> c, Throwable t) {
                loading.dismiss();
                showDescDialog(title, "Network error: " + t.getMessage());
            }
        });
    }

    private void showDescDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


    /** ===================== 3. ADICIONAR LIVRO ===================== **/

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


    /** =====================================================================
     * FLUXO DE EDIÇÃO/ELIMINAÇÃO (Iniciado pelo botão "Editar" da barra)
     * ===================================================================== **/

    /**
     * PASSO 1 (NOVO): Mostra um pop-up com todos os livros da biblioteca.
     */
    private void openBookEditFlow() {
        if (currentBooksList == null || currentBooksList.isEmpty()) {
            showError("Não há livros para editar.");
            return;
        }

        // Cria um array de strings com os títulos dos livros
        String[] bookTitles = new String[currentBooksList.size()];
        for (int i = 0; i < currentBooksList.size(); i++) {
            bookTitles[i] = getSafeBookTitle(currentBooksList.get(i));
        }

        // Mostra o pop-up
        new AlertDialog.Builder(this)
                .setTitle("Escolher livro para gerir")
                .setItems(bookTitles, (dialog, which) -> {
                    // "which" é a posição do livro em que o utilizador clicou
                    LibraryBook selectedBook = currentBooksList.get(which);

                    // Chama o menu "hub" (Passo 2) para o livro escolhido
                    showEditDeleteBookDialog(selectedBook);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     *
     * Mostra as opções "Editar"  um livro.
     */
    private void showEditDeleteBookDialog(LibraryBook libraryBook) {
        String title = getSafeBookTitle(libraryBook);
        String[] actions = {"Editar Stock"};

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        // "Editar Stock"
                        showEditStockDialog(libraryBook); // (Renomeei o teu método)

                    }
                })
                .setNegativeButton("Fechar", null)
                .show();
    }

    /**
     * PASSO 3a: Editar Stock
     */
    private void showEditStockDialog(LibraryBook lb) {
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
                    fetchBooks(libraryId); // Atualiza a lista
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
