package com.example.styleswipe;

public class GalleryItem {
    public String date;
    public model outfit;
    public boolean isHeader;

    public GalleryItem(String date) { 
        this.date = date; 
        this.isHeader = true; 
    }
    
    public GalleryItem(model outfit) { 
        this.outfit = outfit; 
        this.isHeader = false; 
    }
}
