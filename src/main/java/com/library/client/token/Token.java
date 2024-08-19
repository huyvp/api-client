package com.library.client.token;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Token {
    private static final int DEFAULT_EXPIRE_GAP_SECONDS = 10;
    private static long expireGapSeconds = DEFAULT_EXPIRE_GAP_SECONDS;
    private String token_type;
    private volatile String access_token;
    private String scope;
    private long expires_in = 0;
    private long consented_on = 0;  // Thời gian hiệu lực trung của refresh token và Access token
    private volatile String refresh_token;
    private long refresh_token_expires_in = 0;
    private Date validUntil;
    private Date refreshTokenValidUntil;
    private Date bothValidUntil;

    public static String getDisplayFormat(String token) {
        return token.length() < 10 ? token : String.format("%s....%s", token.substring(0, 5), token.substring(token.length() - 5));
    }
    public static Token from(String jsonString) {
        Token token = new Token();

        jsonString = jsonString.replace("\\n", "").replace("\\t", "").replaceAll("\\s+", "");
        Pattern pattern = Pattern.compile("\"(\\w+)\":\"([^\"]+)\"|\"(\\w+)\":(\\d+)");
        Matcher matcher = pattern.matcher(jsonString);

        String field;
        String value;

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                field = matcher.group(1);
                value = matcher.group(2);
            } else if (matcher.group(3) != null) {
                field = matcher.group(3);
                value = matcher.group(4);
            } else {
                field = null;
                value = null;
            }

            if ("token_type".equalsIgnoreCase(field)) {
                token.setToken_type(value);
            } else if ("access_token".equalsIgnoreCase(field)) {
                token.setAccess_token(value);
            } else if ("scope".equalsIgnoreCase(field)) {
                token.setScope(value);
            } else if ("expires_in".equalsIgnoreCase(field)) {
                token.setExpires_in(Long.parseLong(value));
            } else if ("consented_on".equalsIgnoreCase(field)) {
                token.setConsented_on(Long.parseLong(value));
            } else if ("refresh_token".equalsIgnoreCase(field)) {
                token.setRefresh_token(value);
            } else if ("refresh_token_expires_in".equalsIgnoreCase(field)) {
                token.setRefresh_token_expires_in(Long.parseLong(value));
            }
        }

        if (!token.isValid()) {
            throw new RuntimeException("Invalid token string. input='" + jsonString + ";");
        }

        return token;
    }

    private boolean isValid() {
        if (access_token == null
                || access_token.isEmpty() || expires_in == 0
                || refresh_token == null || refresh_token.isEmpty()
                || refresh_token_expires_in == 0 || token_type == null
                || token_type.isEmpty() || scope == null
                || scope.isEmpty() || consented_on == 0)
            return false;
        return true;
    }

    public boolean isExpired() {
        Date now = new Date();
        return now.after(validUntil) || now.after(bothValidUntil);
    }

    public boolean refreshTokenIsExpired(){
        Date now = new Date();
        return now.after(refreshTokenValidUntil) || now.after(bothValidUntil);
    }

    public static void setExpireGapSeconds(long expireGapSeconds) {
        Token.expireGapSeconds = expireGapSeconds;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }

    public void setExpires_in(long expires_in) {

        this.expires_in = expires_in;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public void setConsented_on(long consented_on) {
        setBothValidUntil(consented_on);
        this.consented_on = consented_on;
    }

    public long getConsented_on() {
        return consented_on;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token_expires_in(long refresh_token_expires_in) {
        setRefreshTokenValidUntil(refresh_token_expires_in);
        this.refresh_token_expires_in = refresh_token_expires_in;
    }

    public long getRefresh_token_expires_in() {
        return refresh_token_expires_in;
    }

    @Override
    public String toString() {
        return "Token [token_type=" + token_type + ", access_token=" + access_token + ", scope=" + scope
                + ", expires_in=" + expires_in + ", consented_on=" + consented_on + ", refresh_token=" + refresh_token
                + ", refresh_token_expires_in=" + refresh_token_expires_in + ", validUntil=" + validUntil
                + ", refreshTokenValidUntil=" + refreshTokenValidUntil + ", isExpired()=" + isExpired()
                + ", isRefreshTokenExpired()=" + refreshTokenIsExpired() + "]";
    }

    private void setValidUntil(long validUntilSecond) {
        this.validUntil = getDateAfterSeconds(validUntilSecond - expireGapSeconds);
    }

    private void setRefreshTokenValidUntil(long validUntilSeconds) {
        this.refreshTokenValidUntil = getDateAfterSeconds(validUntilSeconds - expireGapSeconds);
    }

    private void setBothValidUntil(long validUntilSeconds) {
        this.bothValidUntil = getDateAfterSeconds(validUntilSeconds - expireGapSeconds);
    }

    private static Date getDateAfterSeconds(long seconds) {
        return new Date(new Date().getTime() + (seconds * 1000));
    }
}
