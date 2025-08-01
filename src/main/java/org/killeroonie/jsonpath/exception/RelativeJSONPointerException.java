package org.killeroonie.jsonpath.exception;

/**
 * Base class for all Relative JSON Pointer exceptions.
 */
public class RelativeJSONPointerException extends RuntimeException {
    public RelativeJSONPointerException(String message) {
        super(message);
    }

    public RelativeJSONPointerException(String message, Throwable cause) {
        super(message, cause);
    }
}