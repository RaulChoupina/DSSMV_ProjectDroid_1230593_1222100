package com.example.projdroid.ui;

import android.util.Log;
import android.widget.*;
import com.example.projdroid.R;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.api.RetrofitClient;
import com.example.projdroid.models.Library;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class LibrariesActivity extends AppCompatActivity {

    private static final String TAG = "LibrariesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libraries);
        fetchLibraries(); // só isto
    }

    /** Chama a API e obtém a lista de bibliotecas */
    private void fetchLibraries() {
        LibraryApi api = RetrofitClient
                .getClient("http://193.136.62.24/v1/")
                .create(LibraryApi.class);

        api.getLibraries().enqueue(new Callback<List<Library>>() {
            @Override
            public void onResponse(Call<List<Library>> call, Response<List<Library>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayLibraries(response.body());
                } else {
                    showError("Erro HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Library>> call, Throwable t) {
                showError("Falha na ligação: " + t.getMessage());
            }
        });
    }

    /** Mostra a lista de bibliotecas num LinearLayout simples */
    private void displayLibraries(List<Library> libraries) {
        LinearLayout container = findViewById(R.id.containerLibraryData);
        container.removeAllViews();

        for (Library lib : libraries) {
            TextView libraryView = new TextView(this);
            libraryView.setTextSize(16);
            libraryView.setTextColor(getResources().getColor(android.R.color.black));
            libraryView.setBackgroundResource(R.drawable.library_item_background);
            libraryView.setText(
                    "Library Name: " + safe(lib.getName()) + "\n" +
                            "Address: "     + safe(lib.getAddress()) + "\n" +
                            "Open Status: " + (lib.isOpen() ? "Open" : "Closed") + "\n" +
                            "Open Days: "   + safe(lib.getOpenDays()) + "\n"
            );
            container.addView(libraryView);
        }
    }

    private String safe(String s) { return s == null || s.isEmpty() ? "N/A" : s; }

    private void showError(String msg) {
        Log.e(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
