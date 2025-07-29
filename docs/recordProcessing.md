### Iterating Over Record Fields in Java

Yes, you can iterate over the fields of a record in a general way using Java's Reflection API. Records (introduced in Java 14) have a special structure that makes them particularly amenable to reflection-based field access.

### Using Record Components

Records provide a special reflection method called `getRecordComponents()` which returns an array of `RecordComponent` objects. This is the preferred way to iterate over record fields:

```java
public String prettyPrintRecord(Record record, int indentLevel) {
    StringBuilder result = new StringBuilder();
    result.append(record.getClass().getSimpleName()).append("(");
    
    RecordComponent[] components = record.getClass().getRecordComponents();
    boolean first = true;
    
    for (RecordComponent component : components) {
        if (!first) {
            result.append(", ");
        }
        first = false;
        
        String name = component.getName();
        try {
            // Get the accessor method that corresponds to this component
            Object value = component.getAccessor().invoke(record);
            result.append(name).append("=").append(prettyPrint(value, indentLevel + 1));
        } catch (Exception e) {
            result.append(name).append("=<error>");
        }
    }
    
    result.append(")");
    return result.toString();
}
```

### Key Advantages of Using RecordComponent

1. **Type Safety**: `RecordComponent` provides type information via `getType()` method
2. **Accessor Methods**: You can directly get the accessor method via `getAccessor()`
3. **Annotations**: You can retrieve annotations on the record components
4. **Guaranteed Order**: The components are returned in declaration order

### Alternative: Using getDeclaredFields()

If you need to support both records and regular classes with a unified approach, you can use the more general `getDeclaredFields()` method:

```java
public String prettyPrintObject(Object obj, int indentLevel) {
    if (obj == null) return "null";
    
    // Special case for records
    if (obj instanceof Record) {
        return prettyPrintRecord((Record)obj, indentLevel);
    }
    
    // For regular classes
    StringBuilder result = new StringBuilder();
    result.append(obj.getClass().getSimpleName()).append("{");
    
    Field[] fields = obj.getClass().getDeclaredFields();
    boolean first = true;
    
    for (Field field : fields) {
        // Skip static, synthetic, or other special fields
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
            field.isSynthetic()) {
            continue;
        }
        
        if (!first) {
            result.append(", ");
        }
        first = false;
        
        field.setAccessible(true);
        try {
            Object value = field.get(obj);
            result.append(field.getName()).append("=").append(prettyPrint(value, indentLevel + 1));
        } catch (Exception e) {
            result.append(field.getName()).append("=<error>");
        }
    }
    
    result.append("}");
    return result.toString();
}
```

### Detecting if a Class is a Record

To determine if an object is a record, you can use the `instanceof Record` check (as shown above) or check the class itself:

```java
boolean isRecord = someClass.isRecord(); // Java 16+
```

### Handling Cycles in Records

Since records can contain references to other objects (including other records), you'll need to handle potential cycles:

```java
// Add this to your cycle detection mechanism
Set<Object> visited = new HashSet<>();

// Then in your pretty print method
if (visited.contains(obj)) {
    return "<cycle detected>";
}
visited.add(obj);
// Process the object...
// Don't forget to remove the object when done with this branch
visited.remove(obj);
```

### Performance Considerations

Reflection is relatively expensive, so for performance-critical applications, you might want to:

1. Cache the `RecordComponent[]` array for each record class
2. Use method handles instead of reflection for repeated access
3. Consider code generation approaches for known record types

### Conclusion

The `getRecordComponents()` method provides the most direct and type-safe way to iterate over record fields. This approach is specifically designed for records and gives you access to all the metadata about the record components, making it ideal for implementing a pretty printer that needs to handle records specially.