package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.JSONPathQuery;
import org.killeroonie.jsonpath.Token;

public class RootFilterQuery extends FilterQuery {

    public RootFilterQuery(Token token, JSONPathQuery query) {
        super(token, query);
    }

    @Override
    public String toString() {
        return getQuery().toString();
    }
}

/*


class RootFilterQuery(FilterQuery):
    """A JSONPath expression starting at the root node."""

    __slots__ = ()

    def __str__(self) -> str:
        return str(self.query)

    def evaluate(self, context: FilterContext) -> object:
        """Evaluate the filter expression in the given _context_."""
        return JSONPathNodeList(self.query.find(context.root))

 */