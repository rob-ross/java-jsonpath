package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.JSONPathNodeList;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;

import java.util.Objects;

/**
 * A pair of expressions and a logical operator.
 * todo insert InfixExpression as superclass of this class.
 */
public class LogicalExpression extends Expression {

    private static final Nothing NOTHING = Nothing.instance();

    private final Expression left;
    private final TokenKind operator;
    private final Expression right;

    public LogicalExpression(Token token, Expression left, TokenKind operator, Expression right) {
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
        if (!(object instanceof LogicalExpression that)) return false;
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
        Object leftResult = left.evaluate(context);
        if (operator == TokenKind.AND && leftResult instanceof Boolean b && ! b) {
            // short circuit
            return false;
        }
        return compare(leftResult, operator, right.evaluate(context));
    }

    /**
     * Test for truthiness when evaluating filter expressions.
     * @param obj
     * @return
     */
    private boolean isTruthy(Object obj) {
        if (obj instanceof JSONPathNodeList nl && nl.isEmpty()) {
            return false;
        }
        if (obj == NOTHING) {
            return false;
        }
        if (obj == null ) {
            return true;
        }
        if (obj instanceof Number n) {
            return ! (n.longValue() == 0 );
        }
        if (obj instanceof Boolean b) {
            return b;
        }
        throw new IllegalStateException("Unexpected type: " + obj.getClass());
    }

    private boolean compare(Object left, TokenKind operator, Object right) {
        if ( operator == TokenKind.AND ) {
            return isTruthy(left) && isTruthy(right);
        } else if (operator == TokenKind.OR ) {
            return isTruthy(left) || isTruthy(right);
        }
        throw new IllegalStateException("Unexpected operator: " + operator);
    }
}
