package org.killeroonie.jsonpath;

/**
 * JSONPath tokens.
 * <p>
 * This enum is a translation of the string constants from the Python
 * <code>token.py</code> module, with associated regex patterns from
 * <code>lex.py</code>.
 */
public enum TokenKind {
    // Utility tokens
    EOF,
    ILLEGAL("."),
    SKIP("[ \\n\\t\\r\\.]+"),

    // JSONPath expression tokens
    COLON,
    COMMA(","),
    DDOT("\\.\\."),
    DOT,
    DOT_INDEX,
    DOT_PROPERTY("\\.(?P<G_PROP>[\\u0080-\\uFFFFa-zA-Z_][\\u0080-\\uFFFFa-zA-Z0-9_-]*)"),
    FILTER("\\?"),
    FAKE_ROOT("^"),
    KEY("#"),
    KEY_SELECTOR("~"), //Python: TOKEN_KEYS
    RBRACKET("]"),
    BARE_PROPERTY("[\\u0080-\\uFFFFa-zA-Z_][\\u0080-\\uFFFFa-zA-Z0-9_-]*"),
    LIST_SLICE("(?P<G_LSLICE_START>\\-?\\d*)\\s*:\\s*(?P<G_LSLICE_STOP>\\-?\\d*)\\s*(?::\\s*(?P<G_LSLICE_STEP>\\-?\\d*))?"),
    LIST_START("\\["),
    PROPERTY,
    ROOT("$"),
    SLICE_START,
    SLICE_STEP,
    SLICE_STOP,
    WILD("\\*"),

    // Filter expression tokens
    AND("&&|(?:and\\b)"),
    BLANK,
    CONTAINS("contains\\b"),
    FILTER_CONTEXT("_"),
    FUNCTION("(?P<G_FUNC>[a-z][a-z_0-9]+)\\(\\s*"),
    EMPTY,
    EQ("=="),
    FALSE("[Ff]alse\\b"),
    FLOAT("-?\\d+\\.\\d*(?:[eE][+-]?\\d+)?"),
    GE(">="),
    GT(">"),
    IN("in\\b"),
    INT("-?\\d+(?P<G_EXP>[eE][+\\-]?\\d+)?\\b"),
    LE("<="),
    LG("<>"),
    LPAREN("\\("),
    LT("<"),
    NE("!="),
    NIL("[Nn]il\\b"),
    NONE("[Nn]one\\b"),
    NOT("(?:not\\b)|!"),
    NULL("[Nn]ull\\b"),
    OP,
    OR("\\|\\||(?:or\\b)"),
    RE("=~"),
    RE_FLAGS,
    RE_PATTERN("/(?P<G_RE>.+?)/(?P<G_RE_FLAGS>[aims]*)"),
    RPAREN("\\)"),
    SELF("@"),
    STRING,
    DOUBLE_QUOTE_STRING("\"(?P<G_DQUOTE>(?:(?!(?<!\\\\)\").)*)\""),
    SINGLE_QUOTE_STRING("'(?P<G_SQUOTE>(?:(?!(?<!\\\\)').)*)'"),
    TRUE("[Tt]rue\\b"),
    UNDEFINED("undefined\\b"),
    MISSING("missing\\b"),

    // Extension tokens
    UNION("|"),
    INTERSECTION("&");

    /**
     * The regex pattern associated with this token kind, if any.
     * This is used by the lexer to match tokens in a JSONPath string.
     */
    public final String pattern;

    /**
     * A token kind that is not represented by a regex pattern.
     * These are often emitted by the lexer based on context, rather
     * than direct pattern matching.
     */
    TokenKind() {
        this(null);
    }

    /**
     * A token kind that is represented by a regex pattern.
     *
     * @param pattern The Java-compatible regex pattern.
     */
    TokenKind(String pattern) {
        this.pattern = pattern;
    }
}