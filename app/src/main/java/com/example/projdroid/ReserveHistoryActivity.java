package com.example.projdroid;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projdroid.repository.LibraryRepository;
import java.util.ArrayList;
import java.util.List;

public class ReserveHistoryActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView list = new ListView(this);
        setContentView(list);

        String email = getIntent().getStringExtra("cliente_email");
        String userId = (email==null||email.isEmpty()) ? "anon" : email;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(adapter);

        new LibraryRepository().getCheckoutHistory(userId, new LibraryRepository.Callback<List<CheckedOutBook>>() {
            @Override public void onSuccess(List<CheckedOutBook> data) {
                adapter.clear();
                for (CheckedOutBook b : data) {
                    String title = b.book!=null ? b.book.title : "(sem título)";
                    String date = b.updateTimestamp!=null ? b.updateTimestamp : "";
                    adapter.add(title + " • " + (b.active ? "Ativo" : "Devolvido") + " • " + date);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(ReserveHistoryActivity.this, "Erro: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
