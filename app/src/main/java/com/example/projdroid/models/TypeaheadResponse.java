package com.example.projdroid.models;

import java.util.List;

public class TypeaheadResponse {
    public List<String> suggestions;

    public TypeaheadResponse() { }

    public TypeaheadResponse(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
