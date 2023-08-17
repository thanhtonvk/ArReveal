package com.thpttranquangkhai.arreveal.Models;

public class School {
    private String id;
    private String name, address, avatar;
    private String phoneNumber;
    private String idAccount;

    public School(String id, String name, String address, String avatar, String phoneNumber, String idAccount) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.avatar = avatar;
        this.phoneNumber = phoneNumber;
        this.idAccount = idAccount;
    }

    public String getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(String idAccount) {
        this.idAccount = idAccount;
    }

    public School() {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
