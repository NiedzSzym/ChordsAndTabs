package com.chordsandtabs.dto.security;

import com.fasterxml.jackson.annotation.JsonSetter;

public class LoginRequest {

    private String email;

    private char[] password;

    public LoginRequest() {}

    @JsonSetter("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonSetter("password")
    public void setPasswordFromString(String password) {
        this.password = password != null ? password.toCharArray() : null;
    }

    public String email() {
        return email;
    }

    public char[] password() {
        return password;
    }
}
