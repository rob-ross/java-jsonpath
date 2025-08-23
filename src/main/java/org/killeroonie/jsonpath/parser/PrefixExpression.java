package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public class PrefixExpression extends Expression {

    private final String operator;
    private final Expression right;

    public PrefixExpression(Token token, String operator, Expression right) {
        super(token);
        this.operator = operator;
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public Object evaluate(FilterContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}


/*

class PrefixExpression(Expression):
    """An expression composed of a prefix operator and another expression."""

    __slots__ = ("operator", "right")

    def __init__(self, token: Token, operator: str, right: Expression):
        self.operator = operator
        self.right = right
        super().__init__(token)

    def __str__(self) -> str:
        return f"{self.operator}{self.right}"

    def __eq__(self, other: object) -> bool:
        return (
            isinstance(other, PrefixExpression)
            and self.operator == other.operator
            and self.right == other.right
        )

    def evaluate(self, context: FilterContext) -> object:
        """Evaluate the filter expression in the given _context_."""
        if self.operator == "!":
            return not _is_truthy(self.right.evaluate(context))
        raise JSONPathTypeError(f"unknown operator {self.operator} {self.right}")

 */