package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Iterates over a stream of tokens
 */
public final class TokenStream  implements Iterator<Token> {

    // we'll use this List as the token queue with an index variable to control the current position
    private final List<Token> tokens;
    private final int queueSize;
    private int queueIndex;
    private Token currentToken;

    public TokenStream(final List<Token> tokens) {
        this.tokens = tokens;
        queueSize = tokens.size();
        queueIndex = -1;
        String jsonpath = tokens.isEmpty() ? "" : tokens.getFirst().path();
        currentToken = Token.NO_TOKEN;
        next();
    }


    public String toString() {
        return "current: %s\nnext: %s".formatted(currentToken, peek());
    }

    public Token current() {
        return currentToken;
    }

    public int queueIndex() {
        return queueIndex;
    }

    @Override
    public boolean hasNext() {
        return queueIndex < queueSize;
    }

    @Override
    public Token next() {
        Token nextToken;
        if (queueIndex >= queueSize) {
            nextToken = tokens.getLast();  // EOF Token
        } else {
            nextToken = tokens.get(++queueIndex);
        }
        currentToken = nextToken;
        return currentToken;
    }

    /**
     * "Closes" the stream. This just sets the queueIndex to the size of the token list and makes the current token
     * the EOF token.
     */
    public void close() {
        queueIndex = queueSize;
        currentToken = Token.EOF;
    }

    /**
     * Looks at the next token.
     *
     * @return the Token after the current Token, or the EOF Token if at EOF.
     */
    public Token peek() {
        if (queueIndex >= queueSize - 1) {
            return tokens.getLast();
        } else {
            return tokens.get(queueIndex + 1);
        }
    }

    /**
     * Back up the queueIndex by one token. The current token becomes the token previously returned by next().
     *
     * @return the previous Token in the stream.
     */
    public Token backtrack() {
        return backtrack(1);
    }

    /**
     * Back up the queueIndex by the number of Tokens in the argument.
     * The current token becomes the token at the new queueIndex. If the current queueIndex - numTokens is less than
     * zero, the queueIndex becomes zero and the first Token in the queue becomes the current token.
     *
     * @param numTokens the number of Tokens in the queue to backtrack.
     * @return the new current token.
     */
    public Token backtrack(int numTokens) {
        queueIndex = Math.max((queueIndex - numTokens), 0);
        currentToken = tokens.get(queueIndex);
        return currentToken;
    }

    /**
     * Throws an exception if the current token type is not in the argument Set.
     */
    public void expect(Set<TokenKind> kinds) {
        expectImpl(currentToken, kinds);
    }

    /**
     * Throws an exception if the TokenKind of the next Token in the queue is not in the argument Set.
     */
    public void expectPeek(Set<TokenKind> kinds) {
        expectImpl(peek(), kinds);
    }

    private void expectImpl(Token t, Set<TokenKind> ts) {
        if (!ts.contains(t.kind())) {
            throw new JSONPathSyntaxException(
                    "token kind: %s is not in the set: %s"
                            .formatted(t.kind(), ts.toString()), t);
        }
    }

    /**
     * Throws an exception if the TokenKind of the next Token in the queue is present in the argument Set.
     */
    public void peekNotExpected(Set<TokenKind> kinds, String message) {
        if (kinds.contains(peek().kind())) {
            throw new JSONPathSyntaxException(message, peek());
        }
    }
}