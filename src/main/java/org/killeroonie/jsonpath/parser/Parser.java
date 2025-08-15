package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.parser.segment.JSONPathSegment;

import java.util.List;

public interface Parser {

    // Lists for now; we can get fancy later with iterables and token streams
    List<JSONPathSegment> parse(List<Token> tokens);
}
