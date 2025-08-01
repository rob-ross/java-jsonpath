package org.killeroonie.jsonpath.exception;

/**
 * Base class for all JSON Patch exceptions.
 */
public class JSONPatchException extends RuntimeException {
    public JSONPatchException(String message) {
        super(message);
    }

    public JSONPatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
