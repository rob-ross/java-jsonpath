package org.killeroonie.jsonpath.lexer;

import org.jetbrains.annotations.Nullable;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;

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

    /**
     * Returns the LexerRule for the argument TokenKind. This will return null if
     * no rule is defined for the TokenKind. This should not happen for any TokenKinds that are
     * included in the Lexer's defined rules; but it may occur if passed a TokenKind
     * that is not part of the Lexer's rules or associated Parser's grammar.
     * @param kind the TokenKind to get the rule for.
     * @return the LexerRule for the given TokenKind, or null if no rule is defined
     * for this TokenKind.
     */
    @Nullable RulesBuilder.LexerRule findRule(TokenKind kind);


    }