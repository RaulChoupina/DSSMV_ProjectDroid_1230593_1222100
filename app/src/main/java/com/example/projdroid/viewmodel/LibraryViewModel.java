package com.example.projdroid.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.projdroid.models.Library;
import com.example.projdroid.models.LibraryBook;
import com.example.projdroid.repository.LibraryRepository;
import java.util.List;

public class LibraryViewModel extends ViewModel {
    private final LibraryRepository repo = new LibraryRepository();
    private final MutableLiveData<List<Library>> libraries = new MutableLiveData<>();
    private final MutableLiveData<List<LibraryBook>> books = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Library>> libraries() { return libraries; }
    public LiveData<List<LibraryBook>> books() { return books; }
    public LiveData<String> error() { return error; }

    public void loadLibraries() {
        repo.getLibraries(new LibraryRepository.Callback<List<Library>>() {
            @Override public void onSuccess(List<Library> data) { libraries.postValue(data); }
            @Override public void onError(Throwable t) { error.postValue(t.getMessage()); }
        });
    }

    public void loadBooks(String libraryId) {
        repo.getBooks(libraryId, new LibraryRepository.Callback<List<LibraryBook>>() {
            @Override public void onSuccess(List<LibraryBook> data) { books.postValue(data); }
            @Override public void onError(Throwable t) { error.postValue(t.getMessage()); }
        });
    }
}
