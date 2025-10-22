package com.example.projdroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projdroid.models.Book;
import com.example.projdroid.repository.LibraryRepository;
import com.example.projdroid.ui.BookAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ClientHomeActivity extends AppCompatActivity {

    private BookAdapter adapter;
    private final LibraryRepository repo = new LibraryRepository();
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);

        userEmail = getIntent().getStringExtra("cliente_email");

        // Toolbar
        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_search) {
                Intent i = new Intent(this, SearchBooksActivity.class);
                i.putExtra("cliente_email", userEmail);
                startActivity(i);
                return true;
            }
            return false;
        });

        // Lista
        RecyclerView rv = findViewById(R.id.rvBooks);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(book -> {
            if (book.isbn == null || book.isbn.isEmpty()) {
                Toast.makeText(this, "Sem ISBN disponível.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, BookDetailActivity.class);
            i.putExtra("isbn", book.isbn);
            i.putExtra("cliente_email", userEmail);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        // Bottom nav
        BottomNavigationView bottom = findViewById(R.id.bottomNav);
        bottom.setOnItemSelectedListener(this::onBottomNav);
        bottom.setSelectedItemId(R.id.nav_home);

        // Carregar lista inicial (query genérica)
        loadBooks("a");
    }

    private boolean onToolbarMenu(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent i = new Intent(this, SearchBooksActivity.class);
            i.putExtra("cliente_email", userEmail);
            startActivity(i);
            return true;
        }
        return false;
    }

    private boolean onBottomNav(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) return true;
        if (id == R.id.nav_loans) {
            Intent i = new Intent(this, MyReserveActivity.class);
            i.putExtra("cliente_email", userEmail);
            startActivity(i);
            return true;
        }
        if (id == R.id.nav_history) {
            Intent i = new Intent(this, ReserveHistoryActivity.class);
            i.putExtra("cliente_email", userEmail);
            startActivity(i);
            return true;
        }
        return false;
    }

    private void loadBooks(String query) {
        repo.search(query, new LibraryRepository.Callback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> data) {
                runOnUiThread(() -> {
                    adapter.setItems(data);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(ClientHomeActivity.this,
                                "Erro a carregar livros: " +
                                        (t.getMessage() != null ? t.getMessage() : "desconhecido"),
                                Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}
