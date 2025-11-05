package com.example.projdroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
// Importa o SearchView correto
import androidx.appcompat.widget.SearchView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.projdroid.R;
import com.example.projdroid.api.ApiConstants;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.api.RetrofitClient;
import com.example.projdroid.models.Book;
import com.example.projdroid.models.LibraryBook;

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

    private final List<LibraryBook> allBooksInLibrary = new ArrayList<>();
    private LibraryApi api;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_detail);

        // 1. Encontra a Toolbar do teu XML
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        // 2. Define-a como a barra de ação
        setSupportActionBar(toolbar);

        container = findViewById(R.id.containerBooksData);

        // Inicializa a API
        api = RetrofitClient.getClient("http://193.136.62.24/v1/").create(LibraryApi.class);

        Intent i = getIntent();
        libraryId = (i != null) ? i.getStringExtra("library_id") : null;

        // --- CORREÇÃO APLICADA ---
        // 3. Obtém o NOME da biblioteca da Intent
        String libraryName = (i != null) ? i.getStringExtra("library_name") : "Livros"; // "Livros" é o título padrão

        // 4. Define o nome da biblioteca como o título da barra
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(libraryName);
        }
        // --- FIM DA CORREÇÃO ---

        fetchBooksForLibrary(libraryId);
    }

    /* ===================== MENU / LUPA ===================== */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla o menu (menu_libraries.xml) que o teu XML definiu
        getMenuInflater().inflate(R.menu.menu_libraries, menu);

        // Encontra o item de pesquisa DENTRO DO MENU
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Pesquisar livro...");

        // Define os listeners
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
                startTypeahead(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
                pendingSearch = () -> startTypeahead(newText);
                searchHandler.postDelayed(pendingSearch, 300);
                return true;
            }
        });

        // Listener para quando a pesquisa é fechada (clicando no 'X' ou 'back')
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override public boolean onMenuItemActionExpand(MenuItem item) { return true; } // Permite abrir

            @Override public boolean onMenuItemActionCollapse(MenuItem item) {
                // Quando fecha, restaura a lista completa
                displayBooks(allBooksInLibrary);
                return true; // Permite fechar
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    /* ===================== API: LIVROS DA BIBLIOTECA ===================== */

    private void fetchBooksForLibrary(String id) {
        if (TextUtils.isEmpty(id)) {
            info("Sem ID de biblioteca.");
            return;
        }

        api.getBooksByLibraryId(id).enqueue(new Callback<List<LibraryBook>>() {
            @Override
            public void onResponse(Call<List<LibraryBook>> call, Response<List<LibraryBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBooksInLibrary.clear();
                    allBooksInLibrary.addAll(response.body());
                    displayBooks(allBooksInLibrary);
                } else {
                    info("Erro HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<LibraryBook>> call, Throwable t) {
                info("Falha: " + t.getMessage());
            }
        });
    }

    /* ===================== UI LISTA ===================== */

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

        for (LibraryBook lb : books) {
            Book b = lb.getBook();

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

            fetchBookCover(lb.getIsbn(), b, cover);

            TextView tv = new TextView(this);
            tv.setTextSize(16);
            tv.setTextColor(getResources().getColor(android.R.color.black));

            String title  = (b != null && b.getTitle() != null) ? b.getTitle() : "N/A";
            String author = (b != null) ? b.getAuthorDisplay() : "Unknown"; // Usa o método seguro

            tv.setText("Title: " + title + "\n" +
                    "Author: " + author + "\n" +
                    "Stock: " + lb.getStock());
            row.addView(tv);

            row.setOnClickListener(v -> showBookDescription(lb));
        }
    }

    private void fetchBookCover(String isbn, Book book, ImageView target) {
        String imageName = null;
        if (book != null && book.getCover() != null) {
            imageName = book.getCover().getImageName(); // Usa o objeto Cover
        }

        if ((imageName == null || imageName.isEmpty()) && isbn != null && !isbn.isEmpty()) {
            imageName = isbn + ".jpg";
        }

        String url = (imageName != null &&
                (imageName.startsWith("http://") || imageName.startsWith("https://")))
                ? imageName
                : ApiConstants.coverUrl(imageName);

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.cover_placeholder)
                .error(R.drawable.cover_error)
                .transform(new CenterCrop(), new RoundedCorners(12))
                .into(target);
    }

    /* ===================== DESCRIÇÃO ===================== */

    private void showBookDescription(LibraryBook lb) {
        Book b = lb != null ? lb.getBook() : null;
        String title = (b != null && b.getTitle()!=null) ? b.getTitle() : "Book";

        if (b != null && b.getDescription() != null && !b.getDescription().trim().isEmpty()) {
            showDescDialog(title, b.getDescription());
            return;
        }

        Call<Book> call = null;

        if (b != null && b.getIsbn() != null && !b.getIsbn().trim().isEmpty()) {
            call = api.loadBook(b.getIsbn().trim(), false);
        }
        else if (lb != null && lb.getBookId() != null && !lb.getBookId().trim().isEmpty()) {
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
                if (r.isSuccessful() && r.body() != null &&
                        r.body().getDescription() != null &&
                        !r.body().getDescription().trim().isEmpty()) {
                    showDescDialog(title, r.body().getDescription());
                } else {
                    showDescDialog(title, "No description available.");
                }
            }
            @Override public void onFailure(Call<Book> c, Throwable t) {
                loading.dismiss();
                showDescDialog(title, "Failed to load description.\n" + t.getMessage());
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

    /* ===================== TYPEAHEAD (lupa) ===================== */

    private void startTypeahead(String raw) {
        String q = raw == null ? "" : raw.trim();
        if (q.isEmpty()) {
            displayBooks(allBooksInLibrary);
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
        for (LibraryBook lb : allBooksInLibrary) {
            Book b = lb.getBook();
            String t = b != null && b.getTitle()!=null ? b.getTitle().toLowerCase(Locale.ROOT) : "";
            String i = b != null && b.getIsbn()!=null  ? b.getIsbn().toLowerCase(Locale.ROOT)  : "";

            if (titles.contains(t) || isbns.contains(i) || t.contains(ql)) {
                filtered.add(lb);
            }
        }
        displayBooks(filtered);
    }

    private void filterLocally(String q) {
        String ql = q.toLowerCase(Locale.ROOT);
        List<LibraryBook> filtered = new ArrayList<>();
        for (LibraryBook lb : allBooksInLibrary) {
            Book b = lb.getBook();
            String title = b != null && b.getTitle()!=null ? b.getTitle().toLowerCase(Locale.ROOT) : "";
            String author = (b != null) ? b.getAuthorDisplay().toLowerCase(Locale.ROOT) : ""; // Usa o método seguro

            if (title.contains(ql) || author.contains(ql)) {
                filtered.add(lb);
            }
        }
        displayBooks(filtered);
    }

    /* ===================== UTIL ===================== */

    private void info(String msg) {
        Log.d(TAG, msg);
        new AlertDialog.Builder(this)
                .setTitle("Info")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }
}