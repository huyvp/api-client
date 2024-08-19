package com.library.client.exception;

public class SSLException extends RuntimeException {
    private static final long serialVersionUID = -5064474453848143216L;

    public SSLException(String message, Throwable cause) {
        super(message, cause);
    }
}
