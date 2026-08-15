package com.example.catalogueservice.exception;

public class StalePartVersionException extends RuntimeException {

    public StalePartVersionException(String message) {
        super(message);
    }
}
