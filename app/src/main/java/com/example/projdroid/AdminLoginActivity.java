package com.example.projdroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    // Credenciais de exemplo (substitui por validação real quando tiveres backend/DB)
    private static final String ADMIN_EMAIL = "admin1@gmail.com";
    private static final String ADMIN_PASS  = "1234";

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Ligações às views
        setContentView(R.layout.activity_admin_login);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);


        // Clicar no botão → tentar login
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Carregar "Enter/Done" no teclado na password → tentar login
        edtPassword.setOnEditorActionListener((v, actionId, event) -> {
            boolean imeDone = actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO;
            boolean enterKey = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN;
            if (imeDone || enterKey) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void attemptLogin() {
        String email = safeText(edtEmail);
        String pass  = safeText(edtPassword);

        // Esconde o teclado para melhor UX
        hideKeyboard();

        // Validação de formato básico do email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            show("Insere um email válido.");
            edtEmail.requestFocus();
            return;
        }

        // Password obrigatória
        if (pass.isEmpty()) {
            show("A palavra-passe é obrigatória.");
            edtPassword.requestFocus();
            return;
        }

        // Verificação simples das credenciais do admin (mock)
        if (email.equalsIgnoreCase(ADMIN_EMAIL) && pass.equals(ADMIN_PASS)) {
            show("Login de administrador bem-sucedido!");
            goToDashboard();
        } else {
            show("Credenciais incorretas. Tente novamente.");
        }
    }

    private void goToDashboard() {
        // Garante que tens uma Activity chamada AdminDashboardActivity declarada no Manifest
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        startActivity(intent);
        finish(); // fecha o ecrã de login para não voltar atrás com back
    }

    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
