package com.thpttranquangkhai.arreveal.Models;

public class Subject {
    private String id;
    private String name;
    private String detail;
    private String image;
    private String idTeacher;

    public Subject() {

    }

    public Subject(String id, String name, String detail, String image, String idTeacher) {
        this.id = id;
        this.name = name;
        this.detail = detail;
        this.image = image;
        this.idTeacher = idTeacher;
    }

    public String getIdTeacher() {
        return idTeacher;
    }

    public void setIdTeacher(String idTeacher) {
        this.idTeacher = idTeacher;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
