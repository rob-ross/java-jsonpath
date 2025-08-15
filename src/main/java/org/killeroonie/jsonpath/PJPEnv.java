package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.lexer.PJPLexer;
import org.killeroonie.jsonpath.lexer.PJPRulesBuilder;
import org.killeroonie.jsonpath.lexer.RulesBuilder;
import org.killeroonie.jsonpath.parser.PJPParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
        super(true, true, true, PJPRulesBuilder.class, PJPLexer.class, PJPParser.class);
    }

    // todo these are here to duplicate PJP custom env rules, but these are redundant and have been moved into
    // PJPRulesBuilder
    protected Map<TokenKind, RulesBuilder.LexerRule> buildCustomEnvRules() {
        // These should be escaped strings.
        final String pseudoRootToken = "\\^";
        final String filterContextToken = "_";
        final String intersectionToken = "\\&";
        final String keyToken = "\\#";
        final String keysSelectorToken = "\\~";
        final String unionToken = "\\|";
        Map<TokenKind, RulesBuilder.LexerRule> rules = new LinkedHashMap<>();
        rules.put(TokenKind.PSEUDO_ROOT,
                new RulesBuilder.RegexRule(Pattern.compile(pseudoRootToken), TokenKind.PSEUDO_ROOT)
        );
        rules.put(TokenKind.FILTER_CONTEXT,
                new RulesBuilder.RegexRule(Pattern.compile(filterContextToken), TokenKind.FILTER_CONTEXT)
        );
        rules.put(TokenKind.INTERSECTION,
                new RulesBuilder.RegexRule(Pattern.compile(intersectionToken), TokenKind.INTERSECTION)
        );
        rules.put(TokenKind.KEY,
                new RulesBuilder.RegexRule(Pattern.compile(keyToken), TokenKind.KEY)
        );
        rules.put(TokenKind.KEY_SELECTOR,
                new RulesBuilder.RegexRule(Pattern.compile(keysSelectorToken), TokenKind.KEY_SELECTOR)
        );
        rules.put(TokenKind.UNION,
                new RulesBuilder.RegexRule(Pattern.compile(unionToken), TokenKind.UNION)
        );
        return rules;
    }
}

