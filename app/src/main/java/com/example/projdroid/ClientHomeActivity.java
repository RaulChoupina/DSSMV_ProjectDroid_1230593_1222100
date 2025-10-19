package com.example.projdroid.viewmodel;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.projdroid.R;

public class ClientHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);

        String email = getIntent().getStringExtra("cliente_email");
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Bem-vindo, " + (email == null ? "cliente" : email) + "!");
    }
}
