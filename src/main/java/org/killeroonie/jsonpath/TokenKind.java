package org.killeroonie.jsonpath;

import java.util.*;

/**
 * JSONPath tokens.
 * <p>
 * This enum is a translation of the string constants from the Python
 * <code>token.py</code> module.
 */
public enum TokenKind {

    SKIP(  EnumSet.of(TokenCategory.DELIMITER) ),
    SPACE( EnumSet.of(TokenCategory.DELIMITER) ), // NEW. REPLACES SKIP,

    // single char tokens
    LIST_START( EnumSet.of(TokenCategory.DELIMITER) ),
    LBRACKET( EnumSet.of(TokenCategory.DELIMITER) ), // NEW. REPLACES LIST_START
    RBRACKET( EnumSet.of(TokenCategory.DELIMITER) ),
    LPAREN( EnumSet.of(TokenCategory.DELIMITER) ),
    RPAREN( EnumSet.of(TokenCategory.DELIMITER) ),
    // we're not emitting tokens for individual quotes around strings
    COMMA( EnumSet.of(TokenCategory.DELIMITER) ),
    DOT( EnumSet.of(TokenCategory.DELIMITER) ),
    ROOT( EnumSet.of(TokenCategory.DELIMITER) ), // my Python impl uses DOLLAR as TokenKind here.
    DOLLAR( EnumSet.of(TokenCategory.DELIMITER) ),
    FILTER( EnumSet.of(TokenCategory.DELIMITER) ), // my Python impl uses QMARK as TokenKind here.
    QMARK( EnumSet.of(TokenCategory.DELIMITER) ),
    WILD( EnumSet.of(TokenCategory.DELIMITER) ), // my Python impl uses STAR as TokenKind here.
    SELF( EnumSet.of(TokenCategory.DELIMITER) ),
    COLON( EnumSet.of(TokenCategory.DELIMITER) ),
    NOT( EnumSet.of(TokenCategory.LOGICAL_OPERATOR) ),
    GT( EnumSet.of(TokenCategory.COMPARISON_OPERATOR) ),
    LT( EnumSet.of(TokenCategory.COMPARISON_OPERATOR) ),

    // multi-char tokens
    DDOT( EnumSet.of(TokenCategory.DELIMITER) ),
    EQ( EnumSet.of(TokenCategory.COMPARISON_OPERATOR) ),
    NE( EnumSet.of(TokenCategory.COMPARISON_OPERATOR) ),
    GE( EnumSet.of(TokenCategory.COMPARISON_OPERATOR) ),
    LE( EnumSet.of(TokenCategory.COMPARISON_OPERATOR) ),
    AND( EnumSet.of(TokenCategory.LOGICAL_OPERATOR) ),
    OR( EnumSet.of(TokenCategory.LOGICAL_OPERATOR) ),

    // literal types
    NUMBER( EnumSet.of(TokenCategory.LITERAL)),
    INT(    EnumSet.of(TokenCategory.LITERAL) ),
    FLOAT(  EnumSet.of(TokenCategory.LITERAL) ),
    // todo refactor so a single slice token is emitted and parsed later
    LIST_SLICE( EnumSet.of(TokenCategory.LITERAL) ),
    SLICE_START( EnumSet.of(TokenCategory.LITERAL) ),
    SLICE_STEP( EnumSet.of(TokenCategory.LITERAL) ),
    SLICE_STOP( EnumSet.of(TokenCategory.LITERAL) ),
    // string literals
    DOUBLE_QUOTE_STRING( EnumSet.of(TokenCategory.LITERAL) ),
    SINGLE_QUOTE_STRING( EnumSet.of(TokenCategory.LITERAL) ),
    // identifiers - todo these can be combined into a single Lexer Token and handled in the Parser for specificity
    IDENTIFIER( EnumSet.of(TokenCategory.IDENTIFIER) ), // NEW
    // these are member-name-shorthand identifiers
    PROPERTY( EnumSet.of(TokenCategory.IDENTIFIER) ),
    DOT_PROPERTY( EnumSet.of(TokenCategory.IDENTIFIER) ),
    BARE_PROPERTY( EnumSet.of(TokenCategory.IDENTIFIER) ),

    FUNCTION( EnumSet.of(TokenCategory.IDENTIFIER) ), // we can use IDENTIFIER for this

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
    TRUE(  EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),
    FALSE( EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),
    // NULL: Note - JSON null is treated the same as any other JSON value, i.e., it is not taken to mean
    // "undefined" or "missing".
    NULL(  EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),

    // regex literals
    RE_PATTERN( EnumSet.of(TokenCategory.LITERAL) ), // a regular expression literal
    RE_FLAGS(   EnumSet.of(TokenCategory.LITERAL) ), // a regular expression flags literal

    // Filter expression tokens
    // special token used to access context inside a filer.
    FILTER_CONTEXT( EnumSet.of(TokenCategory.DELIMITER) ),


    // Special
    // Utility tokens,
    EOF(     EnumSet.of(TokenCategory.EOF) ),
    ILLEGAL( EnumSet.of(TokenCategory.NO_OP) ),
    NO_OP(   EnumSet.of(TokenCategory.NO_OP) ),   // NEW
    UNKNOWN( EnumSet.of(TokenCategory.UNKNOWN) ), // NEW

    // unused in python-jsonpath
    BLANK,
    STRING,
    OP,
    EMPTY,
    DOT_INDEX,

    /*
    ************************************************************************
    *
    *  Extension tokens - not part of the official RFC9535 spec.
    *
    * **********************************************************************
    */

    PSEUDO_ROOT( EnumSet.of(TokenCategory.DELIMITER) ),

    // new keywords, most are operators except UNDEFINED and MISSING
    AND_EXT(   EnumSet.of(TokenCategory.LOGICAL_OPERATOR, TokenCategory.KEYWORD) ),
    OR_EXT(    EnumSet.of(TokenCategory.LOGICAL_OPERATOR, TokenCategory.KEYWORD) ),
    NOT_EXT(   EnumSet.of(TokenCategory.LOGICAL_OPERATOR, TokenCategory.KEYWORD) ),
    IN(        EnumSet.of(TokenCategory.COMPARISON_OPERATOR, TokenCategory.KEYWORD) ),
    CONTAINS(  EnumSet.of(TokenCategory.COMPARISON_OPERATOR, TokenCategory.KEYWORD) ),
    UNDEFINED( EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),
    MISSING(   EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),

    // New operators
    KEY( EnumSet.of(TokenCategory.DELIMITER) ),
    KEY_SELECTOR( EnumSet.of(TokenCategory.DELIMITER) ), //Python: TOKEN_KEYS,
    LG(  EnumSet.of(TokenCategory.COMPARISON_OPERATOR) ),
    DIAMOND(  EnumSet.of(TokenCategory.COMPARISON_OPERATOR) ),

    RE(  EnumSet.of(TokenCategory.COMPARISON_OPERATOR) ),
    UNION( EnumSet.of(TokenCategory.DELIMITER) ),
    INTERSECTION( EnumSet.of(TokenCategory.DELIMITER) ),


    // Not used in the Java version, but available for customization.
    NIL( EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),
    NONE( EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),
    FALSE_EXT( EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),
    TRUE_EXT( EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),
    NULL_EXT( EnumSet.of(TokenCategory.KEYWORD, TokenCategory.IDENTIFIER) ),
    ;

    private final EnumSet<TokenCategory> categories;

    TokenKind() {
        this( EnumSet.of(TokenCategory.UNKNOWN) );
    }
    TokenKind(EnumSet<TokenCategory> categories) {
        this.categories = categories;
    }

    public EnumSet<TokenCategory> getCategories() {
        return categories;
    }

    public boolean isIdentifier() {
        return categories.contains(TokenCategory.IDENTIFIER);
    }

    public boolean isKeyword() {
        return categories.contains(TokenCategory.KEYWORD);
    }

    public boolean isLiteral() {
        return categories.contains(TokenCategory.LITERAL);
    }
}