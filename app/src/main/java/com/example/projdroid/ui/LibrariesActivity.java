package com.example.projdroid.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projdroid.R;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.api.RetrofitClient;
import com.example.projdroid.models.Library;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibrariesActivity extends AppCompatActivity {

    private static final String TAG = "LibrariesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libraries);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Libraries");
        }

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_add) {
                addLibrary();
                bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                return true;
            } else if (id == R.id.action_edit) {
                openEditOrDeleteFlow();
                bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                return true;
            } else if (id == R.id.nav_home) {
                return true;
            }
            return false;
        });

        // Inicial
        fetchLibraries();
    }

    /** ========== API CALLS ========== */

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
                    showError("Falha ao obter bibliotecas (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<Library>> call, Throwable t) {
                showError("Erro: " + t.getMessage());
            }
        });
    }

    /** ========== UI HELPERS ========== */

    private void displayLibraries(List<Library> libraries) {
        LinearLayout container = findViewById(R.id.containerLibraryData);
        if (container == null) {
            showError("Layout containerLibraryData não encontrado no XML.");
            return;
        }

        container.removeAllViews();
        int pad = (int) (16 * getResources().getDisplayMetrics().density);

        for (Library lib : libraries) {
            TextView libraryView = new TextView(this);
            libraryView.setTextSize(16);
            libraryView.setTextColor(getResources().getColor(android.R.color.black));
            libraryView.setBackgroundResource(R.drawable.library_item_background);
            libraryView.setPadding(pad, pad, pad, pad);

            libraryView.setText(
                    "Library Name: " + safe(lib.getName()) + "\n" +
                            "Address: " + safe(lib.getAddress()) + "\n" +
                            "Open Status: " + (lib.isOpen() ? "Open" : "Closed") + "\n" +
                            "Open Days: " + safe(lib.getOpenDays()) + "\n" +
                            "Open Time: " + safe(lib.getOpenTime()) + "\n" +
                            "Close Time: " + safe(lib.getCloseTime())
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, pad);
            container.addView(libraryView, params);
        }
    }

    private void showError(String msg) {
        Log.e(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String safe(String s) {
        return (s == null || s.isEmpty()) ? "N/A" : s;
    }

    private boolean isValidTime(String hhmm) {
        return hhmm != null && hhmm.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    /** ========== ADD LIBRARY ========== */

    private void addLibrary() {
        final boolean[] selectedDays = new boolean[7];
        final String[] daysOfWeek = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_library, null, false);
        EditText etName = dialogView.findViewById(R.id.editTextLibraryName);
        EditText etAddress = dialogView.findViewById(R.id.editTextLibraryAddress);
        EditText etOpenTime = dialogView.findViewById(R.id.editTextOpenTime);
        EditText etCloseTime = dialogView.findViewById(R.id.editTextCloseTime);
        Button btnSelectDays = dialogView.findViewById(R.id.btnSelectOpenDays);

        btnSelectDays.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Open Days");
            builder.setMultiChoiceItems(daysOfWeek, selectedDays, (dialog, which, isChecked) -> selectedDays[which] = isChecked);
            builder.setPositiveButton("OK", (dialog, which) -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < daysOfWeek.length; i++) {
                    if (selectedDays[i]) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(daysOfWeek[i]);
                    }
                }
                btnSelectDays.setText(sb.length() > 0 ? sb.toString() : "Select Days");
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add New Library")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String addr = etAddress.getText().toString().trim();
                    String oTime = etOpenTime.getText().toString().trim();
                    String cTime = etCloseTime.getText().toString().trim();

                    if (name.isEmpty()) { showError("Name is required"); return; }
                    if (addr.isEmpty()) { showError("Address is required"); return; }
                    if (!isValidTime(oTime)) { showError("Open time must be HH:mm"); return; }
                    if (!isValidTime(cTime)) { showError("Close time must be HH:mm"); return; }

                    StringBuilder openDaysBuilder = new StringBuilder();
                    for (int i = 0; i < daysOfWeek.length; i++) {
                        if (selectedDays[i]) {
                            if (openDaysBuilder.length() > 0) openDaysBuilder.append(", ");
                            openDaysBuilder.append(daysOfWeek[i]);
                        }
                    }

                    Library newLibrary = new Library();
                    newLibrary.setName(name);
                    newLibrary.setAddress(addr);
                    newLibrary.setOpenTime(oTime);
                    newLibrary.setCloseTime(cTime);
                    newLibrary.setOpenDays(openDaysBuilder.toString().trim());

                    LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/").create(LibraryApi.class);
                    api.addLibrary(newLibrary).enqueue(new Callback<Library>() {
                        @Override
                        public void onResponse(Call<Library> call, Response<Library> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(LibrariesActivity.this, "Library added!", Toast.LENGTH_SHORT).show();
                                fetchLibraries();
                            } else {
                                showError("Failed to add library (HTTP " + response.code() + ")");
                            }
                        }

                        @Override
                        public void onFailure(Call<Library> call, Throwable t) {
                            showError("Error: " + t.getMessage());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** ========== EDIT / DELETE FLOW ========== */

    private void openEditOrDeleteFlow() {
        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/").create(LibraryApi.class);
        api.getLibraries().enqueue(new Callback<List<Library>>() {
            @Override
            public void onResponse(Call<List<Library>> call, Response<List<Library>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    showError("Não foi possível carregar bibliotecas.");
                    return;
                }

                List<Library> list = response.body();
                String[] items = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    items[i] = safe(list.get(i).getName());
                }

                new AlertDialog.Builder(LibrariesActivity.this)
                        .setTitle("Escolher biblioteca")
                        .setItems(items, (d, which) -> showEditDeleteDialog(list.get(which)))
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onFailure(Call<List<Library>> call, Throwable t) {
                showError("Erro: " + t.getMessage());
            }
        });
    }

    private void showEditDeleteDialog(Library lib) {
        String[] actions = {"Editar", "Apagar"};

        new AlertDialog.Builder(this)
                .setTitle(safe(lib.getName()))
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) showInlineEditDialog(lib);
                    else if (which == 1) confirmDeleteLibrary(lib);
                })
                .setNegativeButton("Fechar", null)
                .show();
    }

    private void showInlineEditDialog(Library lib) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText etName = new EditText(this);
        etName.setHint("Name");
        etName.setText(safe(lib.getName()));
        layout.addView(etName);

        EditText etAddr = new EditText(this);
        etAddr.setHint("Address");
        etAddr.setText(safe(lib.getAddress()));
        layout.addView(etAddr);

        new AlertDialog.Builder(this)
                .setTitle("Editar biblioteca")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    Library updated = new Library();
                    updated.setId(lib.getId());
                    updated.setName(etName.getText().toString().trim());
                    updated.setAddress(etAddr.getText().toString().trim());
                    updated.setOpenDays(lib.getOpenDays());
                    updated.setOpenTime(lib.getOpenTime());
                    updated.setCloseTime(lib.getCloseTime());
                    updateLibrary(updated);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmDeleteLibrary(Library lib) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar biblioteca")
                .setMessage("Queres mesmo apagar \"" + safe(lib.getName()) + "\"?")
                .setPositiveButton("Apagar", (d, w) -> deleteLibrary(lib.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** ========== UPDATE / DELETE ========== */

    private void updateLibrary(Library library) {
        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/").create(LibraryApi.class);
        api.updateLibrary(library.getId(), library).enqueue(new Callback<Library>() {
            @Override
            public void onResponse(Call<Library> call, Response<Library> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LibrariesActivity.this, "Library updated successfully!", Toast.LENGTH_SHORT).show();
                    fetchLibraries();
                } else {
                    showError("Failed to update library (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Library> call, Throwable t) {
                showError("Error: " + t.getMessage());
            }
        });
    }

    private void deleteLibrary(String libraryId) {
        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/").create(LibraryApi.class);
        api.removeLibrary(libraryId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LibrariesActivity.this, "Library deleted successfully!", Toast.LENGTH_SHORT).show();
                    fetchLibraries();
                } else {
                    showError("Failed to delete library (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showError("Error: " + t.getMessage());
            }
        });
    }
}
