package org.killeroonie.jsonpath.exception;

/**
 * An exception raised when an array index is out of range.
 */
public class JSONPointerIndexException extends JSONPointerResolutionException {
    public JSONPointerIndexException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "pointer index exception: " + super.getMessage();
    }
}
