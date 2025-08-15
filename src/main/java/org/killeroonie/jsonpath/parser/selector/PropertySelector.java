package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.Token;

public class PropertySelector extends JSONPathSelector {
    public PropertySelector(JSONPathEnvironment env, Token token, String name, boolean isShorthand) {
        super(env, token);
    }
}

/*

class PropertySelector(JSONPathSelector):
    """A shorthand or bracketed property selector."""

    __slots__ = ("name", "shorthand")

    def __init__(
        self,
        *,
        env: JSONPathEnvironment,
        token: Token,
        name: str,
        shorthand: bool,
    ) -> None:
        super().__init__(env=env, token=token)
        self.name = name
        self.shorthand = shorthand

    def __str__(self) -> str:
        return (
            f"[{canonical_string(self.name)}]"
            if self.shorthand
            else f"{canonical_string(self.name)}"
        )

    def __eq__(self, __value: object) -> bool:
        return (
            isinstance(__value, PropertySelector)
            and self.name == __value.name
            and self.token == __value.token
        )

    def __hash__(self) -> int:
        return hash((self.name, self.token))

    def resolve(self, matches: Iterable[JSONPathMatch]) -> Iterable[JSONPathMatch]:
        for match in matches:
            if not isinstance(match.obj, Mapping):
                continue

            with suppress(KeyError):
                _match = self.env.match_class(
                    filter_context=match.filter_context(),
                    obj=self.env.getitem(match.obj, self.name),
                    parent=match,
                    parts=match.parts + (self.name,),
                    path=match.path + f"[{canonical_string(self.name)}]",
                    root=match.root,
                )
                match.add_child(_match)
                yield _match

    async def resolve_async(
        self, matches: AsyncIterable[JSONPathMatch]
    ) -> AsyncIterable[JSONPathMatch]:
        async for match in matches:
            if not isinstance(match.obj, Mapping):
                continue

            with suppress(KeyError):
                _match = self.env.match_class(
                    filter_context=match.filter_context(),
                    obj=await self.env.getitem_async(match.obj, self.name),
                    parent=match,
                    parts=match.parts + (self.name,),
                    path=match.path + f"[{canonical_string(self.name)}]",
                    root=match.root,
                )
                match.add_child(_match)
                yield _match



 */