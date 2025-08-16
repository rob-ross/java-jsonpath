package org.killeroonie.jsonpath.parser.selector;

import org.killeroonie.jsonpath.Constants;
import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.JSONPathMatch;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.exception.JSONPathIndexException;

import java.util.List;

/**
 * Base class for all JSONPath segments and selectors
 */
public abstract class JSONPathSelector {

    private final JSONPathEnvironment env;
    private final Token token;

    public JSONPathSelector(JSONPathEnvironment env, Token token) {
        this.env = env;
        this.token = token;
    }

    public  JSONPathEnvironment getEnv() {
        return env;
    }

    public Token getToken() {
        return token;
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

    void checkRange(long indexValue, String indexName) {
        if ( indexValue < Constants.JSON_MIN_INT_INDEX || indexValue > Constants.JSON_MAX_INT_INDEX) {
            throw new JSONPathIndexException("%s index out of JSON range: %d".formatted(indexName, indexValue), getToken());
        } else if (indexValue < Constants.Java_MIN_INT_INDEX ||  indexValue > Constants.Java_MAX_INT_INDEX) {
            throw new JSONPathIndexException("%s index out of Java range: %d".formatted(indexName, indexValue), getToken());
        }
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
