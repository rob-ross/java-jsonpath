package org.killeroonie.jsonpath.parser.segment;

import org.killeroonie.jsonpath.JSONPathEnvironment;
import org.killeroonie.jsonpath.JSONPathNode;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.parser.selector.JSONPathSelector;

import java.util.ArrayList;
import java.util.List;

public class JSONPathChildSegment extends JSONPathSegment {
    public JSONPathChildSegment(JSONPathEnvironment env, Token token, List<JSONPathSelector> selectors) {
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

    // f"[{', '.join(str(itm) for itm in self.selectors)}]"

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        int index = 0;
        for (JSONPathSelector selector : getSelectors()) {
            sb.append(selector.toString());
            index++;
            if ( getSelectors().size() > index ) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
