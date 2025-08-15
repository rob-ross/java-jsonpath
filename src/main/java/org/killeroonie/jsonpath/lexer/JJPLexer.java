package org.killeroonie.jsonpath.lexer;

import org.killeroonie.jsonpath.*;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;

import java.util.*;
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
 * Main Lexer class for Java-JSONPath. Attempts to have parity with python-jsonpath's feature set, with these exceptions:
 * - Does not treat `none` nor `nil` as special keywords. They can be used as regular identifiers
 */
public class JJPLexer extends BaseLexer{

    // Instance variables
    private final Map<String, TokenKind> tokenLookupMap = new LinkedHashMap<>(TokenKind.values().length, 1);

    // This maps single-char lexemes as their char int values to their String representations. This avoids String creation
    // during the scanner loop.
    private final IntKeyMap<String> oneCharLexemesMap = new IntKeyMap<>(String.class, 128);

    // todo - turn twoCharLexeme set into a map of two-chars to String so we don't have to convert the two chars to a
    // new String object every loop iteration.
    private final Set<String> twoCharLexemesSet = new HashSet<>();
    private final Map<String, TokenKind> keywordMap = new HashMap<>();
    private final Map<TokenKind, RulesBuilder.LexerRule> regExRuleMap = new LinkedHashMap<>();


    /**
     * Constructor for Lexer.
     *
     * @param env The JSONPathEnvironment configuration
     */
    public JJPLexer(JSONPathEnvironment env) {
        super(env);
    }


    /**
     * Builds this Lexer's rules based on the default rules and including any custom rules
     * for the Lexer and the JSONPathEnvironment. It also populates {@code tokenLookupMap},
     * {@code oneCharLexemesMap}, {@code twoCharLexemesSet}, and {@code keywordMap}
     */
    @Override
    protected void buildRules(final Map<TokenKind, RulesBuilder.LexerRule> lexerRulesMap) {
        super.buildRules(lexerRulesMap);
        tokenLookupMap.clear();
        for (var entry: lexerRulesMap.entrySet()) {
            TokenKind kind = entry.getKey();
            RulesBuilder.LexerRule rule = lexerRulesMap.get(kind);
            if (rule instanceof RulesBuilder.LexemeRule(String lexeme, TokenKind emitKind) && lexeme != null) {
                // looking up a null or by a regex pattern string is useless, so we omit these from the lookup map
                tokenLookupMap.put(lexeme, emitKind);
//                System.out.println("   buildRules():  tokenLookupMap.put(lexeme=" + lexeme + ", emitKind=" + emitKind + "):");
            }
            //System.out.println("key:" + entry.getKey() + ", value:"+entry.getValue());
        }

        // generate the lexeme and regex sets and maps for matching
        oneCharLexemesMap.clear();
        twoCharLexemesSet.clear();
        keywordMap.clear();
        regExRuleMap.clear();
        for (var entry: lexerRulesMap.entrySet()) {
            TokenKind kind = entry.getKey();
            RulesBuilder.LexerRule rule = entry.getValue();
            if (rule instanceof RulesBuilder.LexemeRule lr) {
                int length = lr.lexeme().length();
                if (length == 1) {
                    //System.out.println("writing to onecharLexemesMap: "+lr.lexeme());
                    oneCharLexemesMap.put(lr.lexeme().charAt(0), lr.lexeme()); // all lexemes are in the BMP, so this is safe.
                } else if (length == 2) {
                    twoCharLexemesSet.add(lr.lexeme());
                }
                if ( kind.isKeyword() ) {
                    keywordMap.put(lr.lexeme(), kind);
                }
            } else {
                regExRuleMap.put(kind, rule);
            }
        }
/*        System.out.println("One char lexemes map: "+oneCharLexemesMap.toString());
        for (int key : oneCharLexemesMap.keySet()) {
            System.out.println(oneCharLexemesMap.get(key));
        }*/

    }


    //*************************************************************************
    //*    TOKENIZE
    //*************************************************************************

    /* Java Regex behavior - quick observations
    matcher.find() acts like Python's re pattern.search(), with no anchor chars (^,%). It will match any substring in
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
    public List<Token> tokenize(final String jsonPathText) {
        final ScannerState                              scanner = initScanner(jsonPathText);
        final Map<TokenKind, RulesBuilder.LexerRule> lexerRules = getLexerRulesMap();
        while ( currentChar() != EOF_CHAR) {
//            System.out.printf("current char is %s, pos= %d%n", currentChar(), position());
            Matcher matcher;
            RulesBuilder.RegexRule regexRule;
            regexRule = (RulesBuilder.RegexRule) lexerRules.get(TokenKind.SPACE);
            if ( inFirstSet(regexRule) ) {
                matcher = regexRule.getRegionMatcher(jsonPathText, position());
                if (matcher.lookingAt()) {
                    scanner.advanceToken(TokenKind.SPACE, matcher.group());
                    continue;
                }
            }
            //4 of these RegExRules could all be processed in a simple loop
            // since they just match, and if they match, they emit the token with the matched text as the token value.
            // SPACE, LIST_SLICE, NUMBER, AND RE_PATTERN all are processed the same way
            // Quoted strings and Identifiers are processed a little differently.
            // Identifiers could be processed with a simple rule if we have a KEYWORD match rule
            // that runs before Identifiers and we match the KEYWORDS on a word boundary.
            // That would just leave Strings as special. Strings are "special" because of error handling and reporting.
            // if we encounter a quote in the input, we expect to eventually find that same quote before EOF, or we
            // have an unterminated String.
            // maybe we can add a handler function parameter to the RegExRule for cases like this. A callback function.
            // it would be null for all other RegExRules except for String.

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
            if ( Constants.SINGLE_QUOTE_CHAR == currentChar() ||
                 Constants.DOUBLE_QUOTE_CHAR == currentChar() ) {
                processStringLiteral(jsonPathText);
                continue;
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
                matcher = regexRule.getRegionMatcher(jsonPathText, position());
                if (matcher.lookingAt()) {
                    scanner.advanceToken( emitKind(TokenKind.NUMBER),  matcher.group());
                    continue;
                }
            }

            // regex pattern for =~ comparisons
            regexRule = (RulesBuilder.RegexRule) lexerRules.get(TokenKind.RE_PATTERN);
            if ( Constants.SLASH_CHAR == currentChar() ) {
                matcher = regexRule.getRegionMatcher(jsonPathText, position());
                if (matcher.lookingAt()) {
                    scanner.advanceToken( emitKind(TokenKind.RE_PATTERN), matcher.group());
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

            // Illegal Character
            //System.out.println(scanner.getTokenList());
            String errMsg = "Unrecognized character: '%c' at start of string '%s', jsonpath string: %s"
                    .formatted( scanner.currentChar(),jsonPathText.substring( scanner.getPositionIndex() ),jsonPathText );
            throw new JSONPathSyntaxException( errMsg
            , new Token( TokenKind.ILLEGAL, String.valueOf(scanner.currentChar()), scanner.getPositionIndex(), jsonPathText));
        }
        // remove spaces if whitespace policy is lenient.
        List<Token> tokens = enactWhitespacePolicy(scanner.getTokenList());
        tokens.add(Token.EOF);
        return tokens;
    }

    /**
     * Returns {@code true} if the caller should try scanning for the regex rule. This is either because
     * - there is no first-set
     * - there is a first-set and the current scanner character is present in the first-set.
     * Otherwise, this method returns {@code false}, indicating that a first-set exists for the rule AND the current character
     * was NOT found in that set.
     * @param regexRule the rule from which to extract a first-set
     * @return {@code false} if the current character is not the first-set, otherwise returns {@code true}.
     */
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
        TokenKind kind = keywordMap.get(text);
        ScannerState scannerState = getScannerState();
        if (kind != null) {
            // we scanned a keyword
            scannerState.advanceToken(emitKind(kind), text);
            return;
        }
        scannerState.advanceToken(emitKind(TokenKind.IDENTIFIER), text);
    }

    protected void processStringLiteral(final String jsonPathText) {
        RulesBuilder.RegexRule regexRule = (RulesBuilder.RegexRule) getLexerRulesMap().get(TokenKind.SINGLE_QUOTE_STRING);
        if ( inFirstSet(regexRule) ) {
            Matcher matcher = regexRule.getRegionMatcher(jsonPathText, position());
            if (matcher.lookingAt()) {
                advanceToken( emitKind(TokenKind.SINGLE_QUOTE_STRING),  matcher.group());
            } else {
                // region started with a single quote but couldn't finish scanning, so it's unterminated
                throw new JSONPathSyntaxException("Unterminated string literal %s".formatted(jsonPathText.substring(position())),
                        new Token(TokenKind.ILLEGAL, jsonPathText.substring(position()), position(),  jsonPathText));
            }

        }
        regexRule = (RulesBuilder.RegexRule) getLexerRulesMap().get(TokenKind.DOUBLE_QUOTE_STRING);
        if ( inFirstSet(regexRule) ) {
            Matcher matcher = regexRule.getRegionMatcher(jsonPathText, position());
            if (matcher.lookingAt()) {
                advanceToken( emitKind(TokenKind.DOUBLE_QUOTE_STRING),  matcher.group());
            } else {
                // region started with a single quote but couldn't finish scanning, so it's unterminated
                throw new JSONPathSyntaxException("Unterminated string literal %s".formatted(jsonPathText.substring(position())),
                        new Token(TokenKind.ILLEGAL, jsonPathText.substring(position()), position(),  jsonPathText));
            }
        }
    }


    // Helper classes and methods



    //**************************************************************************


    static void t2() {
        /*
         * This test creates an anonymous JSONPathEnvironment subclass so we can test adding a custom rule.
         */
//        JJPLexer lexer2 = new JJPLexer(new  JSONPathEnvironment(){
//            @Override
//            public Map<TokenKind, RulesBuilder.LexerRule> buildCustomEnvRules() {
//                var rules = super.buildCustomEnvRules();
//                rules.put(TokenKind.PSEUDO_ROOT, new RulesBuilder.LexemeRule("%", TokenKind.PSEUDO_ROOT));
//                return rules;
//            }
//        });
//        System.out.println("Lexer2: ");
//        lexer2.displayRules();
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
        JJPEnv env = new JJPEnv();
        JJPLexer lexer = new JJPLexer(env);
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
        JJPEnv env = new JJPEnv();
        JJPLexer lexer = new JJPLexer(env);
        lexer.displayRegexpRules();
    }

    static void t1() {
        JSONPathEnvironment env = new JJPEnv();
        var lexer = env.getLexer();
        assert lexer.getClass() == JJPLexer.class : "lexer class is " + lexer.getClass();
        //lexer.setWhitespacePolicy(WhitespacePolicy.STRICT);
        String jsonpath;
        jsonpath = "$[]^# >= <> and && foofar bar() $.foo 1 2 3 4 5 789 \"I am groot\" 'I am also groot' 1 1: :1 00 01";
        jsonpath = "::-1:";
        jsonpath = "1 \"foo\" [100]()";
        var tokens = lexer.tokenize(jsonpath);
        System.out.println();
        System.out.println("input text: " + jsonpath);
        System.out.println(tokens);
    }

    public static void main(String[] args) {
        //showRegexpTokenStats();
        //showLexemeTokenStats();
        t1();
    }

}