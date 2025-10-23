package com.example.projdroid.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Library {

    @SerializedName("id")
    private String id;

    @SerializedName("address")
    private String address;

    @SerializedName("name")
    private String name;

    @SerializedName("open")
    private boolean open;

    @SerializedName("openDays")
    private String openDays;

    @SerializedName("openStatement")
    private String openStatement;

    @SerializedName("openTime")
    private String openTime;

    @SerializedName("closeTime")
    private String closeTime;

    @SerializedName("books")
    private List<Book> books = new ArrayList<>();

    // Constructors
    public Library(String address, String name, boolean open, String openDays, String openStatement, String openTime, String closeTime) {
        this.address = address;
        this.name = name;
        this.open = open;
        this.openDays = openDays;
        this.openStatement = openStatement;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public Library() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getOpenDays() {
        return openDays;
    }

    public void setOpenDays(String openDays) {
        this.openDays = openDays;
    }

    public String getOpenStatement() {
        return openStatement;
    }

    public void setOpenStatement(String openStatement) {
        this.openStatement = openStatement;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return "Library{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", open=" + open +
                ", openDays='" + openDays + '\'' +
                ", openStatement='" + openStatement + '\'' +
                ", openTime='" + openTime + '\'' +
                ", closeTime='" + closeTime + '\'' +
                ", books=" + books +
                '}';
    }
}
