package com.thpttranquangkhai.arreveal.Models;

public class Subject {
    private String id;
    private String name;
    private String image;
    private String idTeacher;
    private String idSchool;

    public Subject(String id, String name, String image, String idTeacher, String idSchool) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.idTeacher = idTeacher;
        this.idSchool = idSchool;
    }

    public String getIdTeacher() {
        return idTeacher;
    }

    public void setIdTeacher(String idTeacher) {
        this.idTeacher = idTeacher;
    }

    public String getIdSchool() {
        return idSchool;
    }

    public void setIdSchool(String idSchool) {
        this.idSchool = idSchool;
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
}
