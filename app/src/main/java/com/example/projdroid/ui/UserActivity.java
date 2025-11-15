package com.example.projdroid.ui;

import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import com.example.projdroid.R;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.api.RetrofitClient;
import com.example.projdroid.models.Book;
import com.example.projdroid.models.LibraryBook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

public class UserActivity extends AppCompatActivity implements SensorEventListener {

    // Vistas do layout
    private EditText inputUsername;
    private Button btnSearch;
    private ProgressBar progress;
    private LinearLayout containerCheckedOut;
    private LinearLayout containerHistory;
    private TextView txtCheckedOutTitle;
    private TextView txtHistoryTitle;

    // Estado
    private String username;
    private int requestsInFlight = 0;
    private boolean showingHistory = false;

    // API
    private LibraryApi api;

    // Listas: ativos vs histórico
    private List<LibraryBook> checkedOutList = Collections.emptyList();
    private List<LibraryBook> historyList = Collections.emptyList();

    // Sensor (shake)
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 15.0f;
    private static final int SHAKE_SLOP_TIME_MS = 2000; // Aumentado para 2 segundos
    private long lastShakeTime = 0;
    private float lastX = 0, lastY = 0, lastZ = 0;
    private boolean firstReading = true;
    private boolean isProcessingShake = false; // Flag para evitar processamento duplo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Views
        inputUsername = findViewById(R.id.inputUsername);
        btnSearch = findViewById(R.id.btnSearch);
        progress = findViewById(R.id.progress);
        containerCheckedOut = findViewById(R.id.containerCheckedOut);
        containerHistory = findViewById(R.id.containerHistory);
        txtCheckedOutTitle = findViewById(R.id.txtCheckedOutTitle);
        txtHistoryTitle = findViewById(R.id.txtHistoryTitle);

        // API - usar a base URL correta
        api = RetrofitClient.getClient("http://193.136.62.24/v1/").create(LibraryApi.class);

        // Sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Esconder histórico inicialmente
        hideHistorySection();

        btnSearch.setOnClickListener(v -> search());
    }

    // Registar / remover o listener do sensor
    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            firstReading = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // --- Pesquisa de utilizador ---
    private void search() {
        username = inputUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            inputUsername.setError("Indica o nome do utilizador");
            return;
        }

        containerCheckedOut.removeAllViews();
        containerHistory.removeAllViews();
        showingHistory = false;
        hideHistorySection();
        requestsInFlight = 0;
        setLoading(true);

        // 1) livros atualmente emprestados (ativos)
        fetchCheckedOutBooks(username);
        // 2) histórico de empréstimos
        fetchCheckoutHistory(username);
    }

    // --- Chamada: livros ativos (user/checked-out) ---
    private void fetchCheckedOutBooks(String userId) {
        Log.d(TAG, "Fetching checked-out for userId: " + userId);
        requestsInFlight++;

        Call<List<LibraryBook>> call = api.getBooksByUser(userId);
        call.enqueue(new Callback<List<LibraryBook>>() {
            @Override
            public void onResponse(Call<List<LibraryBook>> call, Response<List<LibraryBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    checkedOutList = response.body();
                    Log.d(TAG, "Checked-out count: " + checkedOutList.size());
                    displayAllBooks();
                } else {
                    checkedOutList = Collections.emptyList();
                    Log.e(TAG, "Erro a obter livros ativos. Código: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao ler error body", e);
                        }
                    }
                    showError("Erro a obter livros ativos: " + response.code());
                    displayAllBooks();
                }
                endLoading();
            }

            @Override
            public void onFailure(Call<List<LibraryBook>> call, Throwable t) {
                checkedOutList = Collections.emptyList();
                Log.e(TAG, "Erro checked-out", t);
                showError("Erro a obter livros ativos: " + t.getMessage());
                displayAllBooks();
                endLoading();
            }
        });
    }

    // --- Chamada: histórico (user/checkout-history) ---
    private void fetchCheckoutHistory(String usernameParam) {
        Log.d(TAG, "Fetching history for username: " + usernameParam);
        requestsInFlight++;

        // Verificar se a API espera userId ou username - tentar com o mesmo parâmetro usado em checked-out
        Call<List<LibraryBook>> call = api.getCheckoutHistory(usernameParam);
        Log.d(TAG, "History API call - parameter: " + usernameParam);
        call.enqueue(new Callback<List<LibraryBook>>() {
            @Override
            public void onResponse(Call<List<LibraryBook>> call, Response<List<LibraryBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    historyList = response.body();
                    Log.d(TAG, "History count (before filter): " + historyList.size());
                    // Filtrar histórico será feito quando ambas as listas estiverem carregadas
                } else {
                    historyList = Collections.emptyList();
                    Log.w(TAG, "Erro a obter histórico. Código: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao ler error body", e);
                        }
                    }
                    // Não mostrar erro se o histórico estiver vazio (pode ser normal)
                    if (response.code() != 200) {
                        showError("Erro a obter histórico: " + response.code());
                    }
                }
                displayAllBooks();
                endLoading();
            }

            @Override
            public void onFailure(Call<List<LibraryBook>> call, Throwable t) {
                historyList = Collections.emptyList();
                Log.e(TAG, "Erro histórico", t);
                showError("Erro a obter histórico: " + t.getMessage());
                displayAllBooks();
                endLoading();
            }
        });
    }

    // Filtrar histórico: remover livros que ainda estão checked-out
    private void filterHistoryList() {
        if (checkedOutList == null || checkedOutList.isEmpty()) {
            Log.d(TAG, "Nenhum livro checked-out, histórico não precisa ser filtrado");
            return; // Se não há checked-out, não precisa filtrar
        }

        if (historyList == null || historyList.isEmpty()) {
            return; // Se não há histórico, não precisa filtrar
        }

        // Criar um Set com IDs únicos dos empréstimos checked-out (o ID do LibraryBook é o ID do checkout)
        Set<String> checkedOutRecordIds = new HashSet<>();
        
        for (LibraryBook checkedOut : checkedOutList) {
            // O ID do LibraryBook é o ID único do registro de checkout
            if (checkedOut.getId() != null && !checkedOut.getId().isEmpty()) {
                checkedOutRecordIds.add(checkedOut.getId());
                Log.d(TAG, "Checked-out record ID: " + checkedOut.getId());
            }
        }

        Log.d(TAG, "Total de IDs checked-out: " + checkedOutRecordIds.size());
        Log.d(TAG, "Total de livros no histórico antes do filtro: " + historyList.size());

        // Filtrar histórico: manter apenas livros que NÃO estão na lista de checked-out
        // Comparar pelo ID único do registro de checkout
        List<LibraryBook> filteredHistory = new ArrayList<>();
        for (LibraryBook historyBook : historyList) {
            // Se o ID do histórico está na lista de checked-out, significa que ainda está emprestado
            if (historyBook.getId() != null && checkedOutRecordIds.contains(historyBook.getId())) {
                Log.d(TAG, "Removendo livro do histórico (ainda está checked-out): ID=" + historyBook.getId() + ", ISBN=" + historyBook.getIsbn());
            } else {
                // Livro foi devolvido (checkout + checkin completo)
                filteredHistory.add(historyBook);
            }
        }

        Log.d(TAG, "Total de livros no histórico depois do filtro: " + filteredHistory.size());
        historyList = filteredHistory;
    }

    // Mostrar ambas as listas
    private void displayAllBooks() {
        // Só mostrar quando ambas as chamadas terminarem
        if (requestsInFlight > 0) {
            return;
        }

        // Filtrar histórico: remover livros que ainda estão checked-out
        filterHistoryList();

        // Mostrar checked-out sempre
        displayLibraryBooks(checkedOutList, containerCheckedOut, false);
        
        // Mostrar histórico apenas se o shake foi detectado (não esconder se já estava visível)
        if (showingHistory && historyList != null && !historyList.isEmpty()) {
            showHistorySection();
            displayLibraryBooks(historyList, containerHistory, true);
        }
        // Se não está a mostrar histórico, não fazer nada (manter estado atual)
    }

    // Reutiliza a UI que já tinhas, mas para LibraryBook
    private void displayLibraryBooks(List<LibraryBook> books, LinearLayout container, boolean isHistory) {
        container.removeAllViews();

        if (books == null || books.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText(isHistory
                    ? "Sem histórico de empréstimos para este utilizador."
                    : "Sem livros atualmente emprestados para este utilizador.");
            emptyView.setPadding(16, 16, 16, 16);
            container.addView(emptyView);
            return;
        }

        Log.d(TAG, "Starting to display " + books.size() + " books (" + (isHistory ? "history" : "checked-out") + ")");

        // Para histórico, mostrar todos os livros (já filtrados)
        // Para checked-out, limitar a 100 para performance
        int maxBooksToShow = isHistory ? books.size() : 100;
        int booksToProcess = Math.min(books.size(), maxBooksToShow);

        if (!isHistory && books.size() > maxBooksToShow) {
            TextView limitNotice = new TextView(this);
            limitNotice.setText("A mostrar " + maxBooksToShow + " de " + books.size() + " livros");
            limitNotice.setPadding(16, 8, 16, 8);
            limitNotice.setTextSize(12);
            container.addView(limitNotice);
        }

        int displayedCount = 0;
        for (int i = 0; i < booksToProcess; i++) {
            LibraryBook libraryBook = books.get(i);
            try {
                Book book = libraryBook.getBook();

                // Container horizontal
                LinearLayout horizontalContainer = new LinearLayout(this);
                horizontalContainer.setOrientation(LinearLayout.HORIZONTAL);
                horizontalContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                horizontalContainer.setPadding(0, 0, 0, 16);

                // ImageView capa
                ImageView coverImageView = new ImageView(this);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200, 300);
                coverImageView.setLayoutParams(imageParams);
                coverImageView.setPadding(8, 8, 8, 8);
                coverImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // ISBN do LibraryBook ou do Book
                String isbn = libraryBook.getIsbn();
                if ((isbn == null || isbn.isEmpty()) && book != null) {
                    isbn = book.getIsbn();
                }
                fetchBookCover(isbn, book, coverImageView);

                // TextView detalhes
                TextView bookDetails = new TextView(this);
                bookDetails.setTextSize(16);
                bookDetails.setPadding(16, 0, 0, 0);

                SpannableStringBuilder data = new SpannableStringBuilder();
                String titleText = "Title: " + (book != null && book.getTitle() != null ? book.getTitle() : (libraryBook.getBookId() != null ? libraryBook.getBookId() : "Sem título")) + "\n";
                data.append(titleText);
                data.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                String authorName = "Unknown Author";
                if (book != null && book.getAuthors() != null && !book.getAuthors().isEmpty()) {
                    authorName = book.getAuthors().get(0).getName();
                }
                data.append("Author: ").append(authorName).append("\n");

                String dueDate = libraryBook.getDueDate();
                if (dueDate != null && !dueDate.isEmpty()) {
                    if (dueDate.contains("T")) {
                        String formattedDate = dueDate.split("T")[0];
                        data.append("Data para entrega: ").append(formattedDate).append("\n");
                    } else {
                        data.append("Data para entrega: ").append(dueDate).append("\n");
                    }
                }

                bookDetails.setText(data);

                horizontalContainer.addView(coverImageView);
                horizontalContainer.addView(bookDetails);
                container.addView(horizontalContainer);
                displayedCount++;
            } catch (Exception e) {
                Log.e(TAG, "Erro ao processar livro", e);
                // Continuar com o próximo livro mesmo se houver erro
            }
        }
        Log.d(TAG, "Displayed " + displayedCount + " books successfully");
    }

    // --- Capa do livro ---
    private void fetchBookCover(String isbn, Book book, ImageView coverImageView) {
        String imageName = null;

        if (book != null && book.getCover() != null) {
            imageName = book.getCover().getImageName();
        }

        if ((imageName == null || imageName.isEmpty()) && isbn != null && !isbn.isEmpty()) {
            imageName = isbn + ".jpg";
        }

        if (imageName != null && !imageName.isEmpty()) {
            String coverUrl = "http://193.136.62.24/v1/assets/cover/" + imageName;
            Log.d(TAG, "Loading cover from URL: " + coverUrl + " for book: " + (book != null ? book.getTitle() : "null"));

            Glide.with(this)
                    .load(coverUrl)
                    .placeholder(R.drawable.cover_placeholder)
                    .error(R.drawable.cover_error)
                    .into(coverImageView);
        } else {
            coverImageView.setImageResource(R.drawable.cover_placeholder);
            Log.d(TAG, "No imageName found for book: " + (book != null ? book.getTitle() : "null") + ", ISBN: " + isbn);
        }
    }

    // --- SHAKE: deteção do movimento ---
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (firstReading) {
            lastX = x;
            lastY = y;
            lastZ = z;
            firstReading = false;
            return;
        }

        // Calcular a diferença de aceleração (mudança de velocidade)
        float deltaX = Math.abs(x - lastX);
        float deltaY = Math.abs(y - lastY);
        float deltaZ = Math.abs(z - lastZ);

        // Se a mudança for significativa em qualquer eixo, pode ser um shake
        if (deltaX > SHAKE_THRESHOLD || deltaY > SHAKE_THRESHOLD || deltaZ > SHAKE_THRESHOLD) {
            long now = System.currentTimeMillis();
            if (now - lastShakeTime > SHAKE_SLOP_TIME_MS && !isProcessingShake) {
                lastShakeTime = now;
                isProcessingShake = true;
                Log.d(TAG, "Shake detectado! Delta: x=" + deltaX + ", y=" + deltaY + ", z=" + deltaZ);
                // Executar no thread principal
                runOnUiThread(() -> {
                    onShakeDetected();
                    // Reset flag após um delay
                    new android.os.Handler().postDelayed(() -> {
                        isProcessingShake = false;
                    }, SHAKE_SLOP_TIME_MS);
                });
            }
        }

        lastX = x;
        lastY = y;
        lastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // não precisamos de nada aqui
    }

    // Alternar visibilidade do histórico
    private void onShakeDetected() {
        Log.d(TAG, "=== onShakeDetected chamado ===");
        Log.d(TAG, "Estado atual - showingHistory: " + showingHistory);
        Log.d(TAG, "Checked-out count: " + (checkedOutList != null ? checkedOutList.size() : 0));
        Log.d(TAG, "History count: " + (historyList != null ? historyList.size() : 0));

        // Se ainda não há dados carregados, mostra mensagem
        if (requestsInFlight > 0) {
            Toast.makeText(this, "Ainda a carregar dados...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Ainda a carregar dados, ignorando shake");
            return;
        }

        // Alternar estado
        showingHistory = !showingHistory;
        Log.d(TAG, "Novo estado - showingHistory: " + showingHistory);

        if (showingHistory) {
            // Filtrar histórico antes de mostrar (caso ainda não tenha sido filtrado)
            filterHistoryList();
            
            if (historyList != null && !historyList.isEmpty()) {
                showHistorySection();
                displayLibraryBooks(historyList, containerHistory, true);
                Toast.makeText(this, "A mostrar histórico de empréstimos (" + historyList.size() + " livros)", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Histórico mostrado com " + historyList.size() + " livros");
            } else {
                Toast.makeText(this, "Sem histórico de empréstimos disponível", Toast.LENGTH_SHORT).show();
                showingHistory = false; // Reverter estado
                hideHistorySection();
                Log.d(TAG, "Sem histórico disponível");
            }
        } else {
            hideHistorySection();
            Toast.makeText(this, "Histórico oculto", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Histórico oculto");
        }
    }

    private void showHistorySection() {
        txtHistoryTitle.setVisibility(View.VISIBLE);
        containerHistory.setVisibility(View.VISIBLE);
    }

    private void hideHistorySection() {
        txtHistoryTitle.setVisibility(View.GONE);
        containerHistory.setVisibility(View.GONE);
    }

    // --- Helpers de UI ---
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!loading);
    }

    private void endLoading() {
        requestsInFlight = Math.max(0, requestsInFlight - 1);
        if (requestsInFlight == 0) {
            setLoading(false);
            displayAllBooks(); // Garantir que mostra quando ambas terminam
        }
    }
}
