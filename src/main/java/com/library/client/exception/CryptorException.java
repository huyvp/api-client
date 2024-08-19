package com.library.client.exception;

public class CryptorException extends RuntimeException {
	private static final long serialVersionUID = -5064474453848143216L;

	public CryptorException(String message, Throwable cause) {
        super(message, cause);
    }
}