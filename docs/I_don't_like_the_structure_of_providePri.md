### Making `providePrimitiveTestCases()` More Compact

I understand you want to make the `providePrimitiveTestCases()` method more compact, similar to how parameterized tests are structured in Pytest. The current implementation is quite verbose with each test case taking multiple lines with `Arguments.of()` calls.

Here's a more compact approach for your JUnit 5 parameterized tests:

```java
static Stream<Arguments> providePrimitiveTestCases() {
    // Define test cases in a more compact format: primitive, expected output, message
    return Stream.of(
        // Format: primitive object, expected output, error message
        testCase(new JsonString("foo"), "foo", "String primitive should not be quoted with default format"),
        testCase(new JsonBoolean(true), "true", "Boolean true should be formatted as 'true'"),
        testCase(new JsonBoolean(false), "false", "Boolean false should be formatted as 'false'"),
        testCase(JsonNull.getInstance(), "null", "Null should be formatted as 'null'"),
        testCase(new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"),
        testCase(new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"),
        testCase(new JsonNumber(42), "42", "Integer should be formatted as its string representation"),
        testCase(new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"),
        testCase(new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"),
        testCase(new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation")
    );
}

// Helper method to create test cases more concisely
private static Arguments testCase(JsonTypes.JsonPrimitive<?> primitive, String expected, String message) {
    return Arguments.of(primitive, expected, message);
}
```

This approach:

1. Uses a helper method `testCase()` to create each test case more concisely
2. Keeps each test case on a single line, making it easier to read and maintain
3. Maintains the same functionality as the original implementation
4. Follows a similar pattern to the Python example you provided

The helper method `testCase()` encapsulates the `Arguments.of()` call, making the code more readable and reducing repetition. This approach is similar to how you would define a list of tuples in Python.

If you want to make it even more compact (though possibly less readable), you could use this alternative approach:

```java
static Stream<Arguments> providePrimitiveTestCases() {
    // Define test data as arrays for maximum compactness
    Object[][] testData = {
        {new JsonString("foo"), "foo", "String primitive should not be quoted with default format"},
        {new JsonBoolean(true), "true", "Boolean true should be formatted as 'true'"},
        {new JsonBoolean(false), "false", "Boolean false should be formatted as 'false'"},
        {JsonNull.getInstance(), "null", "Null should be formatted as 'null'"},
        {new JsonNumber((byte) 42), "42", "Byte should be formatted as its string representation"},
        {new JsonNumber((short) 1234), "1234", "Short should be formatted as its string representation"},
        {new JsonNumber(42), "42", "Integer should be formatted as its string representation"},
        {new JsonNumber(9876543210L), "9876543210", "Long should be formatted as its string representation"},
        {new JsonNumber(3.14f), "3.14", "Float should be formatted as its string representation"},
        {new JsonNumber(2.71828), "2.71828", "Double should be formatted as its string representation"}
    };
    
    return Arrays.stream(testData).map(Arguments::of);
}
```

This second approach uses a 2D array to define the test cases and then converts it to a Stream of Arguments, which is even more similar to the Python example you provided. However, it loses some type safety since the array elements are treated as Objects.

Choose the approach that best balances compactness and readability for your project.