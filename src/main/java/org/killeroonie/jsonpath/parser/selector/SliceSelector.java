package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;

public class SliceSelector extends JSONPathSelector {
    public SliceSelector(JSONPathEnvironment env, Token token, int start, int stop, int step) {
        super(env, token);
    }
}
