package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.JSONPathQuery;
import org.killeroonie.jsonpath.Token;

public abstract class FilterQuery extends Expression {

    private final JSONPathQuery query;

    FilterQuery(Token token, JSONPathQuery query) {
        super(token);
        this.query = query;
    }

    public JSONPathQuery getQuery() {
        return query;
    }

    /*

    class FilterQuery(Expression, ABC):
    """Base class for all query selectors."""

    __slots__ = ("query",)

    def __init__(self, token: Token, query: JSONPathQuery) -> None:
        super().__init__(token)
        self.query = query

    def __eq__(self, other: object) -> bool:
        return isinstance(other, FilterQuery) and str(self) == str(other)

     */
}
