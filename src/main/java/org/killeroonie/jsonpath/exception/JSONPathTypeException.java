package org.killeroonie.jsonpath.exception;

import org.killeroonie.jsonpath.Token;

/**
 * An exception raised due to a type exception.
 * <p>
 * This should only occur when evaluating filter expressions.
 */
public class JSONPathTypeException extends JSONPathException {

    /**
     * Constructs a new JSONPathTypeException.
     *
     * @param message The detail message.
     */
    public JSONPathTypeException(String message) {
        super(message);
    }

    /**
     * Constructs a new JSONPathTypeException.
     *
     * @param message The detail message.
     * @param token   The token that caused the exception.
     */
    public JSONPathTypeException(String message, Token token) {
        super(message, token);
    }

    /**
     * Constructs a new JSONPathTypeException.
     *
     * @param message The detail message.
     * @param cause   The cause.
     * @param token   The token that caused the exception.
     */
    public JSONPathTypeException(String message, Throwable cause, Token token) {
        super(message, cause, token);
    }
}
