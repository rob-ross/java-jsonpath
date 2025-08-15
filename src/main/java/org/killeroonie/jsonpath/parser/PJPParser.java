package org.killeroonie.jsonpath.parser;

import org.jetbrains.annotations.NotNull;
import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;
import org.killeroonie.jsonpath.exception.JSONPathSyntaxException;
import org.killeroonie.jsonpath.parser.segment.JSONChildSegment;
import org.killeroonie.jsonpath.parser.segment.JSONPathRecursiveDescentSegment;
import org.killeroonie.jsonpath.parser.segment.JSONPathSegment;
import org.killeroonie.jsonpath.parser.selector.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PJPParser extends BaseParser {

    // we may need an environment parameter here
    public PJPParser(JSONPathEnvironment env) {
        super(env);
    }

    static final EnumSet<TokenKind> START_KINDS  = EnumSet.of(TokenKind.ROOT, TokenKind.PSEUDO_ROOT);

    private TokenStream stream;


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

    protected SliceSelector parseSlice(){
        return new SliceSelector( getEnv() , stream.current(), 0,0,0);
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
                    
                }   
                case NUMBER -> {
                    
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

    protected FilterSelector parseFilterSelector() {
        Token token = stream.next();
        Expression expr = parseFilterExpression();
        return new FilterSelector(getEnv(), token, new FilterExpression(expr.getToken(), expr));
    }

    protected Expression parseFilterExpression() {
        //stub
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    protected void parseRelativeQuery() {
        Token tok = stream.current();
        parseQuery(true);
    }

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

    /*
    
    def _decode_string_literal(self, token: Token) -> str:
        if token.type_ == TokenType.SINGLE_QUOTE_STRING:
            value = token.value.replace('"', '\\"').replace("\\'", "'")
        else:
            value = token.value

        return self._unescape_string(value, token)

    def _unescape_string(self, value: str, token: Token) -> str:
        unescaped: List[str] = []
        index = 0

        while index < len(value):
            ch = value[index]
            if ch == "\\":
                index += 1
                _ch, index = self._decode_escape_sequence(value, index, token)
                unescaped.append(_ch)
            else:
                self._string_from_codepoint(ord(ch), token)
                unescaped.append(ch)
            index += 1
        return "".join(unescaped)

    def _decode_escape_sequence(  # noqa: PLR0911
        self, value: str, index: int, token: Token
    ) -> Tuple[str, int]:
        ch = value[index]
        if ch == '"':
            return '"', index
        if ch == "\\":
            return "\\", index
        if ch == "/":
            return "/", index
        if ch == "b":
            return "\x08", index
        if ch == "f":
            return "\x0c", index
        if ch == "n":
            return "\n", index
        if ch == "r":
            return "\r", index
        if ch == "t":
            return "\t", index
        if ch == "u":
            codepoint, index = self._decode_hex_char(value, index, token)
            return self._string_from_codepoint(codepoint, token), index

        raise JSONPathSyntaxError(
            f"unknown escape sequence at index {token.index + index - 1}",
            token=token,
        )

    def _decode_hex_char(self, value: str, index: int, token: Token) -> Tuple[int, int]:
        length = len(value)

        if index + 4 >= length:
            raise JSONPathSyntaxError(
                f"incomplete escape sequence at index {token.index + index - 1}",
                token=token,
            )

        index += 1  # move past 'u'
        codepoint = self._parse_hex_digits(value[index : index + 4], token)

        if self._is_low_surrogate(codepoint):
            raise JSONPathSyntaxError(
                f"unexpected low surrogate at index {token.index + index - 1}",
                token=token,
         

*/

}
