package com.example.projdroid.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Book {
    @SerializedName("title")
    private String title;

    @SerializedName("authors")
    private List<Author> authors;

    @SerializedName("isbn")
    private String isbn;

    @SerializedName("description")
    private String description; // Novo campo para a descrição

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getTitle() {return title;}

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }


}
