package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;

public class WildcardSelector extends JSONPathSelector {
    public WildcardSelector(JSONPathEnvironment env, Token token) {
        super(env, token);
    }

    @Override
    public String toString() {
        return "*";
    }
}
