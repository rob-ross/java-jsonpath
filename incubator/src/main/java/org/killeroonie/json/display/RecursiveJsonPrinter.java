package org.killeroonie.json.display;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class RecursiveJsonPrinter {

    public static void main(String[] args) throws IOException {
        String jsonInput = """
        {
          "name": "Project Apollo",
          "active": true,
          "crew_size": 3,
          "missions": [
            {
              "name": "Apollo 11",
              "year": 1969,
              "crew": ["Armstrong", "Aldrin", "Collins"]
            },
            {
              "name": "Apollo 13",
              "year": 1970,
              "crew": ["Lovell", "Swigert", "Haise"]
            }
          ],
          "contractor": null
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonInput);

        System.out.println("--- Printing Object Graph ---");
        printNode(rootNode, "");
    }

    /**
     * Recursively prints the contents of a JsonNode with indentation.
     *
     * @param node The current JsonNode to print.
     * @param indent The string used for indentation.
     */
    private static void printNode(JsonNode node, String indent) {
        // This is the equivalent of your "instanceof Map" check.
        if (node.isObject()) {
            System.out.println(indent + "{");
            // Iterate over all fields in the object
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                System.out.print(indent + "  \"" + field.getKey() + "\": ");
                // Recurse on the field's value
                printNode(field.getValue(), indent + "  ");
            }
            System.out.println(indent + "}");
        }
        // This is the equivalent of your "instanceof List" check.
        else if (node.isArray()) {
            System.out.println(indent + "[");
            // Iterate over all elements in the array
            for (JsonNode element : node) {
                // Recurse on the array element
                printNode(element, indent + "  ");
            }
            System.out.println(indent + "]");
        }
        // This is the base case for all primitive types (string, number, boolean, null).
        else if (node.isValueNode()) {
            // asText() provides a safe string representation for any value node.
            System.out.println(node.asText());
        }
        // This handles the case where a value is explicitly null
        else if (node.isNull()) {
            System.out.println("null");
        }
    }
}