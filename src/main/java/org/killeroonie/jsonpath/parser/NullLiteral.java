package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

import javax.validation.constraints.Null;

public class NullLiteral extends FilterExpressionLiteral<NullLiteralType> {

    public static final NullLiteral NULL = new NullLiteral();
    private NullLiteral() {
        super(Token.NO_TOKEN, null);
    }

    @Override
    public String toString() {
        return "null";
    }
}
