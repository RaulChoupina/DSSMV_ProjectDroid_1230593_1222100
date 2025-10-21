// RegisterActivity.java
package com.example.projdroid;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projdroid.data.ClientData;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtEmail, edtPass, edtPass2;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPass  = findViewById(R.id.edtPassword);
        edtPass2 = findViewById(R.id.edtConfirmPassword);
        Button btn = findViewById(R.id.btnRegister);

        btn.setOnClickListener(v -> {
            String email = t(edtEmail).toLowerCase().trim();
            String p1 = t(edtPass);
            String p2 = t(edtPass2);

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { msg("Email inválido"); return; }
            if (p1.isEmpty() || !p1.equals(p2)) { msg("Passwords não coincidem"); return; }

            boolean ok = ClientData.registar(this, email, p1);
            if (ok) { msg("Conta criada!"); finish(); }
            else    { msg("Já existe uma conta com esse email."); }
        });
    }
    private String t(EditText e){ return e.getText()==null? "": e.getText().toString(); }
    private void msg(String s){ Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
