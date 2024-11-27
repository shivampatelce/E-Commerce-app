package com.example.project_2;

import java.util.List;

public class Product {
    private String title;
    private String description;
    private String subDescription;
    private Double price;
    private String imageURL;
    private List<String> subImages;

    public Product(String title, String description, String subDescription, Double price, String imageURL, List<String> subImages) {
        this.title = title;
        this.description = description;
        this.subDescription = subDescription;
        this.price = price;
        this.imageURL = imageURL;
        this.subImages = subImages;
    }

    public String getSubDescription() {
        return subDescription;
    }

    public void setSubDescription(String subDescription) {
        this.subDescription = subDescription;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public List<String> getSubImages() {
        return subImages;
    }

    public void setSubImages(List<String> subImages) {
        this.subImages = subImages;
    }

    @Override
    public String toString() {
        return "Product{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", subDescription='" + subDescription + '\'' +
                ", price=" + price +
                '}';
    }
}
