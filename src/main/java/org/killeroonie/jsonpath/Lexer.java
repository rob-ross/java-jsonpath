package org.killeroonie.jsonpath;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/*
The conventional order for field modifiers is:
1.public, protected, private
2.static
3.final
4.transient
5.volatile
 */

/**
 * Tokenizes a JSONPath string.
 * Some customization can be achieved by subclassing Lexer and setting
 * class attributes. Then setting lexer_class on a JSONPathEnvironment.
 * Class attributes:
 *     key_pattern: The regular expression pattern used to match mapping
 *         keys/properties.
 *     logical_not_pattern: The regular expression pattern used to match
 *         logical negation tokens. By default, `not` and `!` are
 *         equivalent.
 *     logical_and_pattern: The regular expression pattern used to match
 *         logical and tokens. By default, `and` and `&&` are equivalent.
 *     logical_or_pattern: The regular expression pattern used to match
 *         logical or tokens. By default, `or`and `||` are equivalent.
 */
public class Lexer {

    private final char EOF_CHAR = '\0';

    // Instance variables
    private final JSONPathEnvironment env;
    private final EnumMap<TokenKind, LexerRule> customRules = new EnumMap<>(TokenKind.class);
    private final EnumMap<TokenKind, LexerRule> lexerRules;
    private final Map<String, TokenKind> tokenLookupMap = new LinkedHashMap<>(TokenKind.values().length, 1);

    // This maps single-char lexemes as their char int values to their String representations. This avoids String creation
    // during the scanner loop.
    private final IntKeyMap<String> oneCharLexemesMap = new IntKeyMap<>(String.class, 128);

    private final Set<String> twoCharLexemesSet = new HashSet<>();
    private final Map<String, TokenKind> keywordMap = new HashMap<>();

    private transient ScannerState scannerState;
    private WhitespacePolicy whitespacePolicy = WhitespacePolicy.LENIENT;



    /**
     * Constructor for Lexer.
     * todo - maybe we can have a no-arg constructor that creates a DefaultEnvironment ?
     *
     * @param env The JSONPathEnvironment configuration
     */
    public Lexer(JSONPathEnvironment env) {
        this.env = env;
        // The order of these operations is significant.
        buildCustomRules();
        lexerRules = buildRules();
    }

    protected ScannerState getScannerState() {
        return scannerState;
    }

    /**
     * Populates the {@code customRules} Map.
     *
     * The default Lexer implementation provides no custom rules.
     * Subclasses can override {@code buildCustomRules()} to specify custom matching and {@code TokenKind} emitting rules.
     * When {@code buildRules} is called from this class' constructor, any custom rules specified here for a TokenKind
     * will be used instead of the default values as defined in TokenKind. <p>
     * Note this implementation should be interpreted as an example of how subclasses could implement this method. The
     * actual `custom rules` used here are exactly the same as the default rules for each TokenKind, so no new behavior
     * is implemented.
     *
     */
    public void buildCustomRules() {
        /*
         * Customization rules
         * Token lexemes and regexp match rules can be customized in both the environment and the Lexer.
         * You can "override" matching rules in `lexerRules`.
         * Create an entry in `lexerRules` that maps an exiting TokenKind to a new LexerRule. A LexerRule will allow
         * you to change the type of match (lexeme or regexp pattern), and the lexeme or pattern for that TokenKind.
         * You can also change the default emitKind to specify a different TokenKind that should be emitted when matching
         * your overridden TokenKind rule.
         * This buildRules method will search this path for locating the LexerRule for a token:
         * 1. Search for the Token in the Environment custom rules. If found, use that rule for the TokenKind
         * 2. If not found in the Env, search the Lexer for a custom rule and use that if found
         * 3. If still not found, use the default rule from the DefaultRulesBuilder.
         *
         */
        final String key_pattern = "[\\u0080-\\uFFFFA-Za-z_][\\u0080-\\uFFFFA-Za-z0-9_-]*";
        final String logical_not_pattern = "not";
        customRules.put(TokenKind.BARE_PROPERTY,
                new RegexRule(Pattern.compile(key_pattern), TokenKind.BARE_PROPERTY)
        );
        customRules.put(TokenKind.NOT_EXT,
                new LexemeRule(logical_not_pattern, TokenKind.NOT)
        );
    }


    /**
     * If a custom rule exists in the JSONPathEnvironment or Lexer, return it.
     * Environment rules take precedent over Lexer rules.
     * @param kind the TokenKind of the custom rule to locate.
     * @return the custom rule for the TokenKind or null.
     */
    private LexerRule findRule(TokenKind kind) {
        Optional<LexerRule> rule = env.findRule(kind);
        return rule.orElseGet(() -> customRules.getOrDefault(kind, null));
    }

    /**
     * Builds this Lexer's rules based on the default rules and including any custom rules
     * for the Lexer and the JSONPathEnvironment. It also populates {@code tokenLookupMap},
     * {@code oneCharLexemesMap}, {@code twoCharLexemesSet}, and {@code keywordMap}
     */
    private EnumMap<TokenKind, Lexer.LexerRule> buildRules() {
        // start with default rules
        EnumMap<TokenKind, Lexer.LexerRule> rules =  new DefaultRulesBuilder().getRules();
        // Now we apply custom rules from the Env and Lexer. Env rules have the highest priority, then Lexer rules.
        for (var entry: rules.entrySet()) {
            TokenKind kind = entry.getKey();
            LexerRule customRule = findRule(kind);
            if  (customRule != null) {
                rules.put(kind, customRule); // replace the default rule with the custom rule.
            }
            LexerRule rule = rules.get(kind);
            if (rule instanceof LexemeRule(String lexeme, TokenKind emitKind) && lexeme != null) {
                // looking up a null or by a regex pattern string is useless, so we omit these from the lookup map
                tokenLookupMap.put(lexeme, emitKind);
//                System.out.println("   buildRules():  tokenLookupMap.put(lexeme=" + lexeme + ", emitKind=" + emitKind + "):");
            }
        }
        // generate the lexeme sets for matching
        for (var entry: rules.entrySet()) {
            TokenKind kind = entry.getKey();
            LexerRule rule = entry.getValue();
            if (rule instanceof LexemeRule lr) {
                int length = lr.lexeme.length();
                if (length == 1) {
                    oneCharLexemesMap.put(lr.lexeme.charAt(0), lr.lexeme); // all lexemes are in the BMP, so this is safe.
                } else if (length == 2) {
                    twoCharLexemesSet.add(lr.lexeme);
                }
                if ( kind.isKeyword() ) {
                    keywordMap.put(lr.lexeme(), kind);
                }
            }
        }
        return rules;
    }


    private ScannerState initScanner(String jsonPathText) {
        // reset the Lexer state in preparation of tokenizing an input string
        scannerState = new ScannerState(jsonPathText);
        return scannerState;
    }

    //*************************************************************************
    //*    TOKENIZE
    //*************************************************************************

    /**
     * Generate a sequence of tokens from a JSONPath string.
     *
     * @param jsonPathText The JSONPath string to tokenize
     * @return Iterator of Token objects
     */
    public List<Token> tokenize(String jsonPathText) {
        final ScannerState scanner = initScanner(jsonPathText);
        while ( scannerState.currentChar() != EOF_CHAR) {
            System.out.printf("current char is %s, pos= %d%n", scannerState.currentChar(), scannerState.positionIndex);
            Matcher matcher;
            RegexRule rule = (RegexRule) lexerRules.get(TokenKind.SPACE);
            Set<Character> firstSet = rule.firstSet();
            if ( firstSet.contains(scannerState.currentChar())) {
                matcher = rule.pattern().matcher(jsonPathText);
                if (matcher.find(scannerState.positionIndex)) {
                    String spaces = matcher.group();
                    if (!spaces.isEmpty() && getWhitespacePolicy() == WhitespacePolicy.STRICT) {
                        Token t = scannerState.advanceToken(TokenKind.SPACE, spaces);
                    } else {
                        scannerState.advance(spaces.length());  // advance without adding Token
                    }
                    continue;
                }
            }
            String firstTwoChars = scannerState.peekNextChars(2);
            if (twoCharLexemesSet.contains(firstTwoChars)) {
                final TokenKind kind = tokenLookupMap.get(firstTwoChars);
                scannerState.advanceToken( emitKind(kind),  firstTwoChars);
                continue;
            }
            String lexeme = oneCharLexemesMap.get(scannerState.currentChar());
            if ( lexeme != null ){
                final TokenKind kind = tokenLookupMap.get(lexeme);
                scannerState.advanceToken( emitKind(kind),  lexeme);
                continue;
            }

            // Identifiers and keywords also handled here
            // (member-name-shorthand, true, false, null, function names, extension keywords)
            //-----------------------------------------------------------------------------------------
            rule = (RegexRule) lexerRules.get(TokenKind.IDENTIFIER);
            matcher = rule.pattern().matcher(jsonPathText);
            if (matcher.find(scannerState.positionIndex)) {
                processIdentifier(matcher.group());
            }

        }
        return scanner.getTokenList();
    }

    /**
     * Looks up the argument {@link TokenKind} in the lexer rules and returns the TokenKind that should be emitted.
     * @param lookupToken the lookup key.
     * @return the TokenKind that should be emitted for the given key. This allows customization by aliasing several
     * TokenKinds to a single emitted TokenKind.
     */
    protected TokenKind emitKind(TokenKind lookupToken) {
        return lexerRules.get(lookupToken).emitKind();
    }

    /**
     * Processes identifiers and keywords, including member-name-shorthand, `true`, `false`, `null`, function names,
     * and extension keywords.
     * @param text the matched group value. I.e., the text that matched the identifier regex pattern.
     */
    protected void processIdentifier(String text) {
        // todo - refactor after all unit tests pass
        // the Lexer *should* just create a Token for the identifier and let the parse deal with the specifics in context.
        // for the moment, we want to pass the existing unit tests for python-jsonpath, so we need to produce the same
        // tokens as that library for the same input.
        TokenKind kind = keywordMap.getOrDefault(text, null);
        if (kind != null) {
            // we scanned a keyword
            scannerState.advanceToken(emitKind(kind), text);
            return;
        }
        // this is either a function or member-name-shorthand identifier.
        char previousChar = scannerState.previousChar();
        if ( previousChar == '.') {
            // DOT_PROPERTY
            scannerState.advanceToken(emitKind(TokenKind.DOT_PROPERTY),  text);
            return;
        }





    }

    public Iterator<Token> tokenizeOld(String jsonPathText) {
        this.scannerState = initScanner(jsonPathText);
        final List<Token> tokens = scannerState.tokenList;  // for readability



        Matcher matcher = null;
        // matcher = rules.matcher(jsonPathText);
        // temp for compiler, this method does not work yet


        /*
        Tokens that are substituted for scanned token kinds:
        - **TOKEN_DOT_PROPERTY**
        - **TOKEN_LIST_SLICE**
        - **TOKEN_INT** (conditionally)
        - **TOKEN_NONE**
        - **TOKEN_NULL**

         */
        while (matcher.find()) {
            TokenKind kind = null;

            assert kind != null;

            if (kind.equals(TokenKind.DOT_PROPERTY)) {
                String propValue = matcher.group("GPROP");
                int propStart = matcher.start("GPROP");
                tokens.add(new Token(TokenKind.PROPERTY, propValue, propStart, jsonPathText));

            } else if (kind.equals(TokenKind.BARE_PROPERTY)) {
                tokens.add(new Token(TokenKind.BARE_PROPERTY, matcher.group(), matcher.start(), jsonPathText));

            } else if (kind.equals(TokenKind.LIST_SLICE)) {
                String startValue = matcher.group("GLSLICESTART");
                String stopValue = matcher.group("GLSLICESTOP");
                String stepValue = matcher.group("GLSLICESTEP");

                tokens.add(new Token(TokenKind.SLICE_START, startValue, matcher.start("GLSLICESTART"), jsonPathText));
                tokens.add(new Token(TokenKind.SLICE_STOP, stopValue, matcher.start("GLSLICESTOP"), jsonPathText));
                tokens.add(new Token(TokenKind.SLICE_STEP, stepValue != null ? stepValue : "",
                        matcher.start("GLSLICESTEP"), jsonPathText));

            } else if (kind.equals(TokenKind.DOUBLE_QUOTE_STRING)) {
                String quotedValue = matcher.group("GDQUOTE");
                tokens.add(new Token(TokenKind.DOUBLE_QUOTE_STRING, quotedValue,
                        matcher.start("GDQUOTE"), jsonPathText));

            } else if (kind.equals(TokenKind.SINGLE_QUOTE_STRING)) {
                String quotedValue = matcher.group("GSQUOTE");
                tokens.add(new Token(TokenKind.SINGLE_QUOTE_STRING, quotedValue,
                        matcher.start("GSQUOTE"), jsonPathText));

            } else if (kind.equals(TokenKind.INT)) {
                String expGroup = matcher.group("GEXP");
                if (expGroup != null) {
                    tokens.add(new Token(TokenKind.FLOAT, matcher.group(), matcher.start(), jsonPathText));
                } else {
                    tokens.add(new Token(TokenKind.INT, matcher.group(), matcher.start(), jsonPathText));
                }

            } else if (kind.equals(TokenKind.RE_PATTERN)) {
                String reValue = matcher.group("GRE");
                String flagsValue = matcher.group("GREFLAGS");

                tokens.add(new Token(TokenKind.RE_PATTERN, reValue, matcher.start("GRE"), jsonPathText));
                tokens.add(new Token(TokenKind.RE_FLAGS, flagsValue, matcher.start("GREFLAGS"), jsonPathText));

            } else if (kind.equals(TokenKind.FUNCTION)) {
                String funcValue = matcher.group("GFUNC");
                tokens.add(new Token(TokenKind.FUNCTION, funcValue, matcher.start("GFUNC"), jsonPathText));

            } else if (!kind.equals(TokenKind.SKIP)) {
                tokens.add(new Token(kind, matcher.group(), matcher.start(), jsonPathText));
            }
        }

        return tokens.iterator();
    }

    // Helper classes and methods

    //*************************************************************************
    //*    ScannerState
    //*************************************************************************

    final protected class ScannerState {

        private final String jsonPathText;
        private int positionIndex;
        private final List<Token> tokenList;

        private ScannerState(String jsonPathText) {
            this.jsonPathText = jsonPathText;
            this.positionIndex = 0;
            this.tokenList = new ArrayList<>();
        }

        protected String getJsonPathText() {
            return jsonPathText;
        }

        protected int getPositionIndex() {
            return positionIndex;
        }

        protected List<Token> getTokenList() {
            return tokenList;
        }

        /**
         * Returns the character from the input string at the current scanner position. If the current position is past
         * the end of the input, this method returns null. Does not advance the position.
         * @return the current scanner character, or null if the current position is past EOF.
         */
        protected char currentChar() {
            return has0() ? c0() : '\0';
        }

        /**
         * Return, without consuming, the first `numberOfChars` characters from the current position.
         * @param numberOfChars the number of characters to peek.
         * @return {@code numberOfChars} characters from the current scanner position. If the scanner position is at
         * the end of the input, this method returns null. If there are fewer characters remaining to be scanned than
         * requested in the argument, all characters from the current position to the end of the input are returned.
         */
        private @Nullable String peekNextChars(int numberOfChars) {
            int len = jsonPathText.length();
            if (positionIndex >= len) return null;
            int end = Math.min(len, positionIndex + numberOfChars);
            return jsonPathText.substring(positionIndex, end);
        }

        /**
         * Returns the character from the input string just before the current scanner position.
         * If the current position is 0, this method returns null. The position is unchanged after calling this method.
         * @return the previous scanner character, or null if the current position is at the start of the text.
         */
        protected char previousChar() {
            return hasp() ? cp() : '\0';
        }

        protected void advance(int length){
            advanceImpl(length);
        }

        protected void advance(LexemeRule rule) {
            advanceImpl(rule);
        }

        protected void advance(Token token) {
            advanceImpl(token);
        }

        /**
         * Advance the position in the scanned text by the length of the `length_specifier` argument.
         * @param lengthSpecifier int, LexemeRule or Token from which to obtain the length to advance the scanner.
         */
        private void advanceImpl(Object lengthSpecifier) {
            int length =
                    switch (lengthSpecifier) {
                        case Integer i -> i;
                        case LexemeRule lr -> lr.lexeme.length();
                        case Token t -> t.value().length();
                        default -> throw new IllegalArgumentException(
                                "Expected int, LexemeRule or Token, got %s"
                                        .formatted(lengthSpecifier.getClass().getSimpleName()));
                    };
            positionIndex += length;
        }

        /**
         * Create and add the new {@link Token} to the list of tokens and advance the scanner position by
         * the length of the Token value.
         * @param kind the {@link TokenKind} of the new Token.
         * @param value the scanned text the Token represents.
         * @return the newly created Token.
         */

        private Token advanceToken(TokenKind kind, String value) {
            Token newToken = makeToken(kind, value);
            tokenList.add(newToken);
            advance(newToken);
            return newToken;
        }

        private Token makeToken(TokenKind kind, String value) {
            return new Token(kind, value, positionIndex, jsonPathText);
        }


        ////////////////////////////////////////////////////////////////////
        /// Helper Methods
        ////////////////////////////////////////////////////////////////////

        /**
         * @return true if there is at least 1 unscanned character remaining in the input text,
         * or false if the scanner is at EOF.
         */
        boolean has0() { return positionIndex < jsonPathText.length(); }
        /**
         * @return true if there are at least 2 unscanned characters remaining in the input text, or false otherwise.
         */
        boolean has1() { return positionIndex + 1 < jsonPathText.length(); }

        /**
         * @return fase if the current position is at the start of the input, or the input text is empty.
         * Otherwise, returns true, indicating that a character before the current character is available.
         */
        boolean hasp() {
            return !jsonPathText.isEmpty() && positionIndex > 0;
        }
        /**
         * @return the character at the current position.
         */
        char c0() { return jsonPathText.charAt(positionIndex); }

        /**
         *
         * @return the character after the current character, at index position + 1.
         */
        char c1() { return jsonPathText.charAt(positionIndex + 1); }

        /**
         *
         * @return the character just before the current character
         */
        char cp() { return jsonPathText.charAt(positionIndex  - 1 ); }

        boolean regionEquals(String s) {
            return jsonPathText.regionMatches(positionIndex, s, 0, s.length());
        }

    } // end class ScannerState

    //*************************************************************************
    //*    LexerRule
    //*************************************************************************

    public interface LexerRule {
        TokenKind emitKind();
    }
    /**
     * If scanned text matches the regex {@code Pattern}, emit the {@link TokenKind} in {@code emitKind}.
     * @param pattern the regexp pattern to match.
     * @param emitKind the TokenKind to emit when the pattern is matched
     */
    record RegexRule(Pattern pattern, TokenKind emitKind, Set<Character> firstSet) implements LexerRule {

        public RegexRule(Pattern pattern, TokenKind emitKind){
            this(pattern, emitKind, null);
        }
    }
    /**
     * If scanned text matches the lexeme, emit the {@link TokenKind} in {@code emitKind}.
     * @param lexeme the lexeme String to match.
     * @param emitKind the TokenKind to emit when the lexeme is matched
     */
    record LexemeRule(String lexeme, TokenKind emitKind) implements LexerRule {}

    public enum WhitespacePolicy {
        LENIENT, STRICT;
    }

    public void setWhitespacePolicy(WhitespacePolicy policy) {
        this.whitespacePolicy = policy;
    }

    /**
     * Returns the current whitespace policy for this Lexer. {@code WhitespacePolicy.LENIENT} (the default) causes the
     * Lexer to consume all whitespace and emit no SPACE tokens. {@code WhitespacePolicy.STRICT} will emit all
     * whitespace characters as SPACE tokens. A run of contiguous whitespace characters only produces a single SPACE token.
     * Note that a STRICT policy enforces whitespace rules in the RFC9535 spec, and thus JSON path strings that may parse
     * correctly in LENIENT mode may fail with a syntax error when using STRICT mode.
     * @return the current {@code WhitespacePolicy} for this Lexer.
     */
    public WhitespacePolicy getWhitespacePolicy() {
        return this.whitespacePolicy;
    }
    //**************************************************************************


    static void t2() {
        /*
         * This test creates an anonymous JSONPathEnvironment subclass so we can test adding a custom rule.
         */
        Lexer lexer2 = new Lexer(new  JSONPathEnvironment(){
            @Override
            public EnumMap<TokenKind, LexerRule> buildCustomRules() {
                var rules = super.buildCustomRules();
                rules.put(TokenKind.PSEUDO_ROOT, new Lexer.LexemeRule("%", TokenKind.PSEUDO_ROOT));
                return rules;
            }
        });
        System.out.println("Lexer2: ");
        lexer2.displayRules();
    }

    /* Debug methods

     */
    public void displayRules() {
        //debug method
        for (LexerRule rule : lexerRules.values()) {
            System.out.println(rule);
        }
    }

    public void displayTokenLookup() {
        System.out.println("Displaying " + tokenLookupMap.size() + " tokens in tokenLookupMap:");
        for (var entry: tokenLookupMap.entrySet()) {
            System.out.println(entry);
        }
        ArrayList<String> sortedList = new ArrayList<>(tokenLookupMap.keySet());
        sortedList.sort( (Comparator.comparingInt(String::length).reversed() ));
        System.out.println("Lexemes sorted by length ("+sortedList.size()+"):");
        System.out.println(sortedList);
    }

    public void displayLexemeCounts() {
        Map<Integer, Integer> counter = new HashMap<>();
        for (var entry: tokenLookupMap.entrySet()) {
            int lexemeLength = entry.getKey().length();
            counter.put(lexemeLength, counter.getOrDefault(lexemeLength, 0) + 1);
        }
        ArrayList<Map.Entry<Integer, Integer>> sortedList = new ArrayList<>(counter.entrySet());
        sortedList.sort( (Comparator.comparingInt( (Map.Entry<Integer, Integer> entry) -> entry.getKey()).reversed() ));

        System.out.println("Sorted Lexeme Counts in tokenLookupMap:");
        System.out.println(sortedList);
    }


    public void displayRegexpRules() {
        List<LexerRule> rules = new ArrayList<>();
        for (LexerRule rule : lexerRules.values()) {
            if (rule instanceof RegexRule r) {
                rules.add(r);
            }
        }
        System.out.println("Displaying " + rules.size() + " regular expression rules:");
        for (LexerRule rule : rules) {
            System.out.println(rule);
        }
    }

    static void showLexemeTokenStats() {
        JSONPathEnvironment env = new JSONPathEnvironment();
        Lexer lexer = new Lexer(env);
        lexer.displayTokenLookup();
        System.out.println();
        lexer.displayLexemeCounts();

        System.out.println();
        //lexer.displayRules();
        System.out.println();
        //lexer.displayLexerTokens();

        /*
        Console output:
        Lexemes sorted by length (42):
        [undefined, contains, missing, false, False, true, null, True, Null, and, not, .., &&, ==, >=, <=, !=, ||, or, in, <>, =~, :, ,, ., ?, ], [, $, *, _, >, (, <, !, ), @, |, &, ^, #, ~]

        Sorted Lexeme Counts in tokenLookupMap:
        [9=1, 8=1, 7=1, 5=2, 4=4, 3=2, 2=11, 1=20]

        We can create a set for 1-char and 2-char lexemes for efficient scanning. The lexemes longer than 2 characters can all be matched with an Identifier pattern and TokenKind.
         */
    }

    static void showRegexpTokenStats() {
        JSONPathEnvironment env = new JSONPathEnvironment();
        Lexer lexer = new Lexer(env);
        lexer.displayRegexpRules();
    }

    static void t1() {
        JSONPathEnvironment env = new JSONPathEnvironment();
        Lexer lexer = new Lexer(env);
        //lexer.setWhitespacePolicy(WhitespacePolicy.STRICT);
        String jsonpath = "$[]^# >= <> and && ";
        var tokens = lexer.tokenize(jsonpath);
        System.out.println(tokens);
    }

    public static void main(String[] args) {
        //showRegexpTokenStats();
        //showLexemeTokenStats();
        t1();
    }

}