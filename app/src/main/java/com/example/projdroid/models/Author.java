package com.example.projdroid.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Author {
    @SerializedName("alternate_names")
    private List<String> alternateNames;

    @SerializedName("bio")
    private String bio;

    @SerializedName("birth_date")
    private String birthDate;

    @SerializedName("death_date")
    private String deathDate;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    public Author(List<String> alternateNames, String bio, String birthDate, String deathDate, String id, String name) {
        this.alternateNames = alternateNames;
        this.bio = bio;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.id = id;
        this.name = name;
    }

    public List<String> getAlternateNames() {
        return alternateNames;
    }

    public String getBio() {
        return bio;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getDeathDate() {
        return deathDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
