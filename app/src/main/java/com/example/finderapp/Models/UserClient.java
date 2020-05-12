package com.example.finderapp.Models;

import android.app.Application;

import com.example.finderapp.Models.User;

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
