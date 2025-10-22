package com.example.projdroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projdroid.models.Book;
import com.example.projdroid.repository.LibraryRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchBooksActivity extends AppCompatActivity {

    // <- usa SEMPRE esta biblioteca
    private static final String LIBRARY_ID = "bb385aa2-866f-419b-85fd-202ecec8cfde";

    private EditText edtQuery;
    private Button btnGo;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private final List<Book> current = new ArrayList<>();
    private final LibraryRepository repo = new LibraryRepository();
    private String userEmail;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_books);

        userEmail = getIntent().getStringExtra("cliente_email");

        edtQuery = findViewById(R.id.edtQuery);
        btnGo    = findViewById(R.id.btnGo);
        list     = findViewById(R.id.listResults);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(adapter);

        // pesquisa inicial (remove se não quiseres)
        edtQuery.setText("os maias");
        doSearch();

        btnGo.setOnClickListener(v -> doSearch());

        list.setOnItemClickListener((parent, view, position, id) -> {
            Book b = current.get(position);
            if (b.isbn == null || b.isbn.isEmpty()) {
                Toast.makeText(this, "Este resultado não tem ISBN.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, BookDetailActivity.class);
            i.putExtra("isbn", b.isbn);
            i.putExtra("cliente_email", userEmail);
            startActivity(i);
        });
    }

    private void doSearch() {
        String q = edtQuery.getText() == null ? "" : edtQuery.getText().toString().trim();

        repo.searchBooksInLibrary(LIBRARY_ID, q, new LibraryRepository.Callback<List<Book>>() {
            @Override public void onSuccess(List<Book> data) {
                current.clear();
                if (data != null) current.addAll(data);

                List<String> titles = new ArrayList<>();
                for (Book b : current) {
                    String title = (b.title == null ? "(sem título)" : b.title);
                    String isbn  = (b.isbn  == null ? "" : " • " + b.isbn);
                    titles.add(title + isbn);
                }
                adapter.clear();
                adapter.addAll(titles);
                adapter.notifyDataSetChanged();

                if (titles.isEmpty())
                    Toast.makeText(SearchBooksActivity.this, "Sem resultados nesta biblioteca.", Toast.LENGTH_SHORT).show();
            }

            @Override public void onError(Throwable t) {
                Toast.makeText(SearchBooksActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
