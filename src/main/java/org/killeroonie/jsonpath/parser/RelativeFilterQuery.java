package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.JSONPathQuery;
import org.killeroonie.jsonpath.Token;

public class RelativeFilterQuery extends FilterQuery{
    public RelativeFilterQuery(Token token, JSONPathQuery jsonPathQuery) {
        super(token, jsonPathQuery);
    }
}

/*

class RelativeFilterQuery(FilterQuery):
    """A JSONPath expression starting at the current node."""

    __slots__ = ()

    def __str__(self) -> str:
        return "@" + str(self.query)[1:]

    def evaluate(self, context: FilterContext) -> object:
        """Evaluate the filter expression in the given _context_."""
        if not isinstance(context.current, (list, dict)):
            if self.query.empty():
                return context.current
            return JSONPathNodeList()

        return JSONPathNodeList(self.query.find(context.current))
 */