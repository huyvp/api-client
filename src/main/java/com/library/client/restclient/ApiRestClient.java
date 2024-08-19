package com.library.client.restclient;

import javax.net.ssl.HttpsURLConnection;

public class ApiRestClient extends BaseRestClient {

    @Override
    void setRequestProperty(HttpsURLConnection connection, String authString) {
        connection.setRequestProperty(HEADER_CONTENT_TYPE, CT_FORM_URL_ENCODED);
        connection.setRequestProperty(HEADER_ACCEPT, CT_APPLICATION_JSON);
        connection.setRequestProperty(HEADER_AUTHORIZATION, AUTH_PREFIX_BEARER + authString);
    }
}
