// Certifica-te que este é o teu package correto
package com.example.projdroid.ui;

// Imports necessários
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.projdroid.R; // O teu R
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.api.RetrofitClient;

// Os teus modelos (garante que estes existem no teu package 'models')
import com.example.projdroid.models.Book;
// import com.example.projdroid.models.Library; // Já não é preciso
import com.example.projdroid.models.LibraryBook;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity {

    // Vistas do layout
    private EditText inputUsername;
    private Button btnSearch;
    private ProgressBar progress;
    private LinearLayout containerBooks;

    // Variáveis de estado
    private String username;
    private int requestsInFlight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user); // Usa o XML atualizado

        // Encontra as vistas
        inputUsername   = findViewById(R.id.inputUsername);
        btnSearch       = findViewById(R.id.btnSearch);
        progress        = findViewById(R.id.progress);
        containerBooks  = findViewById(R.id.containerBooks);

        btnSearch.setOnClickListener(v -> search());
    }

    private void search() {
        username = inputUsername.getText().toString().trim(); // Guarda o username
        if (TextUtils.isEmpty(username)) {
            inputUsername.setError("Indica o nome do utilizador");
            return;
        }

        containerBooks.removeAllViews(); // Limpa resultados anteriores
        setLoading(true);
        fetchBooksByUser(username); // Chama o método que funciona
    }

    // Método para procurar os livros (copiado do UserLinkActivity)
    private void fetchBooksByUser(String username) {
        Log.d("UserActivity", "Fetching books for user: " + username);
        // Cria a API
        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/").create(LibraryApi.class);

        requestsInFlight++;
        Call<List<LibraryBook>> call = api.getBooksByUser(username);

        call.enqueue(new Callback<List<LibraryBook>>() {
            @Override
            public void onResponse(Call<List<LibraryBook>> call, Response<List<LibraryBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("UserActivity", "Books fetched successfully: " + response.body().size());
                    displayBooks(response.body());
                } else {
                    Log.d("UserActivity", "Error fetching books: " + response.code());
                    showError("Erro: " + response.code());
                }
                endLoading();
            }

            @Override
            public void onFailure(Call<List<LibraryBook>> call, Throwable throwable) {
                Log.d("UserActivity", "Failed to fetch books: " + throwable.getMessage());
                showError("Erro: " + throwable.getMessage());
                endLoading();
            }
        });
    }


    // Método para exibir os livros (copiado do UserLinkActivity)
    private void displayBooks(List<LibraryBook> books) {
        containerBooks.removeAllViews(); // Garante que está limpo
        Log.d("UserActivity", "Displaying books: " + books.size());

        for (LibraryBook libraryBook : books) {
            Book book = libraryBook.getBook();

            // Cria o container horizontal
            LinearLayout horizontalContainer = new LinearLayout(this);
            horizontalContainer.setOrientation(LinearLayout.HORIZONTAL);
            horizontalContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            horizontalContainer.setPadding(0, 0, 0, 16);

            // ImageView para a capa
            ImageView coverImageView = new ImageView(this);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200, 300);
            coverImageView.setLayoutParams(imageParams);
            coverImageView.setPadding(8, 8, 8, 8);
            coverImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Obter ISBN do LibraryBook ou do Book
            String isbn = libraryBook.getIsbn();
            if ((isbn == null || isbn.isEmpty()) && book != null) {
                isbn = book.getIsbn();
            }
            fetchBookCover(isbn, book, coverImageView);

            // TextView para os detalhes
            TextView bookDetails = new TextView(this);
            bookDetails.setTextSize(16);
            bookDetails.setPadding(16, 0, 0, 0);

            SpannableStringBuilder data = new SpannableStringBuilder();
            String titleText = "Title: " + book.getTitle() + "\n";
            data.append(titleText);
            data.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            String authorName = (book.getAuthors() != null && !book.getAuthors().isEmpty())
                    ? book.getAuthors().get(0).getName()
                    : "Unknown Author";
            data.append("Author: ").append(authorName).append("\n");

            String dueDate = libraryBook.getDueDate();
            String formattedDate = dueDate.split("T")[0];
            data.append("Data para entrega: ").append(formattedDate).append("\n");
            bookDetails.setText(data);

            // === LÓGICA DE CHECK-IN REMOVIDA ===
            // horizontalContainer.setOnLongClickListener(v -> { ... });

            horizontalContainer.addView(coverImageView);
            horizontalContainer.addView(bookDetails);
            containerBooks.addView(horizontalContainer);
        }
    }

    // === MÉTODOS DE CHECK-IN REMOVIDOS ===
    // showCheckInDialog(...)
    // performCheckIn(...)


    // Método para buscar a capa (seguindo a lógica do LibraryDetailActivity)
    private void fetchBookCover(String isbn, Book book, ImageView coverImageView) {
        String imageName = null;

        // Primeiro tenta obter do objeto Book (cover.imageName)
        if (book != null && book.getCover() != null) {
            imageName = book.getCover().getImageName();
        }

        // Se não tiver, usa o ISBN para construir o nome da imagem
        if ((imageName == null || imageName.isEmpty()) && isbn != null && !isbn.isEmpty()) {
            imageName = isbn + ".jpg";
        }

        if (imageName != null && !imageName.isEmpty()) {
            String coverUrl = "http://193.136.62.24/v1/assets/cover/" + imageName;
            Log.d("UserActivity", "Loading cover from URL: " + coverUrl + " for book: " + (book != null ? book.getTitle() : "null"));

            Glide.with(this)
                    .load(coverUrl)
                    .placeholder(R.drawable.cover_placeholder)
                    .error(R.drawable.cover_error)
                    .into(coverImageView);
        } else {
            coverImageView.setImageResource(R.drawable.cover_placeholder);
            Log.d("UserActivity", "No imageName found for book: " + (book != null ? book.getTitle() : "null") + ", ISBN: " + isbn);
        }
    }

    // Método para exibir erros (copiado do UserLinkActivity)
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // --- Métodos de loading (do UserActivity original) ---
    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!loading);
    }

    private void endLoading() {
        requestsInFlight = Math.max(0, requestsInFlight - 1);
        if (requestsInFlight == 0) setLoading(false);
    }
}
