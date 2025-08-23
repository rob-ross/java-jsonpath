package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.parser.segment.JSONPathRecursiveDescentSegment;
import org.killeroonie.jsonpath.parser.segment.JSONPathSegment;

import java.util.List;

/**
 * A compiled JSONPath expression ready to be applied to JSON-like data.
 */
public class JSONPathQuery {

    private final JSONPathEnvironment env;
    private final List<JSONPathSegment> segments;

    /**
     *
     * @param env The {@link JSONPathEnvironment} this query is bound to.
     * @param segments the List<{@link JSONPathSegment}> instances that make up this query.
     */
    public JSONPathQuery(JSONPathEnvironment env, List<JSONPathSegment> segments) {
        this.env = env;
        this.segments = segments;
    }

    public JSONPathEnvironment getEnv() {
        return env;
    }

    public List<JSONPathSegment> getSegments() {
        return segments;
    }

    /**
     * Returns true if this JSONPath expression is a singular query.
     * @return true if this JSONPath expression is a singular query.
     */
    public boolean isSingularQuery() {
        for ( JSONPathSegment segment : segments ) {
            if (segment instanceof JSONPathRecursiveDescentSegment) {
                return false;
            }
            if ( segment.getSelectors().size() == 1 ){

            }
        }
        return true;
    }

    /**
     * Returns true if this query has no segments.
     * @return true if this query has no segments.
     */
    public boolean isEmpty() {
        return segments == null || segments.isEmpty();
    }
}


/*

    def __init__(
        self,
        *,
        env: JSONPathEnvironment,
        segments: Tuple[JSONPathSegment, ...],
    ) -> None:
        self.env = env
        self.segments = segments

    def __str__(self) -> str:
        return "$" + "".join(str(segment) for segment in self.segments)

    def __hash__(self) -> int:
        return hash(self.segments)

    def finditer(
        self,
        value: JSONValue,
    ) -> Iterable[JSONPathNode]:
        """Generate `JSONPathNode` instances for each match of this query in value.

        Arguments:
            value: JSON-like data to query, as you'd get from `json.load`.

        Returns:
            An iterator yielding `JSONPathNode` objects for each match.

        Raises:
            JSONPathSyntaxError: If the query is invalid.
            JSONPathTypeError: If a filter expression attempts to use types in
                an incompatible way.
        """
        nodes: Iterable[JSONPathNode] = [
            JSONPathNode(
                value=value,
                location=(),
                root=value,
            )
        ]

        for segment in self.segments:
            nodes = segment.resolve(nodes)

        return nodes

    def find(
        self,
        value: JSONValue,
    ) -> JSONPathNodeList:
        """Apply this JSONPath expression to JSON-like _value_ and return a node list.

        Arguments:
            value: JSON-like data to query, as you'd get from `json.load`.

        Returns:
            A list of `JSONPathNode` instance.

        Raises:
            JSONPathSyntaxError: If the query is invalid.
            JSONPathTypeError: If a filter expression attempts to use types in
                an incompatible way.
        """
        return JSONPathNodeList(self.finditer(value))

    apply = find

    def find_one(self, value: JSONValue) -> Optional[JSONPathNode]:
        """Return the first node from applying this query to _value_.

        Arguments:
            value: JSON-like data to query, as you'd get from `json.load`.

        Returns:
            The first available `JSONPathNode` instance, or `None` if there
                are no matches.

        Raises:
            JSONPathSyntaxError: If the query is invalid.
            JSONPathTypeError: If a filter expression attempts to use types in
                an incompatible way.
        """
        try:
            return next(iter(self.finditer(value)))
        except StopIteration:
            return None

    def singular_query(self) -> bool:
        """Return `True` if this JSONPath expression is a singular query."""
        for segment in self.segments:
            if isinstance(segment, JSONPathRecursiveDescentSegment):
                return False

            if len(segment.selectors) == 1 and isinstance(
                segment.selectors[0], (NameSelector, IndexSelector)
            ):
                continue

            return False

        return True




 */