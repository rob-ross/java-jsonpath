package org.killeroonie.jsonpath;

import java.util.EnumMap;
import java.util.Map;

public class DefaultRulesBuilder implements RulesBuilder {

    private final EnumMap<TokenKind, LexerRule> rules;

    public DefaultRulesBuilder() {
        rules = buildDefaultRulesMap();
    }

    public EnumMap<TokenKind, LexerRule> getRules() {
        return rules;
    }

    private  EnumMap<TokenKind, LexerRule> buildDefaultRulesMap() {
        // first create builders for each TokenKind in the default rules.
        final EnumMap<TokenKind, LexerRuleBuilder> builders = new EnumMap<>(TokenKind.class);
        builders.put(TokenKind.SKIP, new LexerRuleBuilder(true, Constants.SPACES_RE));
        builders.put(TokenKind.SPACE, new LexerRuleBuilder(true, Constants.SPACES_RE));
        builders.put(TokenKind.LIST_START, new LexerRuleBuilder(false, Constants.LEFT_BRACKET));
        builders.put(TokenKind.RBRACKET, new LexerRuleBuilder(false, Constants.RIGHT_BRACKET));
        builders.put(TokenKind.LPAREN, new LexerRuleBuilder(false, Constants.LEFT_PAREN));
        builders.put(TokenKind.RPAREN, new LexerRuleBuilder(false, Constants.RIGHT_PAREN));
        builders.put(TokenKind.COMMA, new LexerRuleBuilder(false, Constants.COMMA));
        builders.put(TokenKind.DOT, new LexerRuleBuilder(false, Constants.DOT));
        builders.put(TokenKind.ROOT, new LexerRuleBuilder(false, Constants.DOLLAR));
        builders.put(TokenKind.FILTER, new LexerRuleBuilder(false, Constants.QUESTION));
        builders.put(TokenKind.WILD, new LexerRuleBuilder(false, Constants.STAR));
        builders.put(TokenKind.SELF, new LexerRuleBuilder(false, Constants.AT));
        //builders.put(TokenKind.COLON, new LexerRuleBuilder(false, Constants.COLON));
        builders.put(TokenKind.NOT, new LexerRuleBuilder(false, Constants.LOGICAL_NOT_OP));
        builders.put(TokenKind.GT, new LexerRuleBuilder(false, Constants.GREATER_THAN));
        builders.put(TokenKind.LT, new LexerRuleBuilder(false, Constants.LESS_THAN));
        builders.put(TokenKind.DDOT, new LexerRuleBuilder(false, Constants.DOUBLE_DOT));
        builders.put(TokenKind.EQ, new LexerRuleBuilder(false, Constants.EQUAL));
        builders.put(TokenKind.NE, new LexerRuleBuilder(false, Constants.NOT_EQUAL));
        builders.put(TokenKind.GE, new LexerRuleBuilder(false, Constants.GREATER_THAN_OR_EQUAL));
        builders.put(TokenKind.LE, new LexerRuleBuilder(false, Constants.LESS_THAN_OR_EQUAL));
        builders.put(TokenKind.AND, new LexerRuleBuilder(false, Constants.LOGICAL_AND_OP));
        builders.put(TokenKind.OR, new LexerRuleBuilder(false, Constants.LOGICAL_OR_OP));
        builders.put(TokenKind.NUMBER, new LexerRuleBuilder(true, Constants.NUMBER_RE));
        builders.put(TokenKind.INT, new LexerRuleBuilder(true, Constants.INT_RE));
        builders.put(TokenKind.FLOAT, new LexerRuleBuilder(true, Constants.FLOAT_RE));
        builders.put(TokenKind.LIST_SLICE, new LexerRuleBuilder(true, Constants.LIST_SLICE_RE));
        builders.put(TokenKind.DOUBLE_QUOTE_STRING, new LexerRuleBuilder(true, Constants.DOUBLE_QUOTE_STRING_RE));
        builders.put(TokenKind.SINGLE_QUOTE_STRING, new LexerRuleBuilder(true, Constants.SINGLE_QUOTE_STRING_RE));
        builders.put(TokenKind.DOT_PROPERTY, new LexerRuleBuilder(true, Constants.DOT_PROPERTY_RE, TokenKind.PROPERTY));

        builders.put(TokenKind.IDENTIFIER, new LexerRuleBuilder(true, Constants.IDENTIFIER_RE));

        builders.put(TokenKind.BARE_PROPERTY, new LexerRuleBuilder(true, Constants.BARE_PROPERTY_RE));
        builders.put(TokenKind.FUNCTION, new LexerRuleBuilder(true, Constants.FUNCTION_RE));
        builders.put(TokenKind.TRUE, new LexerRuleBuilder(false, Constants.KEYWORD_TRUE));
        builders.put(TokenKind.FALSE, new LexerRuleBuilder(false, Constants.KEYWORD_FALSE));
        builders.put(TokenKind.NULL, new LexerRuleBuilder(false, Constants.KEYWORD_NULL));
        builders.put(TokenKind.RE_PATTERN, new LexerRuleBuilder(true, Constants.REGEX_PATTERN_RE));
        builders.put(TokenKind.FILTER_CONTEXT, new LexerRuleBuilder(false, Constants.UNDERSCORE));
        builders.put(TokenKind.PSEUDO_ROOT, new LexerRuleBuilder(false, Constants.CARRET));
        builders.put(TokenKind.AND_EXT, new LexerRuleBuilder(false, Constants.KEYWORD_AND, TokenKind.AND));
        builders.put(TokenKind.OR_EXT, new LexerRuleBuilder(false, Constants.KEYWORD_OR, TokenKind.OR));
        builders.put(TokenKind.NOT_EXT, new LexerRuleBuilder(false, Constants.KEYWORD_NOT, TokenKind.NOT));
        builders.put(TokenKind.IN, new LexerRuleBuilder(false, Constants.KEYWORD_IN));
        builders.put(TokenKind.CONTAINS, new LexerRuleBuilder(false, Constants.KEYWORD_CONTAINS));
        builders.put(TokenKind.UNDEFINED, new LexerRuleBuilder(false, Constants.KEYWORD_UNDEFINED));
        builders.put(TokenKind.MISSING, new LexerRuleBuilder(false, Constants.KEYWORD_MISSING));
        builders.put(TokenKind.KEY, new LexerRuleBuilder(false, Constants.HASH));
        builders.put(TokenKind.KEY_SELECTOR, new LexerRuleBuilder(false, Constants.TILDE));
        builders.put(TokenKind.LG, new LexerRuleBuilder(false, Constants.DIAMOND, TokenKind.NE));
        builders.put(TokenKind.RE, new LexerRuleBuilder(false, Constants.EQUAL_TILDE));
        builders.put(TokenKind.UNION, new LexerRuleBuilder(false, Constants.PIPE));
        builders.put(TokenKind.INTERSECTION, new LexerRuleBuilder(false, Constants.AMPERSAND));

        // add first sets where applicable
        RulesBuilder.addFirstSet(builders.get(TokenKind.SKIP), Constants.SPACE_FIRST_SET);
        RulesBuilder.addFirstSet(builders.get(TokenKind.SPACE), Constants.SPACE_FIRST_SET);
        // skipped INT and FLOAT
        RulesBuilder.addFirstSet(builders.get(TokenKind.LIST_SLICE), Constants.SLICE_FIRST_SET);
        RulesBuilder.addFirstSet(builders.get(TokenKind.DOUBLE_QUOTE_STRING), Constants.DOUBLE_QUOTE);
        RulesBuilder.addFirstSet(builders.get(TokenKind.SINGLE_QUOTE_STRING), Constants.SINGLE_QUOTE);
        RulesBuilder.addFirstSet(builders.get(TokenKind.DOT_PROPERTY), Constants.DOT);
        // todo skipped BARE_PROPERTY and FUNCTION for now
        RulesBuilder.addFirstSet(builders.get(TokenKind.RE_PATTERN), Constants.BACKSLASH);

        // add the emitToken to the builders that don't have one yet (most of them.)
        for (Map.Entry<TokenKind, LexerRuleBuilder> entry: builders.entrySet()) {
            if (entry.getValue().getEmitKind() == null ) {
                LexerRuleBuilder lrb = entry.getValue();
                lrb.emitKind(entry.getKey()); // emitKind is the same as the TokenKind, which is typical.
            }
        }

        // finally, we build all the rules
        final EnumMap<TokenKind, LexerRule> rules = new EnumMap<>(TokenKind.class);
        for (var entry: builders.entrySet()) {
            rules.put(entry.getKey(), entry.getValue().build());
        }

        return rules;
    }
}