package org.killeroonie.jsonpath.exception;

import org.killeroonie.jsonpath.Token;

/**
 * An exception raised when an array index is out of range.
 */
public class JSONPathIndexException extends JSONPathException {

    /**
     * Constructs a new JSONPathIndexException.
     *
     * @param message The detail message.
     * @param token   The token that caused the exception.
     */
    public JSONPathIndexException(String message, Token token) {
        super(message, token);
    }

    /**
     * Constructs a new JSONPathIndexException.
     *
     * @param message The detail message.
     * @param cause   The cause.
     * @param token   The token that caused the exception.
     */
    public JSONPathIndexException(String message, Throwable cause, Token token) {
        super(message, cause, token);
    }
}