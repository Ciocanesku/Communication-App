package com.communication.sockets.var2;

import java.io.Serializable;

public class ClientData implements Serializable {
    private String username;
    private String password;

    public ClientData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}