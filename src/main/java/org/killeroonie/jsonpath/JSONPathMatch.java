package org.killeroonie.jsonpath;

import java.util.List;

public class JSONPathMatch {
    public final FilterContextVars filterContext;
    public final Object obj;
    public final JSONPathMatch parent;
    public final String path;
    public final List<Object> parts;
    public final Object root;

    public JSONPathMatch(FilterContextVars filterContext, Object obj, JSONPathMatch parent,
                         String path, List<Object> parts, Object root) {
        this.filterContext = filterContext;
        this.obj = obj;
        this.parent = parent;
        this.path = path;
        this.parts = parts;
        this.root = root;
    }

    public String getParts() { return ""; }
}

/*

    """A matched object with a concrete path.

    Attributes:
        children: Matched child nodes. This will only be populated after
            all children have been visited, usually by using `findall()`
            or `list(finditer())`.
        obj: The matched object.
        parent: The immediate parent to this match in the JSON document.
            If this is the root node, _parent_ will be `None`.
        path: The canonical string representation of the path to this match.
        parts: The keys, indices and/or slices that make up the path to this
            match.
        root: A reference to the root node in the JSON document.
    """

    __slots__ = (
        "_filter_context",
        "children",
        "obj",
        "parent",
        "parts",
        "path",
        "root",
    )

    pointer_class = JSONPointer

    def __init__(
        self,
        *,
        filter_context: FilterContextVars,
        obj: object,
        parent: Optional[JSONPathMatch],
        path: str,
        parts: Tuple[PathPart, ...],
        root: Union[Sequence[Any], Mapping[str, Any]],
    ) -> None:
        self._filter_context = filter_context
        self.children: List[JSONPathMatch] = []
        self.obj: object = obj
        self.parent: Optional[JSONPathMatch] = parent
        self.parts: Tuple[PathPart, ...] = parts
        self.path: str = path
        self.root: Union[Sequence[Any], Mapping[str, Any]] = root

    def __str__(self) -> str:
        return f"{_truncate(str(self.obj), 5)!r} @ {_truncate(self.path, 5)}"

    def add_child(self, *children: JSONPathMatch) -> None:
        """Append one or more children to this match."""
        self.children.extend(children)

    def filter_context(self) -> FilterContextVars:
        """Return filter context data for this match."""
        return self._filter_context

    def pointer(self) -> JSONPointer:
        """Return a `JSONPointer` pointing to this match's path."""
        return JSONPointer.from_match(self)

    @property
    def value(self) -> object:
        """Return the value associated with this match/node."""
        return self.obj


def _truncate(val: str, num: int, end: str = "...") -> str:
    # Replaces consecutive whitespace with a single newline.
    # Treats quoted whitespace the same as unquoted whitespace.
    words = val.split()
    if len(words) < num:
        return " ".join(words)
    return " ".join(words[:num]) + end

 */