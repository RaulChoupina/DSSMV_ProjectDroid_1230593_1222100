package com.example.projdroid.repository;

import com.example.projdroid.api.ApiClient;
import com.example.projdroid.api.LibraryApi;
import com.example.projdroid.models.*;
import java.util.List;
import retrofit2.*;

public class LibraryRepository {
    private final LibraryApi api = ApiClient.get().create(LibraryApi.class);

    public interface Callback<T> { void onSuccess(T data); void onError(Throwable t); }

    public void getLibraries(Callback<List<Library>> cb) {
        api.getLibraries().enqueue(new retrofit2.Callback<List<Library>>() {
            @Override public void onResponse(Call<List<Library>> c, Response<List<Library>> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<Library>> c, Throwable t) { cb.onError(t); }
        });
    }

    public void getBooks(String libraryId, Callback<List<LibraryBook>> cb) {
        api.getBooks(libraryId).enqueue(new retrofit2.Callback<List<LibraryBook>>() {
            @Override public void onResponse(Call<List<LibraryBook>> c, Response<List<LibraryBook>> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new Exception("HTTP " + r.code()));
            }
            @Override public void onFailure(Call<List<LibraryBook>> c, Throwable t) { cb.onError(t); }
        });
    }
}
