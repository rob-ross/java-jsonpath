package org.killeroonie.jsonpath.parser.selector;


import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;

public class IndexSelector extends JSONPathSelector {
    private final long index;

    public IndexSelector(JSONPathEnvironment env, Token token, long index) {
        super(env, token);
        this.index = index;
        checkRange(index, "index");
    }

    public int getIndex() {
        return (int)index;
    }

    @Override
    public String toString() {
        return ""+index;
    }
}

/*

class IndexSelector(JSONPathSelector):
    """The array index selector."""

    __slots__ = ("index", "_as_key")

    def __init__(
        self,
        *,
        env: JSONPathEnvironment,
        token: Token,
        index: int,
    ) -> None:
        if index < env.min_int_index or index > env.max_int_index:
            raise JSONPathIndexError("index out of range", token=token)

        super().__init__(env=env, token=token)
        self.index = index
        self._as_key = str(self.index)

    def __str__(self) -> str:
        return str(self.index)

    def __eq__(self, __value: object) -> bool:
        return (
            isinstance(__value, IndexSelector)
            and self.index == __value.index
            and self.token == __value.token
        )

    def __hash__(self) -> int:
        return hash((self.index, self.token))

    def _normalized_index(self, obj: Sequence[object]) -> int:
        if self.index < 0 and len(obj) >= abs(self.index):
            return len(obj) + self.index
        return self.index

    def resolve(self, node: JSONPathNode) -> Iterable[JSONPathNode]:
        """Select an element from an array by index."""
        if isinstance(node.value, list):
            norm_index = self._normalized_index(node.value)
            with suppress(IndexError):
                yield node.new_child(node.value[self.index], norm_index)



 */
