package com.example.projdroid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnUsers, btnReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnUsers   = findViewById(R.id.btnUsers);
        btnReports = findViewById(R.id.btnReports);

        btnUsers.setOnClickListener(v -> Toast.makeText(this, "Abrir gestão de utilizadores (TODO)", Toast.LENGTH_SHORT).show());
        btnReports.setOnClickListener(v -> Toast.makeText(this, "Abrir relatórios (TODO)", Toast.LENGTH_SHORT).show());
    }
}
