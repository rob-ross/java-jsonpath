package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.lexer.JJPLexer;
import org.killeroonie.jsonpath.lexer.Lexer;
import org.killeroonie.jsonpath.lexer.RFCRulesBuilder;
import org.killeroonie.jsonpath.parser.Parser;

public class RFCEnv extends JSONPathEnvironment {

    public RFCEnv() {
        super(true, true, true, RFCRulesBuilder.class, JJPLexer.class, Parser.class);
        getLexer().setWhitespacePolicy(Lexer.WhitespacePolicy.STRICT);
    }
}
