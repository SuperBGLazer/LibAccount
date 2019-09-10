/*
 * Copyright (c) 2019 Breyon Gunn.
 */

package com.ninjamodding.LibAccount;

public class UserAccount implements Account {
    private String firstName;
    private String lastName;
    private String email;
    private String token;
    private int accountID;
    private String pubToken;

    public UserAccount(String firstName, String lastName, String email, int id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.accountID = id;
    }

    public UserAccount() {
    }

    public UserAccount(String firstName, String lastName, String email, String token, String pubToken, int accountID) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.token = token;
        this.pubToken = pubToken;
        this.accountID = accountID;
    }

    public String getPubToken() {
        return pubToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public int getID() {
        return accountID;
    }
}
