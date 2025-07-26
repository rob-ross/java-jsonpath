### Implementing Parameterized Tests in JUnit 5

Since you're familiar with PyTest parameterized tests, I'll explain how to implement parameterized tests in JUnit 5, which is what this project is using.

#### Basic Setup

First, ensure you have the required dependencies in your build file. Looking at your project, you already have JUnit Jupiter Params set up. For reference, here's what you need in a Gradle project:

```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.x.x")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.x.x") // For parameterized tests
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.x.x")
}
```

#### Creating Parameterized Tests

Your current file already has a good example of a parameterized test. Here's how it works:

1. Annotate your test method with `@ParameterizedTest`
2. Add a source annotation to specify where the parameters come from
3. Define the test method with parameters that match the provided arguments

```java
@ParameterizedTest
@MethodSource("provideJsonStructures")
void testPrettyPrintJsonStructured(JsonStructured<?> jsonValue, FormatFlags format, String expected) {
    JsonPrettyPrinter printer = new JsonPrettyPrinter();
    String result = printer.prettyPrintJson(jsonValue, format, new ArrayList<>(), 0);
    assertEquals(expected, result);
}

static Stream<Arguments> provideJsonStructures() {
    return Stream.of(
            Arguments.of(
                    new JsonArray(List.of()),
                    new FormatFlags(true, false, false, false, 2, false, false),
                    "[ ]"
            ),
            Arguments.of(
                    new JsonObject(Map.of(new JsonString("key"), new JsonString("value"))),
                    new FormatFlags(true, false, false, false, 2, false, false),
                    "{ \"key\": \"value\" }"
            )
    );
}
```

#### Parameter Sources

JUnit 5 provides several ways to supply parameters to your tests:

1. **@MethodSource**: References a method that provides a stream of arguments (as shown in your example)

```java
@ParameterizedTest
@MethodSource("stringProvider")
void testWithMethodSource(String argument) {
    assertNotNull(argument);
}

static Stream<String> stringProvider() {
    return Stream.of("apple", "banana", "orange");
}
```

2. **@ValueSource**: Provides an array of literal values

```java
@ParameterizedTest
@ValueSource(strings = {"apple", "banana", "orange"})
void testWithValueSource(String fruit) {
    assertNotNull(fruit);
}
```

3. **@CsvSource**: Provides comma-separated values

```java
@ParameterizedTest
@CsvSource({
    "apple, 1",
    "banana, 2",
    "orange, 3"
})
void testWithCsvSource(String fruit, int rank) {
    assertNotNull(fruit);
    assertTrue(rank > 0);
}
```

4. **@CsvFileSource**: Loads CSV values from a file

```java
@ParameterizedTest
@CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
void testWithCsvFileSource(String fruit, int rank) {
    assertNotNull(fruit);
    assertTrue(rank > 0);
}
```

5. **@EnumSource**: Provides enum constants

```java
@ParameterizedTest
@EnumSource(TimeUnit.class)
void testWithEnumSource(TimeUnit timeUnit) {
    assertNotNull(timeUnit);
}
```

6. **@ArgumentsSource**: Uses a custom implementation of ArgumentsProvider

```java
@ParameterizedTest
@ArgumentsSource(MyArgumentsProvider.class)
void testWithArgumentsSource(String argument) {
    assertNotNull(argument);
}

static class MyArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of("apple", "banana").map(Arguments::of);
    }
}
```

#### Differences from PyTest

If you're coming from PyTest, here are some key differences:

1. JUnit 5 uses annotations rather than decorators
2. Parameter sources are more explicitly defined in JUnit 5
3. JUnit 5 has more built-in parameter sources
4. The syntax for providing multiple parameters is different (using `Arguments.of()` in JUnit 5)

#### Display Names

You can customize the display name of each test invocation:

```java
@ParameterizedTest(name = "Run {index}: fruit={0}, rank={1}")
@CsvSource({
    "apple, 1",
    "banana, 2",
    "orange, 3"
})
void testWithCustomDisplayName(String fruit, int rank) {
    // test code
}
```

#### Argument Conversion

JUnit 5 can automatically convert string arguments to other types:

```java
@ParameterizedTest
@ValueSource(strings = {"01.01.2020", "31.12.2020"})
void testWithImplicitConversion(LocalDate date) {
    assertNotNull(date);
}
```

For more complex conversions, you can use `@ConvertWith` to specify a custom converter.

This should give you a good starting point for implementing parameterized tests in JUnit 5. The example in your current file is already following these best practices.