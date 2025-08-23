package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public class FilterExpression extends Expression {

    private final Expression expression;

    public FilterExpression(Token token, Expression expression) {
        super(token);
        this.expression = expression;
    }

    @Override
    public Object evaluate(FilterContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
