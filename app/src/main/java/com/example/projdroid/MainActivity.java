package com.example.projdroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projdroid.data.ClientData;
import com.example.projdroid.viewmodel.ClientHomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "admin1@gmail.com";
    private static final String ADMIN_PASS  = "1234";

    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtEmail    = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvRegister  = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> doLogin());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
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

        // ADMIN
        if (email.equalsIgnoreCase(ADMIN_EMAIL) && pass.equals(ADMIN_PASS)) {
            show("Login admin OK. A abrir o dashboard…");
            startActivity(new Intent(this, AdminDashboardActivity.class));
            // finish(); // opcional
            return;
        }

        // CLIENTE predefinido
        if (ClientData.validar(email, pass)) {
            show("Login cliente OK.");
            Intent i = new Intent(this, ClientHomeActivity.class);
            i.putExtra("cliente_email", email);
            startActivity(i);
            // finish(); // opcional
            return;
        }

        // Caso contrário
        show("Credenciais inválidas!");
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
