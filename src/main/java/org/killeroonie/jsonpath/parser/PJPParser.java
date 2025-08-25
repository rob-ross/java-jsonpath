package org.killeroonie.jsonpath.parser;

import org.jetbrains.annotations.NotNull;
import org.killeroonie.jsonpath.*;
import org.killeroonie.jsonpath.exception.JSONPathException;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;
import org.killeroonie.jsonpath.exception.JSONPathTypeException;
import org.killeroonie.jsonpath.lexer.RulesBuilder;
import org.killeroonie.jsonpath.parser.segment.JSONPathChildSegment;
import org.killeroonie.jsonpath.parser.segment.JSONPathRecursiveDescentSegment;
import org.killeroonie.jsonpath.parser.segment.JSONPathSegment;
import org.killeroonie.jsonpath.parser.selector.*;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        map.put(TokenKind.NUMBER, PJPParser::parseNumberLiteral );
        map.put(TokenKind.INT,   PJPParser::parseIntegerLiteral );
        map.put(TokenKind.FLOAT, PJPParser::parseDoubleLiteral) ;

        // Grouping and prefix
        map.put(TokenKind.LPAREN, PJPParser::parseGroupedExpression);
        map.put(TokenKind.NOT,    PJPParser::parsePrefixExpression);

        // Null literal
        map.put(TokenKind.NULL, PJPParser::parseNull);

        // Query roots
        map.put(TokenKind.ROOT,    PJPParser::parseAbsoluteQuery);
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
        MEMBERSHIP(6),
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

    private static final Set<TokenKind> INFIX_LITERAL_OPERATORS = EnumSet.copyOf(COMPARISON_OPERATORS);

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
        stream.consume(TokenKind.ROOT);
        List<JSONPathSegment> segments = new ArrayList<>(parseQuery());

        if ( stream.current() != Token.EOF ) {
            throw new JSONPathSyntaxException(
                    "unexpected token %s\n%s".formatted(stream.current().value(), stream.getTokens()), stream.current());
        }
        return segments;
    }

    private static final EnumSet<TokenKind> parseQuerySet =
            EnumSet.of(TokenKind.LBRACKET, TokenKind.PROPERTY, TokenKind.WILD);
    /**
     * Parse a JSONPath query string.
     * This method assumes the root identifier or current has
     * already been consumed.
     * @return the List of JSONPathSegment
     */
    protected List<JSONPathSegment> parseQuery() {
        List<JSONPathSegment> segments = new ArrayList<>();
        loop: while (true) {
            stream.consumeIf(TokenKind.SPACE);
            final TokenKind kind = stream.currentKind();
            switch (kind) {
                case DOT -> {
                    JSONPathSelector selector = parseShorthandSelector();
                    segments.add(new JSONPathChildSegment(getEnv(), stream.current(), List.of(selector)));
                }
                case DOUBLE_DOT -> {
                    Token token =  stream.consume(TokenKind.DOUBLE_DOT);
                    List<JSONPathSelector> selectors;
                    if ( stream.currentKind() == TokenKind.LBRACKET ) {
                        selectors = parseBracketedSelection();
                    } else {
                        selectors = List.of(parseShorthandSelector());
                    }
                    segments.add( new JSONPathRecursiveDescentSegment( getEnv(), token, selectors));
                }
                case LBRACKET -> {
                    Token token = stream.current();
                    List<JSONPathSelector> selectors = parseBracketedSelection();
                    segments.add( new JSONPathChildSegment( getEnv(), token, selectors));
                }
                case EOF -> {
                    break loop;
                }
                default -> {
                    // An embedded query.
                    break loop;
                }
            }
        }
        return segments;
    }

    protected JSONPathSelector parseShorthandSelector() {
        final Token token = stream.consume(TokenKind.DOT);
        JSONPathSelector selector;
        switch (token.kind()) {
            case IDENTIFIER ->  {
                selector = new NameSelector( getEnv(), token, token.value());
                stream.consume(TokenKind.IDENTIFIER);
            }
            case WILD ->  {
                selector = new WildcardSelector( getEnv(), token);
                stream.consume(TokenKind.WILD);
            }
            default -> throw new JSONPathSyntaxException("expected a shorthand selector, got " + token.kind(), token);
        }
        return selector;
    }

    /**
     * Parses a comma-separated list of JSONPath selectors
     * @return the List of JSONPath selectors
     */
    protected List<JSONPathSelector> parseBracketedSelection() {
        Token token = stream.consume(TokenKind.LBRACKET);
        List<JSONPathSelector> selectors = new ArrayList<>();

        loop: while ( stream.currentKind() != TokenKind.RBRACKET ) {
            token = stream.consumeIf(TokenKind.SPACE);
            final TokenKind kind = stream.currentKind();
            switch (kind) {
                case LIST_SLICE -> {
                    selectors.add(parseSlice());
                }
                case NUMBER -> {
                    selectors.add(parseIndexSelector());
                }
                case DOUBLE_QUOTE_STRING, SINGLE_QUOTE_STRING -> {
                    selectors.add(new NameSelector( getEnv(), stream.current(), stream.current().value()));
                    stream.next(); // consume the string
                }
                case WILD -> {
                    selectors.add(new WildcardSelector(getEnv(), stream.current()));
                    stream.consume(TokenKind.WILD);
                }
                case FILTER -> {
                    selectors.add(parseFilterSelector());
                }
//                case ROOT, IDENTIFIER -> {
//                    selectors.add( parseSingularQuerySelector());
//                }
                case SPACE -> { stream.consume(TokenKind.SPACE); continue; }
                case EOF -> {
                    throw new JSONPathSyntaxException("unexpected end of query",  stream.current());
                }
                default -> {
                    throw new JSONPathSyntaxException(
                            "unexpected token in bracketed selection " + stream.currentKind(),
                            stream.current());
                }
            }

            stream.consumeIf(TokenKind.SPACE);

            if ( stream.current() == Token.EOF) {
                System.out.println("stream.getTokens() = " + stream.getTokens());
                throw new JSONPathSyntaxException("unexpected end of selector list",  stream.current());
            }

            if ( stream.currentKind() != TokenKind.RBRACKET ) {
                stream.consume(TokenKind.COMMA);
                stream.consumeIf(TokenKind.SPACE);
                stream.throwIf(TokenKind.RBRACKET, "unexpected trailing comma");
            }


        }

        stream.consume(TokenKind.RBRACKET);

        if (selectors.isEmpty()) {
            throw new JSONPathSyntaxException("empty bracketed segment", token);
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
                        null : Long.parseLong(startStr);
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
        SliceSelector ss = new SliceSelector( getEnv() , stream.current(), start, stop, step);
        stream.consume(TokenKind.LIST_SLICE);
        return ss;
    }

    protected IndexSelector parseIndexSelector() {

        // todo - reconcile Constants.INT_RE and INT2_RE
        // We should  get the Int regex rule from the Lexer. But the current
        // PJPRulesBuilder uses a rule for ints (INT_RE) that requires extra checking after the fact
        // for leading zeroes to ensure it's valid.

        Matcher matcher = Constants.INT2_PATTERN.matcher(stream.current().value());
        if (matcher.matches()) {
            IndexSelector is = new IndexSelector( getEnv(), stream.current(), Long.parseLong(matcher.group() ));
            stream.consume(TokenKind.NUMBER);
            return is;
        } else {
            throw new JSONPathSyntaxException("invalid int literal", stream.current());
        }
    }



    protected FilterSelector  parseFilterSelector() {
        Token token = stream.consume(TokenKind.FILTER);
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
        BooleanLiteral result = stream.currentKind() == TokenKind.TRUE ? BooleanLiteral.TRUE : BooleanLiteral.FALSE;
        stream.next(); // consume 'true' or 'false'
        return result;
    }

    protected Expression parseNull() {
        stream.consume(TokenKind.NULL);
        return NullLiteral.NULL;
    }

    protected Expression parseStringLiteral() {
        Token strToken = stream.current();
        StringLiteral sl = new StringLiteral(strToken, decodeStringLiteral(strToken));
        stream.next();
        return sl;
    }

    protected Expression parseNumberLiteral() {
        Token token = stream.current();
        var rule = getEnv().findRule(TokenKind.INT);
        if (rule instanceof RulesBuilder.RegexRule rer) {
            Matcher m = rer.getRegionMatcher(token.value(), 0);
            if (m.matches()) {
                return parseIntegerLiteral();
            } else {
                return parseDoubleLiteral();
            }
        }
        throw new JSONPathException("No regex rule found for INT", stream.current());
    }

    protected Expression parseIntegerLiteral() {
        Token token = stream.current();
        stream.consume(TokenKind.NUMBER);
        String value = token.value();
        try {
            int i = Integer.parseInt(value);
            return new IntegerLiteral(token, i);
        } catch (NumberFormatException e) {
            throw new JSONPathSyntaxException("number value '" + value + "' is not a valid integer", token);
        }
    }

    protected Expression parseDoubleLiteral() {
        Token token = stream.current();
        stream.consume(TokenKind.NUMBER);
        String value = token.value();
        try {
            double d = Double.parseDouble(value);
            return new DoubleLiteral(token, d);
        } catch (NumberFormatException ne) {
            throw new JSONPathSyntaxException("number value '" + value + "' is not a valid double", token);
        }
    }

    protected Expression parsePrefixExpression() {
        Token token = stream.current();
        stream.consume(TokenKind.NOT);
        return new PrefixExpression(token, "!", parseFilterExpression(PrecedenceLevel.PREFIX));
    }

    /**
     * todo make this return InfixExpression, superclass of both Logical and Comparison Expressions
     * @return
     */
    protected Expression parseInfixExpression(Expression left) {
        Token token = stream.current();
        stream.next();
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
        stream.consume(TokenKind.LPAREN);
        Expression expr = parseFilterExpression();

        while (stream.currentKind() != TokenKind.RPAREN ) {
            if (stream.currentKind() == TokenKind.EOF || stream.currentKind() == TokenKind.RBRACKET ) {
                throw new  JSONPathSyntaxException("unbalanced parentheses", stream.current());
            }

            expr = parseInfixExpression(expr);
        }
        stream.consume(TokenKind.RPAREN);
        return expr;
    }

    protected Expression parseAbsoluteQuery() {
        Token root = stream.current();
        stream.consume(TokenKind.ROOT);
        return new RootFilterQuery( root, new JSONPathQuery( getEnv(), parseQuery()));
    }

    protected Expression parseRelativeQuery() {
        Token token = stream.consume(TokenKind.CURRENT);
        return new RelativeFilterQuery(
                token,
                new JSONPathQuery(getEnv(), parseQuery()
                ));
    }

    protected SingularQuerySelector parseSingularQuerySelector() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /*

        def parse_singular_query_selector(
        self, stream: TokenStream
    ) -> SingularQuerySelector:
        token = (
            stream.next() if stream.current().kind == TOKEN_ROOT else stream.current()
        )

        query = JSONPath(env=self.env, segments=self.parse_query(stream))

        if not query.singular_query():
            raise JSONPathSyntaxError(
                "embedded query selectors must be singular queries", token=token
            )

        return SingularQuerySelector(
            env=self.env,
            token=token,
            query=query,
        )

     */

    protected Expression parseFunctionExtension() {
        List<Expression> funcArgs = new ArrayList<>();
        Token function_token = stream.current();
        stream.next(); // consume function name
        stream.consume(TokenKind.LPAREN);

        while ( true ) {
            stream.consumeIf(TokenKind.SPACE);

            if (stream.currentKind() == TokenKind.RPAREN ) {
                break;
            }

            Function<PJPParser, Expression> func;
            func = TOKEN_KIND_TO_FUNCTION.get(stream.currentKind());
            if (func == null ) {
                throw new JSONPathSyntaxException("unexpected token in function extension", stream.current());
            }

            Expression expr = func.apply(this);
            stream.consume(TokenKind.SPACE);

            // The argument could be a comparison or logical expression
            while ( BINARY_OPERATORS.contains( stream.currentKind() )) {
                expr = parseInfixExpression(expr);
            }

            funcArgs.add(expr);
            stream.consumeIf(TokenKind.SPACE);

            if ( stream.currentKind() != TokenKind.RPAREN) {
                stream.consume(TokenKind.COMMA);
            }
        }

        stream.consume(TokenKind.RPAREN);

        return new FunctionExtension(
                function_token,
                function_token.value(),
                getEnv().validateFunctionExtensionSignature(function_token, funcArgs));
    }

    protected Expression parseFilterExpression() {
        return parseFilterExpression(PrecedenceLevel.LOWEST);
    }

    protected Expression parseFilterExpression(final PrecedenceLevel level) {
        Expression left;
        System.out.println("start parseFilterExpression: stream.currentKind() = " + stream.currentKind());
        stream.consumeIf(TokenKind.SPACE);

        Function<PJPParser, Expression> fn = TOKEN_KIND_TO_FUNCTION.get(stream.currentKind());
        if (fn == null) {
            throw new JSONPathSyntaxException(
                    "unexpected token " + stream.currentKind(), stream.current());
        }
        try {
            left = fn.apply(this); // calls this.parseXxx()
        } catch (JSONPathException e) {
            System.out.println("e = " + e);
            String msg;
            if ( stream.currentKind() == TokenKind.EOF || stream.currentKind() == TokenKind.RBRACKET ) {
                msg = "end of expression";
            } else {
                msg = stream.current().toString();
            }
            System.out.println("stream.currentKind() = " + stream.currentKind());
            throw new JSONPathSyntaxException("unexpected " + msg, stream.current());
        }

        while ( true ) {
            stream.consumeIf(TokenKind.SPACE);
            TokenKind kind = stream.currentKind();

            if ( ! BINARY_OPERATORS.contains(kind) ||
                    PRECEDENCES.getOrDefault(kind, PrecedenceLevel.LOWEST).getLevel() < level.getLevel())
            {
                break;
            }
            left = parseInfixExpression(left);
        }
        return left;
    }

    ////////////////////////////////////////////////////////////////////
    /// STRING DECODING
    ////////////////////////////////////////////////////////////////////

    private String decodeStringLiteral(Token token) {
        String value = token.value();
        if ( value.isEmpty() || value.equals( "''")) {
            return "''";
        }
        value = value.substring(1, value.length() - 1); // remove enclosing quotes
        if (token.kind() == TokenKind.SINGLE_QUOTE_STRING)
        {
            value = value.replace("\"", "\\").replace("'","\\'");
        } else {
            value = value.replace("\\\"", "\"");
        }
        System.out.println("value after replace  = " + value);

        return Constants.SINGLE_QUOTE + unescapeString(value, token) + Constants.SINGLE_QUOTE;
        
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
