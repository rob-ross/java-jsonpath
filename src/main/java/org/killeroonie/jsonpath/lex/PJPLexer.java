package org.killeroonie.jsonpath.lex;

import org.killeroonie.jsonpath.*;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Implements the Lexer algorithm from the python-jsonpath project at <p>
 *  <a href="https://github.com/jg-rp/python-jsonpath">https://github.com/jg-rp/python-jsonpath</a>
 *  Full fidelity with that version also requires the proper Environment and RulesBuilder to be used,
 *  along with the Parser and Matcher, etc.
 */
public class PJPLexer extends BaseLexer{
    // Instance variables

    private final Map<String, TokenKind> tokenLookupMap = new LinkedHashMap<>(TokenKind.values().length, 1);
    private final IntKeyMap<String> oneCharLexemesMap = new IntKeyMap<>(String.class, 128);
    private final Set<String> twoCharLexemesSet = new HashSet<>();
    private final Map<String, TokenKind> keywordMap = new HashMap<>();

    private final EnumSet<TokenKind> specialProcessing = EnumSet.noneOf(TokenKind.class);

    /**
     * Constructor for Lexer.
     * @param env The JSONPathEnvironment configuration
     */
    public PJPLexer(JSONPathEnvironment env) {
        super(env);
    }

    @Override
    protected void buildRules(Map<TokenKind, RulesBuilder.LexerRule> lexerRulesMap) {
        // copy rules from the RulesBuilder
        lexerRulesMap.clear();
        specialProcessing.clear();
        Map<TokenKind, RulesBuilder.LexerRule> rules = getEnv().getRulesBuilder().getRules();
        // PJP has explicit checks and processing for these Token kinds
        specialProcessing.add(TokenKind.DOT_PROPERTY);
        specialProcessing.add(TokenKind.BARE_PROPERTY);
        specialProcessing.add(TokenKind.LIST_SLICE);
        specialProcessing.add(TokenKind.DOUBLE_QUOTE_STRING);
        specialProcessing.add(TokenKind.SINGLE_QUOTE_STRING);
        specialProcessing.add(TokenKind.INT);
        specialProcessing.add(TokenKind.RE_PATTERN);
        specialProcessing.add(TokenKind.NONE);
        specialProcessing.add(TokenKind.NULL);
        specialProcessing.add(TokenKind.FUNCTION);
        specialProcessing.add(TokenKind.SKIP);
        specialProcessing.add(TokenKind.ILLEGAL);

        lexerRulesMap.putAll(rules);
    }

    @Override
    public List<Token> tokenize(String jsonPathText) {
        final ScannerState scanner = initScanner(jsonPathText);
        Map<TokenKind, RulesBuilder.LexerRule> lexerRules = getLexerRulesMap();
        // current issue is that lexerRules isn't maintaining the order from PJPRulesBuilder, because it's defined as
        // and EnumMap in BaseLexer. Which we want to maintain. We may need to redefined lexerRules so it can be a
        // LinkedHashMap
        while ( currentChar() != EOF_CHAR) {
            System.out.printf("current char is %s, pos= %d%n", currentChar(), position());
            Matcher matcher = null;
            RulesBuilder.RegexRule regexRule = null;
            TokenKind kind = null;
            String matchtext = null;
            for (TokenKind key : lexerRules.keySet()) {
                regexRule = (RulesBuilder.RegexRule) lexerRules.get(key);
                matcher = regexRule.getRegionMatcher(jsonPathText, position());
                if (matcher.lookingAt()) {
                    matchtext = matcher.group();
                    kind = key;
                    break;
                }
            }
            assert kind != null;

            if (kind == TokenKind.DOT_PROPERTY) {
                scanner.advance(1); // consume the dot
                advanceToken(emitKind(kind), matcher.group("GPROP"));
            }
            else if (kind == TokenKind.BARE_PROPERTY) {
                // this doesn't seem to do anything different from the default behavior.
                advanceToken(emitKind(kind), matchtext);
            }
            else if (kind == TokenKind.LIST_SLICE) {
                // todo Token position will be off on STOP/STEP
                int startPos = position();
                advanceToken(TokenKind.SLICE_START, matcher.group("GLSLICESTART"));
                advanceToken(TokenKind.SLICE_STOP, matcher.group("GLSLICESTOP"));
                String sliceStep = matcher.group("GLSLICESTEP");
                if ( sliceStep != null ) {
                    advanceToken(TokenKind.SLICE_STEP, sliceStep);
                } else {
                    advanceToken(TokenKind.SLICE_STEP,"");
                }
                if (startPos <= position()) {
                    // position hasn't advanced due to empty slice parts
                    scanner.advance(matchtext.length());
                }
            }
            else if ( kind == TokenKind.DOUBLE_QUOTE_STRING ) {
                scanner.advance(1); // opening quote
                advanceToken(emitKind(kind), matcher.group("GDQUOTE"));
                scanner.advance(1); // closing quote
            }
            else if ( kind == TokenKind.SINGLE_QUOTE_STRING ) {
                scanner.advance(1); // opening quote
                advanceToken(emitKind(kind), matcher.group("GSQUOTE"));
                scanner.advance(1); // closing quote
            }
            else if ( kind == TokenKind.INT ) {
                // PJP treats positive exponents without fractions as ints.
                if (matcher.group("GEXP") != null && matcher.group("GEXP").charAt(1) == '-') {
                    advanceToken(TokenKind.FLOAT, matchtext);
                } else {
                    advanceToken(TokenKind.INT, matchtext);
                }
            }
            else if ( kind == TokenKind.RE_PATTERN ) {
                // todo Token position will be off on RE_FLAGS
                advanceToken( TokenKind.RE_PATTERN, matcher.group("GRE"));
                advanceToken( TokenKind.RE_FLAGS, matcher.group("GREFLAGS"));
            }
            else if ( kind == TokenKind.NONE  || kind == TokenKind.NULL ) {
                // this behavior is not different from the default, no reason for a separate rule here
                // emitKind() will emit a TokenKind.NIL TokenKind for either of these
                advanceToken(emitKind(kind), matchtext);
            }
            else if ( kind == TokenKind.FUNCTION) {
                advanceToken(emitKind(kind), matcher.group("GFUNC"));
            }
            else if ( kind == TokenKind.SKIP ) {
                scanner.advance(matchtext.length());
            }
            else if ( kind == TokenKind.ILLEGAL ) {
                throw new JSONPathSyntaxException("unexpected token %s".formatted(matchtext),
                        new Token(TokenKind.ILLEGAL, matchtext , position(), jsonPathText));
            }
            else {
                // standard behavior for the non-special tokens
                advanceToken(emitKind(kind), matchtext);
            }
        }
        return scanner.getTokenList();
    }

    static void t1() {
        JSONPathEnvironment env = new PJPEnv();
        var lexer = env.getLexer();
        assert lexer.getClass() == PJPLexer.class : "lexer class is " + lexer.getClass();

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
