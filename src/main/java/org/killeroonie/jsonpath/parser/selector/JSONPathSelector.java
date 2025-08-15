package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.JSONPathMatch;
import org.killeroonie.jsonpath.Token;

import java.util.List;

/**
 * Base class for all JSONPath segments and selectors
 */
public abstract class JSONPathSelector {

    private static JSONPathEnvironment env;


    public JSONPathSelector(JSONPathEnvironment env, Token token) {
        this.env = env;
    }

    public static JSONPathEnvironment getEnv() {
        return env;
    }

    /**
     * Apply the segment/selector to each node in _matches_.
     *         Arguments:
     *             matches: Nodes matched by preceding segments/selectors. This is like
     *                 a lazy _NodeList_, as described in RFC 9535, but each match carries
     *                 more than the node's value and location.
     *         Returns:
     *             The `JSONPathMatch` instances created by applying this selector to each
     *             preceding node.
     */
    protected List<JSONPathMatch> resolve() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*




    def resolve(self, matches: Iterable[JSONPathMatch]) -> Iterable[JSONPathMatch]:
        """Apply the segment/selector to each node in _matches_.

        Arguments:
            matches: Nodes matched by preceding segments/selectors. This is like
                a lazy _NodeList_, as described in RFC 9535, but each match carries
                more than the node's value and location.

        Returns:
            The `JSONPathMatch` instances created by applying this selector to each
            preceding node.
        """

    @abstractmethod
    def resolve_async(
        self, matches: AsyncIterable[JSONPathMatch]
    ) -> AsyncIterable[JSONPathMatch]:
        """An async version of `resolve`."""


     */
}
