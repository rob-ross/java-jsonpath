package org.killeroonie.jsonpath.parser;

import org.jetbrains.annotations.NotNull;
import org.killeroonie.jsonpath.*;
import org.killeroonie.jsonpath.exception.JSONPathException;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;
import org.killeroonie.jsonpath.exception.JSONPathTypeException;
import org.killeroonie.jsonpath.lexer.RulesBuilder;
import org.killeroonie.jsonpath.parser.segment.JSONChildSegment;
import org.killeroonie.jsonpath.parser.segment.JSONPathRecursiveDescentSegment;
import org.killeroonie.jsonpath.parser.segment.JSONPathSegment;
import org.killeroonie.jsonpath.parser.selector.*;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;

public class PJPParser extends BaseParser {


    static final EnumSet<TokenKind> START_KINDS  = EnumSet.of(TokenKind.ROOT, TokenKind.PSEUDO_ROOT);
    // Static dispatch table shared by all instances
    private static final Map<TokenKind, Function<PJPParser, Expression>> TOKEN_KIND_TO_FUNCTION;
    static {
        EnumMap<TokenKind, Function<PJPParser, Expression>> map = new EnumMap<>(TokenKind.class);

        // String literals
        map.put(TokenKind.DOUBLE_QUOTE_STRING, PJPParser::parseStringLiteral);
        map.put(TokenKind.SINGLE_QUOTE_STRING, PJPParser::parseStringLiteral);

        // Booleans
        map.put(TokenKind.TRUE,  PJPParser::parseBoolean);
        map.put(TokenKind.FALSE, PJPParser::parseBoolean);

        // Numbers
        map.put(TokenKind.INT,   PJPParser::parseIntegerLiteral);
        map.put(TokenKind.FLOAT, PJPParser::parseDoubleLiteral);

        // Grouping and prefix
        map.put(TokenKind.LPAREN, PJPParser::parseGroupedExpression);
        map.put(TokenKind.NOT,    PJPParser::parsePrefixExpression);

        // Null literal
        map.put(TokenKind.NULL, PJPParser::parseNull);

        // Query roots
        map.put(TokenKind.ROOT,    PJPParser::parseRootQuery);
        map.put(TokenKind.CURRENT, PJPParser::parseRelativeQuery);

        // Function extension
        map.put(TokenKind.FUNCTION, PJPParser::parseFunctionExtension);

        TOKEN_KIND_TO_FUNCTION = Collections.unmodifiableMap(map);
    }

    public enum PrecedenceLevel {
        LOWEST(1),
        LOGICAL_OR(3),
        LOGICAL_AND(4),
        RELATIONAL(5),
        PREFIX(7);

        private final int level;

        PrecedenceLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    private static final Map<TokenKind, PrecedenceLevel> PRECEDENCES;
    static {
        Map<TokenKind, PrecedenceLevel> map = new EnumMap<>(TokenKind.class);
        map.put(TokenKind.AND, PrecedenceLevel.LOGICAL_AND);
        map.put(TokenKind.EQ,  PrecedenceLevel.RELATIONAL);
        map.put(TokenKind.GE,  PrecedenceLevel.RELATIONAL);
        map.put(TokenKind.GT,  PrecedenceLevel.RELATIONAL);
        map.put(TokenKind.LE,  PrecedenceLevel.RELATIONAL);
        map.put(TokenKind.LT,  PrecedenceLevel.RELATIONAL);
        map.put(TokenKind.NE,  PrecedenceLevel.RELATIONAL);
        map.put(TokenKind.NOT,    PrecedenceLevel.PREFIX);
        map.put(TokenKind.OR,     PrecedenceLevel.LOGICAL_OR);
        map.put(TokenKind.RPAREN, PrecedenceLevel.LOWEST);

        PRECEDENCES = map;
    }

    private static final Set<TokenKind> COMPARISON_OPERATORS;
    static {
        Set<TokenKind> set = EnumSet.noneOf(TokenKind.class);
        set.add(TokenKind.EQ);
        set.add(TokenKind.GE);
        set.add(TokenKind.GT);
        set.add(TokenKind.LE);
        set.add(TokenKind.LT);
        set.add(TokenKind.NE);
        COMPARISON_OPERATORS = set;
    }

    private static final Set<TokenKind> BINARY_OPERATORS;
    static {
        Set<TokenKind> set = EnumSet.copyOf(COMPARISON_OPERATORS);
        set.add(TokenKind.AND);
        set.add(TokenKind.OR);
        BINARY_OPERATORS = set;
    }



    private TokenStream stream;



    public PJPParser(JSONPathEnvironment env) {
        super(env);
    }




    /**
     * Parses a JSONPath from a stream of tokens.
     * @param stream the stream of Tokens
     * @return the List of JSONPathSegment
     */
    @Override
    public List<JSONPathSegment> parse(final @NotNull TokenStream stream) {
        this.stream = stream;
        stream.expect(START_KINDS);
        stream.next();
        List<JSONPathSegment> segments = new ArrayList<>(parseQuery(false));

        if ( stream.current() != Token.EOF ) {
            throw new JSONPathSyntaxException(
                    "unexpected token %s".formatted(stream.current().value()), stream.current());
        }
        return segments;
    }

    private static final EnumSet<TokenKind> parseQuerySet =
            EnumSet.of(TokenKind.LBRACKET, TokenKind.PROPERTY, TokenKind.WILD);
    /**
     * Parse a top-level JSONPath expression or a filter query.
     * @param inFilter true if this method should parse the current position as a filter query
     * @return the List of JSONPathSegment
     */
    protected List<JSONPathSegment> parseQuery(final boolean inFilter) {
        List<JSONPathSegment> segments = new ArrayList<>();
        while (true) {
            final TokenKind kind = stream.currentKind();
            switch (kind) {
                case DOUBLE_DOT -> {
                    Token tok =  stream.next();
                    List<JSONPathSelector> selectors = parseSelectors();
                    segments.add( new JSONPathRecursiveDescentSegment( getEnv(), tok, selectors));
                }
                case LBRACKET, PROPERTY, WILD -> {
                    Token tok = stream.current();
                    List<JSONPathSelector> selectors = parseSelectors();
                    segments.add( new JSONChildSegment( getEnv(), tok, selectors));
                }
                default -> {
                    if ( inFilter) {
                        stream.backtrack(); // was stream.push() in Python. 
                    }
                    stream.next();
                    return segments;
                }
            }
        }
    }


    protected List<JSONPathSelector> parseSelectors() {
        List<JSONPathSelector> selectors = new ArrayList<>();
        final TokenKind tok = stream.currentKind();
        switch (tok) {
            case PROPERTY -> {
                selectors.add(new NameSelector( getEnv(), stream.current(), stream.current().value()));
            }
            case WILD -> {
                selectors.add(new WildcardSelector(getEnv(), stream.current()));
            }
            case LBRACKET -> {
                selectors.addAll(parseBracketedSelection());
            }
        }
        return selectors;
    }

    protected SliceSelector parseSlice() {
        RulesBuilder.LexerRule p = getEnv().findRule(TokenKind.LIST_SLICE);
        assert p instanceof RulesBuilder.RegexRule :
                "Expected Regexrule, got %s for %s".formatted(p, TokenKind.LIST_SLICE);

        RulesBuilder.RegexRule regexRule = (RulesBuilder.RegexRule) p;
        Matcher matcher = regexRule.pattern().matcher(stream.current().value());
        Long start = null;
        Long stop  = null;
        Long step  = null;

        if (matcher.lookingAt()) {
            String startStr = matcher.group(Constants.GN_SLICE_START);
            String  stopStr = matcher.group(Constants.GN_SLICE_STOP);
            String  stepStr = matcher.group(Constants.GN_SLICE_STEP);
            try {
                start = ( startStr == null || startStr.isEmpty() ) ?
                        null : Long.parseLong(stepStr);
            } catch (NumberFormatException e) {
                throw e; // todo do we wrap this in a framework exception?
            }
            try {
                stop = ( stopStr == null || stopStr.isEmpty() ) ?
                        null : Long.parseLong(stopStr);
            } catch (NumberFormatException e) {
                throw e; // todo do we wrap this in a framework exception?
            }
            try {
                // default step is 1 per spec
                step = ( stepStr == null || stepStr.isEmpty() ) ?
                        1 : Long.parseLong(stepStr);
            } catch (NumberFormatException e) {
                throw e; // todo do we wrap this in a framework exception?
            }
        } else {
            // throw invalid slice selector exception
            throw new JSONPathSyntaxException("Invalid slice literal: " + stream.current().value(), stream.current());
        }
        return new SliceSelector( getEnv() , stream.current(), start, stop, step);
    }

    protected IndexSelector parseIndexSelector() {

        // todo - reconcile Constants.INT_RE and INT2_RE
        // We should  get the Int regex rule from the Lexer. But the current
        // PJPRulesBuilder uses a rule for ints (INT_RE) that requires extra checking after the fact
        // for leading zeroes to ensure it's valid.

        Matcher matcher = Constants.INT2_PATTERN.matcher(stream.current().value());
        if (matcher.matches()) {
            return new IndexSelector(getEnv(), stream.current(), Long.parseLong(matcher.group()));
        } else {
            throw new JSONPathSyntaxException("invalid int literal", stream.current());
        }
    }

    /**
     * Parses a comma-separated list of JSONPath selectors
     * @return the List of JSONPath selectors
     */
    protected List<JSONPathSelector> parseBracketedSelection() {
        List<JSONPathSelector> selectors = new ArrayList<>();
        Token token = stream.next(); // consume LBRACKET
        while ( stream.current().kind() != TokenKind.RBRACKET ) {
            final TokenKind kind = stream.current().kind();
            switch (kind) {
                case LIST_SLICE -> {
                    selectors.add(parseSlice());
                }
                case NUMBER -> {
                    selectors.add(parseIndexSelector());
                }
                case DOUBLE_QUOTE_STRING, SINGLE_QUOTE_STRING -> {
                    selectors.add(new NameSelector( getEnv(), stream.current(), stream.current().value()));
                }
                case WILD -> {
                    selectors.add(new WildcardSelector(getEnv(), stream.current()));
                }
                case FILTER -> {
                    selectors.add(parseFilterSelector());
                }
                case EOF -> {
                    throw new JSONPathSyntaxException("unexpected end of query",  stream.current());
                }
                default -> {
                    throw new JSONPathSyntaxException(
                            "unexpected token in bracketed selection " + stream.currentKind(),
                            stream.current());
                }
            }

            if ( stream.peek() == Token.EOF) {
                throw new JSONPathSyntaxException("unexpected end of selector list",  stream.current());
            }

            if ( stream.peek().kind() != TokenKind.RBRACKET ) {
                stream.expectPeek(TokenKind.COMMA);
                stream.next();
                stream.peekNotExpected(TokenKind.RBRACKET, "unexpected trailing comma");
            }

            stream.next();
        }


        if (selectors.isEmpty()) {
            throw new JSONPathSyntaxException("empty bracketed segment", token);
        }
        return selectors;
    }

    protected FilterSelector  parseFilterSelector() {
        Token token = stream.next();
        Expression expr = parseFilterExpression();

        if ( expr instanceof FunctionExtension fe) {
            FilterFunction func = getEnv().getFunctionExtensionMap().get(fe.getName());
            if (func  != null && func.getReturnType() == FilterFunction.ExpressionType.VALUE) {
                throw new JSONPathTypeException("result of %s() must be compared".formatted( fe.getName()), token);
            }
        }
        if (expr instanceof FilterExpressionLiteral<?>) {
            throw new JSONPathSyntaxException(
                    "filter expression literals outside of function expressions " +
                            "must be compared", expr.getToken());
        }
        return new FilterSelector(getEnv(), token, new FilterExpression(expr.getToken(), expr));
    }

    protected Expression parseBoolean() {
        if (stream.currentKind() == TokenKind.TRUE) {
            return new BooleanLiteral(stream.current(), true);
        }
        return new BooleanLiteral(stream.current(), false);
    }

    protected Expression parseNull() {
        return new NullLiteral(stream.current(), null);
    }

    protected Expression parseStringLiteral() {
        return new StringLiteral(stream.current(), decodeStringLiteral(stream.current()));
    }

    protected Expression parseIntegerLiteral() {
        String value = stream.current().value();
        try {
            int i = Integer.parseInt(value);
            return new IntegerLiteral(stream.current(), i);
        } catch (NumberFormatException e) {
            throw new JSONPathSyntaxException("number value '" + value + "' is not a valid integer", stream.current());
        }
    }

    protected Expression parseDoubleLiteral() {
        String value = stream.current().value();
        try {
            double d = Double.parseDouble(value);
            return new DoubleLiteral(stream.current(), d);
        } catch (NumberFormatException ne) {
            throw new JSONPathSyntaxException("number value '" + value + "' is not a valid double", stream.current());
        }
    }



    protected Expression parsePrefixExpression() {
        Token token = stream.current();
        assert token.kind() == TokenKind.NOT;
        return new PrefixExpression(token, "!", parseFilterExpression(PrecedenceLevel.PREFIX));
    }

    protected Expression parseInfixExpression(Expression left) {
        Token token = stream.next();
        PrecedenceLevel precedence = PRECEDENCES.getOrDefault(token.kind(), PrecedenceLevel.LOWEST);
        Expression right = parseFilterExpression(precedence);
        if ( COMPARISON_OPERATORS.contains(token.kind()) ) {
            throwForNonComparableFunction(left, token);
            throwForNonComparableFunction(right, token);
            return new ComparisonExpression(token, left, token.kind(), right);
        }

        if (left instanceof FilterExpressionLiteral) {
            throw new JSONPathSyntaxException(
                    "filter expression literals outside of function expressions must be compared",
                    left.getToken());
        }
        if (right instanceof FilterExpressionLiteral) {
            throw new JSONPathSyntaxException(
                    "filter expression literals outside of function expressions must be compared",
                    right.getToken());
        }

        return new LogicalExpression(token, left, token.kind(), right);
    }

    protected Expression parseGroupedExpression() {
        stream.next();  // consume (
        Expression expr = parseFilterExpression();
        stream.next(); // todo what does this consume??

        while (stream.currentKind() != TokenKind.RPAREN ) {
            if (stream.currentKind() == TokenKind.EOF ) {
                throw new  JSONPathSyntaxException("unbalanced parentheses", stream.current());
            }
            // todo - only if binary op
            expr = parseInfixExpression(expr);
        }
        stream.expect(TokenKind.RPAREN);
        return expr;
    }

    protected Expression parseRootQuery() {
        Token root = stream.next();
        assert  root.kind() == TokenKind.ROOT : "Expecting a ROOT token";
        return new RootFilterQuery( root, new JSONPathQuery( getEnv(), parseQuery(true)));
    }

    protected Expression parseRelativeQuery() {
        Token token = stream.current();
        return new RelativeFilterQuery(
                token,
                new JSONPathQuery(getEnv(), parseQuery(true)
                ));
    }

    protected Expression parseFunctionExtension() {
        List<Expression> funcArgs = new ArrayList<>();
        Token token = stream.next();

        while (stream.currentKind() != TokenKind.RPAREN) {
            Function<PJPParser, Expression> func;
            func = TOKEN_KIND_TO_FUNCTION.get(stream.currentKind());
            if (func == null ) {
                throw new JSONPathSyntaxException("unexpected token in function extension", stream.current());
            }

            Expression expr = func.apply(this);
            // The argument could be a comparison or logical expression
            TokenKind peekKind = stream.peek().kind();
            while ( BINARY_OPERATORS.contains(peekKind) ) {
                stream.next();
                expr = parseInfixExpression(expr);
                peekKind = stream.peek().kind();
            }

            funcArgs.add(expr);

            if ( stream.peek().kind() != TokenKind.RPAREN) {
                stream.expectPeek(TokenKind.COMMA);
                stream.next();
            }
            stream.next();
        }

        return new FunctionExtension(
                token,
                token.value(),
                getEnv().validateFunctionExtensionSignature(token, funcArgs));
    }

    protected Expression parseFilterExpression() {
        return parseFilterExpression(PrecedenceLevel.LOWEST);
    }

    protected Expression parseFilterExpression(final PrecedenceLevel level) {
        Expression left;

        Function<PJPParser, Expression> fn = TOKEN_KIND_TO_FUNCTION.get(stream.currentKind());
        if (fn == null) {
            throw new JSONPathSyntaxException(
                    "unexpected token " + stream.currentKind(), stream.current());
        }
        try {
            left = fn.apply(this); // calls this.parseXxx()
        } catch (JSONPathException e) {
            String msg;
            if ( stream.currentKind() == TokenKind.EOF || stream.currentKind() == TokenKind.RBRACKET ) {
                msg = "end of expression";
            } else {
                msg = stream.current().value();
            }
            throw new JSONPathSyntaxException("unexpected " + msg, stream.current());
        }

        while ( true ) {
            TokenKind peekKind = stream.peek().kind();
            if ( peekKind == TokenKind.EOF ||
                 peekKind == TokenKind.RBRACKET ||
                 PRECEDENCES.getOrDefault(peekKind, PrecedenceLevel.LOWEST).getLevel() < level.getLevel()
             ) {
                break;
            }
            if (! BINARY_OPERATORS.contains(peekKind) ) {
                return left;
            }
            stream.next();
            left = parseInfixExpression(left);

        }
        return left;
    }

    ////////////////////////////////////////////////////////////////////
    /// STRING DECODING
    ////////////////////////////////////////////////////////////////////

    private String decodeStringLiteral(Token token) {
        String value;
        if (token.kind() == TokenKind.SINGLE_QUOTE_STRING)
        {
            value = token.value().replace("\"", "\\").replace("\\'", "'");
        } else {
            value = token.value();    
        }
        return unescapeString(value, token);
        
    }
    
    private String unescapeString(String value, Token token) {
        // Translate Python _unescape_string. We iterate by Unicode code point for non-escaped
        // characters because Java String is UTF-16, and iterating by char would split
        // supplementary characters into surrogate pairs, unlike Python which iterates by
        // code point. This ensures we mirror Python's behavior for direct non-escaped chars.
        StringBuilder unescaped = new StringBuilder();
        int index = 0;
        final int length = value.length();

        while (index < length) {
            char ch = value.charAt(index);
            if (ch == '\\') {
                // Encountered an escape. Move past the backslash and decode the sequence.
                index += 1;
                DecodeResult res = decodeEscapeSequence(value, index, token);
                unescaped.append(res.text);
                // res.index points at the last character consumed by the escape sequence.
                index = res.index + 1; // move to the next character after the escape sequence
            } else {
                // Validate and append the non-escaped code point.
                int codePoint = value.codePointAt(index);
                stringFromCodePoint(codePoint, token);
                unescaped.appendCodePoint(codePoint);
                index += Character.charCount(codePoint);
            }
        }
        return unescaped.toString();
    }

    // Translated from _decode_escape_sequence. Returns the decoded text and the index of the
    // last character consumed by the escape sequence (Java idiom uses a small helper class).
    private DecodeResult decodeEscapeSequence(String value, int index, Token token) {
        if (index >= value.length()) {
            // Backslash at end of string: incomplete escape.
            throw new JSONPathSyntaxException(
                    "incomplete escape sequence at index " + (token.index() + index - 1), token);
        }
        char ch = value.charAt(index);
        switch (ch) {
            case '"':
                return new DecodeResult("\"", index);
            case '\\':
                return new DecodeResult("\\", index);
            case '/':
                return new DecodeResult("/", index);
            case 'b':
                return new DecodeResult("\b", index);
            case 'f':
                return new DecodeResult("\f", index);
            case 'n':
                return new DecodeResult("\n", index);
            case 'r':
                return new DecodeResult("\r", index);
            case 't':
                return new DecodeResult("\t", index);
            case 'u': {
                HexResult hex = decodeHexChar(value, index, token);
                String text = stringFromCodePoint(hex.codePoint, token);
                return new DecodeResult(text, hex.index);
            }
            default:
                throw new JSONPathSyntaxException(
                        "unknown escape sequence at index " + (token.index() + index - 1), token);
        }
    }

    // Translated from _decode_hex_char. Decodes a U+XXXX escape sequence (the JSON "backslash-uXXXX" form) and, if a high surrogate is
    // encountered, validates and consumes a following low-surrogate escape to produce a
    // supplementary code point. Returns the code point and the index of the last consumed char.
    private HexResult decodeHexChar(String value, int index, Token token) {
        final int length = value.length();
        // index currently points at 'u' (the character after the backslash)
        if (index + 4 >= length) {
            // Need at least 'u' plus 4 hex digits following.
            throw new JSONPathSyntaxException(
                    "incomplete escape sequence at index " + (token.index() + index - 1), token);
        }
        int i = index + 1; // move past 'u' to first hex digit
        int hi = parseHexDigits(value, i, token);
        int consumedIndex = i + 3; // index of last hex digit consumed

        if (isLowSurrogate(hi)) {
            throw new JSONPathSyntaxException(
                    "unexpected low surrogate at index " + (token.index() + index - 1), token);
        }

        if (isHighSurrogate(hi)) {
            // Expect a following backslash-uXXXX sequence representing a low surrogate
            int next = consumedIndex + 1; // position after the 4 digits
            if (next + 5 >= length || value.charAt(next) != '\\' || value.charAt(next + 1) != 'u') {
                // In Python we'd raise something akin to "expected low surrogate" here.
                throw new JSONPathSyntaxException(
                        "expected low surrogate at index " + (token.index() + consumedIndex), token);
            }
            int lowStart = next + 2; // point at first hex digit of low surrogate
            int lo = parseHexDigits(value, lowStart, token);
            if (!isLowSurrogate(lo)) {
                throw new JSONPathSyntaxException(
                        "expected low surrogate at index " + (token.index() + lowStart - 1), token);
            }
            int codePoint = Character.toCodePoint((char) hi, (char) lo);
            int lastIndex = lowStart + 3; // last hex digit of the low surrogate
            return new HexResult(codePoint, lastIndex);
        }

        return new HexResult(hi, consumedIndex);
    }

    // Parses exactly 4 hex digits starting at 'start' into a 16-bit value.
    private int parseHexDigits(String s, int start, Token token) {
        if (start + 4 > s.length()) {
            throw new JSONPathSyntaxException(
                    "incomplete escape sequence at index " + (token.index() + start - 1), token);
        }
        int val = 0;
        for (int k = 0; k < 4; k++) {
            char c = s.charAt(start + k);
            int d = Character.digit(c, 16);
            if (d == -1) {
                throw new JSONPathSyntaxException(
                        "invalid unicode escape at index " + (token.index() + start + k - 1), token);
            }
            val = (val << 4) | d;
        }
        return val;
    }

    private boolean isLowSurrogate(int codeUnit) {
        return codeUnit >= 0xDC00 && codeUnit <= 0xDFFF;
    }

    private boolean isHighSurrogate(int codeUnit) {
        return codeUnit >= 0xD800 && codeUnit <= 0xDBFF;
    }

    // Returns the String for a validated code point, raising syntax errors for invalid values.
    private String stringFromCodePoint(int codePoint, Token token) {
        // JSON strings cannot contain unescaped control characters U+0000..U+001F.
        if (codePoint >= 0 && codePoint <= 0x1F) {
            throw new JSONPathSyntaxException(
                    "invalid control character in string at index " + token.index(), token);
        }
        // Disallow standalone surrogate code points (must only appear as UTF-16 pairs from escapes).
        if (isHighSurrogate(codePoint) || isLowSurrogate(codePoint)) {
            throw new JSONPathSyntaxException(
                    "invalid unpaired surrogate in string at index " + token.index(), token);
        }
        if (codePoint < 0 || codePoint > 0x10FFFF) {
            throw new JSONPathSyntaxException(
                    "invalid unicode code point at index " + token.index(), token);
        }
        // Java-specific: build String from code point.
        return new String(Character.toChars(codePoint));
    }

    // Small helper types to mirror Python's multiple return values.
    private static final class DecodeResult {
        final String text;
        final int index; // index of last consumed character in the source value
        DecodeResult(String text, int index) { this.text = text; this.index = index; }
    }

    private static final class HexResult {
        final int codePoint;
        final int index; // index of last consumed character in the source value
        HexResult(int codePoint, int index) { this.codePoint = codePoint; this.index = index; }
    }

    private void throwForNonComparableFunction( Expression expr, Token token) {
        if (expr instanceof FilterQuery && !((FilterQuery) expr).getQuery().isSingularQuery() ) {
            throw new JSONPathTypeException("non-singular query is not comparable", token);
        }

        if ( expr instanceof FunctionExtension fe) {
            FilterFunction func = getEnv().getFunctionExtensionMap().get(fe.getName());
            if ( func != null && func.getReturnType() != FilterFunction.ExpressionType.VALUE) {
                throw new JSONPathTypeException("result of %s() is not comparable".formatted(fe.getName() ), token );
            }
        }
    }

}
