package com.geniteam.firebasesignup;

/**
 * Created by 7CT on 2/1/2018.
 */

public class User {
    String name;
    String email;
    String pass;
    String photoUrl;
    String Address;
    String uid;

    public User(String name, String email, String pass, String photoUrl, String address) {
        this.name = name;
        this.email = email;
        this.pass = pass;
        this.photoUrl = photoUrl;
        Address = address;
    }

    public User() {

    }
}
