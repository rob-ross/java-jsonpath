package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.lex.JJPLexer;
import org.killeroonie.jsonpath.lex.JJPRulesBuilder;

public class JJPEnv extends JSONPathEnvironment {

    public JJPEnv() {
        super(true, true, true, JJPRulesBuilder.class, JJPLexer.class, Parser.class);
    }
}
