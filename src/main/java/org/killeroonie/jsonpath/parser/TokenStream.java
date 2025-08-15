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

    /**
     * Returns the {@link TokenKind} of the current token. It is a convenience method that is
     * equivalent to calling {@code current().kind();}
     * @return the TokenKind of the current token.
     */
    public TokenKind currentKind(){
        return currentToken.kind();
    }

    public int queueIndex() {
        return queueIndex;
    }

    @Override
    public boolean hasNext() {
        return queueIndex < queueSize;
    }

    /**
     * Returns the next Token in the stream, advancing the queueIndex by 1.
     * If there are no more Tokens, returns Token.EOF.
     * @return
     */
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
     * Throws an exception if the current Token kine is not equal to the argument kind.
     */
    public void expect(TokenKind kind) throws JSONPathSyntaxException {
        expectImpl(kind, currentToken);
    }

    /**
     * Throws an exception if the current token type is not in the argument Set.
     */
    public void expect(Set<TokenKind> kinds) throws JSONPathSyntaxException {
        expectImpl(kinds, currentToken);
    }


    /**
     * Throws an exception if the kind of the next Token is not equal to the argument kind.
     */
    public void expectPeek(TokenKind kind) throws JSONPathSyntaxException {
        expectImpl(kind, peek());
    }

    /**
     * Throws an exception if the TokenKind of the next Token in the queue is not in the argument Set.
     */
    public void expectPeek(Set<TokenKind> kinds) throws JSONPathSyntaxException {
        expectImpl(kinds, peek());
    }

    private void expectImpl(TokenKind expectedKind, Token actual) {
        if ( expectedKind != actual.kind()) {
            throw new JSONPathSyntaxException(
                    "expected: %s, got %s"
                            .formatted(expectedKind, actual), actual);
        }
    }

    private void expectImpl(Set<TokenKind> kinds,Token actual) {
        if (!kinds.contains(actual.kind())) {
            throw new JSONPathSyntaxException(
                    "token kind: %s is not in the set: %s"
                            .formatted(actual.kind(), kinds.toString()), actual);
        }
    }


    public void peekNotExpected(TokenKind kind, String message) {
        if (kind != peek().kind()) {
            throw new JSONPathSyntaxException(message, peek());
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