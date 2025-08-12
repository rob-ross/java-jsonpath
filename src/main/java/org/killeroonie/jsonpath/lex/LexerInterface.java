package org.killeroonie.jsonpath.lex;

import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;

import java.util.EnumMap;
import java.util.List;

public interface LexerInterface {

    public static final char EOF_CHAR = '\0';

    List<Token> tokenize(String jsonPathText);

    enum WhitespacePolicy {
        LENIENT,
        STRICT
    }


}
