package org.killeroonie.jsonpath.exception;

import org.killeroonie.jsonpath.Token;

/**
 * An exception raised when parsing a JSONPath string.
 */
public class JSONPathSyntaxException extends JSONPathException {

    /**
     * Constructs a new JSONPathSyntaxException.
     *
     * @param message The detail message.
     * @param token   The token that caused the exception.
     */
    public JSONPathSyntaxException(String message, Token token) {
        super(message, token);
    }

    /**
     * Constructs a new JSONPathSyntaxException.
     *
     * @param message The detail message.
     * @param cause   The cause.
     * @param token   The token that caused the exception.
     */
    public JSONPathSyntaxException(String message, Throwable cause, Token token) {
        super(message, cause, token);
    }
}
