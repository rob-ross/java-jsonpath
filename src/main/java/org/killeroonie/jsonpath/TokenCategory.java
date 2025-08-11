package org.killeroonie.jsonpath;

public enum TokenCategory {
    LITERAL,
    KEYWORD,
    COMPARISON_OPERATOR,
    LOGICAL_OPERATOR,
    DELIMITER,
    IDENTIFIER,
    // special
    NO_OP,
    UNKNOWN,
    EOF
}
