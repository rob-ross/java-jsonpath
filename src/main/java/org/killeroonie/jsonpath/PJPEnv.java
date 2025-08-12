package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.lex.PJPLexer;
import org.killeroonie.jsonpath.lex.PJPRulesBuilder;

/**
 * Environment that implements all the parts required to completely emulate the
 * python-jsonpath library lexing and parsing rules.
 * RulesBuilder - PJPRulesBuilder
 * Lexer - PJPLexer
 * Parser - PJPParser
 *
 * Unlike PJP, the standard env rules have already been added in PJPRulesBuilder, so no additional rule customization
 * is required here.
 */
public class PJPEnv extends JSONPathEnvironment {
    public PJPEnv() {
        super(true, true, true, PJPRulesBuilder.class, PJPLexer.class, Parser.class);
    }
}

