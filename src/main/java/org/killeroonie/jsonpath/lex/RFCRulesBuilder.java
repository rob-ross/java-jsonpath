package org.killeroonie.jsonpath.lex;

import org.killeroonie.jsonpath.Constants;
import org.killeroonie.jsonpath.TokenKind;

import java.util.EnumMap;

/**
 * Builds rules in strict compliance with RFC9535.
 * <p>Full fidelity with that spec also requires the proper Environment and Lexer to be used,
 * along with the Parser and Matcher, etc.
 */
public class RFCRulesBuilder implements RulesBuilder {

    private final EnumMap<TokenKind, LexerRule> rules = new EnumMap<>(TokenKind.class);

    @Override
    public EnumMap<TokenKind, LexerRule> getRules() {
        if (rules.isEmpty()) {
            rules.putAll(buildDefaultRulesMap());
        }
        return rules;
    }

    private  EnumMap<TokenKind, LexerRule> buildDefaultRulesMap() {
        // first create builders for each TokenKind in the default rules.
        final EnumMap<TokenKind, LexerRuleBuilder> builders = new EnumMap<>(TokenKind.class);
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
        // todo - we need a RFC compliant INT_RE here
        builders.put(TokenKind.INT, new LexerRuleBuilder(true, Constants.INT_RE));
        builders.put(TokenKind.FLOAT, new LexerRuleBuilder(true, Constants.FLOAT_RE));
        builders.put(TokenKind.LIST_SLICE, new LexerRuleBuilder(true, Constants.LIST_SLICE_RE));
        builders.put(TokenKind.DOUBLE_QUOTE_STRING, new LexerRuleBuilder(true, Constants.DOUBLE_QUOTE_STRING_RE));
        builders.put(TokenKind.SINGLE_QUOTE_STRING, new LexerRuleBuilder(true, Constants.SINGLE_QUOTE_STRING_RE));
        builders.put(TokenKind.IDENTIFIER, new LexerRuleBuilder(true, Constants.IDENTIFIER_RE));

        builders.put(TokenKind.TRUE, new LexerRuleBuilder(false, Constants.KEYWORD_TRUE));
        builders.put(TokenKind.FALSE, new LexerRuleBuilder(false, Constants.KEYWORD_FALSE));
        builders.put(TokenKind.NULL, new LexerRuleBuilder(false, Constants.KEYWORD_NULL));

        assert builders.size() == 47 : "Expected builder size: 47, got: " + builders.size();
        // add first sets where applicable
        RulesBuilder.addFirstSet(builders.get(TokenKind.SPACE), Constants.SPACE_FIRST_SET);
        RulesBuilder.addFirstSet(builders.get(TokenKind.LIST_SLICE), Constants.SLICE_FIRST_SET);
        RulesBuilder.addFirstSet(builders.get(TokenKind.DOUBLE_QUOTE_STRING), Constants.DOUBLE_QUOTE);
        RulesBuilder.addFirstSet(builders.get(TokenKind.SINGLE_QUOTE_STRING), Constants.SINGLE_QUOTE);

        // add the emitToken to the builders that don't have one yet (most of them.)
        RulesBuilder.addDefaultEmitKind(builders);

        // finally, we build all the rules
        final EnumMap<TokenKind, LexerRule> rules = new EnumMap<>(TokenKind.class);
        for (var entry: builders.entrySet()) {
            rules.put(entry.getKey(), entry.getValue().build());
        }
        return rules;
    }

}
