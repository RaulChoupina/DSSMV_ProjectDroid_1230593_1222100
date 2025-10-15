package com.example.projdroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "admin1@gmail.com";
    private static final String ADMIN_PASS  = "1234";

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = safeText(edtEmail);
        String pass  = safeText(edtPassword);

        // Validação de formato básico
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            show("Insere um email válido.");
            return;
        }

        if (pass.isEmpty()) {
            show("A palavra-passe é obrigatória.");
            return;
        }

        // Verificação das credenciais do admin
        if (email.equalsIgnoreCase(ADMIN_EMAIL) && pass.equals(ADMIN_PASS)) {
            show("Login de administrador bem-sucedido!");
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
        } else {
            show("Credenciais incorretas. Tente novamente.");
        }
    }

    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
