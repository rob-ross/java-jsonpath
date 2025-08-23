package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.JSONPathEnvironment;

/**
 * Contextual information and data for evaluating a filter expression.
 */
public class FilterContext {

    private final JSONPathEnvironment env;
    private final Object current;
    private final Object root;

    public FilterContext(JSONPathEnvironment env, Object current, Object root) {
        this.env = env;
        this.current = current;
        this.root = root;
    }

    public JSONPathEnvironment getEnv() {
        return env;
    }

    public Object getCurrent() {
        return current;
    }

    public Object getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return "FilterContext{" +
                "current=" + current +
                '}';
    }
}


