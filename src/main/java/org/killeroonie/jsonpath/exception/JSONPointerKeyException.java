package org.killeroonie.jsonpath.exception;

/**
 * An exception raised when a pointer references a mapping with a missing key.
 */
public class JSONPointerKeyException extends JSONPointerResolutionException {
    public JSONPointerKeyException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "pointer key exception: " + super.getMessage();
    }
}
