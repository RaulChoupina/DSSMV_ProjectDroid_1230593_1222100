package com.example.projdroid;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.projdroid.viewmodel.LibraryViewModel;

public class MainActivity extends AppCompatActivity {
    private LibraryViewModel vm;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vm = new ViewModelProvider(this).get(LibraryViewModel.class);

        vm.libraries().observe(this, libs ->
                Toast.makeText(this, "Bibliotecas: " + (libs==null?0:libs.size()), Toast.LENGTH_SHORT).show());

        vm.error().observe(this, err -> { if (err!=null) Toast.makeText(this, "Erro: "+err, Toast.LENGTH_LONG).show(); });

        vm.loadLibraries();   // chama API ao arrancar
    }
}
