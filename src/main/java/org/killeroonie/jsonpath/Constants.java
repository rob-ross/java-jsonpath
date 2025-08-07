package org.killeroonie.jsonpath;

public class Constants {

    public static final String BLANK_CHAR = " \\t\\n\\x0B\\f\\r";
    public static final String SPACES = "(?:[%s]+)".formatted(BLANK_CHAR);


    // Basic characters
    public static final String SOLIDUS         = chr(0x2F);  // forward slash '/'
    public static final String REVERSE_SOLIDUS = chr(0x5C);  // backslash '\'
    public static final String SLASH           = SOLIDUS;
    public static final String BACKSLASH       = REVERSE_SOLIDUS;

    public static final String SINGLE_QUOTE = chr(0x27);  // single quote ' character
    public static final String DOUBLE_QUOTE = chr(0x22);   // double quote " character
    public static final String ESC          = BACKSLASH;   // '\'

    public static final String UNDERSCORE    = "_";
    public static final String COMMA         = ",";

    // Brackets, braces, and other special characters

    public static final String LEFT_PAREN    = "(";
    public static final String RIGHT_PAREN   = ")";
    public static final String LEFT_BRACKET  = "[";
    public static final String RIGHT_BRACKET = "]";
    public static final String LEFT_BRACE    = "{";
    public static final String RIGHT_BRACE   = "}";
    public static final String QUESTION      = "?";
    public static final String STAR          = "*";
    public static final String PLUS          = "+";
    public static final String MINUS         = "-";
    public static final String PIPE          = "|";
    public static final String CARRET        = "^";
    public static final String DOLLAR        = "$";
    public static final String DOT           = ".";
    public static final String AMPERSAND     = "&";
    public static final String TILDE         = "~";
    public static final String HASH          = "#";
    public static final String AT            = "@";
    public static final String COLON         = ":";

    public static final String DOUBLE_DOT    = "..";

    // comparison operators
    public static final String EQUAL                 = "==";
    public static final String NOT_EQUAL             = "!=";
    public static final String GREATER_THAN          = ">";
    public static final String GREATER_THAN_OR_EQUAL = ">=";
    public static final String LESS_THAN             = "<";
    public static final String LESS_THAN_OR_EQUAL    = "<=";
    public static final String DIAMOND               = "<>";
    public static final String EQUAL_TILDE           = "=~";

    // logical operators
    public static final String LOGICAL_NOT_OP =  "!";
    public static final String LOGICAL_AND_OP = "&&";
    public static final String LOGICAL_OR_OP  = "||";


    // JSON keywords. But only treated as keywords in certain contexts
    public static final String KEYWORD_TRUE = "true";
    public static final String KEYWORD_FALSE = "false";
    public static final String KEYWORD_NULL = "null";

    // Extension keywords. Not in RFC spec, but useful.
    public static final String KEYWORD_AND = "and";
    public static final String KEYWORD_OR = "or";
    public static final String KEYWORD_NOT = "not";
    public static final String KEYWORD_IN        = "in";
    public static final String KEYWORD_CONTAINS = "contains";
    public static final String KEYWORD_UNDEFINED = "undefined";
    public static final String KEYWORD_MISSING = "missing";



    // Default regex patterns
    public static final String DOT_PROPERTY = "\\.(?<GPROP>[\\u0080-\\uFFFFa-zA-Z_][\\u0080-\\uFFFFa-zA-Z0-9_-]*)";
    public static final String BARE_PROPERTY = "[\\\\u0080-\\\\uFFFFa-zA-Z_][\\\\u0080-\\\\uFFFFa-zA-Z0-9_-]*";
    public static final String LIST_SLICE = "(?<GLSLICESTART>\\-?\\d*)\\s*:\\s*(?<GLSLICESTOP>\\-?\\d*)\\s*(?::\\s*(?<GLSLICESTEP>\\-?\\d*))?";
    public static final String FUNCTION = "(?<GFUNC>[a-z][a-z_0-9]+)\\(\\s*";
    public static final String FLOAT = "-?\\d+\\.\\d*(?:[eE][+-]?\\d+)?";
    public static final String INT = "-?\\d+(?<GEXP>[eE][+\\-]?\\d+)?\\b";
    public static final String RE_PATTERN = "/(?<GRE>.+?)/(?<GREFLAGS>[aims]*)";
    public static final String DOUBLE_QUOTE_STRING = "\"(?<GDQUOTE>(?:(?!(?<!\\\\)\").)*)\"";
    public static final String SINGLE_QUOTE_STRING = "'(?<GSQUOTE>(?:(?!(?<!\\\\)').)*)'";



    /**
     * Helper method that duplicates Python's {@code chr()} function.
     * @param codePoint the Unicode codepoint for the character
     * @return a String representation of the codepoint character.
     */
    public static String chr(int codePoint) {
        return new String(Character.toChars(codePoint));
    }
}
