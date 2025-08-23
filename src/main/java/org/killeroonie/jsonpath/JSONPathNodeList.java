package org.killeroonie.jsonpath;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * A List of JSONPathNode instances.
 */
public class JSONPathNodeList extends AbstractList<JSONPathNode> {

    private final List<JSONPathNode> nodes = new ArrayList<>();

    public JSONPathNodeList(List<JSONPathNode> nodes) {
        this.nodes.addAll(nodes);
    }

    public JSONPathNodeList() { }


    public JSONPathNode get(int index) {
        return nodes.get(index);
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public void add(int index, JSONPathNode element) {
        nodes.add(index, element);
    }

    @Override
    public JSONPathNode remove(int index) {
        return nodes.remove(index);
    }
}
