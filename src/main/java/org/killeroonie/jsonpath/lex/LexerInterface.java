package org.killeroonie.jsonpath.lex;

import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;

import java.util.EnumMap;
import java.util.List;

public interface LexerInterface {

    public static char EOF_CHAR = '\0';

    List<Token> tokenize(String jsonPathText);

    enum WhitespacePolicy {
        LENIENT,
        STRICT
    }

    WhitespacePolicy getWhitespacePolicy();

    void setWhitespacePolicy(LexerInterface.WhitespacePolicy policy);


}
