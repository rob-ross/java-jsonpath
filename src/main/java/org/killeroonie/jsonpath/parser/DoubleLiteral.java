package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public class DoubleLiteral extends FilterExpressionLiteral<Double> {

    public DoubleLiteral(Token token, Double value) {
        super(token, value);
    }
}
