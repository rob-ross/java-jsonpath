package org.killeroonie.jsonpath;

public class Constants {

    public static final String BLANK_CHAR = " \\t\\n\\x0B\\f\\r";
    public static final String SPACES = "(?:[%s]*)".formatted(BLANK_CHAR);
}
