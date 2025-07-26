### Test Method Naming Convention

Based on the examination of the test files in your project, the test methods follow a naming convention where **"test" appears at the start of the method name**, not at the end.

For example, in the `JsonPrettyPrinterTest.java` file, all test methods follow this pattern:

- `testFormatPrimitiveString()`
- `testPrettyPrintJsonStructured()`
- `testIsEmptyOrSingleItem()`

This naming convention (prefixing with "test") is a common practice in Java testing, especially in older JUnit versions. While JUnit 5 doesn't require this prefix (since it uses annotations to identify tests), maintaining this convention can help with readability and consistency, especially if you're following established team or project conventions.

Note that the `@Test` annotation is what actually marks a method as a test in JUnit 5, regardless of the method name. The naming convention is purely for human readability and code organization.