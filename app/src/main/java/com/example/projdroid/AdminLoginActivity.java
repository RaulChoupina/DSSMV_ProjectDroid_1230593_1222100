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

    private EditText edtEmail, edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 👉 este layout TEM os IDs edtEmail, edtPassword, btnLogin
        setContentView(R.layout.activity_register);

        edtEmail   = findViewById(R.id.edtEmail);
        edtPassword= findViewById(R.id.edtPassword);
        btnLogin   = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();
        String pass  = edtPassword.getText() == null ? "" : edtPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Insere um email válido."); return; }
        if (pass.isEmpty()) { toast("A palavra-passe é obrigatória."); return; }

        if (email.equalsIgnoreCase(ADMIN_EMAIL) && pass.equals(ADMIN_PASS)) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
        } else {
            toast("Credenciais incorretas. Tente novamente.");
        }
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
