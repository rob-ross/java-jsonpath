package org.killeroonie.jsonpath.exception;

/**
 * An exception raised when modifying a pointer index out of range.
 */
public class RelativeJSONPointerIndexException extends RelativeJSONPointerException {
    public RelativeJSONPointerIndexException(String message) {
        super(message);
    }
}