package com.example.projdroid.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projdroid.R;
import com.example.projdroid.models.Book;
import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.VH> {

    public interface OnBookClick {
        void onClick(Book book);
    }

    private final List<Book> items = new ArrayList<>();
    private final OnBookClick listener;

    public BookAdapter(OnBookClick listener) { this.listener = listener; }

    public void submit(List<Book> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Book b = items.get(pos);
        h.title.setText(b.title == null ? "(Sem título)" : b.title);
        String author = (b.authors != null && !b.authors.isEmpty()) ? b.authors.get(0).name : "Autor desconhecido";
        h.author.setText(author);
        h.isbn.setText(b.isbn == null ? "" : "ISBN: " + b.isbn);
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(b); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, author, isbn;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            author = v.findViewById(R.id.tvAuthor);
            isbn = v.findViewById(R.id.tvIsbn);
        }
    }
}
