package org.killeroonie.jsonpath.lexer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;

import java.util.ArrayList;
import java.util.List;

public final class ScannerState {

    private final String jsonPathText;
    private int positionIndex;
    private final List<Token> tokenList;

    ScannerState(String jsonPathText) {
        this.jsonPathText = jsonPathText;
        this.positionIndex = 0;
        this.tokenList = new ArrayList<>();
    }

    String getJsonPathText() {
        return jsonPathText;
    }

    int getPositionIndex() {
        return positionIndex;
    }

    List<Token> getTokenList() {
        return tokenList;
    }

    /**
     * Returns the character from the input string at the current scanner getPositionIndex. If the current getPositionIndex is past
     * the end of the input, this method returns null. Does not advance the getPositionIndex.
     *
     * @return the current scanner character, or null if the current getPositionIndex is past EOF.
     */
    char currentChar() {
        return has0() ? c0() : '\0';
    }

    /**
     * Return, without consuming, the first `numberOfChars` characters from the current getPositionIndex.
     *
     * @param numberOfChars the number of characters to peek.
     * @return {@code numberOfChars} characters from the current scanner getPositionIndex. If the scanner getPositionIndex is at
     * the end of the input, this method returns null. If there are fewer characters remaining to be scanned than
     * requested in the argument, all characters from the current getPositionIndex to the end of the input are returned.
     */
     @Nullable
     String peekNextChars(int numberOfChars) {
        int len = jsonPathText.length();
        if (positionIndex >= len) return null;
        int end = Math.min(len, positionIndex + numberOfChars);
        return jsonPathText.substring(positionIndex, end);
    }

    /**
     * Returns the character from the input string just before the current scanner getPositionIndex.
     * If the current getPositionIndex is 0, this method returns null. The getPositionIndex is unchanged after calling this method.
     *
     * @return the previous scanner character, or null if the current getPositionIndex is at the start of the text.
     */
    char previousChar() {
        return hasp() ? cp() : '\0';
    }

    void advance(int length) {
        advanceImpl(length);
    }

    void advance(RulesBuilder.LexemeRule rule) {
        advanceImpl(rule);
    }

    void advance(Token token) {
        advanceImpl(token);
    }

    /**
     * Advance the getPositionIndex in the scanned text by the length of the `length_specifier` argument.
     *
     * @param lengthSpecifier int, LexemeRule or Token from which to obtain the length to advance the scanner.
     */
    private void advanceImpl(Object lengthSpecifier) {
        int length =
                switch (lengthSpecifier) {
                    case Integer i -> i;
                    case RulesBuilder.LexemeRule lr -> lr.lexeme().length();
                    case Token t -> t.value().length();
                    default -> throw new IllegalArgumentException(
                            "Expected int, LexemeRule or Token, got %s"
                                    .formatted(lengthSpecifier.getClass().getSimpleName()));
                };
        positionIndex += length;
    }

    /**
     * Create and add the new {@link Token} to the list of getTokenList and advance the scanner getPositionIndex by
     * the length of the Token value.
     *
     * @param kind  the {@link TokenKind} of the new Token.
     * @param value the scanned text the Token represents.
     * @return the newly created Token.
     */
    @NotNull
    Token advanceToken(TokenKind kind, String value) {
        final Token newToken = makeToken(kind, value);
        tokenList.add(newToken);
        advance(value.length());
        return newToken;
    }

    @Contract("_, _ -> new")
    @NotNull
    Token makeToken(TokenKind kind, String value) {
        return new Token(kind, value, positionIndex, jsonPathText);
    }


    ////////////////////////////////////////////////////////////////////
    /// Helper Methods
    ////////////////////////////////////////////////////////////////////

    /**
     * @return true if there is at least 1 unscanned character remaining in the input text,
     * or false if the scanner is at EOF.
     */
    boolean has0() {
        return positionIndex < jsonPathText.length();
    }

    /**
     * @return true if there are at least 2 unscanned characters remaining in the input text, or false otherwise.
     */
    boolean has1() {
        return positionIndex + 1 < jsonPathText.length();
    }

    /**
     * @return fase if the current getPositionIndex is at the start of the input, or the input text is empty.
     * Otherwise, returns true, indicating that a character before the current character is available.
     */
    boolean hasp() {
        return !jsonPathText.isEmpty() && positionIndex > 0;
    }

    /**
     * @return the character at the current getPositionIndex.
     */
    char c0() {
        return jsonPathText.charAt(positionIndex);
    }

    /**
     * @return the character after the current character, at index getPositionIndex + 1.
     */
    char c1() {
        return jsonPathText.charAt(positionIndex + 1);
    }

    /**
     * @return the character just before the current character
     */
    char cp() {
        return jsonPathText.charAt(positionIndex - 1);
    }

    boolean regionEquals(String s) {
        return jsonPathText.regionMatches(positionIndex, s, 0, s.length());
    }

} // end class ScannerState
