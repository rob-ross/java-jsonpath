package org.killeroonie.jsonpath.lex;

import org.killeroonie.jsonpath.Token;

import java.util.List;

public interface LexerInterface {

    char EOF_CHAR = '\0';

    List<Token> tokenize(String jsonPathText);

    enum WhitespacePolicy {
        LENIENT,
        STRICT
    }

    WhitespacePolicy getWhitespacePolicy();

    void setWhitespacePolicy(LexerInterface.WhitespacePolicy policy);


}
