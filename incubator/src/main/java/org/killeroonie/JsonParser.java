package org.killeroonie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class JsonParser {

    private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);

    String jsonString = """
    {
        "name": "John Doe",
        "age": 30,
        "address": {
            "street": "123 Main St",
            "city": "Anytown"
        }
    }
    """;

    private final ObjectMapper objectMapper;

    public JsonParser() {
        // Create an ObjectMapper instance (the main entry point for Jackson functionality)
        objectMapper = new ObjectMapper();

    }

    /**
     * Parse JSON string to a generic JsonNode
     */
    public JsonNode parseToJsonNode(String json) throws JsonProcessingException {
        return objectMapper.readTree(json);
    }

    /**
     * Parse JSON string to a specific Java class
     */
    public <T> T parseToObject(String json, Class<T> valueType) throws JsonProcessingException {
        return objectMapper.readValue(json, valueType);
    }

    /**
     * Parse JSON string to a Map
     */
    public Map<String, Object> parseToMap(String json) throws JsonProcessingException {
        //noinspection Convert2Diamond
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Parse JSON array to a List of a specific type
     */
    public <T> List<T> parseToList(String json, Class<T> elementType) throws JsonProcessingException {
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
    }

    // Example POJO class for JSON deserialization
    @SuppressWarnings("unused")
    static class Person {
        private String name;
        private int age;
        private boolean isEmployee;
        private Address address;
        private String[] phoneNumbers;

        // Getters and setters (required for Jackson)
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public boolean isEmployee() { return isEmployee; }
        public void setEmployee(boolean employee) { isEmployee = employee; }

        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }

        public String[] getPhoneNumbers() { return phoneNumbers; }
        public void setPhoneNumbers(String[] phoneNumbers) { this.phoneNumbers = phoneNumbers; }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + ", isEmployee=" + isEmployee + "}";
        }
    }

    @SuppressWarnings("unused")
    static class Address {
        private String street;
        private String city;
        private String zipCode;

        // Getters and setters
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    }

    private void parseTest1() {
        try {
            logger.debug("Starting JSON parsing test");

            // Parse to a generic JsonNode (useful for dynamic access)
            JsonNode jsonNode = parseToJsonNode(jsonString);
            logger.info("Name: {}", jsonNode.get("name").asText());
            logger.info("City: {}", jsonNode.get("address").get("city").asText());

            // Parse to a specific Java class
            Person person = parseToObject(jsonString, Person.class);
            logger.info("Person object: {}", person);

            // Parse to a Map
            var dataMap = parseToMap(jsonString);
            logger.info("Map representation: {}", dataMap);

            logger.debug("JSON parsing test completed successfully");
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON", e);
        }
    }

    public static void main(String[] args) {
        JsonParser jsonParser = new JsonParser();
        jsonParser.parseTest1();
    }


}
