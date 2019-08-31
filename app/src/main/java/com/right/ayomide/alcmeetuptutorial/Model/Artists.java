package com.right.ayomide.alcmeetuptutorial.Model;

public class Artists {
    private String name, image;

    public Artists() {
    }

    public Artists(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
