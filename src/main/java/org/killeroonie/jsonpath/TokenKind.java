package org.killeroonie.jsonpath;

import java.util.*;
import java.util.regex.Pattern;

/**
 * JSONPath tokens.
 * <p>
 * This enum is a translation of the string constants from the Python
 * <code>token.py</code> module, with associated regex patterns from
 * <code>lex.py</code>.
 */
public enum TokenKind {
    // Constructor args:
    // boolean isRegExp, String defaultPatternString, TokenKind emitKind

    // Utility tokens
    EOF,
    ILLEGAL,
    SKIP(true, "[ \\n\\t\\r\\.]+"),
    SPACE(true, Constants.SPACES),
    // JSONPath expression tokens
    COLON(false, ":"),
    COMMA(false, ","),
    DDOT(false, ".."),
    DOT(false, "."),
    DOT_INDEX, // unused in python-jsonpath
    PROPERTY,
    DOT_PROPERTY(true, "\\.(?<GPROP>[\\u0080-\\uFFFFa-zA-Z_][\\u0080-\\uFFFFa-zA-Z0-9_-]*)", PROPERTY),
    FILTER(false, "?"),
    RBRACKET(false, "]"),
    BARE_PROPERTY(true, "[\\u0080-\\uFFFFa-zA-Z_][\\u0080-\\uFFFFa-zA-Z0-9_-]*"),
    LIST_SLICE(true, "(?<GLSLICESTART>\\-?\\d*)\\s*:\\s*(?<GLSLICESTOP>\\-?\\d*)\\s*(?::\\s*(?<GLSLICESTEP>\\-?\\d*))?"),
    LIST_START(false, "["),
    ROOT(false, "$"),
    SLICE_START,
    SLICE_STEP,
    SLICE_STOP,
    WILD(false, "*"),

    // Filter expression tokens
    AND(false, "&&"),
    BLANK,
    FILTER_CONTEXT(false, "_"),
    FUNCTION(true, "(?<GFUNC>[a-z][a-z_0-9]+)\\(\\s*"),
    EMPTY,
    EQ(false, "=="),

    FALSE(false, "false"),
    TRUE(false, "true"),
    NULL(false, "null"),


    FLOAT(true, "-?\\d+\\.\\d*(?:[eE][+-]?\\d+)?"),
    GE(false, ">="),
    GT(false, ">"),
    INT(true, "-?\\d+(?<GEXP>[eE][+\\-]?\\d+)?\\b"),
    LE(false, "<="),
    LPAREN(false, "("),
    LT(false, "<"),
    NE(false, "!="),
    NOT(false, "!"),
    OP,
    OR(false, "||"),
    RE_FLAGS,
    RE_PATTERN(true, "/(?<GRE>.+?)/(?<GREFLAGS>[aims]*)"),
    RPAREN(false, ")"),
    SELF(false, "@"),
    STRING,
    DOUBLE_QUOTE_STRING(true, "\"(?<GDQUOTE>(?:(?!(?<!\\\\)\").)*)\""),
    SINGLE_QUOTE_STRING(true, "'(?<GSQUOTE>(?:(?!(?<!\\\\)').)*)'"),


    // Extension tokens
    UNION(false, "|"),
    INTERSECTION(false, "&"),
    AND_EXT(false, "and", AND),
    OR_EXT( false, "or",  OR),
    NOT_EXT(false, "not", NOT),
    FALSE_EXT(false, "False", FALSE),
    TRUE_EXT(false, "True", TRUE),
    NULL_EXT(false, "Null", NULL),
    PSEUDO_ROOT(false, "^"),

    KEY(false, "#"),
    KEY_SELECTOR(false, "~"), //Python: TOKEN_KEYS
    CONTAINS(false, "contains"),
    IN(false, "in"),
    LG(false, "<>", NE),
    NIL(true, "[Nn]il\\b"),
    NONE(true, "[Nn]one\\b"),
    RE(false, "=~"),
    UNDEFINED(false, "undefined"),
    MISSING(false, "missing"),
    ;


    public static final EnumSet<TokenKind> oneCharTokens = EnumSet.noneOf(TokenKind.class);
    public static final EnumSet<TokenKind> twoCharTokens = EnumSet.noneOf(TokenKind.class);
    public static final EnumSet<TokenKind> regexpTokens = EnumSet.noneOf(TokenKind.class);
    public static final Map<String, TokenKind> tokenLookupMap = new LinkedHashMap<>(TokenKind.values().length);
    public static final List<TokenKind> reTokensByLength;

    static {
        for ( TokenKind kind : TokenKind.values() ) {
            if (kind.getDefaultPatternString() != null) {
                if (!kind.isRegExp()) {
                    if (kind.getDefaultPatternString().length() == 1) {
                        oneCharTokens.add(kind);
                    } else if (kind.getDefaultPatternString().length() == 2) {
                        twoCharTokens.add(kind);
                    }
                } else {
                    regexpTokens.add(kind);
                }
                tokenLookupMap.put(kind.getDefaultPatternString(), kind);
            }
        }
        // sort regexp Tokens by length descending.
        // this is a first-pass attempt to rank these regexp patterns in a way to try to match longer strings before
        // shorter strings. We'll probably end up having to manually add these in the right precedence order
        reTokensByLength =  new ArrayList<>(regexpTokens);
        reTokensByLength.sort(Comparator.comparingInt((TokenKind t) -> t.getDefaultPatternString().length()).reversed());
    }


    private final boolean isRegExp;
    private final String defaultPatternString;
    private final Pattern defaultPattern;
    private final TokenKind defaultEmitKind;

    /**
     * {@code true} if {@code pattern} represents a regular expression pattern, otherwise {@code false}.
     */
    public boolean isRegExp() {
        return isRegExp;
    }

    /**
     * The regex pattern associated with this token kind, if any.
     * This is used by the lexer to match tokens in a JSONPath string.
     */
    public String getDefaultPatternString() {
        return defaultPatternString;
    }

    /**
     * If {@code isRegExp()} returns true, this method returns the compiled {@link Pattern} obtained from calling
     * {@code Pattern.compile(getPatternString())}. Otherwise, returns null.
     * @return null if isRegExp() is false, otherwise returns the compiled Pattern for getPatternString().
     */
    public Pattern getDefaultPattern() {
        return defaultPattern;
    }

    /**
     * Returns the TokenKind that should be emitted instead of {@code this} TokenKind. This is to facilitate semantic
     * processing by the {@link Lexer}. In most cases, this method just returns {@code this}, but alternate TokenKinds
     * can be specified in the constructor.
     * E.g., the {@code DOT_PROPERTY} TokenKind will emit a {@code PROPERTY} TokenKind.
     * @return the TokenKind that should be emitted for this instance.
     */
    public TokenKind getDefaultEmitKind() {
        return defaultEmitKind;
    }

    /**
     * Returns the lexeme for this TokenKind from {@code getPatternString()} if
     * {@code isRegExp()} returns {@code false}, otherwise returns the empty string.
     * @return the empty string if isRegExp() is true, otherwise returns getPatternString().
     */
    public String lexeme() {
        if (isRegExp()) {
            return null;
        }
        return this.defaultPatternString;
    }

    /**
     * A token kind that is not represented by a regex pattern or lexeme.
     * These are often emitted by the lexer based on context, rather
     * than direct pattern matching.
     */
    TokenKind() {
        this(false, null, null);
    }

    TokenKind(boolean isRegExp, String defaultPatternString) {
        this(isRegExp, defaultPatternString, null);
    }
    /**
     * A token kind that is represented by a regex pattern.
     *
     * @param isRegExp      {@code true} if {@code defaultPatternString} represents a regular expression pattern string, or
     *                      {@code false} if defaultPatternString is a regular lexeme to match by string comparison.
     * @param defaultPatternString The Java-compatible regex pattern.
     *
     */
    TokenKind(boolean isRegExp, String defaultPatternString, TokenKind defaultEmitKind) {
        this.isRegExp = isRegExp;
        this.defaultPatternString = defaultPatternString;
        if (isRegExp) {
            this.defaultPattern = Pattern.compile(defaultPatternString);
        } else {
            this.defaultPattern = null;
        }
        this.defaultEmitKind = defaultEmitKind == null ? this : defaultEmitKind;
    }
}