package com.example.projdroid.models;

public class CreateReviewRequest {

    public String review;
    public boolean recommended;

    public CreateReviewRequest() { }

    public CreateReviewRequest(String review, boolean recommended) {
        this.review = review;
        this.recommended = recommended;
    }
}
