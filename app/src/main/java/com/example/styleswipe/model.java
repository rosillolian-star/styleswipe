package com.example.styleswipe;

import android.graphics.Bitmap;

public class model {
    private int id;
    private String event;
    private String location;
    private String tags;
    private String notes;
    private String imageUri;
    private Bitmap imageBitmap;
    private String dateTaken;
    private int albumId;

    public model(int id, String event, String location, String tags, String notes, 
                 String imageUri, Bitmap imageBitmap, String dateTaken, int albumId) {
        this.id = id;
        this.event = event;
        this.location = location;
        this.tags = tags;
        this.notes = notes;
        this.imageUri = imageUri;
        this.imageBitmap = imageBitmap;
        this.dateTaken = dateTaken;
        this.albumId = albumId;
    }

    // Getters
    public int getId() { return id; }
    public String getEvent() { return event; }
    public String getLocation() { return location; }
    public String getTags() { return tags; }
    public String getNotes() { return notes; }
    public String getImageUri() { return imageUri; }
    public Bitmap getImageBitmap() { return imageBitmap; }
    public String getDateTaken() { return dateTaken; }
    public int getAlbumId() { return albumId; }
}
