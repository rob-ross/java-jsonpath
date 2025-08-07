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


    // Instance variables
    private final JSONPathEnvironment env;
    // lexerTokens: The default Lexer implementation includes *all* TokenKinds.
    // Subclasses can override buildTokenList() to only select a subset of TokenKinds.
    private final EnumSet<TokenKind> lexerTokens;
    private final EnumMap<TokenKind, LexerRule> customRules;
    private final EnumMap<TokenKind, LexerRule> lexerRules;
    private final Map<String, TokenKind> tokenLookupMap =  new LinkedHashMap<>(TokenKind.values().length, 1);
    private transient ScannerState scannerState;
    private WhitespacePolicy whitespacePolicy = WhitespacePolicy.LENIENT;

    /**
     * Constructor for Lexer.
     *
     * @param env The JSONPathEnvironment configuration
     */
    public Lexer(JSONPathEnvironment env) {
        this.env = env;
        // The order of these operations is significant.
        lexerTokens = buildTokenSet();
        customRules = buildCustomRules();
        lexerRules =  buildRules();
    }

    /**
     * The default Lexer implementation includes *all* constants in {@link TokenKind}.
     * Subclasses can override {@code buildTokenList} to only select a subset of TokenKind constants.
     * @return the Set of {@link TokenKind}s that will be produced by this Lexer class.
     */
    public EnumSet<TokenKind> buildTokenSet() {
        EnumSet<TokenKind> lexerTokens = EnumSet.noneOf(TokenKind.class);
        lexerTokens.addAll(List.of(TokenKind.values()));
        return lexerTokens;
    }

    /**
     * The default Lexer implementation provides no custom rules.
     * Subclasses can override {@code buildCustomRules()} to specify custom matching and {@code TokenKind} emitting rules.
     * When {@code buildRules} is called from this class' constructor, any custom rules specified here for a TokenKind
     * will be used instead of the default values as defined in TokenKind. <p>
     * Note this implementation should be interpreted as an example of how subclasses could implement this method. The
     * actual `custom rules` used here are exactly the same as the default rules for each TokenKind, so no new behavior
     * is implemented.
     *
     * @return the Map of {@link TokenKind} to custom {@link LexerRule}s that will be used by Lexer class.
     */
    public EnumMap<TokenKind, LexerRule> buildCustomRules() {
        /*
         * Customization rules
         * Token lexemes and regexp match rules can be customized in both the environment and the Lexer.
         * The TokenKind class stores defaults for all TokenKinds
         * You can "override" matching rules for existing TokenKinds.
         * Create an entry in LexerRules that maps an exiting TokenKind to a new LexerRule. A LexerRule will allow
         * you to change the type of match (lexeme or regexp pattern), and the lexeme or pattern for that TokenKind.
         * You can also change the default emitKind to specify a different TokenKind that should be emitted when matching
         * your overridden TokenKind rule.
         * This buildRules method will search this path for locating the LexerRule for a token:
         * 1. Search for the Token in the Environment custom rules. If found, use that rule for the TokenKind
         * 2. If not found in the Env, search the Lexer for a custom rule and use that if found
         * 3. If still not found, use the default rule given by the TokenKind itself.
         *
         */
        final String key_pattern = "[\\u0080-\\uFFFFA-Za-z_][\\u0080-\\uFFFFA-Za-z0-9_-]*";
        final String logical_not_pattern = "not";
        final String logical_and_pattern = "and";
        final String logical_or_pattern = "or";
        final String double_quote_pattern = "\"(?<GDQUOTE>(?:(?!(?<!\\\\)\").)*?)\"";
        final String single_quote_pattern = "'(?<GSQUOTE>(?:(?!(?<!\\\\)').)*?)'";
        // .thing
        final String dot_property_pattern = "\\.(?<GPROP>" + key_pattern + ")";
        final String slice_list_pattern =
                "(?<GLSLICESTART>-?\\d*)\\s*" +
                        ":\\s*(?<GLSLICESTOP>-?\\d*)\\s*" +
                        "(?::\\s*(?<GLSLICESTEP>-?\\d*))?";
        // /pattern/ or /pattern/flags
        final String re_pattern = "/(?<GRE>.+?)/(?<GREFLAGS>[aims]*)";
        // func(
        final String function_pattern = "(?<GFUNC>[a-z][a-z_0-9]+)\\(\\s*";

        EnumMap<TokenKind, LexerRule> rules = new EnumMap<>(TokenKind.class);
        rules.put(TokenKind.BARE_PROPERTY,
                new RegexRule(key_pattern, TokenKind.BARE_PROPERTY)
        );
        rules.put(TokenKind.NOT_EXT,
                new LexemeRule(logical_not_pattern, TokenKind.NOT)
        );
        rules.put(TokenKind.AND_EXT,
                new LexemeRule(logical_and_pattern, TokenKind.AND)
        );
        rules.put(TokenKind.OR_EXT,
                new LexemeRule(logical_or_pattern, TokenKind.OR)
        );
        rules.put(TokenKind.DOUBLE_QUOTE_STRING,
                new RegexRule(double_quote_pattern, TokenKind.DOUBLE_QUOTE_STRING)
        );
        rules.put(TokenKind.SINGLE_QUOTE_STRING,
                new RegexRule(single_quote_pattern, TokenKind.SINGLE_QUOTE_STRING)
        );
        rules.put(TokenKind.DOT_PROPERTY,
                new RegexRule(dot_property_pattern, TokenKind.PROPERTY)
        );
        // todo python-jsonpath produces 3 tokens for the start, stop, end, We will refactor to just produce 1 Token
        rules.put(TokenKind.LIST_SLICE,
                new RegexRule(slice_list_pattern, TokenKind.LIST_SLICE)
        );
        rules.put(TokenKind.RE_PATTERN,
                new RegexRule(re_pattern, TokenKind.RE_PATTERN)
        );
        rules.put(TokenKind.FUNCTION,
                new RegexRule(function_pattern, TokenKind.FUNCTION)
        );

        return rules;
    }

    private LexerRule makeDefaultRule(TokenKind kind) {
        if (kind.isRegExp()) {
            return new RegexRule(kind.getDefaultPattern(), kind.getDefaultEmitKind());
        }
        return new LexemeRule(kind.lexeme(), kind.getDefaultEmitKind());
    }

    private LexerRule findRule(TokenKind kind) {
        Optional<LexerRule> rule = env.findRule(kind);
        if ( rule.isPresent() ) {
            return rule.get();
        }
        LexerRule customRule = customRules.getOrDefault(kind, null);
        if ( customRule != null ) { return customRule; }
        return makeDefaultRule(kind);
    }

    private EnumMap<TokenKind, LexerRule> buildRules() {
        EnumMap<TokenKind, Lexer.LexerRule> rules =  new EnumMap<>(TokenKind.class);
        for (TokenKind kind : lexerTokens) {
            LexerRule rule = findRule(kind);
            rules.put(kind, rule);
            if (rule instanceof LexemeRule(String lexeme, TokenKind emitKind) && lexeme != null) {
                // looking up a null or by a regex pattern string is useless, so we omit these from the lookup map
                tokenLookupMap.put(lexeme, emitKind);
                //System.out.println("   buildRules():  tokenLookupMap.put(lexeme=" + lexeme + ", emitKind=" + emitKind + "):");
            }
        }
        return rules;
    }


    private ScannerState initScanner(String jsonPathText) {
        // reset the Lexer state in preparation of tokenizing an input string
        return new ScannerState(jsonPathText);
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
        this.scannerState = initScanner(jsonPathText);
        final List<Token> tokens = scannerState.tokens;  // for readability
        int len = jsonPathText.length();
        while (scannerState.currentChar() != null) {
            CharSequence c = scannerState.currentChar();
            assert c != null;  // to make the linter happy
            int pos = scannerState.positionIndex;

            Matcher matcher;
            if ( Constants.BLANK_CHAR.contains(c)) {
                matcher = TokenKind.SPACE.getDefaultPattern().matcher(jsonPathText);
                if (matcher.find(pos)) {
                    String spaces = matcher.group();
                    if (!spaces.isEmpty() && getWhitespacePolicy() == WhitespacePolicy.STRICT) {
                        Token t = scannerState.advanceToken(TokenKind.SPACE, spaces);
                    } else {
                        scannerState.advance(spaces.length());  // advance without adding Token
                    }
                    continue;
                }
            } else {
                scannerState.advanceToken(TokenKind.UNDEFINED, c.toString());
            }

        }

        return tokens;
    }


    public Iterator<Token> tokenize1(String jsonPathText) {
        this.scannerState = initScanner(jsonPathText);
        final List<Token> tokens = scannerState.tokens;  // for readability



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

    private final class ScannerState {

        private final String jsonPathText;
        private int positionIndex;
        private final List<Token> tokens;

        private ScannerState(String jsonPathText) {
            this.jsonPathText = jsonPathText;
            this.positionIndex = 0;
            this.tokens = new ArrayList<>();
        }

        private CharSequence currentChar() {
            return jsonPathText.length() >  positionIndex ? jsonPathText.subSequence(positionIndex, positionIndex + 1) : null;
        }

        private void advance(int length){
            advanceImpl(length);
        }

        private void advance(LexemeRule rule) {
            advanceImpl(rule);
        }

        private void advance(Token token) {
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
            tokens.add(newToken);
            advance(newToken);
            return newToken;
        }


        /**
         * Return, without consuming, the first `numberOfChars` characters from the current position.
         * @param numberOfChars the number of characters to peek.
         * @return {@code numberOfChars} characters from the current scanner position. If the scanner position is at
         * the end of the input, this method returns null. If there are fewer characters remaining to be scanned than
         * requested in the argument, all characters from the current position to the end of the input are returned.
         */
        private @Nullable String peekNextChars(int numberOfChars) {
            int endpoint = Math.min(numberOfChars, jsonPathText.length() - positionIndex);
            if (endpoint >= jsonPathText.length()) { return null;}
            return jsonPathText.substring(positionIndex, endpoint);
        }

        private Token makeToken(TokenKind kind, String value) {
            return new Token(kind, value, positionIndex, jsonPathText);
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
    record RegexRule(Pattern pattern, TokenKind emitKind) implements LexerRule {

        public RegexRule(String patternString, TokenKind emitKind) {
            this(Pattern.compile(patternString, Pattern.DOTALL), emitKind);
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

    public WhitespacePolicy getWhitespacePolicy() {
        return this.whitespacePolicy;
    }
    //**************************************************************************
    static void t1() {
        JSONPathEnvironment env = new JSONPathEnvironment();
        Lexer lexer = new Lexer(env);
        String jsonpath = """
                $.foo.bar..[?@baz, 1, 3:5, *]""";
        var tokens = lexer.tokenize(jsonpath);
//        while (tokens.hasNext()) {
//            var token = tokens.next();
//            System.out.println(token);
//        }
    }

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

    public void displayLexerTokens() {
        System.out.println("Displaying " + lexerTokens.size() + " tokens:");
        for (TokenKind tk :  lexerTokens) {
            System.out.println(tk);
        }
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
        /*
        Console output:

        Displaying 12 regular expression rules:
        RegexRule[pattern=[ \n\t\r\.]+, emitKind=SKIP]
        RegexRule[pattern=\.(?<GPROP>[\u0080-\uFFFFA-Za-z_][\u0080-\uFFFFA-Za-z0-9_-]*), emitKind=PROPERTY]
        RegexRule[pattern=[\u0080-\uFFFFA-Za-z_][\u0080-\uFFFFA-Za-z0-9_-]*, emitKind=BARE_PROPERTY]
        RegexRule[pattern=(?<GLSLICESTART>-?\d*)\s*:\s*(?<GLSLICESTOP>-?\d*)\s*(?::\s*(?<GLSLICESTEP>-?\d*))?, emitKind=LIST_SLICE]
        RegexRule[pattern=(?<GFUNC>[a-z][a-z_0-9]+)\(\s*, emitKind=FUNCTION]
        RegexRule[pattern=-?\d+\.\d*(?:[eE][+-]?\d+)?, emitKind=FLOAT]
        RegexRule[pattern=-?\d+(?<GEXP>[eE][+\-]?\d+)?\b, emitKind=INT]
        RegexRule[pattern=/(?<GRE>.+?)/(?<GREFLAGS>[aims]*), emitKind=RE_PATTERN]
        RegexRule[pattern="(?<GDQUOTE>(?:(?!(?<!\\)").)*?)", emitKind=DOUBLE_QUOTE_STRING]
        RegexRule[pattern='(?<GSQUOTE>(?:(?!(?<!\\)').)*?)', emitKind=SINGLE_QUOTE_STRING]
        RegexRule[pattern=[Nn]il\b, emitKind=NIL]
        RegexRule[pattern=[Nn]one\b, emitKind=NONE]
         */
    }

    public static void main(String[] args) {
        showRegexpTokenStats();

    }

}