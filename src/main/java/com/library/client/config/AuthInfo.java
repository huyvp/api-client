package com.library.client.config;

public class AuthInfo {
    private String clientId;
    private String clientSecret;
    private String userName;
    private String userPassword;
    private String tokenApiUrl;
    private String scope;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getTokenApiUrl() {
        return tokenApiUrl;
    }

    public void setTokenApiUrl(String tokenApiUrl) {
        this.tokenApiUrl = tokenApiUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
