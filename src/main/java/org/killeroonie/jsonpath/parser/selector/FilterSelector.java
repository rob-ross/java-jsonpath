package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.parser.FilterExpression;


public class FilterSelector extends JSONPathSelector {
    private final FilterExpression expression;

    public FilterSelector(JSONPathEnvironment env, Token token, FilterExpression expr) {
        super(env, token);
        this.expression = expr;

    }

    public FilterExpression getExpression() {
        return expression;
    }
}
