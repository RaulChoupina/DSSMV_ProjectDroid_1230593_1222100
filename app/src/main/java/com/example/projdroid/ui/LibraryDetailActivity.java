package com.example.projdroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.projdroid.R;
import com.example.projdroid.api.ApiConstants;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.api.RetrofitClient;
import com.example.projdroid.models.Book;
import com.example.projdroid.models.Library;
import com.example.projdroid.models.LibraryBook;
import com.example.projdroid.models.CreateLibraryBookRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryDetailActivity extends AppCompatActivity {

    private static final String TAG = "LibraryDetailActivity";

    private String libraryId;
    private LinearLayout container;

    private final List<LibraryBook> currentBooksList = new ArrayList<>();
    private LibraryApi api; // base v1
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_detail);

        // Toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        container = findViewById(R.id.containerBooksData);

        // API base /v1/
        api = RetrofitClient.getClient("http://193.136.62.24/v1/").create(LibraryApi.class);

        // Intent extras
        Intent i = getIntent();
        libraryId = (i != null) ? i.getStringExtra("library_id") : null;
        String libraryName = (i != null) ? i.getStringExtra("library_name") : "Livros";
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(libraryName);

        if (libraryId == null) {
            showError("Library ID not found");
            return;
        }

        // Carregar livros
        fetchBooks(libraryId);

        // Bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavBooks);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_add) {
                showAddBookDialog();
                bottomNav.getMenu().findItem(R.id.action_add).setChecked(false);
                return false;
            } else if (id == R.id.nav_loans) {
                showLoanActionsDialog(libraryId);
                bottomNav.getMenu().findItem(R.id.nav_loans).setChecked(false);
                return false;
            } else if (id == R.id.action_edit) {
                openBookEditFlow();
                bottomNav.getMenu().findItem(R.id.action_edit).setChecked(false);
                return false;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_libraries, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Pesquisar livro...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
                startTypeahead(query);
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
                pendingSearch = () -> startTypeahead(newText);
                searchHandler.postDelayed(pendingSearch, 300);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override public boolean onMenuItemActionExpand(MenuItem item) { return true; }
            @Override public boolean onMenuItemActionCollapse(MenuItem item) {
                displayBooks(currentBooksList);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    /** ========== 1) LISTAR LIVROS ========== */
    private void fetchBooks(String libraryId) {
        api.getBooksByLibraryId(libraryId).enqueue(new Callback<List<LibraryBook>>() {
            @Override
            public void onResponse(Call<List<LibraryBook>> call, Response<List<LibraryBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentBooksList.clear();
                    currentBooksList.addAll(response.body());
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
        container.removeAllViews();

        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        int imgW = (int) (64 * getResources().getDisplayMetrics().density);
        int imgH = (int) (96 * getResources().getDisplayMetrics().density);

        if (books.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Nenhum livro encontrado.");
            tv.setPadding(pad, pad, pad, pad);
            container.addView(tv);
            return;
        }

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

            fetchBookCover(libraryBook.getIsbn(), b, cover);

            TextView tv = new TextView(this);
            tv.setTextSize(16);
            tv.setTextColor(getResources().getColor(android.R.color.black));

            String author = (b != null) ? b.getAuthorDisplay() : "Unknown";
            String title = (b != null && b.getTitle() != null) ? b.getTitle() : "N/A";

            tv.setText("Title: " + title + "\n" +
                    "Author: " + author + "\n" +
                    "Stock: " + libraryBook.getStock());
            row.addView(tv);

            row.setOnClickListener(v -> showBookDescription(libraryBook));
        }
    }

    private void fetchBookCover(String isbn, Book book, ImageView target) {
        String imageName = null;
        if (book != null && book.getCover() != null) {
            imageName = book.getCover().getImageName();
        }
        if ((imageName == null || imageName.isEmpty()) && isbn != null && !isbn.isEmpty()) {
            imageName = isbn + ".jpg";
        }
        String url = ApiConstants.coverUrl(imageName);
        Log.d(TAG, "Loading cover from URL: " + url);

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.cover_placeholder)
                .error(R.drawable.cover_error)
                .transform(new CenterCrop(), new RoundedCorners(12))
                .into(target);
    }

    /** ========== 2) DESCRIÇÃO ========== */
    private void showBookDescription(LibraryBook lb) {
        Book b = (lb != null) ? lb.getBook() : null;
        String title = (b != null && b.getTitle() != null) ? b.getTitle() : "Book";

        if (b != null && b.getDescription() != null && !b.getDescription().trim().isEmpty()) {
            showDescDialog(title, b.getDescription());
            return;
        }

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
                    showDescDialog(title, (desc != null && !desc.trim().isEmpty())
                            ? desc : "No description available.");
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

    /** ========== 3) ADICIONAR ========== */
    private void showAddBookDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText isbnInput  = new EditText(this);
        final EditText stockInput = new EditText(this);
        isbnInput.setHint("Enter ISBN");
        stockInput.setHint("Enter Stock");
        stockInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(isbnInput);
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

        CreateLibraryBookRequest req = new CreateLibraryBookRequest(stock);
        api.addBook(libraryId, isbn, req).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LibraryDetailActivity.this, "Book added!", Toast.LENGTH_SHORT).show();
                    fetchBooks(libraryId);
                } else {
                    showError("Failed to add book (HTTP " + response.code() + ")");
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                showError("Error: " + t.getMessage());
            }
        });
    }

    /** ========== 4) EDITAR (Stock) ========== */
    private void openBookEditFlow() {
        if (currentBooksList.isEmpty()) {
            showError("Não há livros para editar.");
            return;
        }
        String[] bookTitles = new String[currentBooksList.size()];
        for (int i = 0; i < currentBooksList.size(); i++) {
            bookTitles[i] = getSafeBookTitle(currentBooksList.get(i));
        }
        new AlertDialog.Builder(this)
                .setTitle("Escolher livro para gerir")
                .setItems(bookTitles, (dialog, which) -> showEditDeleteBookDialog(currentBooksList.get(which)))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showEditDeleteBookDialog(LibraryBook libraryBook) {
        String title = getSafeBookTitle(libraryBook);
        String[] actions = {"Editar Stock"};
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) showEditStockDialog(libraryBook);
                })
                .setNegativeButton("Fechar", null)
                .show();
    }

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
        CreateLibraryBookRequest req = new CreateLibraryBookRequest(newStock);
        api.updateBook(libraryId, isbn, req).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LibraryDetailActivity.this, "Book updated!", Toast.LENGTH_SHORT).show();
                    fetchBooks(libraryId);
                } else {
                    showError("Failed to update (HTTP " + response.code() + ")");
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                showError("Error: " + t.getMessage());
            }
        });
    }

    /** ========== 5) CHECKOUT / CHECKIN / EXTENDER ========== */
    private void showLoanDialog(String libraryId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_checkout, null);

        EditText etLibraryId = v.findViewById(R.id.etLibraryId);
        EditText etBookId    = v.findViewById(R.id.etBookId);
        EditText etUserName  = v.findViewById(R.id.etUserName);

        etLibraryId.setText(libraryId);
        etLibraryId.setEnabled(false);

        b.setTitle("Novo Empréstimo");
        b.setView(v);
        b.setPositiveButton("Confirmar", (d, w) -> {
            String bookId   = etBookId.getText().toString().trim();
            String userName = etUserName.getText().toString().trim();

            if (bookId.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "Preenche Book ID e Nome.", Toast.LENGTH_SHORT).show();
                return;
            }

            api.checkOutBook(libraryId, bookId, userName)
                    .enqueue(new Callback<Library>() {
                        @Override public void onResponse(Call<Library> call, Response<Library> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(LibraryDetailActivity.this,
                                        "Empréstimo registado na biblioteca: " + response.body().getName(),
                                        Toast.LENGTH_LONG).show();
                                fetchBooks(libraryId);
                            } else {
                                Toast.makeText(LibraryDetailActivity.this,
                                        "Falha (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override public void onFailure(Call<Library> call, Throwable t) {
                            Toast.makeText(LibraryDetailActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        b.setNegativeButton("Cancelar", null);
        b.show();
    }

    private void showReturnDialog(String libraryId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_checkout, null);

        EditText etLibraryId = v.findViewById(R.id.etLibraryId);
        EditText etBookId    = v.findViewById(R.id.etBookId);
        EditText etUserName  = v.findViewById(R.id.etUserName);

        etLibraryId.setText(libraryId);
        etLibraryId.setEnabled(false);

        b.setTitle("Devolver Livro");
        b.setView(v);
        b.setPositiveButton("Confirmar", (d, w) -> {
            String bookId   = etBookId.getText().toString().trim();
            String userName = etUserName.getText().toString().trim();

            if (bookId.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "Preenche Book ID e Nome.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check-in funciona no host root (sem /v1/)
            LibraryApi apiRoot = RetrofitClient.getClient("http://193.136.62.24/")
                    .create(LibraryApi.class);

            apiRoot.checkinBook(libraryId, bookId, userName)
                    .enqueue(new Callback<Void>() {
                        @Override public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(LibraryDetailActivity.this,
                                        "Livro devolvido com sucesso.", Toast.LENGTH_LONG).show();
                                fetchBooks(libraryId);
                            } else {
                                String msg = "HTTP " + response.code();
                                try {
                                    if (response.errorBody() != null) msg += " - " + response.errorBody().string();
                                } catch (Exception ignored) {}
                                Toast.makeText(LibraryDetailActivity.this, "Falha na devolução: " + msg, Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(LibraryDetailActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        b.setNegativeButton("Cancelar", null);
        b.show();
    }

    private void showExtendDialog(String libraryId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_checkout, null);

        EditText etLibraryId = v.findViewById(R.id.etLibraryId);
        EditText etBookId    = v.findViewById(R.id.etBookId);
        EditText etUserName  = v.findViewById(R.id.etUserName);

        etLibraryId.setText(libraryId);
        etLibraryId.setEnabled(false);
        etBookId.setHint("ID do checkout (UUID)");
        etUserName.setVisibility(View.GONE);

        b.setTitle("Extender Empréstimo");
        b.setView(v);
        b.setPositiveButton("Confirmar", (d, w) -> {
            String checkoutId = etBookId.getText().toString().trim();
            if (checkoutId.isEmpty()) {
                Toast.makeText(this, "Indica o ID do checkout.", Toast.LENGTH_SHORT).show();
                return;
            }

            api.extendCheckout(checkoutId)
                    .enqueue(new Callback<Library>() {
                        @Override public void onResponse(Call<Library> call, Response<Library> response) {
                            if (response.isSuccessful()) {
                                String libName = (response.body()!=null && response.body().getName()!=null)
                                        ? response.body().getName() : "Biblioteca";
                                Toast.makeText(LibraryDetailActivity.this,
                                        "Empréstimo extendido na " + libName + "!", Toast.LENGTH_LONG).show();
                                fetchBooks(libraryId);
                            } else {
                                Toast.makeText(LibraryDetailActivity.this,
                                        "Falha ao extender (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override public void onFailure(Call<Library> call, Throwable t) {
                            Toast.makeText(LibraryDetailActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        b.setNegativeButton("Cancelar", null);
        b.show();
    }

    private void showLoanActionsDialog(String libraryId) {
        CharSequence[] options = {"Empréstimo", "Devolver Empréstimo", "Extender Empréstimo"};
        new AlertDialog.Builder(this)
                .setTitle("Operação de Empréstimos")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showLoanDialog(libraryId);
                    else if (which == 1) showReturnDialog(libraryId);
                    else if (which == 2) showExtendDialog(libraryId);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String getSafeBookTitle(LibraryBook lb) {
        if (lb == null) return "Livro";
        Book b = lb.getBook();
        if (b != null && b.getTitle() != null && !b.getTitle().trim().isEmpty()) return b.getTitle().trim();
        if (lb.getIsbn() != null && !lb.getIsbn().trim().isEmpty()) return "ISBN " + lb.getIsbn().trim();
        if (lb.getBookId() != null && !lb.getBookId().trim().isEmpty()) return "Livro " + lb.getBookId().trim();
        return "Livro";
    }

    /** ========== 6) TYPEAHEAD ========== */
    private void startTypeahead(String raw) {
        String q = raw == null ? "" : raw.trim();
        if (q.isEmpty()) {
            displayBooks(currentBooksList);
            return;
        }

        filterLocally(q);

        api.typeaheadBooks(q).enqueue(new Callback<List<Book>>() {
            @Override public void onResponse(Call<List<Book>> call, Response<List<Book>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    applyTypeaheadBooks(q, resp.body());
                } else {
                    Log.w(TAG, "Typeahead API failed, sticking to local filter.");
                }
            }
            @Override public void onFailure(Call<List<Book>> call, Throwable t) {
                Log.e(TAG, "Typeahead API failure: " + t.getMessage());
            }
        });
    }

    private void applyTypeaheadBooks(String q, List<Book> suggestions) {
        HashSet<String> titles = new HashSet<>();
        HashSet<String> isbns  = new HashSet<>();
        for (Book b : suggestions) {
            if (b == null) continue;
            if (b.getTitle() != null) titles.add(b.getTitle().toLowerCase(Locale.ROOT));
            if (b.getIsbn()  != null) isbns.add(b.getIsbn().toLowerCase(Locale.ROOT));
        }
        List<LibraryBook> filtered = new ArrayList<>();
        String ql = q.toLowerCase(Locale.ROOT);
        for (LibraryBook lb : currentBooksList) {
            Book b = lb.getBook();
            String t = b != null && b.getTitle()!=null ? b.getTitle().toLowerCase(Locale.ROOT) : "";
            String i = b != null && b.getIsbn()!=null  ? b.getIsbn().toLowerCase(Locale.ROOT)  : "";
            if (titles.contains(t) || isbns.contains(i) || t.contains(ql)) filtered.add(lb);
        }
        displayBooks(filtered);
    }

    private void filterLocally(String q) {
        String ql = q.toLowerCase(Locale.ROOT);
        List<LibraryBook> filtered = new ArrayList<>();
        for (LibraryBook lb : currentBooksList) {
            Book b = lb.getBook();
            String title  = b != null && b.getTitle()!=null ? b.getTitle().toLowerCase(Locale.ROOT) : "";
            String author = (b != null) ? b.getAuthorDisplay().toLowerCase(Locale.ROOT) : "";
            if (title.contains(ql) || author.contains(ql)) filtered.add(lb);
        }
        displayBooks(filtered);
    }

    /** ========== AUX ========== */
    private void showError(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
        Log.e(TAG, msg);
    }
}
