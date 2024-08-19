package com.library.client.restclient;

public interface RestClient {
    int DEFAULT_TIMEOUT = 3 * 100;
    String HEADER_CONTENT_TYPE = "Content-type";
    String HEADER_ACCEPT = "Accept";
    String HEADER_AUTHORIZATION = "Authorization";
    String HEADER_REFERER = "Referer";


    String CT_APPLICATION_JSON = "application/json";
    String CT_APPLICATION_OCTET_STREAM = "application/octet-stream";
    String CT_MULTIPART_FORM_DATA = "multipart/form-data";
    String CT_FORM_URL_ENCODED = "application/x-www-form-urlencoded";

    String CLIENT_ID = "X-IBM-Client-Id";
    String CLIENT_SECRET = "X-IBM-Client-Secret";

    String MESSAGE_ID = "X-Global-Transaction-ID";
    String AUTH_PREFIX_BEARER = "Bearer ";
    String BACKEND_AUTHORIZATION = "Backend-Authorization";
    String JDBC_ACTION = "action";
    String JDBC_ACTION_RECEIVE = "recv";
    String JDBC_ACTION_SEND = "send";
    String JDBC_CONDITION = "condition";

    String sendRequest(String apiUrl, String method, String authString, String requestBody);
}
