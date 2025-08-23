package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public class IntegerLiteral extends FilterExpressionLiteral<Integer> {

    public IntegerLiteral(Token token, Integer value) {
        super(token, value);
    }
}
