package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.lex.JJPLexer;
import org.killeroonie.jsonpath.lex.Lexer;
import org.killeroonie.jsonpath.lex.RFCRulesBuilder;

public class RFCEnv extends JSONPathEnvironment {

    public RFCEnv() {
        super(true, true, true, RFCRulesBuilder.class, JJPLexer.class, Parser.class);
        getLexer().setWhitespacePolicy(Lexer.WhitespacePolicy.STRICT);
    }
}
