package org.killeroonie.jsonpath.exception;

import org.killeroonie.jsonpath.Token;

/**
 * An exception raised when the maximum recursion depth is reached.
 */
public class JSONPathRecursionException extends JSONPathException{

    public JSONPathRecursionException(String message, Token token) {
        super(message, token);
    }

    public JSONPathRecursionException(String message, Throwable cause, Token token) {
        super(message, cause, token);
    }
}

/*

class JSONPathRecursionError(JSONPathError):
    """An exception raised when the maximum recursion depth is reached.

    Arguments:
        args: Arguments passed to `Exception`.
        token: The token that caused the error.
    """

    def __init__(self, *args: object, token: Token) -> None:
        super().__init__(*args)
        self.token = token

 */
