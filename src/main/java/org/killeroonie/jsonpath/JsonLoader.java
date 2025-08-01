package org.killeroonie.jsonpath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility to load JSON data from various sources into standard Java objects.
 */
public class JsonLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Loads data from a source, parsing it as JSON if it's a String or InputStream.
     *
     * @param data The data source. Can be a String, InputStream, or an existing
     *             Map/List structure.
     * @return A Java object representation (Map, List, String, etc.).
     */
    public static Object load(Object data) {
        if (data instanceof String s) {
            try {
                return MAPPER.readTree(s);
            } catch (IOException e) {
                // Python version returns the string if it's not malformed JSON.
                // For simplicity here, we'll throw. A more complex check could be added.
                throw new UncheckedIOException("Failed to parse JSON string", e);
            }
        }
        if (data instanceof InputStream is) {
            try {
                return MAPPER.readTree( is );
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to parse JSON from stream", e);
            }
        }
        return data;
    }

    /**
     * Unpacks a Jackson JsonNode into a standard Java object (Map, List, primitive).
     * This is useful for returning results that are not tied to the Jackson library.
     */
    public static Object unpack(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isPojo()) {
            // Could be anything, so return it as is.
            return node;
        }
        if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                map.put(field.getKey(), unpack(field.getValue()));
            }
            return map;
        }
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode element : node) {
                list.add(unpack(element));
            }
            return list;
        }
        if (node instanceof ValueNode) {
            if (node.isBoolean()) return node.asBoolean();
            if (node.isNumber()) return node.numberValue();
            return node.asText();
        }
        // Should not be reached
        return node.toString();
    }
}