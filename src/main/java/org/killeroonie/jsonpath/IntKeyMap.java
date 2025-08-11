package org.killeroonie.jsonpath;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * <p>keys:   int</p>
 * <p>values: V, at present intended to be TokenKind or String</p>
 * <p>
 * The use case for this class is as a lookup table, mapping a single character to a TokenKind or String;
 * although, any type for Value will work correctly.
 * The keys are intended to be Java char representing a character. We only need to map ASCII chars used as symbols,
 * like $, #, etc., and these are all ASCII, so we only call the constructor with IntKeyMap(TokenKind.class, 128), e.g.
 * Any char can all support the full Unicode BMP.</p>
 * <p>This implementation uses boolean arrays. As with all Java arrays, array indices are ints. Each index is used
 * as the value of the char it maps, so lookups just use the value of the char as the index. Each index can support the
 * entire Unicode set, including surrogate pairs, although this has not been tested.</p>
 */
public final class IntKeyMap<V> {

    private final Class<V> valueType;
    private int size = 0;

    private final V[] table;
    private final Set<Integer> keySet = new HashSet<>();
    private final Set<V> values = new HashSet<>();

    public IntKeyMap(Class< V> valueType, int capacity) {
        this.valueType = valueType;
        @SuppressWarnings("unchecked")
        final V[] tmp  = (V[]) java.lang.reflect.Array.newInstance(valueType, capacity);
        table = tmp;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(char key) {
        return table[key] != null;
    }

    public boolean containsKey(int key) {
        return table[key] != null;
    }

    public boolean containsKey(Object key) {
        return table[(char) key] != null;
    }

    public boolean containsValue(Object value) {
        //noinspection SuspiciousMethodCalls
        return values.contains(value);
    }

    public V get(char key) {
        return get((int) key);
    }

    public V get(int key) {
        return table[key];
    }

    /**
     * @param key the Integer key whose associated value is to be returned
     * @return the V value for the given int key.
     */
    public V get(Object key) {
        return table[(int) key];
    }

    public V put(char key, V value) {
        return put((int)key, value);
    }

    public V put(int key, V value) {
        V previousValue = table[key];
        if (value == previousValue) {
            return value;
        }
        table[key] = value;
        values.remove(previousValue);
        values.add(value);
        keySet.add(key);
        size++;
        return previousValue;

    }

    public V put(Integer key, V value) {
        return put((int)key, value);
    }


    public V remove(char key) {
        return remove((int) key);
    }

    public V remove(int key) {
        V previousValue = table[ key ];
        table[key] = null;
        values.remove(previousValue);
        keySet.remove(key);
        size--;
        return previousValue;
    }

    public V remove(Object key) {
        return remove((int) key);
    }

    public void putAll(@NotNull Map<? extends Integer, ? extends V> m) {
        for (Map.Entry<? extends Integer, ? extends V> entry : m.entrySet()) {
            put((int)entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        Arrays.fill(table, null);
        values.clear();
        keySet.clear();
        size = 0;
    }

    Set<Integer> keySet() {
        return keySet;
    }

    public Collection<V> values() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<Map.Entry<Integer, V>> entrySet() {
        Set<Map.Entry<Integer, V>> result = new LinkedHashSet<>();
        for (int key : keySet) {
            result.add(Map.entry(key, table[key]));
        }
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof IntKeyMap<?> IntKeyMap)) return false;
        return size == IntKeyMap.size && Objects.deepEquals(table, IntKeyMap.table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, Arrays.hashCode(table));
    }

    public V getOrDefault(char key, V defaultValue) {
        return getOrDefault((int)key, defaultValue);
    }

    public V getOrDefault(int key, V defaultValue) {
        return table[key] == null ? defaultValue : table[key];
    }

    public V getOrDefault(Integer key, V defaultValue) {
        return getOrDefault((int)key, defaultValue);
    }

    public V getOrDefault(Object key, V defaultValue) {
        return getOrDefault((int)key, defaultValue);
    }
}