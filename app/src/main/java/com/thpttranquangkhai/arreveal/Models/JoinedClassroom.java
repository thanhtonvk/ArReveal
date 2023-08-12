package com.thpttranquangkhai.arreveal.Models;

public class JoinedClassroom {
    private String id, name, hostName, idHost;

    public JoinedClassroom() {

    }

    public JoinedClassroom(String id, String name, String hostName, String idHost) {
        this.id = id;
        this.name = name;
        this.hostName = hostName;
        this.idHost = idHost;
    }

    public String getIdHost() {
        return idHost;
    }

    public void setIdHost(String idHost) {
        this.idHost = idHost;
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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
