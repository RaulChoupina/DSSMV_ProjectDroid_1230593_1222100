package com.example.projdroid.models;

import com.google.gson.annotations.SerializedName;

public class BookItem {
    @SerializedName("title")   public String title;
    @SerializedName("author")  public String author;
    @SerializedName("dueDate") public String dueDate;



    // LINHA ADICIONADA:
    // Precisamos disto para ir buscar a capa
    @SerializedName("isbn")    public String isbn;

    @SerializedName("book")
    private Book book;
    public Book getBook() {
        return book;
    }
}