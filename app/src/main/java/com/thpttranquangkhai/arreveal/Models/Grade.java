package com.thpttranquangkhai.arreveal.Models;

public class Grade {
    private String id;
    private int name;
    private String idSchool;

    public Grade(String id, int name, String idSchool) {
        this.id = id;
        this.name = name;
        this.idSchool = idSchool;
    }
    public Grade(){}
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public String getIdSchool() {
        return idSchool;
    }

    public void setIdSchool(String idSchool) {
        this.idSchool = idSchool;
    }
}
