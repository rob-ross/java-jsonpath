package org.killeroonie.jsonpath;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class test_cts {

    /*
    Notes about loading resources (e.g., a .json test case file):

    For resources in the same package as the class:
        getClass().getResourceAsStream("test.json")

    For resources in a different package/directory:
        // From class package to another package
        getClass().getResourceAsStream("../otherpackage/test.json")

        // Or from classpath root (preferred for arbitrary locations).
            getClassLoader() always starts looking from the root of the classpath.
        getClass().getClassLoader().getResourceAsStream("parsingTests/test.json")

    From classpath root using class-relative method
        (i.e., calling getResourceAsStream() off getClass(), as opposed to getClass().getClassLoader().
        1. It uses the Class object as the starting point for resource resolution.
        2. By default, (without a leading slash), it looks for resources relative to the package of the class.
        3. The resource path is interpreted relative to the class's location in the package hierarchy):
        getClass().getResourceAsStream("/parsingTests/test.json")

    The most reliable approach for arbitrary locations is to use the ClassLoader method without a leading slash.
     */
    public record CTSTestCases(String description, List<CTSTestCase> tests){}

    /**
     Holds a single test case from a `cts` JSON file, and maps domain names from the test file domain to the domain names in
     RFC 9535. Abstracted away is the distinction between a single test result and multiple test results for a single
     test case by implementing only a {@code resultsValues} and {@code resultsPaths} list for each test case.
     Test cases in the JSON file with {@code result} and {@code result_paths} keys are represented as single element
     {@code resultsValues} and {@code resultsPaths} lists.
     */
    @JsonPropertyOrder({"name", "selector", "document", "invalid_selector", "tags", "results", "results_paths"})
    public record CTSTestCase(@JsonProperty("name") String testName,
                       @JsonProperty("selector") String jsonPath,
                       @JsonProperty("document") Object rootValue,
                       @JsonProperty("invalid_selector") boolean isInvalid,
                       @JsonProperty("tags") List<String> tags,
                       @JsonProperty("results") List<Object> resultsValues,
                       @JsonProperty("results_paths") List<List<String>> resultsPaths) {


        @JsonCreator
        public CTSTestCase(
                @JsonProperty("name") String testName,
                @JsonProperty("selector") String jsonPath,
                @JsonProperty("document") Object rootValue,
                @JsonProperty("invalid_selector") boolean isInvalid,
                @JsonProperty("tags") List<String> tags,
                @JsonProperty("results") List<Object> resultsValues,
                @JsonProperty("results_paths") List<List<String>> resultsPaths,
                @JsonProperty("result") Object result,
                @JsonProperty("result_paths") List<String> resultPaths
        ) {
            this(
                    testName,
                    jsonPath,
                    rootValue,
                    isInvalid,
                    tags != null ? tags : Collections.emptyList(),
                    // Handle the special case for results/result
                    resultsValues != null ? resultsValues :
                            (result != null ? Collections.singletonList(result) : Collections.emptyList()),
                    // Handle the special case for results_paths/result_path
                    resultsPaths != null ? resultsPaths :
                            (resultPaths != null ? Collections.singletonList(resultPaths) : Collections.emptyList())
            );
        }

    }

    public static CTSTestCases test_load_ctsFile() {
        final String fileName = "cts.json";
        final ObjectMapper mapper = new ObjectMapper();
        CTSTestCases testCases = null;
        try(var inputStream = test_cts.class.getResourceAsStream(fileName)) {
            testCases = mapper.readValue(inputStream, new TypeReference<CTSTestCases>() {});
        } catch (IOException  e) {
            throw new RuntimeException("Couldn't load " + fileName, e);
        }
//        String prettyJson = null;
//        System.out.println("Test Cases loaded: ");
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        try {
//            prettyJson = mapper.writeValueAsString(testCases);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//        System.out.println(prettyJson);
//        System.out.println("num test cases: " + testCases.tests.size());
        return testCases;
    }

    public static void main(String[] args) {
        test_load_ctsFile();
    }
}
