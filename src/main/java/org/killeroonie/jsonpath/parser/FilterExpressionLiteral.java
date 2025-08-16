package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

public class FilterExpressionLiteral<T> extends FilterExpression {

    private final T value;

    public FilterExpressionLiteral(Token token, Expression expression, T value) {
        super(token, expression);
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}

/*

class FilterExpressionLiteral(Expression, Generic[LITERAL_T]):
    """Base class for filter expression literals."""

    __slots__ = ("value",)

    def __init__(self, token: Token, value: LITERAL_T) -> None:
        super().__init__(token=token)
        self.value = value

    def __str__(self) -> str:
        return repr(self.value).lower()

    def __eq__(self, other: object) -> bool:
        return self.value == other

    def __hash__(self) -> int:
        return hash(self.value)

    def evaluate(self, _: FilterContext) -> LITERAL_T:
        """Return the value associated with this literal."""
        return self.value


 */
