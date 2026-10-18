package com.lostandfound.model;

import java.sql.Timestamp;

// 'items' table
public class Item {

    private int itemId;
    private String title;
    private String description;
    private String location;
    private String status;
    private byte[] imageData; // nullable – for optional image uploads. Stores the image in memory
    private int categoryId;
    private int reportedByUserId;
    private Timestamp dateReported;

    // temporary display helpers (populated by JOINs)
    private String categoryName;
    private String reporterName;
    private String reporterEmail;

    public Item() {
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getReportedByUserId() {
        return reportedByUserId;
    }

    public void setReportedByUserId(int userId) {
        this.reportedByUserId = userId;
    }

    public Timestamp getDateReported() {
        return dateReported;
    }

    public void setDateReported(Timestamp dateReported) {
        this.dateReported = dateReported;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    @Override
    public String toString() {
        return "[" + status + "] " + title;
    }
}
