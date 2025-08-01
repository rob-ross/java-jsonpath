package org.killeroonie.jsonpath.exception;

/**
 * An exception raised when a JSON Patch _test_ op fails.
 */
public class JSONPatchTestFailureException extends JSONPatchException {
    public JSONPatchTestFailureException(String message) {
        super(message);
    }

    public JSONPatchTestFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}