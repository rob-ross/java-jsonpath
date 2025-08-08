package org.killeroonie.jsonpath;

import java.util.*;
import java.util.regex.Pattern;

enum TokenCategory {
    LITERAL,
    KEYWORD,
    COMPARISON_OPERATOR,
    LOGICAL_OPERATOR,
    DELIMITER,
    IDENTIFIER,
    // special
    NO_OP,
    UNKNOWN,
    EOF,
    ;
}

// todo - Refactoring time! Go back to the enum constants being simple with no instance state.
// move all the instance variables into the LexerRule subclasses,
// for RegexRules, add a first-set.
/*
    Steps for refactoring:

    1. if isRegExp is false, we will create a default LexemeRule for it that includes
        a. the lexeme, which is the second constructor argument (Contants.XXX)
        b. emitKind, which is the third argument if present, and the TokenKind.this if not

    2. if isRegExp is true, we will create a default RegexRule that includes
        a. the regex pattern string, which is the second constructor argument (Constants.XXX)
        b. emitKind, which is the third argument if present, and the TokenKind.this if not
        c. a new optional parameter argument, "first-set", the set of single characters that  must be present in the string
        for a match to occur. Note that matching a first-set character doesn't guarantee the rest of the string will match.
        It's just a quick check when possible to avoid a regexp match if we already know it will fail. This is an
        optional argument so null is allowed.

    3. Refactor TokenKind to take a set of TokenCategory members. Some TokenKinds will fit into multiple categories

 */

/**
 * JSONPath tokens.
 * <p>
 * This enum is a translation of the string constants from the Python
 * <code>token.py</code> module.
 */
public enum TokenKind {

    SKIP,
    SPACE(true, Constants.SPACES_RE), // NEW. REPLACES SKIP,

    // single char tokens
    LIST_START,
    LBRACKET, // NEW. REPLACES LIST_START
    RBRACKET,
    LPAREN,
    RPAREN,
    // we're not emitting tokens for individual quotes around strings
    COMMA,
    DOT,
    ROOT, // my Python impl uses DOLLAR as TokenKind here.
    FILTER, // my Python impl uses QMARK as TokenKind here.
    WILD, // my Python impl uses STAR as TokenKind here.
    SELF,
    COLON,
    NOT,
    GT,
    LT,

    // multi-char tokens
    DDOT,
    EQ,
    NE,
    GE,
    LE,
    AND,
    OR,

    // literal types
    INT,
    FLOAT,
    // todo refactor so a single slice token is emitted and parsed later
    LIST_SLICE,
    SLICE_START,
    SLICE_STEP,
    SLICE_STOP,
    // string literals
    DOUBLE_QUOTE_STRING,
    SINGLE_QUOTE_STRING,
    // identifiers - todo these can be combined into a single Lexer Token and handled in the Parser for specificity
    IDENTIFIER, // NEW
    // these are member-name-shorthand identifiers
    PROPERTY,
    DOT_PROPERTY,
    BARE_PROPERTY,

    FUNCTION, // we can use IDENTIFIER for this

    /*
    ------------------
     JSON keywords
    ------------------
     But only treated as keywords in certain contexts such as logical comparisons.
     They are allowed as member values,
       e.g., { "key1" : "true"}, where the member value is the string "true" and not the JSON value true,
       which is distinct from { "key1": true } (no quotes around true here).
     and they are allowed as member names, e.g., { "null": "foo"}.
     Although these examples might be confusing design choices for an object/map and should be avoided,
     they are syntactically allowed by the spec.
    */
    TRUE,
    FALSE,
    // NULL: Note - JSON null is treated the same as any other JSON value, i.e., it is not taken to mean
    // "undefined" or "missing".
    NULL,

    // regex literals
    RE_PATTERN, // a regular expression literal
    RE_FLAGS, // a regular expression flags literal

    // Filter expression tokens
    // special token used to access context inside a filer.
    // this looks like it belongs in the same category as ROOT and SELF which are DELIMITERS.
    FILTER_CONTEXT,


    // Special
    // Utility tokens,
    EOF,
    ILLEGAL,
    NO_OP,   // NEW
    UNKNOWN, // NEW

    // unused in python-jsonpath
    BLANK,
    STRING,
    OP,
    EMPTY,
    DOT_INDEX,


    // Extension tokens
    PSEUDO_ROOT,

    // new keywords, most are operators except UNDEFINED and MISSING
    AND_EXT,
    OR_EXT,
    NOT_EXT,
    IN,
    CONTAINS,
    UNDEFINED,
    MISSING,

    // New operators
    KEY,
    KEY_SELECTOR, //Python: TOKEN_KEYS,
    LG,
    RE,
    UNION,
    INTERSECTION,


    // Not used in the Java version, but available for customization.
    NIL,
    NONE,
    FALSE_EXT,
    TRUE_EXT,
    NULL_EXT,

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