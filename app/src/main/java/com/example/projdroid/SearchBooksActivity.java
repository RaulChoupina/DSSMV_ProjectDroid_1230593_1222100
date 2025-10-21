package com.example.projdroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projdroid.models.Book;
import com.example.projdroid.models.Library;
import com.example.projdroid.models.LibraryBook;
import com.example.projdroid.repository.LibraryRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchBooksActivity extends AppCompatActivity {

    private EditText edtQuery;
    private Button btnGo;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private final List<Book> current = new ArrayList<>();
    private final LibraryRepository repo = new LibraryRepository();
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_books);

        userEmail = getIntent().getStringExtra("cliente_email");

        edtQuery = findViewById(R.id.edtQuery);
        btnGo    = findViewById(R.id.btnGo);
        list     = findViewById(R.id.listResults);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(adapter);

        // pesquisa inicial apenas para teste (podes remover)
        edtQuery.setText("harry");
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
        if (q.isEmpty()) {
            Toast.makeText(this, "Escreve algo para pesquisar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pesquisa normal
        repo.searchBooks(q, 0, new LibraryRepository.Callback<List<Book>>() {
            @Override public void onSuccess(List<Book> data) {
                if (data != null && !data.isEmpty()) {
                    showResults(data, "Resultados");
                } else {
                    // Se não houver resultados, tenta carregar livros da 1ª biblioteca
                    loadFirstLibraryBooks();
                }
            }

            @Override public void onError(Throwable t) {
                Toast.makeText(SearchBooksActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                // fallback também em erro
                loadFirstLibraryBooks();
            }
        });
    }

    /** Fallback: carregar livros da primeira biblioteca */
    private void loadFirstLibraryBooks() {
        repo.getLibraries(new LibraryRepository.Callback<List<Library>>() {
            @Override public void onSuccess(List<Library> libs) {
                if (libs == null || libs.isEmpty() || libs.get(0) == null || libs.get(0).id == null) {
                    Toast.makeText(SearchBooksActivity.this, "Sem bibliotecas disponíveis.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String libId = libs.get(0).id;
                repo.getBooks(libId, new LibraryRepository.Callback<List<LibraryBook>>() {
                    @Override public void onSuccess(List<LibraryBook> lbs) {
                        List<Book> books = new ArrayList<>();
                        if (lbs != null) {
                            for (LibraryBook lb : lbs) {
                                if (lb != null && lb.book != null) books.add(lb.book);
                            }
                        }
                        showResults(books, "Livros da biblioteca");
                    }

                    @Override public void onError(Throwable t) {
                        Toast.makeText(SearchBooksActivity.this, "Erro ao carregar livros: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override public void onError(Throwable t) {
                Toast.makeText(SearchBooksActivity.this, "Erro ao listar bibliotecas: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Mostra a lista de livros no ecrã */
    private void showResults(List<Book> data, String origem) {
        current.clear();
        if (data != null) current.addAll(data);

        List<String> titles = new ArrayList<>();
        for (Book b : current) {
            String title = (b.title == null ? "(sem título)" : b.title);
            String isbn  = (b.isbn == null ? "" : " • " + b.isbn);
            titles.add(title + isbn);
        }

        adapter.clear();
        adapter.addAll(titles);
        adapter.notifyDataSetChanged();

        if (titles.isEmpty())
            Toast.makeText(this, "Sem resultados (" + origem + ")", Toast.LENGTH_SHORT).show();
    }
}
