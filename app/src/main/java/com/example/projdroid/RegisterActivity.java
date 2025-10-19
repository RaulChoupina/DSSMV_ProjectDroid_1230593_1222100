package com.example.projdroid;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName            = findViewById(R.id.edtName);
        edtEmail           = findViewById(R.id.edtEmail);
        edtPassword        = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister        = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String name  = safeText(edtName);
        String email = safeText(edtEmail);
        String pass  = safeText(edtPassword);
        String confirm = safeText(edtConfirmPassword);

        if (name.isEmpty()) {
            show("O nome é obrigatório.");
            return;
        }
        if (!isValidEmail(email)) {
            show("Insere um email válido.");
            return;
        }
        if (pass.isEmpty()) {
            show("A palavra-passe é obrigatória.");
            return;
        }
        if (!pass.equals(confirm)) {
            show("As palavras-passe não coincidem.");
            return;
        }

        // TODO: Fazer chamada Retrofit quando o endpoint de registo estiver disponível
        show("Conta criada com sucesso (simulado).");
        finish();
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
