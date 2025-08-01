package org.killeroonie.jsonpath.exception;
/**
 * Base exception for those that can be raised during pointer resolution.
 */
public class JSONPointerResolutionException extends JSONPointerException {
    public JSONPointerResolutionException(String message) {
        super(message);
    }

    public JSONPointerResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
