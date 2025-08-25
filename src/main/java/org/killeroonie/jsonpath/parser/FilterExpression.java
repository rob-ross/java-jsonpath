package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;

public class FilterExpression extends Expression {

    private final Expression expression;

    public FilterExpression(Token token, Expression expression) {
        super(token);
        this.expression = expression;
    }

    @Override
    public Object evaluate(FilterContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return canonicalStr(expression, PJPParser.PrecedenceLevel.LOWEST);
    }

    private String canonicalStr(Expression expression, PJPParser.PrecedenceLevel parentPrecedence) {
        if (expression instanceof LogicalExpression le) {
            if (le.getOperator() == TokenKind.AND) {
                String left = canonicalStr(le.getLeft(), PJPParser.PrecedenceLevel.LOGICAL_AND);
                String right = canonicalStr(le.getRight(), PJPParser.PrecedenceLevel.LOGICAL_AND);
                String expr = left + " && " + right;
                return (parentPrecedence.getLevel() >= PJPParser.PrecedenceLevel.LOGICAL_AND.getLevel())
                        ? "(" + expr + ")"
                        : expr;
            }
            if (le.getOperator() == TokenKind.OR) {
                String left = canonicalStr(le.getLeft(), PJPParser.PrecedenceLevel.LOGICAL_OR);
                String right = canonicalStr(le.getRight(), PJPParser.PrecedenceLevel.LOGICAL_OR);
                String expr = left + " || " + right;
                return (parentPrecedence.getLevel() >= PJPParser.PrecedenceLevel.LOGICAL_OR.getLevel())
                        ? "(" + expr + ")"
                        : expr;
            }
        }

        if (expression instanceof PrefixExpression pe) {
            String operand = canonicalStr(pe.getRight(), PJPParser.PrecedenceLevel.PREFIX);
            String expr = "!" + operand;
            return (parentPrecedence.getLevel() > PJPParser.PrecedenceLevel.PREFIX.getLevel())
                    ? "(" + expr + ")"
                    : expr;
        }

        return String.valueOf(expression);
    }
    
    /*
        def _canonical_string(self, expression: Expression, parent_precedence: int) -> str:
        if isinstance(expression, LogicalExpression):
            if expression.operator == "&&":
                left = self._canonical_string(expression.left, PRECEDENCE_LOGICAL_AND)
                right = self._canonical_string(expression.right, PRECEDENCE_LOGICAL_AND)
                expr = f"{left} && {right}"
                return (
                    f"({expr})" if parent_precedence >= PRECEDENCE_LOGICAL_AND else expr
                )

            if expression.operator == "||":
                left = self._canonical_string(expression.left, PRECEDENCE_LOGICAL_OR)
                right = self._canonical_string(expression.right, PRECEDENCE_LOGICAL_OR)
                expr = f"{left} || {right}"
                return (
                    f"({expr})" if parent_precedence >= PRECEDENCE_LOGICAL_OR else expr
                )

        if isinstance(expression, PrefixExpression):
            operand = self._canonical_string(expression.right, PRECEDENCE_PREFIX)
            expr = f"!{operand}"
            return f"({expr})" if parent_precedence > PRECEDENCE_PREFIX else expr

        return str(expression)
     */
}
