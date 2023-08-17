package com.thpttranquangkhai.arreveal.Models;

public class Account {
    private String id, email, name;
    private String avatar;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Account(String id, String email, String name, String avatar) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.avatar = avatar;

    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }



    public Account(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Account() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
