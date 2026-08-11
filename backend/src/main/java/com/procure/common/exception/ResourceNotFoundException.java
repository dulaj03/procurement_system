package com.procure.common.exception;

import java.util.UUID;

/**
 * Thrown when a requested resource cannot be found in the database.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(resource + " not found: " + identifier);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
