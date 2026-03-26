package com.dianping.ai.util;

import java.util.Objects;

public class ParsedIntent {
    private String cuisine;
    private String priceRange;
    private String scene;
    private String location;
    private double confidence;

    public ParsedIntent() {
    }

    public ParsedIntent(String cuisine, String priceRange, String scene, String location, double confidence) {
        this.cuisine = cuisine;
        this.priceRange = priceRange;
        this.scene = scene;
        this.location = location;
        this.confidence = confidence;
    }

    public static ParsedIntent empty() {
        return new ParsedIntent(null, null, null, null, 0.0);
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public boolean hasAnyPreference() {
        return cuisine != null || priceRange != null || scene != null || location != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedIntent that = (ParsedIntent) o;
        return Double.compare(that.confidence, confidence) == 0 &&
                Objects.equals(cuisine, that.cuisine) &&
                Objects.equals(priceRange, that.priceRange) &&
                Objects.equals(scene, that.scene) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cuisine, priceRange, scene, location, confidence);
    }

    @Override
    public String toString() {
        return "ParsedIntent{" +
                "cuisine='" + cuisine + '\'' +
                ", priceRange='" + priceRange + '\'' +
                ", scene='" + scene + '\'' +
                ", location='" + location + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
