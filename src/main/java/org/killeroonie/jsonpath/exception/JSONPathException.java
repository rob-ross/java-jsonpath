package org.killeroonie.jsonpath.exception;

import org.killeroonie.jsonpath.Token;

/**
 * Base exception for all JSONPath exceptions.
 * <p>
 * This is the base for all exceptions that can be raised during JSONPath
 * parsing or evaluation.
 */
public class JSONPathException extends RuntimeException {

    private final transient Token token;

    /**
     * Constructs a new JSONPathException with the specified detail message.
     *
     * @param message The detail message.
     */
    public JSONPathException(String message) {
        this(message, null, null);
    }

    /**
     * Constructs a new JSONPathException with the specified detail message and token.
     *
     * @param message The detail message.
     * @param token   The token that caused the exception.
     */
    public JSONPathException(String message, Token token) {
        this(message, null, token);
    }

    /**
     * Constructs a new JSONPathException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause   The cause.
     */
    public JSONPathException(String message, Throwable cause) {
        this(message, cause, null);
    }

    /**
     * Constructs a new JSONPathException with the specified detail message, cause, and token.
     *
     * @param message The detail message.
     * @param cause   The cause.
     * @param token   The token that caused the exception.
     */
    public JSONPathException(String message, Throwable cause, Token token) {
        super(message, cause);
        this.token = token;
    }

    /**
     * Returns the token that may have caused this exception.
     *
     * @return The token, which may be null.
     */
    public Token getToken() {
        return token;
    }

    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (token == null) {
            return msg;
        }
        return String.format("%s, line %d, column %d", msg, token.getLineNumber(), token.getColumnNumber());
    }
}