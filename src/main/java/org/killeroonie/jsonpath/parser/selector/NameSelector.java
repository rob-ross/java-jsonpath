package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.parser.StringLiteral;

public class NameSelector extends JSONPathSelector {
    private final String name;

    public NameSelector(JSONPathEnvironment env, Token token, String name) {
        super(env, token);
        switch (token.kind()) {
            case IDENTIFIER -> this.name = name;
            case SINGLE_QUOTE_STRING ->  this.name = name.substring(1,  name.length() - 1);
            case DOUBLE_QUOTE_STRING ->  {
                name = StringLiteral.canonicalString(name.substring(1, name.length() - 1));
                // do we need to deal with escaping and unescaping quotes?
                this.name = name.substring(1, name.length() - 1);
            }
            default -> {
                throw new RuntimeException("Unexpected token " + token.kind());
            }
        }

    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return StringLiteral.canonicalString(name);
    }
}
