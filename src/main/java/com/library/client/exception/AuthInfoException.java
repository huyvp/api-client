package com.library.client.exception;

public class AuthInfoException extends RuntimeException{
    private static final long serialVersionUID = 6157484061994746506L;

    public AuthInfoException(String message, Throwable cause) {
        super(message, cause);
    }
}
