package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.JSONPathEnvironment;

public abstract  class BaseParser implements Parser {

    private final JSONPathEnvironment env;

    public BaseParser(JSONPathEnvironment env) {
        this.env = env;
    }

    public JSONPathEnvironment getEnv() {
        return env;
    }
}
