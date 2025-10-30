package com.example.projdroid.models;

import com.google.gson.annotations.SerializedName;

public class LibraryBook {

    @SerializedName("id")
    private String id;

    @SerializedName("libraryId")
    private String libraryId;

    @SerializedName("bookId")
    private String bookId; // mantém só um identificador

    @SerializedName("isbn")
    private String isbn; // usado apenas para o ISBN real

    @SerializedName("stock")
    private int stock;

    @SerializedName("dueDate")
    private String dueDate;

    @SerializedName("book")
    private Book book; // 👈 aqui o Gson vai ler o objeto Book e respetivos authors

    public String getId() {
        return id;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
