package com.example.projdroid;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projdroid.models.*;
import com.example.projdroid.repository.LibraryRepository;
import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvIsbn;
    private ListView listReviews;
    private EditText edtReview;
    private CheckBox cbRecommended;
    private Button btnSend;

    private final LibraryRepository repo = new LibraryRepository();
    private String isbn, userEmail;
    private ArrayAdapter<String> reviewsAdapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        isbn = getIntent().getStringExtra("isbn");
        userEmail = getIntent().getStringExtra("cliente_email");

        tvTitle = findViewById(R.id.tvTitle);
        tvIsbn  = findViewById(R.id.tvIsbn);
        listReviews = findViewById(R.id.listReviews);
        edtReview = findViewById(R.id.edtReview);
        cbRecommended = findViewById(R.id.cbRecommended);
        btnSend = findViewById(R.id.btnSendReview);

        reviewsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listReviews.setAdapter(reviewsAdapter);

        tvIsbn.setText("ISBN: " + (isbn==null?"":isbn));
        tvTitle.setText("Detalhe do livro");

        // carregar reviews
        loadReviews();

        btnSend.setOnClickListener(v -> sendReview());
    }

    private void loadReviews() {
        if (isbn==null || isbn.isEmpty()) return;
        repo.getReviews(isbn, 20, new LibraryRepository.Callback<List<Review>>() {
            @Override public void onSuccess(List<Review> data) {
                reviewsAdapter.clear();
                for (Review r : data) {
                    String line = (r.reviewer!=null ? r.reviewer : "Anónimo") +
                            (r.recommended ? " ★ " : " · ") +
                            (r.review!=null ? r.review : "");
                    reviewsAdapter.add(line);
                }
                reviewsAdapter.notifyDataSetChanged();
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Erro ao carregar reviews: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendReview() {
        String text = edtReview.getText()==null ? "" : edtReview.getText().toString().trim();
        boolean recommended = cbRecommended.isChecked();
        if (text.isEmpty()) { Toast.makeText(this, "Escreve a review.", Toast.LENGTH_SHORT).show(); return; }
        if (isbn==null || isbn.isEmpty()) { Toast.makeText(this, "ISBN em falta.", Toast.LENGTH_SHORT).show(); return; }

        CreateReviewRequest req = new CreateReviewRequest();
        req.review = text;
        req.recommended = recommended;

        String userId = (userEmail==null || userEmail.isEmpty()) ? "anon" : userEmail; // por agora usamos email como userId
        repo.createReview(isbn, req, userId, new LibraryRepository.Callback<Review>() {
            @Override public void onSuccess(Review data) {
                Toast.makeText(BookDetailActivity.this, "Review enviada!", Toast.LENGTH_SHORT).show();
                edtReview.setText("");
                cbRecommended.setChecked(false);
                loadReviews();
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Erro ao enviar review: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
