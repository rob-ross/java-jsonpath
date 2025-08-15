package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public abstract class Expression {

    private final Token token;

    public Expression(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
