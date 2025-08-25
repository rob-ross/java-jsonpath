package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;

public class SingularQuerySelector extends JSONPathSelector {


    public SingularQuerySelector(JSONPathEnvironment env, Token token) {
        super(env, token);
    }
}

/*

class SingularQuerySelector(JSONPathSelector):
    """An embedded absolute query.

    The result of the embedded query is used as an object member name or array element
    index.

    NOTE: This is a non-standard selector.
    """

    __slots__ = ("query",)

    def __init__(
        self, *, env: JSONPathEnvironment, token: Token, query: JSONPath
    ) -> None:
        super().__init__(env=env, token=token)
        self.query = query

        if env.strict:
            raise JSONPathSyntaxError("unexpected query selector", token=token)

    def __str__(self) -> str:
        return str(self.query)

    def __eq__(self, __value: object) -> bool:
        return (
            isinstance(__value, SingularQuerySelector)
            and self.query == __value.query
            and self.token == __value.token
        )

    def __hash__(self) -> int:
        return hash((self.query, self.token))

    def resolve(self, node: JSONPathMatch) -> Iterable[JSONPathMatch]:
        if isinstance(node.obj, Mapping):
            nodes = NodeList(self.query.finditer(node.root))

            if nodes.empty():
                return

            value = nodes[0].value

            if not isinstance(value, str):
                return

            with suppress(KeyError):
                match = node.new_child(self.env.getitem(node.obj, value), value)
                node.add_child(match)
                yield match

        if isinstance(node.obj, Sequence) and not isinstance(node.obj, str):
            nodes = NodeList(self.query.finditer(node.root))

            if nodes.empty():
                return

            value = nodes[0].value

            if not isinstance(value, int):
                return

            index = self._normalized_index(node.obj, value)

            with suppress(IndexError):
                match = node.new_child(self.env.getitem(node.obj, index), index)
                node.add_child(match)
                yield match

    async def resolve_async(self, node: JSONPathMatch) -> AsyncIterable[JSONPathMatch]:
        if isinstance(node.obj, Mapping):
            nodes = NodeList(
                [match async for match in await self.query.finditer_async(node.root)]
            )

            if nodes.empty():
                return

            value = nodes[0].value

            if not isinstance(value, str):
                return

            with suppress(KeyError):
                match = node.new_child(
                    await self.env.getitem_async(node.obj, value), value
                )
                node.add_child(match)
                yield match

        if isinstance(node.obj, Sequence) and not isinstance(node.obj, str):
            nodes = NodeList(
                [match async for match in await self.query.finditer_async(node.root)]
            )

            if nodes.empty():
                return

            value = nodes[0].value

            if not isinstance(value, int):
                return

            index = self._normalized_index(node.obj, value)

            with suppress(IndexError):
                match = node.new_child(
                    await self.env.getitem_async(node.obj, index), index
                )
                node.add_child(match)
                yield match

    def _normalized_index(self, obj: Sequence[object], index: int) -> int:
        if index < 0 and len(obj) >= abs(index):
            return len(obj) + index
        return index


 */