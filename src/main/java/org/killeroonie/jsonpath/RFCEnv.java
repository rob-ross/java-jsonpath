package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.lexer.JJPLexer;
import org.killeroonie.jsonpath.lexer.Lexer;
import org.killeroonie.jsonpath.lexer.PJPLexer;
import org.killeroonie.jsonpath.lexer.RFCRulesBuilder;
import org.killeroonie.jsonpath.parser.PJPParser;
import org.killeroonie.jsonpath.parser.Parser;

public class RFCEnv extends JSONPathEnvironment {

    public RFCEnv() {
        super(true, true, RFCRulesBuilder.class, JJPLexer.class, PJPParser.class);
        getLexer().setWhitespacePolicy(Lexer.WhitespacePolicy.STRICT);
    }
}
