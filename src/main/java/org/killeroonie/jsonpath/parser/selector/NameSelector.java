package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;

public class NameSelector extends JSONPathSelector {
    public NameSelector(JSONPathEnvironment env, Token token, String value) {
        super(env, token);
    }
}
