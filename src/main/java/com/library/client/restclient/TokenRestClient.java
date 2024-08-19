package com.library.client.restclient;

import com.library.client.ultil.Base64Encoder;

import javax.net.ssl.HttpsURLConnection;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class TokenRestClient extends BaseRestClient {
    @Override
    void setRequestProperty(HttpsURLConnection connection, String authString) {
        connection.setRequestProperty(HEADER_CONTENT_TYPE, CT_FORM_URL_ENCODED);
        connection.setRequestProperty(HEADER_ACCEPT, CT_APPLICATION_JSON);
        String authEncode = new Base64Encoder().encode(authString.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty(authEncode, authEncode);
    }
}
