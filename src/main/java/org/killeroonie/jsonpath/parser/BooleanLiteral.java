package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public class BooleanLiteral extends FilterExpressionLiteral<Boolean> {

    public static final BooleanLiteral TRUE = new BooleanLiteral(true);
    public static final BooleanLiteral FALSE = new BooleanLiteral(false);

    private BooleanLiteral(Boolean value) {
        super(Token.NO_TOKEN, value);
    }
}
