package org.killeroonie.jsonpath.exception;

import org.killeroonie.jsonpath.Token;

/**
 * An exception raised when an unknown function extension is called.
 */
public class JSONPathNameException extends JSONPathException {

    /**
     * Constructs a new JSONPathNameException.
     *
     * @param message The detail message.
     * @param token   The token that caused the exception.
     */
    public JSONPathNameException(String message, Token token) {
        super(message, token);
    }

    /**
     * Constructs a new JSONPathNameException.
     *
     * @param message The detail message.
     * @param cause   The cause.
     * @param token   The token that caused the exception.
     */
    public JSONPathNameException(String message, Throwable cause, Token token) {
        super(message, cause, token);
    }
}