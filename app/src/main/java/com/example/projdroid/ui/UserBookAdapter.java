package com.example.projdroid.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.projdroid.R;
import com.example.projdroid.models.Book;
import com.example.projdroid.models.BookItem;

import java.util.List;

public class UserBookAdapter extends ArrayAdapter<BookItem> {

    private final String dueDatePrefix; // "Devolve até: " ou "Data limite: "

    public UserBookAdapter(@NonNull Context context, @NonNull List<BookItem> objects, @NonNull String dueDatePrefix) {
        super(context, 0, objects);
        this.dueDatePrefix = dueDatePrefix;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // 1. Infla o layout
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_user_book, parent, false);
        }

        // 2. Apanha os Views do layout
        ImageView coverImageView = itemView.findViewById(R.id.bookCover);
        TextView titleTextView = itemView.findViewById(R.id.bookTitle);
        TextView authorTextView = itemView.findViewById(R.id.bookAuthor);
        TextView dueDateTextView = itemView.findViewById(R.id.bookDueDate);

        // 3. Obtém o item atual
        BookItem currentItem = getItem(position);

        // 4. Preenche os dados
        if (currentItem != null) {
            // Preenche os textos
            titleTextView.setText(currentItem.title != null ? currentItem.title : "(Sem título)");
            authorTextView.setText(currentItem.author != null ? currentItem.author : "(Sem autor)");

            // Adiciona o prefixo correto à data
            String dueDateStr = dueDatePrefix + (currentItem.dueDate != null ? currentItem.dueDate.split("T")[0] : "(data indisponível)");
            dueDateTextView.setText(dueDateStr);
            String isbn = currentItem.isbn;
            Book book = currentItem.getBook();

            // 5. Carrega a imagem com o Glide
            if ((isbn == null || isbn.isEmpty()) && book != null) {
                isbn = book.getIsbn();
            }
            // ASSUMINDO que o teu BookItem tem um campo 'isbn'
            if (currentItem.isbn != null && !currentItem.isbn.isEmpty()) {
                String coverUrl = "http://193.136.62.24/v1/assets/cover/" + currentItem.isbn + "-S.jpg";
                Glide.with(getContext())
                        .load(coverUrl)
                        .placeholder(R.drawable.cover_placeholder)
                        .error(R.drawable.cover_error)
                        .into(coverImageView);
            } else {
                // Se não houver ISBN, usa o placeholder
                coverImageView.setImageResource(R.drawable.cover_placeholder);
            }
        }

        return itemView;
    }
}