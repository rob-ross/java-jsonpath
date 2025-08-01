package org.killeroonie.jsonpath.exception;
/**
 * Base class for all JSON Pointer exceptions.
 */
public class JSONPointerException extends RuntimeException {
    public JSONPointerException(String message) {
        super(message);
    }

    public JSONPointerException(String message, Throwable cause) {
        super(message, cause);
    }
}