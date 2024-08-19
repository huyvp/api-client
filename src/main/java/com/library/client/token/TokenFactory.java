package com.library.client.token;

import com.library.client.config.AuthInfo;
import com.library.client.config.ConfigRegistry;
import com.library.client.restclient.RestClient;
import com.library.client.restclient.TokenRestClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.library.client.restclient.RestClient.AUTH_PREFIX_BEARER;

public class TokenFactory {
    private static RestClient tokenRestClient = new TokenRestClient();
    private static List<String> regions;

    private static final ConcurrentMap<String, Token> tokens = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Lock> readLocks= new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Lock> writeLocks = new ConcurrentHashMap<>();

    private static final String REQUEST_BODY_TEMPLATE_TO_CREATE_TOKEN = "grant_type=password&username=%s&password=%s&scope=%s";
    private static final String REQUEST_BODY_TEMPLATE_TO_REFRESH_TOKEN = "grant_type=refresh_token&refresh_token=%s";

    public static void init() {
        readLocks.clear();
        writeLocks.clear();

        regions = ConfigRegistry.getRegions();
        for (String region: regions){
            ReadWriteLock lock = new ReentrantReadWriteLock();
            readLocks.put(region, lock.readLock());
            writeLocks.put(region, lock.writeLock());
        }

        tokens.clear();
    }

    public static void setTokenRestClient(RestClient restClient) {
        TokenFactory.tokenRestClient = restClient;
    }

    public static String getAuthorizationValue(String region) {
        return AUTH_PREFIX_BEARER + TokenFactory.getAccessToken(region);
    }

    public static String getAccessToken(String region) {
        checkRegionParam(region);
        Token token = tokens.get(region);
        if (token == null || token.isExpired()) {
            handleToken(region);
        }
        readLocks.get(region).lock();
        tokens.put(region, token);

        try {
            return tokens.get(region).getAccess_token();
        } finally {
            readLocks.get(region).unlock();
        }
    }

    private static void checkRegionParam(String region) {
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("Region can not be null or empty string.");
        } else if (!regions.contains(region)) {
            throw new IllegalArgumentException("Invalid region : " + region);
        }
    }
    private static void handleToken(String region){
        checkRegionParam(region);

        if (writeLocks.get(region).tryLock()){
            Token token = tokens.get(region);
            try {
                AuthInfo authInfo = ConfigRegistry.getAuthInfo(region);
                if (token == null || token.refreshTokenIsExpired()) {
                    token = createToken(authInfo);
                } else {
                    token = refreshToken(authInfo, token);
                }
                tokens.put(region, token);
            } finally {
                writeLocks.get(region).unlock();
            }
        }
    }

    private static Token createToken(AuthInfo authInfo) {
        try {
            String reqBody = String.format(REQUEST_BODY_TEMPLATE_TO_CREATE_TOKEN,
                    URLEncoder.encode(authInfo.getUserName(), "UTF-8"),
                    URLEncoder.encode(authInfo.getUserPassword(), "UTF-8"),
                    URLEncoder.encode(authInfo.getScope(), "UTF-8")
            );
            String authString = authInfo.getClientId() + ":" + authInfo.getClientSecret();
            return Token.from(tokenRestClient.sendRequest(authInfo.getTokenApiUrl(), "POST", authString, reqBody));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode request body.");
        }
    }

    private static Token refreshToken(AuthInfo authInfo, Token oldToken) {
        try {
            String reqBody = String.format(REQUEST_BODY_TEMPLATE_TO_REFRESH_TOKEN,
                    URLEncoder.encode(oldToken.getRefresh_token(), "UTF-8")
            );
            String authString = authInfo.getClientId() + ":" + authInfo.getClientSecret();
            return Token.from(tokenRestClient.sendRequest(authInfo.getTokenApiUrl(), "POST", authString, reqBody));
        } catch (UnsupportedEncodingException e) {
            if (isRefreshTokenExpireError(e)) {
                return createToken(authInfo);
            }
            throw new RuntimeException(e);
        }
    }
    private static boolean isRefreshTokenExpireError(Exception e) {
        return e.getMessage().startsWith("400");
    }
}
