package org.killeroonie.jsonpath.exception;
/**
 * An exception raised when a pointer resolves a string against a sequence.
 */
public class JSONPointerTypeException extends JSONPointerResolutionException {
    public JSONPointerTypeException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "pointer type exception: " + super.getMessage();
    }
}
