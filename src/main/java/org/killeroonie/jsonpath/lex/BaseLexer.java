package org.killeroonie.jsonpath.lex;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;

import java.util.*;

public abstract class BaseLexer implements LexerInterface {

    private final JSONPathEnvironment env;
    private transient ScannerState scannerState;
    private final Map<TokenKind, RulesBuilder.LexerRule> lexerRulesMap = new LinkedHashMap<>();
    private final EnumMap<TokenKind, RulesBuilder.LexerRule> customRulesMap = new EnumMap<>(TokenKind.class);

    private WhitespacePolicy whitespacePolicy = WhitespacePolicy.LENIENT;

    public abstract List<Token> tokenize(String jsonPathText);
    protected abstract void buildRules(Map<TokenKind, RulesBuilder.LexerRule> lexerRulesMap);



    BaseLexer(JSONPathEnvironment env) {
        this.env = env;
    }

    protected final JSONPathEnvironment getEnv() {
        return env;
    }

    protected final ScannerState getScannerState() {
        return scannerState;
    }

    /**
     * Resets the Lexer state in preparation of tokenizing an input string
     * @param jsonPathText the JSON path query string being scanned
     * @return the newly initialized ScannerState
     */
    protected final ScannerState initScanner(String jsonPathText) {
        // reset the Lexer state in preparation of tokenizing an input string
        scannerState = new ScannerState(jsonPathText);
        // The order of these operations is significant.
        buildCustomRules(customRulesMap);
        buildRules(lexerRulesMap);
        return scannerState;
    }

    protected final Map<TokenKind, RulesBuilder.LexerRule> getLexerRulesMap() {
        return lexerRulesMap;
    }

    protected final Map<TokenKind, RulesBuilder.LexerRule> getCustomRulesMap() {
        return customRulesMap;
    }

    /**
     * Returns the current whitespace policy for this Lexer. {@code WhitespacePolicy.LENIENT} (the default) causes the
     * Lexer to consume all whitespace and emit no SPACE getTokenList. {@code WhitespacePolicy.STRICT} will emit all
     * whitespace characters as SPACE getTokenList. A run of contiguous whitespace characters only produces a single SPACE token.
     * Note that a STRICT policy enforces whitespace rules in the RFC9535 spec, and thus JSON path strings that may parse
     * correctly in LENIENT mode may fail with a syntax error when using STRICT mode.
     * @return the current {@code WhitespacePolicy} for this Lexer.
     */
    public final WhitespacePolicy getWhitespacePolicy() {
        return this.whitespacePolicy;
    }

    protected final void setWhitespacePolicy(LexerInterface.WhitespacePolicy policy) {
        this.whitespacePolicy = policy;
    }



    /**
     * Default implementation builds no custom rules. Subclasses can override to create custom lexer rules.
     */
    protected void buildCustomRules( EnumMap<TokenKind, RulesBuilder.LexerRule> customRulesMap ) {
        lexerRulesMap.clear();
    }

    /**
     * If a custom rule exists in the JSONPathEnvironment or Lexer, return it.
     * Environment rules take precedent over Lexer rules.
     * @param kind the TokenKind of the custom rule to locate.
     * @return the custom rule for the TokenKind or null.
     */
    protected final RulesBuilder.LexerRule findRule(TokenKind kind) {
        Optional<RulesBuilder.LexerRule> rule = getEnv().findRule(kind);
        return rule.orElseGet(() -> customRulesMap.get(kind) );
    }

    /**
     * Looks up the argument {@link TokenKind} in the lexer rules and returns the TokenKind that should be emitted.
     * @param lookupToken the lookup key.
     * @return the TokenKind that should be emitted for the given key. This allows customization by aliasing several
     * TokenKinds to a single emitted TokenKind.
     */
    protected final TokenKind emitKind(TokenKind lookupToken) {
        return lexerRulesMap.get(lookupToken).emitKind();
    }

    /**
     * Convenience method that returns the current scanner character, or '\0' if the scanner is at EOF.
     *
     * @return the current character by calling getScannerState().currentChar();
     */
    protected final char currentChar() {
        return scannerState.currentChar();
    }

    /**
     * Convenience method that returns the current scanner position index. If the value returned equals
     * the length of the jsonpath query text, the scanner is at EOF.
     *
     * @return the current scanner position by calling getScannerState().getPositionIndex();
     */
    protected final int position() {
        return scannerState.getPositionIndex();
    }

    /**
     * Convenience method that calls advanceToken() on the scanner.
     * @param kind the TokenKind for the new Token
     * @param value the scanned value for the Token
     * @return the new Token
     */
    @SuppressWarnings("UnusedReturnValue")
    protected final Token advanceToken(TokenKind kind, String value) {
        return scannerState.advanceToken(kind, value);
    }

}
