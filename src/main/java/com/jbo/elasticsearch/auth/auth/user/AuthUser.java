package com.jbo.elasticsearch.auth.auth.user;

public final class AuthUser {

    private final String username;
    private final String password;

    public AuthUser(final String username, final String password) {
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
