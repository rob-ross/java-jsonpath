package org.killeroonie.jsonpath.parser.segment;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.JSONPathNode;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.parser.selector.JSONPathSelector;

import java.util.ArrayList;
import java.util.List;

public class JSONChildSegment extends JSONPathSegment {
    public JSONChildSegment(JSONPathEnvironment env, Token token, List<JSONPathSelector> selectors) {
        super(env, token, selectors);
    }

    public List<JSONPathNode> resolve(List<JSONPathNode> nodes) {
        final List<JSONPathNode> result = new ArrayList<>();
        for (JSONPathNode node : nodes) {
            for (JSONPathSelector selector : getSelectors()) {
                result.addAll(selector.resolve(node));
            }
        }
        return result;
    }
}
