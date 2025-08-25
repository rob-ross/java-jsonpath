package org.killeroonie.jsonpath;

import org.killeroonie.jsonpath.parser.segment.JSONPathRecursiveDescentSegment;
import org.killeroonie.jsonpath.parser.segment.JSONPathSegment;
import org.killeroonie.jsonpath.parser.selector.IndexSelector;
import org.killeroonie.jsonpath.parser.selector.NameSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public String toString() {
        //String str; //str  = "$" + "".join(str(segment) for segment in self.segments)
        StringBuilder sb = new StringBuilder("$");
        for (JSONPathSegment segment : segments) {
            sb.append(segment.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JSONPathQuery that)) return false;
        return Objects.equals(segments, that.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(segments);
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
            if ( segment.getSelectors().size() == 1 ) {
                Object firstSelector =  segment.getSelectors().getFirst();
                if ( firstSelector instanceof NameSelector || firstSelector instanceof IndexSelector) {
                    continue;
                }
            }
            return false;
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

    /**
     * Generate `JSONPathNode` instances for each match of this query in value.
     *
     *         Arguments:
     *             value: JSON-like data to query, as you'd get from `json.load`.
     *
     *         Returns:
     *             An iterator yielding `JSONPathNode` objects for each match.
     *
     *         Raises:
     *             JSONPathSyntaxError: If the query is invalid.
     *             JSONPathTypeError: If a filter expression attempts to use types in
     *                 an incompatible way.
     * @param value
     * @return
     */
    public Iterable<JSONPathNode> findIter(Object value ) {
        return finditer(value, null);
    }

    public Iterable<JSONPathNode> finditer(Object value, FilterContextVars filterContext) {
        List<JSONPathNode> nodes = new ArrayList<>();
        JSONPathNode node = new JSONPathNode(value, new ArrayList<>(), value);
        nodes.add(node);
        for (JSONPathSegment segment : segments) {
            nodes = segment.resolve(nodes);
        }
        return nodes;
    }

    public JSONPathNodeList find(Object root) {
        return null;
    }
}


/*

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

 */