package org.killeroonie.jsonpath.lex;


import org.killeroonie.jsonpath.TokenKind;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the default Lexer rules from the python-jsonpath project at <p>
 * <a href="https://github.com/jg-rp/python-jsonpath">https://github.com/jg-rp/python-jsonpath</a>
 *
 * <p>Full fidelity with that version also requires the proper Environment and Lexer to be used,
 * along with the Parser and Matcher, etc.
 */
public class PJPRulesBuilder implements RulesBuilder {

    public static final String DOUBLE_QUOTE_STRING_RE = "\"(?<GDQUOTE>(?:(?!(?<!\\\\)\").)*)\"";
    public static final String SINGLE_QUOTE_STRING_RE = "'(?<GSQUOTE>(?:(?!(?<!\\\\)').)*)'";
    public static final String REGEX_PATTERN_RE = "/(?<GRE>.+?)/(?<GREFLAGS>[aims]*)";
    public static final String LIST_SLICE_RE =  "(?<GLSLICESTART>\\-?\\d*)\\s*" +
                                                ":\\s*(?<GLSLICESTOP>\\-?\\d*)\\s*" +
                                                "(?::\\s*(?<GLSLICESTEP>\\-?\\d*))?";
    public static final String FUNCTION_RE = "(?<GFUNC>[a-z][a-z_0-9]+)\\(\\s*";
    public static final String DOT_PROPERTY_RE = "\\.(?<GPROP>[\\u0080-\\uFFFFa-zA-Z_][\\u0080-\\uFFFFa-zA-Z0-9_-]*)";
    public static final String KEY_PATTERN_RE = "[\\u0080-\\uFFFFa-zA-Z_][\\u0080-\\uFFFFa-zA-Z0-9_-]*";
    public static final String FLOAT_RE = "-?\\d+\\.\\d*(?:[eE][+-]?\\d+)?";
    public static final String INT_RE = "-?\\d+(?<GEXP>[eE][+\\-]?\\d+)?\\b";

    public static final String LOGICAL_AND_RE = "&&|(?:and\\b)";
    public static final String LOGICAL_OR_RE = "\\|\\||(?:or\\b)";
    public static final String LOGICAL_NOT_RE = "(?:not\\b)|!";

    public static final String ROOT_TOKEN_RE = "\\$"; // might have to escape these
    public static final String PSEUDO_ROOT_RE = "\\^";
    public static final String SELF_TOKEN_RE = "@";
    public static final String KEY_TOKEN_RE = "\\#";
    public static final String UNION_TOKEN_RE = "\\|";
    public static final String INTERSECTION_TOKEN_RE = "\\&";
    public static final String FILTER_CONTEXT_TOKEN_RE = "_";
    public static final String KEYS_SELECTOR_TOKEN_RE = "\\~";

    private final Map<TokenKind, RulesBuilder.LexerRule> rules = new LinkedHashMap<>();

    public PJPRulesBuilder() {
    }

    public Map<TokenKind, RulesBuilder.LexerRule> getRules() {
        if (rules.isEmpty()) {
            rules.putAll(buildDefaultRulesMap());
        }
        return rules;
    }


    private  Map<TokenKind, RulesBuilder.LexerRule> buildDefaultRulesMap() {
        // first, create builders for each TokenKind in the default rules.
        final Map<TokenKind, RulesBuilder.LexerRuleBuilder> builders = new LinkedHashMap<>();
        // these match the order and regex pattern from compile_rules() in lex.py
        builders.put(TokenKind.DOUBLE_QUOTE_STRING, new RulesBuilder.LexerRuleBuilder(true, DOUBLE_QUOTE_STRING_RE));
        builders.put(TokenKind.SINGLE_QUOTE_STRING, new RulesBuilder.LexerRuleBuilder(true, SINGLE_QUOTE_STRING_RE));
        builders.put(TokenKind.RE_PATTERN, new RulesBuilder.LexerRuleBuilder(true, REGEX_PATTERN_RE));
        builders.put(TokenKind.LIST_SLICE, new RulesBuilder.LexerRuleBuilder(true, LIST_SLICE_RE));
        builders.put(TokenKind.FUNCTION , new RulesBuilder.LexerRuleBuilder(true, FUNCTION_RE));
        builders.put(TokenKind.DOT_PROPERTY, new RulesBuilder.LexerRuleBuilder(true, DOT_PROPERTY_RE, TokenKind.PROPERTY));
        builders.put(TokenKind.FLOAT, new RulesBuilder.LexerRuleBuilder(true, FLOAT_RE));
        builders.put(TokenKind.INT, new RulesBuilder.LexerRuleBuilder(true, INT_RE));
        builders.put(TokenKind.DDOT , new RulesBuilder.LexerRuleBuilder(true, "\\.\\." ));
        builders.put(TokenKind.AND , new RulesBuilder.LexerRuleBuilder(true, LOGICAL_AND_RE ));
        builders.put(TokenKind.OR , new RulesBuilder.LexerRuleBuilder(true, LOGICAL_OR_RE ));

        // env:
        builders.put(TokenKind.ROOT , new RulesBuilder.LexerRuleBuilder(true, ROOT_TOKEN_RE));
        builders.put(TokenKind.PSEUDO_ROOT , new RulesBuilder.LexerRuleBuilder(true, PSEUDO_ROOT_RE));
        builders.put(TokenKind.SELF , new RulesBuilder.LexerRuleBuilder(true, SELF_TOKEN_RE));
        builders.put(TokenKind.KEY , new RulesBuilder.LexerRuleBuilder(true, KEY_TOKEN_RE));
        builders.put(TokenKind.UNION , new RulesBuilder.LexerRuleBuilder(true, UNION_TOKEN_RE));
        builders.put(TokenKind.INTERSECTION , new RulesBuilder.LexerRuleBuilder(true, INTERSECTION_TOKEN_RE));
        builders.put(TokenKind.FILTER_CONTEXT , new RulesBuilder.LexerRuleBuilder(true, FILTER_CONTEXT_TOKEN_RE));
        builders.put(TokenKind.KEY_SELECTOR , new RulesBuilder.LexerRuleBuilder(true, KEYS_SELECTOR_TOKEN_RE));

        // post-env:
        builders.put(TokenKind.WILD , new RulesBuilder.LexerRuleBuilder(true, "\\*"));
        builders.put(TokenKind.FILTER , new RulesBuilder.LexerRuleBuilder(true, "\\?"));
        builders.put(TokenKind.IN , new RulesBuilder.LexerRuleBuilder(true, "in\\b"));
        builders.put(TokenKind.TRUE , new RulesBuilder.LexerRuleBuilder(true, "[Tt]rue\\b"));
        builders.put(TokenKind.FALSE , new RulesBuilder.LexerRuleBuilder(true, "[Ff]alse\\b"));

        builders.put(TokenKind.NIL , new RulesBuilder.LexerRuleBuilder(true, "[Nn]il\\b"));
        builders.put(TokenKind.NULL , new RulesBuilder.LexerRuleBuilder(true, "[Nn]ull\\b", TokenKind.NIL));
        builders.put(TokenKind.NONE , new RulesBuilder.LexerRuleBuilder(true, "[Nn]one\\b", TokenKind.NIL));
        builders.put(TokenKind.CONTAINS , new RulesBuilder.LexerRuleBuilder(true, "contains\\b"));
        builders.put(TokenKind.UNDEFINED , new RulesBuilder.LexerRuleBuilder(true, "undefined\\b"));
        builders.put(TokenKind.MISSING , new RulesBuilder.LexerRuleBuilder(true, "missing\\b"));
        builders.put(TokenKind.LIST_START , new RulesBuilder.LexerRuleBuilder(true, "\\["));


        builders.put(TokenKind.RBRACKET, new RulesBuilder.LexerRuleBuilder(true, "]"));
        builders.put(TokenKind.COMMA, new RulesBuilder.LexerRuleBuilder(true, ","));
        builders.put(TokenKind.EQ, new RulesBuilder.LexerRuleBuilder(true, "=="));
        builders.put(TokenKind.NE, new RulesBuilder.LexerRuleBuilder(true, "!="));
        builders.put(TokenKind.LG, new RulesBuilder.LexerRuleBuilder(true, "<>"));
        builders.put(TokenKind.LE, new RulesBuilder.LexerRuleBuilder(true, "<="));
        builders.put(TokenKind.GE, new RulesBuilder.LexerRuleBuilder(true, ">="));
        builders.put(TokenKind.RE, new RulesBuilder.LexerRuleBuilder(true, "=~"));
        builders.put(TokenKind.LT, new RulesBuilder.LexerRuleBuilder(true, "<"));
        builders.put(TokenKind.GT, new RulesBuilder.LexerRuleBuilder(true, ">"));
        builders.put(TokenKind.NOT, new RulesBuilder.LexerRuleBuilder(true, LOGICAL_NOT_RE));
        builders.put(TokenKind.BARE_PROPERTY, new RulesBuilder.LexerRuleBuilder(true, KEY_PATTERN_RE));
        builders.put(TokenKind.LPAREN, new RulesBuilder.LexerRuleBuilder(true, "\\("));
        builders.put(TokenKind.RPAREN, new RulesBuilder.LexerRuleBuilder(true, "\\)"));
        builders.put(TokenKind.SKIP, new RulesBuilder.LexerRuleBuilder(true, "[ \\n\\t\\r\\.]+"));
        builders.put(TokenKind.ILLEGAL, new RulesBuilder.LexerRuleBuilder(true, "."));

        // not
        assert builders.size() == 47 : "Expected 47 rules for this rule builder";


        // add first sets where applicable
        // PJP doesn't use first sets here.

        // add the emitToken to the builders that don't have one yet (most of them.)
        for (Map.Entry<TokenKind, RulesBuilder.LexerRuleBuilder> entry: builders.entrySet()) {
            if (entry.getValue().getEmitKind() == null ) {
                RulesBuilder.LexerRuleBuilder lrb = entry.getValue();
                lrb.emitKind(entry.getKey()); // emitKind is the same as the TokenKind, which is typical.
            }
        }

        // finally, we build all the rules
        final Map<TokenKind, RulesBuilder.LexerRule> rules = new LinkedHashMap<>();
        for (var entry: builders.entrySet()) {
            rules.put(entry.getKey(), entry.getValue().build());
        }

        return rules;
    }

    public static void main(String[] args) {
        var builder =  new PJPRulesBuilder();
        var rules = builder.buildDefaultRulesMap();
        System.out.println(rules);
        for (var entry: rules.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
