package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.JSONPathNodeList;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;

import java.util.Objects;

/**
 *  A pair of expressions and a comparison operator.
 */
public class ComparisonExpression extends Expression {

    private static final Nothing NOTHING = Nothing.instance();

    private final Expression left;
    private final TokenKind operator;
    private final Expression right;



    public ComparisonExpression(Token token, Expression left, TokenKind operator, Expression right) {
        super(token);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public TokenKind getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "%s %s %s".formatted(left, operator, right);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ComparisonExpression that)) return false;
        return Objects.equals(left, that.left) &&
                operator == that.operator && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, operator, right);
    }

    /**
     * Evaluates the filter expression in the given context.
     * @param context
     * @return
     */
    public Boolean evaluate(FilterContext context) {
        Object left = this.left.evaluate(context);
        if ( left instanceof JSONPathNodeList nl && nl.size() == 1 ) {
            left = nl.getFirst().getValue();
        }

        Object right = this.right.evaluate(context);
        if ( right instanceof JSONPathNodeList nl && nl.size() == 1 ) {
            right = nl.getFirst().getValue();
        }

        return compare(left, operator, right);
    }

    private Boolean compare(Object left, TokenKind operator, Object right) {
        return switch (operator) {
            case EQ -> eq(left, right);
            case NE -> !eq(left, right);
            case LT -> lt(left, right);
            case GT -> lt(right, left);
            case GE -> lt(right, left) || eq(left, right);
            case LE -> lt(left, right) || eq(left, right);
            default -> throw new IllegalStateException("Unexpected token: " + operator);
        };
    }


    private boolean eq(Object left, Object right) {
        if (right instanceof JSONPathNodeList) {
            Object temp = left;
            left = right;
            right = temp;
        }

        if (left instanceof JSONPathNodeList leftNL) {
            if (right instanceof JSONPathNodeList) {
                return left.equals(right);
            }
            if (leftNL.isEmpty()) {
                return right == NOTHING;
            }
            if (leftNL.size() == 1) {
                return leftNL.getFirst().equals(right);
            }
            return false;
        }

        if (left == NOTHING && right == NOTHING) {
            return true;
        }

        if (right instanceof Boolean) {
            Object temp = left;
            left = right;
            right = temp;
        }

        if (left instanceof Boolean ) {
            return right instanceof Boolean && left == right;
        }

        return left.equals(right);
    }

    private boolean lt(Object left, Object right) {
        if (left instanceof String ls && right instanceof String rs) {
            return ls.compareTo(rs) < 0;
        }

        if (left instanceof Number leftN && right instanceof Number rightN) {
            switch (leftN) {
                case Double ld -> {
                    if (rightN instanceof Double rd) return ld < rd;
                }
                case Float lf -> {
                    if (rightN instanceof Float rf) return lf < rf;
                }
                case Integer li -> {
                    if (rightN instanceof Integer ri) return li < ri;
                }
                case Long ll -> {
                    if (rightN instanceof Long rl) return ll < rl;
                }
                default -> throw new IllegalStateException(
                        "Unexpected types left:%s, right: %s ".
                                formatted(leftN.getClass(), rightN.getClass()));
            }
        }
        return false;
    }
}
