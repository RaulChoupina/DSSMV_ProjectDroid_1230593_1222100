package com.example.projdroid;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projdroid.repository.LibraryRepository;
import java.util.ArrayList;
import java.util.List;

public class MyReserveActivity extends AppCompatActivity {

    private ListView list;
    private ArrayAdapter<String> adapter;
    private final LibraryRepository repo = new LibraryRepository();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reserve);

        String email = getIntent().getStringExtra("cliente_email");
        String userId = (email==null||email.isEmpty()) ? "anon" : email;

        list = findViewById(R.id.listLoans);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(adapter);

        repo.getCheckedOut(userId, new LibraryRepository.Callback<List<CheckedOutBook>>() {
            @Override public void onSuccess(List<CheckedOutBook> data) {
                adapter.clear();
                for (CheckedOutBook b : data) {
                    String title = b.book!=null ? b.book.title : "(sem título)";
                    String due = b.dueDate!=null ? b.dueDate : "";
                    adapter.add(title + " • Devolver até: " + due);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(MyReserveActivity.this, "Erro: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
