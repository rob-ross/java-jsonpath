package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public class BooleanLiteral extends FilterExpressionLiteral<Boolean> {


    public BooleanLiteral(Token token, Boolean value) {
        super(token, value);
    }
}
