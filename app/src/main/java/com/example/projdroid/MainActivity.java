package com.example.projdroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views (IDs têm de existir no teu activity_main.xml)
        edtEmail    = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvRegister  = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> doLogin());
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)) // criaremos esta Activity
        );
    }

    private void doLogin() {
        String email = safeText(edtEmail);
        String pass  = safeText(edtPassword);

        if (!isValidEmail(email)) {
            show("Insere um email válido.");
            return;
        }
        if (pass.isEmpty()) {
            show("A palavra-passe é obrigatória.");
            return;
        }

        // TODO: quando tiveres endpoint de autenticação, chama-o aqui com Retrofit
        // Por agora só simula sucesso
        show("Login efetuado (simulado).");
        // startActivity(new Intent(this, ClientHomeActivity.class));
        // finish();
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
