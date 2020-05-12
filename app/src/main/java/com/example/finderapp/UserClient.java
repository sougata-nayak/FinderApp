package com.example.finderapp;

import android.app.Application;

public class UserClient extends Application {

    User user;

    public UserClient(User user) {
        this.user = user;
    }

    public UserClient() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
