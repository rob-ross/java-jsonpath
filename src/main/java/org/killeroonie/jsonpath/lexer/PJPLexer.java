package org.killeroonie.jsonpath.lexer;

import org.killeroonie.jsonpath.*;
import org.killeroonie.jsonpath.exception.JSONPathException;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the Lexer algorithm from the python-jsonpath project at <p>
 *  <a href="https://github.com/jg-rp/python-jsonpath">https://github.com/jg-rp/python-jsonpath</a>
 *  Full fidelity with that version also requires the proper Environment and RulesBuilder to be used,
 *  along with the Parser and Matcher, etc.
 */
public class PJPLexer extends BaseLexer{

    /**
     * Constructor for Lexer.
     * @param env The JSONPathEnvironment configuration
     */
    public PJPLexer(JSONPathEnvironment env) {
        super(env);
    }

    /**
     * Builds the rules for this Lexer and adds them to the argument Map.
     * @param lexerRulesMap the Map of Lexer rules for this instance. This Map will be mutated. It will be cleared, then
     *                      the rules added to them in definition order.
     */
    @Override
    protected void buildRules(Map<TokenKind, RulesBuilder.LexerRule> lexerRulesMap) {
        super.buildRules(lexerRulesMap);
    }

    @Override
    public List<Token> tokenize(final String jsonPathText) {
        final ScannerState  scanner = initScanner(jsonPathText);
        final Map<TokenKind, RulesBuilder.LexerRule> lexerRules = getLexerRulesMap();

        while ( currentChar() != EOF_CHAR) {
//            System.out.printf("current char is %s, pos= %d%n", currentChar(), position());
            Matcher matcher = null;
            RulesBuilder.RegexRule regexRule;
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
            assert kind != null : "Kind is null, ";

            if (kind == TokenKind.DOT_PROPERTY) {
                scanner.advance(1); // consume the dot
                advanceToken(emitKind(kind), matcher.group("GPROP"));
            }
            else if (kind == TokenKind.BARE_PROPERTY) {
                // this doesn't seem to do anything different from the default behavior.
                advanceToken(emitKind(kind), matchtext);
            }
            else if (kind == TokenKind.LIST_SLICE) {
                Token sliceStart = new Token(
                        TokenKind.SLICE_START,
                        matcher.group("GLSLICESTART"),
                        matcher.start("GLSLICESTART"),
                        jsonPathText
                );
                Token sliceStop = new Token(
                        TokenKind.SLICE_STOP,
                        matcher.group("GLSLICESTOP"),
                        matcher.start("GLSLICESTOP"),
                        jsonPathText
                );
                String sliceStepText = matcher.group("GLSLICESTEP") == null ? "" : matcher.group( "GLSLICESTEP");
                Token sliceStep = new Token(
                        TokenKind.SLICE_STEP,
                        sliceStepText,
                        matcher.start("GLSLICESTEP"),
                        jsonPathText
                );
                scanner.getTokenList().add(sliceStart);
                scanner.getTokenList().add(sliceStop);
                scanner.getTokenList().add(sliceStep);
                scanner.advance(matchtext.length());
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
                scanner.advance(1); // consume opening '/'
                advanceToken( TokenKind.RE_PATTERN, matcher.group("GRE"));
                scanner.advance(1);  // consume closing '/'
                advanceToken( TokenKind.RE_FLAGS, matcher.group("GREFLAGS"));
            }
            else if ( kind == TokenKind.NONE  || kind == TokenKind.NULL ) {
                // this behavior is not different from the default, no reason for a separate rule here
                // emitKind() will emit a TokenKind.NIL TokenKind for either of these
                advanceToken(emitKind(kind), matchtext);
            }
            else if ( kind == TokenKind.FUNCTION) {
                advanceToken(emitKind(kind), matcher.group("GFUNC"));
                scanner.advance(1); // to consume left-paren
            }
            else if ( kind == TokenKind.SKIP ) {
                scanner.advance(matchtext.length());
            }
            else if ( kind == TokenKind.ILLEGAL ) {
                throw new JSONPathSyntaxException("unexpected token %s".formatted(matchtext),
                        new Token(TokenKind.ILLEGAL, matchtext , position(), jsonPathText));
            }
            else //noinspection ConstantValue
                if ( kind != null ) {
                // standard behavior for the non-special tokens
                advanceToken(emitKind(kind), matchtext);
            }
            else {
                throw new JSONPathException("TokenKind is null. Position: " + position() +
                        ", current char: " + currentChar() + ", jsonPathText: " + jsonPathText);
            }
        }
        // remove spaces if whitespace policy is lenient.
        List<Token> tokens = enactWhitespacePolicy(scanner.getTokenList());
        tokens.add(Token.EOF);
        return tokens;
    }

    static void t1() {
        JSONPathEnvironment env = new PJPEnv();
        var lexer = env.getLexer();
        assert lexer.getClass() == PJPLexer.class : "lexer class is " + lexer.getClass();

        //lexer.setWhitespacePolicy(WhitespacePolicy.STRICT);
        String jsonpath;
        jsonpath = "$[]^# >= <> and && foofar bar() $.foo 1 2 3 4 5 789 \"I am groot\" 'I am also groot' 1 1: :1 00 01";

        var tokens = lexer.tokenize(jsonpath);
        System.out.println("input text: " + jsonpath);
        System.out.println(tokens);
    }

    static void t2() {
        /*
        Java Regex notes:
            matches(): must match the entire region (like ^pattern$ relative to region).
            lookingAt(): must match at the region start (like ^pattern relative to the region), can be shorter than region.
            find(): searches for the next occurrence anywhere in the region.

        Compared to python:
            Java Matcher.matches() ≈ Python re.fullmatch()
            Java Matcher.lookingAt() ≈ Python re.match() (or compiled_pattern.match(s, pos))
            Java Matcher.find() ≈ Python re.search(); repeated finds ≈ re.finditer() (or repeated search with updated pos)

            Porting hint: Python’s pattern.match(s, pos) ≈ Java’s matcher.region(pos, end).lookingAt().
            Python’s pattern.search(s, pos) ≈ Java’s matcher.region(pos, end).find().
         */
        String reStr = "[Nn]il\\b";
        String text = "nil == none";
        Pattern p = Pattern.compile(reStr);
        Matcher m = p.matcher(text);
        if (m.lookingAt()) {
            System.out.println("match found");
        } else {
            System.out.println("no match");
        }
    }

    // quick ad-hoc testing
    public static void main(String[] args) {
        t1();
        t2();
    }

}
