package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.parser.segment.JSONPathSegment;

import java.util.List;

public class PJPParser extends BaseParser {

    // we may need an environment parameter here
    public PJPParser(JSONPathEnvironment env) {
        super(env);
    }

    /**
     * Parses a JSONPath from a stream of tokens.
     * @param tokens the List of Tokens
     * @return the List of JSONPathSegment
     */
    @Override
    public List<JSONPathSegment> parse(List<Token> tokens) {
        return List.of();
    }
}
