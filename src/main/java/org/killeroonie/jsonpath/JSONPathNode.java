package org.killeroonie.jsonpath;

import java.util.List;

/**
 * A JSON-like value and its location in a JSON document.
 *     Attributes:
 *         value: The JSON-like value at this node.
 *         location: The names indices that make up the normalized path to _value_.
 */
public class JSONPathNode {

    private final Object value;  // todo need better type checking here?
    private final List<Object> location; // in Python Tuple[Union[int, str], ...]
    private final Object root;

    public JSONPathNode(Object value, List<Object> location, Object root) {
        this.value = value;
        this.location = location;
        this.root = root;
    }

    public Object getValue() {
        return value;
    }

    public List<Object> getLocation() {
        return location;
    }

    public Object getRoot() {
        return root;
    }
}
