package com.example.projdroid.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Book {
    @SerializedName(value = "title", alternate = {"name"})
    private String title;

    @SerializedName("authors")
    private List<Author> authors;

    @SerializedName(value = "author_name", alternate = {"author_names"})
    private List<String> authorNames;

    @SerializedName(value = "author", alternate = {"authorName"})
    private String authorSingle;

    @SerializedName(value = "isbn", alternate = {"isbn13", "isbn_13", "isbn10", "isbn_10"})
    private String isbn;

    @SerializedName(value = "description", alternate = {"summary"})
    private String description;

    // Caso o backend já envie o nome diretamente como string
    @SerializedName(value = "image", alternate = {"thumbnail", "thumb"})
    private String image;

    // Caso o backend envie "cover" como OBJETO
    @SerializedName("cover")
    private Cover cover;

    // ------ getters/setters já existentes (mantém os teus) ------
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }

    public List<String> getAuthorNames() { return authorNames; }
    public void setAuthorNames(List<String> authorNames) { this.authorNames = authorNames; }

    public String getAuthorSingle() { return authorSingle; }
    public void setAuthorSingle(String authorSingle) { this.authorSingle = authorSingle; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // ======== Capa ========
    /** Devolve o nome/URL da capa, venha como string em "image" ou dentro do objeto "cover". */
    public String getImage() {
        if (image != null && !image.isEmpty()) return image;
        if (cover != null) return cover.best();
        return null;
    }

    public void setImage(String image) { this.image = image; }
    public Cover getCover() { return cover; }
    public void setCover(Cover cover) { this.cover = cover; }

    // Objeto flexível para vários formatos de "cover"
    public static class Cover {
        @SerializedName(value = "image",    alternate = {"file", "fileName", "filename", "name"})
        private String imageName;

        // alguns backends mandam um URL completo
        @SerializedName(value = "url", alternate = {"href", "path"})
        private String url;

        public String best() {
            // Se já vier um URL completo, usa-o
            if (url != null && !url.isEmpty()) return url;
            // Caso contrário, é o nome do ficheiro (ex.: "9789720046104.jpg")
            return imageName;
        }

        public String getImageName() { return imageName; }
        public String getUrl() { return url; }
    }
    // Mostra um autor "bonitinho" a partir dos vários campos possíveis
    public String getAuthorDisplay() {
        if (authors != null && !authors.isEmpty() && authors.get(0) != null) {
            String n = authors.get(0).getName();
            if (n != null && !n.isEmpty()) return n;
        }
        if (authorNames != null && !authorNames.isEmpty()) {
            String n = authorNames.get(0);
            if (n != null && !n.isEmpty()) return n;
        }
        if (authorSingle != null && !authorSingle.isEmpty()) {
            return authorSingle;
        }
        return "Unknown Author";
    }

}
