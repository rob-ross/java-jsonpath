package org.killeroonie.jsonpath.lex;
import org.killeroonie.jsonpath.IntKeyMap;
import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;

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

order for method modifiers:
1. public, protected, private
2. abstract
3. static
4. final
5. synchronized
6. native
7. strictfp

class modifiers:
1. public, protected, private
2. abstract
3. static
4. final
5. synchronized
6. native
7. strictfp
 */

/**
 * Tokenizes a JSONPath string.
 * Some customization can be achieved by subclassing Lexer and setting
 * class attributes. Then setting lexer_class on a JSONPathEnvironment.
 * Class attributes:
 *     key_pattern: The regular expression pattern used to match mapping
 *         keys/properties.
 *     logical_not_pattern: The regular expression pattern used to match
 *         logical negation getTokenList. By default, `not` and `!` are
 *         equivalent.
 *     logical_and_pattern: The regular expression pattern used to match
 *         logical and getTokenList. By default, `and` and `&&` are equivalent.
 *     logical_or_pattern: The regular expression pattern used to match
 *         logical or getTokenList. By default, `or`and `||` are equivalent.
 */
public class Lexer extends BaseLexer{

    // Instance variables
    private final Map<String, TokenKind> tokenLookupMap = new LinkedHashMap<>(TokenKind.values().length, 1);
    // This maps single-char lexemes as their char int values to their String representations. This avoids String creation
    // during the scanner loop.
    private final IntKeyMap<String> oneCharLexemesMap = new IntKeyMap<>(String.class, 128);

    // todo - turn twoCharLexeme set into a map of two-chars to String so we don't have to convert the two chars to a
    // new String object every loop iteration.
    private final Set<String> twoCharLexemesSet = new HashSet<>();
    private final Map<String, TokenKind> keywordMap = new HashMap<>();



    /**
     * Constructor for Lexer.
     * todo - maybe we can have a no-arg constructor that creates a DefaultEnvironment ?
     *
     * @param env The JSONPathEnvironment configuration
     */
    public Lexer(JSONPathEnvironment env) {
        super(env);
    }


    /**
     * Populates the {@code customRules} Map.
     *
     * The default Lexer implementation provides no custom rules.
     * Subclasses can override {@code buildCustomRules()} to specify custom matching and {@code TokenKind} emitting rules.
     * When {@code buildRules} is called from this class' constructor, any custom rules specified here for a TokenKind
     * will be used instead of the default values as defined in TokenKind. <p>
     * {@code Note:} this implementation should be interpreted as an example of how subclasses could implement this method. The
     * actual `custom rules` used here are exactly the same as the default rules for each TokenKind, so no new behavior
     * is actually implemented here.
     *
     */
    @Override
    protected void buildCustomRules(EnumMap<TokenKind, RulesBuilder.LexerRule> customRulesMap) {
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
        customRulesMap.clear();
        customRulesMap.put(TokenKind.BARE_PROPERTY,
                new RulesBuilder.RegexRule(Pattern.compile(key_pattern), TokenKind.BARE_PROPERTY)
        );
        customRulesMap.put(TokenKind.NOT_EXT,
                new RulesBuilder.LexemeRule(logical_not_pattern, TokenKind.NOT)
        );
    }


    /**
     * Builds this Lexer's rules based on the default rules and including any custom rules
     * for the Lexer and the JSONPathEnvironment. It also populates {@code tokenLookupMap},
     * {@code oneCharLexemesMap}, {@code twoCharLexemesSet}, and {@code keywordMap}
     */
    @Override
    protected void buildRules(Map<TokenKind, RulesBuilder.LexerRule> lexerRulesMap) {
        lexerRulesMap.clear();
        // start with default rules
        // temp, we should get the rules builder from the Environment
        EnumMap<TokenKind, RulesBuilder.LexerRule> rules =  new DefaultRulesBuilder().getRules();
        // Now we apply custom rules from the Env and Lexer. Env rules have the highest priority, then Lexer rules.
        for (var entry: rules.entrySet()) {
            TokenKind kind = entry.getKey();
            RulesBuilder.LexerRule customRule = findRule(kind);
            if  (customRule != null) {
                rules.put(kind, customRule); // replace the default rule with the custom rule.
            }
            RulesBuilder.LexerRule rule = rules.get(kind);
            if (rule instanceof RulesBuilder.LexemeRule(String lexeme, TokenKind emitKind) && lexeme != null) {
                // looking up a null or by a regex pattern string is useless, so we omit these from the lookup map
                tokenLookupMap.put(lexeme, emitKind);
//                System.out.println("   buildRules():  tokenLookupMap.put(lexeme=" + lexeme + ", emitKind=" + emitKind + "):");
            }
        }
        // generate the lexeme sets for matching
        for (var entry: rules.entrySet()) {
            TokenKind kind = entry.getKey();
            RulesBuilder.LexerRule rule = entry.getValue();
            if (rule instanceof RulesBuilder.LexemeRule lr) {
                int length = lr.lexeme().length();
                if (length == 1) {
                    oneCharLexemesMap.put(lr.lexeme().charAt(0), lr.lexeme()); // all lexemes are in the BMP, so this is safe.
                } else if (length == 2) {
                    twoCharLexemesSet.add(lr.lexeme());
                }
                if ( kind.isKeyword() ) {
                    keywordMap.put(lr.lexeme(), kind);
                }
            }
        }
        // copy rules to instance variable as passed in the argument
        lexerRulesMap.putAll(rules);
    }


    //*************************************************************************
    //*    TOKENIZE
    //*************************************************************************

    /* Java Regex behavior - quick observations
    matcher.find() acts like Python's re pattern.search(), with no ancher chars (^,%). It will match any substring in
    the text against the pattern, even if the pattern does not start at the beginning of the text.
    matcher.matches() works like Python's pattern.match() if the pattern ends with $.
    matcher.lookingAt() works like pattern.search() if the pattern starts with ^

     */

    /**
     * Generate a sequence of getTokenList from a JSONPath string.
     *
     * @param jsonPathText The JSONPath string to tokenize
     * @return Iterator of Token objects
     */
    @Override
    public List<Token> tokenize(String jsonPathText) {
        final ScannerState scanner = initScanner(jsonPathText);
        char EOF_CHAR = '\0';
        Map<TokenKind, RulesBuilder.LexerRule> lexerRules = getLexerRulesMap();
        while ( currentChar() != EOF_CHAR) {
            System.out.printf("current char is %s, pos= %d%n", currentChar(), position());
            Matcher matcher;
            RulesBuilder.RegexRule regexRule;
            regexRule = (RulesBuilder.RegexRule) lexerRules.get(TokenKind.SPACE);
            if ( inFirstSet(regexRule) ) {
                matcher = regexRule.getRegionMatcher(jsonPathText, position());
                if (matcher.lookingAt()) {
                    String spaces = matcher.group();
                    if (!spaces.isEmpty() && getWhitespacePolicy() == WhitespacePolicy.STRICT) {
                        scanner.advanceToken(TokenKind.SPACE, spaces);
                    } else {
                        scanner.advance(spaces.length());  // advance without adding Token
                    }
                    continue;
                }
            }

            // todo - this is the only place in the loop we create String objects. We'll want to refactor this like we
            // did with the oneCharLexemesMap so we don't have to create new Strings in each iteration.
            String firstTwoChars = scanner.peekNextChars(2);
            if (twoCharLexemesSet.contains(firstTwoChars)) {
                final TokenKind kind = tokenLookupMap.get(firstTwoChars);
                scanner.advanceToken( emitKind(kind),  firstTwoChars);
                continue;
            }
            String lexeme = oneCharLexemesMap.get(currentChar());
            if ( lexeme != null ){
                final TokenKind kind = tokenLookupMap.get(lexeme);
                scanner.advanceToken( emitKind(kind),  lexeme);
                continue;
            }

            // Identifiers and keywords also handled here
            // (member-name-shorthand, true, false, null, function names, extension keywords)
            //-----------------------------------------------------------------------------------------
            regexRule = (RulesBuilder.RegexRule) lexerRules.get(TokenKind.IDENTIFIER);
            matcher = regexRule.getRegionMatcher(jsonPathText, position());
            if (matcher.lookingAt()) {
                processIdentifier(matcher.group());
                continue;
            }

            // String literals
            // todo combine these into a single method
            regexRule = (RulesBuilder.RegexRule) lexerRules.get(TokenKind.SINGLE_QUOTE_STRING);
            if ( inFirstSet(regexRule) ) {
                matcher = regexRule.getRegionMatcher(jsonPathText, position());
                if (matcher.lookingAt()) {
                    scanner.advanceToken( emitKind(TokenKind.SINGLE_QUOTE_STRING),  matcher.group());
                    continue;
                }
            }
            regexRule = (RulesBuilder.RegexRule) lexerRules.get(TokenKind.DOUBLE_QUOTE_STRING);
            if ( inFirstSet(regexRule) ) {
                matcher = regexRule.getRegionMatcher(jsonPathText, position());
                if (matcher.lookingAt()) {
                    scanner.advanceToken( emitKind(TokenKind.DOUBLE_QUOTE_STRING),  matcher.group());
                    continue;
                }
            }

            // Slice selector
            regexRule = (RulesBuilder.RegexRule) lexerRules.get(TokenKind.LIST_SLICE);
            matcher = regexRule.getRegionMatcher(jsonPathText, position());
            if (matcher.lookingAt()) {
                scanner.advanceToken( emitKind(TokenKind.LIST_SLICE),  matcher.group());
                continue;
            }


            // Number literals
            // Slice selector
            regexRule = (RulesBuilder.RegexRule) lexerRules.get(TokenKind.NUMBER);
            if ( inFirstSet(regexRule) ) {
               // matcher = regexRule.pattern().matcher(jsonPathText);
                matcher = regexRule.getRegionMatcher(jsonPathText, position());

                if (matcher.lookingAt()) {
                    scanner.advanceToken( emitKind(TokenKind.NUMBER),  matcher.group());
                    continue;
                }
            }

            // Illegal Character
            System.out.println(scanner.getTokenList());
            String errMsg = "Unrecognized character: '%c' at start of string '%s'"
                    .formatted( scanner.currentChar(),jsonPathText.substring( scanner.getPositionIndex() ) );
            throw new JSONPathSyntaxException( errMsg
            , new Token( TokenKind.ILLEGAL, String.valueOf(scanner.currentChar()), scanner.getPositionIndex(), jsonPathText));

        }
        return scanner.getTokenList();
    }

    protected final boolean inFirstSet(RulesBuilder.RegexRule regexRule) {
        boolean isInFirstSet = true;
        if (regexRule.hasFirstSet()) {
            isInFirstSet = regexRule.firstSetContains(getScannerState().currentChar());
        }
        return isInFirstSet;
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
        // getTokenList as that library for the same input.
        TokenKind kind = keywordMap.get(text);
        ScannerState scannerState = getScannerState();
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

        // python-jsonpath creates explicit getTokenList for functions and name selectors
        // (TOKEN_FUNCTION and TOKEN_BARE_PROPERTY
        // but here we just combine both into an IDENTIFIER
        scannerState.advanceToken(emitKind(TokenKind.IDENTIFIER), text);
    }

    public Iterator<Token> tokenizeOld(String jsonPathText) {
        initScanner(jsonPathText);
        final List<Token> tokens = getScannerState().getTokenList();  // for readability



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



    //**************************************************************************


    static void t2() {
        /*
         * This test creates an anonymous JSONPathEnvironment subclass so we can test adding a custom rule.
         */
        Lexer lexer2 = new Lexer(new  JSONPathEnvironment(){
            @Override
            public EnumMap<TokenKind, RulesBuilder.LexerRule> buildCustomRules() {
                var rules = super.buildCustomRules();
                rules.put(TokenKind.PSEUDO_ROOT, new RulesBuilder.LexemeRule("%", TokenKind.PSEUDO_ROOT));
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
        for (RulesBuilder.LexerRule rule : getLexerRulesMap().values()) {
            System.out.println(rule);
        }
    }

    public void displayTokenLookup() {
        System.out.println("Displaying " + tokenLookupMap.size() + " getTokenList in tokenLookupMap:");
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
        List<RulesBuilder.LexerRule> rules = new ArrayList<>();
        for (RulesBuilder.LexerRule rule : getLexerRulesMap().values()) {
            if (rule instanceof RulesBuilder.RegexRule r) {
                rules.add(r);
            }
        }
        System.out.println("Displaying " + rules.size() + " regular expression rules:");
        for (RulesBuilder.LexerRule rule : rules) {
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
        var lexer = env.getLexer();
        assert lexer.getClass() == Lexer.class : "lexer class is " + lexer.getClass();
        //lexer.setWhitespacePolicy(WhitespacePolicy.STRICT);
        String jsonpath;
        jsonpath = "$[]^# >= <> and && foofar bar() $.foo 1 2 3 4 5 789 \"I am groot\" 'I am also groot' 1 1: :1 00 01";
        //jsonpath = "::-1:";
        //jsonpath = "1 foo";
        var tokens = lexer.tokenize(jsonpath);
        System.out.println("input text: " + jsonpath);
        System.out.println(tokens);
    }

    public static void main(String[] args) {
        //showRegexpTokenStats();
        //showLexemeTokenStats();
        t1();
    }

}