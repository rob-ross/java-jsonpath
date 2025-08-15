package org.killeroonie.jsonpath.lexer;

import org.killeroonie.jsonpath.Token;

import java.util.List;

public interface Lexer {

    char EOF_CHAR = '\0';

    List<Token> tokenize(String jsonPathText);

    enum WhitespacePolicy {
        LENIENT,
        STRICT
    }

    WhitespacePolicy getWhitespacePolicy();

    void setWhitespacePolicy(Lexer.WhitespacePolicy policy);

}