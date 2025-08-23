package org.killeroonie.jsonpath.parser;

import org.killeroonie.jsonpath.Token;

import java.util.List;

public class FunctionExtension extends Expression {

    private final String name;
    private final List<Expression> args;

    public FunctionExtension(Token token, String name, List<Expression> args) {
        super(token);
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public Object evaluate(FilterContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

/*

class FunctionExtension(Expression):
    """A filter function."""

    __slots__ = ("name", "args")

    def __init__(self, token: Token, name: str, args: Sequence[Expression]) -> None:
        super().__init__(token)
        self.name = name
        self.args = args

    def __str__(self) -> str:
        args = [str(arg) for arg in self.args]
        return f"{self.name}({', '.join(args)})"

    def __eq__(self, other: object) -> bool:
        return (
            isinstance(other, FunctionExtension)
            and other.name == self.name
            and other.args == self.args
        )

    def evaluate(self, context: FilterContext) -> object:
        """Evaluate the filter expression in the given _context_."""
        try:
            func = context.env.function_extensions[self.name]
        except KeyError:
            return NOTHING
        args = [arg.evaluate(context) for arg in self.args]
        return func(*self._unpack_node_lists(func, args))

    def _unpack_node_lists(
        self, func: FilterFunction, args: List[object]
    ) -> List[object]:
        _args: List[object] = []
        for idx, arg in enumerate(args):
            if func.arg_types[idx] != ExpressionType.NODES and isinstance(
                arg, JSONPathNodeList
            ):
                if len(arg) == 0:
                    # If the query results in an empty nodelist, the
                    # argument is the special result Nothing.
                    _args.append(NOTHING)
                elif len(arg) == 1:
                    # If the query results in a nodelist consisting of a
                    # single node, the argument is the value of the node
                    _args.append(arg[0].value)
                else:
                    # This should not be possible as a non-singular query
                    # would have been rejected when checking function
                    # well-typedness.
                    _args.append(arg)
            else:
                _args.append(arg)

        return _args



 */