package com.example.dreamscopebackend.exception;

public class DreamNotFoundException extends RuntimeException {
    public DreamNotFoundException(String message) {
        super(message);
    }
}
