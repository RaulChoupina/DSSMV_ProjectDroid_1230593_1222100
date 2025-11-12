// com/example/projdroid/models/RecommendedCountResponse.java
package com.example.projdroid.models;

import com.google.gson.annotations.SerializedName;

public class RecommendedCountResponse {
    @SerializedName("recommendedCount")
    private int recommendedCount;

    public int getRecommendedCount() {
        return recommendedCount;
    }
}
