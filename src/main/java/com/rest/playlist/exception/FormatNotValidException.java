package com.rest.playlist.exception;


/**
 * FormatNotValidException class extends RuntimeException.
 * It's about a custom exception :
 * throwing an exception for format not valid in Spring Boot Service
 * eg pitch is null
 * FormatNotValidException is thrown with Http 400
 */

public class FormatNotValidException extends RuntimeException {
    public FormatNotValidException(String message) {
        super(message);
    }
}
