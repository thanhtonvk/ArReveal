package com.thpttranquangkhai.arreveal.Models;

import android.graphics.Bitmap;

public class Entity {
    private int id;
    private String name;
    private String path_file_online;
    private String image_online;
    private int type;
    public static Bitmap bitmap;

    public Entity() {
    }

    public Entity(int id, String name, int type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Entity(int id, String name, String path_file_online, String image_online, int type) {
        this.id = id;
        this.name = name;
        this.path_file_online = path_file_online;
        this.image_online = image_online;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPath_file_online() {
        return path_file_online;
    }

    public void setPath_file_online(String path_file_online) {
        this.path_file_online = path_file_online;
    }

    public String getImage_online() {
        return image_online;
    }

    public void setImage_online(String image_online) {
        this.image_online = image_online;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
