package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public class NullLiteral extends FilterExpressionLiteral<NullLiteralType> {

    public NullLiteral(Token token, Object value) {
        super(token, null);
    }

    @Override
    public String toString() {
        return "null";
    }
}
