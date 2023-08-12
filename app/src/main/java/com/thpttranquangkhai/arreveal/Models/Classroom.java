package com.thpttranquangkhai.arreveal.Models;

public class Classroom {
    private String id, class_name, host_name;
    private String idHost;

    public Classroom() {

    }

    public Classroom(String id, String class_name, String host_name, String idHost) {
        this.id = id;
        this.class_name = class_name;
        this.host_name = host_name;
        this.idHost = idHost;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getHost_name() {
        return host_name;
    }

    public void setHost_name(String host_name) {
        this.host_name = host_name;
    }

    public String getIdHost() {
        return idHost;
    }

    public void setIdHost(String idHost) {
        this.idHost = idHost;
    }
}
