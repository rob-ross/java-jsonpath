package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.lexer.JJPLexer;
import org.killeroonie.jsonpath.lexer.JJPRulesBuilder;
import org.killeroonie.jsonpath.parser.Parser;

public class JJPEnv extends JSONPathEnvironment {

    public JJPEnv() {
        super(true, true, true, JJPRulesBuilder.class, JJPLexer.class, Parser.class);
    }
}
