package org.killeroonie.jsonpath.exception;

/**
 * An exception raised when a JSONPathMatch can't be encoded to a JSON Pointer.
 */
public class JSONPointerEncodeException extends JSONPointerException {
    public JSONPointerEncodeException(String message) {
        super(message);
    }

    public JSONPointerEncodeException(String message, Throwable cause) {
        super(message, cause);
    }
}