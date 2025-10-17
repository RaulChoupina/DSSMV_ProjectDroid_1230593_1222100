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
        // ⬇️ APONTA PARA O LAYOUT QUE EXISTE
        setContentView(R.layout.activity_login);

        // Estes IDs têm de existir em activity_login.xml
        edtEmail    = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin    = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();
        String pass  = edtPassword.getText() == null ? "" : edtPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Insere um email válido.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.isEmpty()) {
            Toast.makeText(this, "A palavra-passe é obrigatória.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.equalsIgnoreCase(ADMIN_EMAIL) && pass.equals(ADMIN_PASS)) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Credenciais incorretas.", Toast.LENGTH_SHORT).show();
        }
    }
}
