package com.example.projdroid.ui;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projdroid.R;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.api.RetrofitClient;
import com.example.projdroid.models.Library;
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
// BottomNav: item "add" e "edit" abrem as respetivas ações
        //BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        //bottomNav.setOnItemSelectedListener(item -> {
        //    int id = item.getItemId();
//
        //    if (id == R.id.action_add) {
        //        addLibrary();
        //        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
        //        return false;
        //    } else if (id == R.id.action_edit) {
        //        openEditOrDeleteFlow(); // 👈 abre o diálogo de editar/apagar
        //        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
        //        return false;
        //    } else if (id == R.id.nav_home) {
        //        return true;
        //    }
//
        //    return false;
        //});
//
        fetchLibraries();
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
        if (container == null) {
            showError("Layout containerLibraryData não encontrado no XML.");
            return;
        }
        container.removeAllViews();

        for (Library lib : libraries) {
            TextView libraryView = new TextView(LibrariesActivity.this);
            libraryView.setTextSize(16);
            libraryView.setTextColor(getResources().getColor(android.R.color.black));
            libraryView.setBackgroundResource(R.drawable.library_item_background);
            int pad = (int) (16 * getResources().getDisplayMetrics().density);
            libraryView.setPadding(pad, pad, pad, pad);

            libraryView.setText(
                    "Library Name: " + safe(lib.getName()) + "\n" +
                            "Address: "      + safe(lib.getAddress()) + "\n" +
                            "Open Status: "  + (lib.isOpen() ? "Open" : "Closed") + "\n" +
                            "Open Days: "    + safe(lib.getOpenDays()) + "\n"
            );

            container.addView(libraryView);

            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) libraryView.getLayoutParams();
            params.setMargins(0, 0, 0, pad);
            libraryView.setLayoutParams(params);
        }
    }

    private String safe(String s) {
        return (s == null || s.isEmpty()) ? "N/A" : s;
    }

    private void showError(String msg) {
        Log.e(TAG, msg);
        Toast.makeText(LibrariesActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    /** Valida "HH:mm" (00:00–23:59) */
    private boolean isValidTime(String hhmm) {
        return hhmm != null && hhmm.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    private void addLibrary() {
        final boolean[] selectedDays = new boolean[7];
        final String[] daysOfWeek = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};

        View dialogView = LayoutInflater.from(LibrariesActivity.this)
                .inflate(R.layout.dialog_add_library, null, false);

        EditText editTextLibraryName   = dialogView.findViewById(R.id.editTextLibraryName);
        EditText editTextLibraryAddress= dialogView.findViewById(R.id.editTextLibraryAddress);
        EditText editTextOpenTime      = dialogView.findViewById(R.id.editTextOpenTime);
        EditText editTextCloseTime     = dialogView.findViewById(R.id.editTextCloseTime);
        Button   btnSelectOpenDays     = dialogView.findViewById(R.id.btnSelectOpenDays);

        // Dias abertos (multi-select)
        btnSelectOpenDays.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(LibrariesActivity.this);
            builder.setTitle("Select Open Days");
            builder.setMultiChoiceItems(daysOfWeek, selectedDays, (dialog, which, isChecked) -> {
                selectedDays[which] = isChecked;
            });
            builder.setPositiveButton("OK", (dialog, which) -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < daysOfWeek.length; i++) {
                    if (selectedDays[i]) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(daysOfWeek[i]);
                    }
                }
                btnSelectOpenDays.setText(sb.length() > 0 ? sb.toString() : "Select Days");
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        new AlertDialog.Builder(LibrariesActivity.this)
                .setTitle("Add New Library")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name  = editTextLibraryName.getText().toString().trim();
                    String addr  = editTextLibraryAddress.getText().toString().trim();
                    String oTime = editTextOpenTime.getText().toString().trim();
                    String cTime = editTextCloseTime.getText().toString().trim();

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

                    LibraryApi api = RetrofitClient
                            .getClient("http://193.136.62.24/v1/")
                            .create(LibraryApi.class);

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
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }


    private void deleteLibrary(String libraryId) {
        if (libraryId == null || libraryId.isEmpty()) {
            showError("Invalid library ID");
            return;
        }

        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/")
                .create(LibraryApi.class);

        Call<Void> call = api.removeLibrary(libraryId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LibrariesActivity.this,
                            "Library deleted successfully", Toast.LENGTH_SHORT).show();
                    fetchLibraries(); // refresh
                } else {
                    Toast.makeText(LibrariesActivity.this,
                            "Failed to delete library (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(LibrariesActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateLibrary(Library library) {
        if (library == null || library.getId() == null || library.getId().isEmpty()) {
            showError("Invalid library data");
            return;
        }

        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/")
                .create(LibraryApi.class);

        Call<Library> call = api.updateLibrary(library.getId(), library);
        call.enqueue(new Callback<Library>() {
            @Override
            public void onResponse(Call<Library> call, Response<Library> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LibrariesActivity.this,
                            "Library updated successfully!", Toast.LENGTH_SHORT).show();
                    fetchLibraries();
                } else {
                    Toast.makeText(LibrariesActivity.this,
                            "Failed to update library (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Library> call, Throwable t) {
                Toast.makeText(LibrariesActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Fluxo: ir buscar bibliotecas e deixar o utilizador escolher para editar/apagar */
    private void openEditOrDeleteFlow() {
        LibraryApi api = RetrofitClient.getClient("http://193.136.62.24/v1/")
                .create(LibraryApi.class);

        api.getLibraries().enqueue(new retrofit2.Callback<java.util.List<Library>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<Library>> call,
                                   retrofit2.Response<java.util.List<Library>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    showError("Não foi possível carregar bibliotecas.");
                    return;
                }
                java.util.List<Library> list = response.body();

                // nomes para o selector
                String[] items = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    Library l = list.get(i);
                    String n = (l.getName() == null || l.getName().isEmpty()) ? "(sem nome)" : l.getName();
                    items[i] = n;
                }

                new android.app.AlertDialog.Builder(LibrariesActivity.this)
                        .setTitle("Escolher biblioteca")
                        .setItems(items, (d, which) -> {
                            Library selected = list.get(which);
                            showEditDeleteDialog(selected);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<Library>> call, Throwable t) {
                showError("Erro: " + t.getMessage());
            }
        });
    }

    /** Diálogo para escolher Editar ou Apagar a biblioteca selecionada */
    private void showEditDeleteDialog(Library lib) {
        String[] actions = {"Editar", "Apagar"};

        new android.app.AlertDialog.Builder(this)
                .setTitle(safe(lib.getName()))
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showInlineEditDialog(lib);     // PUT
                    } else if (which == 1) {
                        confirmDeleteLibrary(lib);     // DELETE
                    }
                })
                .setNegativeButton("Fechar", null)
                .show();
    }

    /** Diálogo simples (programático) para editar Nome/Morada e fazer PUT */
    private void showInlineEditDialog(Library lib) {
        // Layout vertical com 2 EditTexts
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
        etAddr.setPadding(0, pad / 2, 0, 0);
        layout.addView(etAddr);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Editar biblioteca")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    String newName = etName.getText().toString().trim();
                    String newAddr = etAddr.getText().toString().trim();

                    // cria uma cópia atualizada
                    Library updated = new Library();
                    updated.setId(lib.getId());
                    updated.setName(newName.isEmpty() ? lib.getName() : newName);
                    updated.setAddress(newAddr.isEmpty() ? lib.getAddress() : newAddr);
                    // manter restantes campos existentes
                    updated.setOpenDays(lib.getOpenDays());
                    updated.setOpenTime(lib.getOpenTime());
                    updated.setCloseTime(lib.getCloseTime());
                    // se tiveres um campo boolean isOpen no modelo:
                    // updated.setOpen(lib.isOpen());

                    updateLibrary(updated); // chama o teu método PUT
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** Confirmação e chamada ao teu método DELETE */
    private void confirmDeleteLibrary(Library lib) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Apagar biblioteca")
                .setMessage("Queres mesmo apagar \"" + safe(lib.getName()) + "\"?")
                .setPositiveButton("Apagar", (d, w) -> deleteLibrary(lib.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }



}
