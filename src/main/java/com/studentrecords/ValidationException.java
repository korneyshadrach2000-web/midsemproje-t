package com.studentrecords;

/**
 * Thrown when user input fails validation.
 */
public class ValidationException extends Exception {
    private final String field;

    public ValidationException(String message) {
        this(message, null);
    }

    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
