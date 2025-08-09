package org.killeroonie.jsonpath;

import java.util.*;

/**
 * Implements a set of single char primitives using a boolean array.
 */
public final class charSet {

    private int size = 0;

    private final boolean[] table;

    public charSet(int capacity){
        table = new boolean[capacity];
    }

    public int size(){
        return size;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public boolean contains(char ch) {
        assert ch < table.length : "out of range";
        return table[ch];
    }

    // Safe variant with a fused bounds check; usually inlined and cheap.
    public boolean contains(int ch) {
        return ch >= 0 && ch < table.length && table[ch];
    }

    public boolean containsUnchecked(char ch) {
        return containsUnchecked((int)ch);
    }

    // Faster variant if the caller already did bounds/ASCII checks.
    public boolean containsUnchecked(int ch) {
        assert ch >= 0 && ch < table.length : "out of range";
        return table[ch];
    }

    public Iterator<Character> iterator() {
        return new Iterator<>() {
            int index = 0;
            final Character[] array = toCharacterArray();
            public boolean hasNext() { return index < array.length; }
            public Character next() { return array[index++]; }
        };
    }

    public char[] toPrimitiveArray() {
        List<Character> list = new ArrayList<Character>();
        for (char i = 0; i < table.length; i++){
            if (table[i]){ list.add(i); }
        }
        char[] result = new char[list.size()];
        for  ( char i = 0; i < result.length; i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public Character[] toCharacterArray() {
        Character[] result =  new Character[size];
        for (char arrayIndex = 0, resultIndex = 0; arrayIndex < table.length; arrayIndex++){
            if (table[arrayIndex]){ result[resultIndex++] = arrayIndex; }
        }
        return result;
    }

    public Object toArray() {
        return toArray(new Object[0]);
    }

    public <T> T[] toArray(T[] a) {
        final Class<?> componentType = a.getClass().getComponentType();
        if (!componentType.isAssignableFrom(Character.class)) {
            throw new ArrayStoreException(
                    "Array component type " + componentType.getTypeName() + " cannot store Character");
        }
        final int _size = size;
        @SuppressWarnings("unchecked")
        final T[] result = ( a.length >= _size ) ? a
                : (T[]) java.lang.reflect.Array.newInstance(componentType, _size);

        final char[] source = toPrimitiveArray();
        char i; // we need access to this value after the for loop ends.
        for ( i = 0; i < source.length; i++){
            //noinspection unchecked
            result[i] = (T)((Character)source[i]);
        }
        if (a.length > _size){
            result[i] = null;
        }
        return result;
    }

    public boolean add(char ch) {
        return add((int)ch);
    }

    public boolean add(int ch) {
        if (!table[ch]) { table[ch] = true; size++; return true; }
        return false;
    }

    public boolean remove(char ch) {
        return remove((int)ch);
    }

    public boolean remove(int ch) {
        if (table[ch]) { table[ch] = false; size--; return true; }
        return false;
    }


    public boolean containsAll(char[] charArray) {
        for  (char ch : charArray) {
            if (! containsUnchecked((int)ch) ) { return false; }
        }
        return true;
    }

    boolean containsAll(Collection<Character> collection) {
        if (collection.isEmpty()) { return true; }
        for (Character ch : collection) {
            if (! containsUnchecked((int)ch)) { return false; }
        }
        return true;
    }

    public boolean addAll(char[] charArray) {
        boolean setWasChanged = false;
        for (char c : charArray) {
            if (add(c)) { setWasChanged = true; }
        }
        return setWasChanged;
    }

    public boolean addAll(Collection<? extends Character> collection) {
        boolean setWasChanged = false;
        for (Character c : collection) {
            if (add(c)) { setWasChanged = true; }
        }
        return setWasChanged;
    }

    boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof charSet charSet)) return false;
        return size == charSet.size && Objects.deepEquals(table, charSet.table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, Arrays.hashCode(table));
    }

    @Override
    public String toString() {
        return "charSet{" +
                "size=" + size +
                '}';
    }
}
