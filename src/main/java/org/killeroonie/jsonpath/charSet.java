package org.killeroonie.jsonpath;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Implements a set of single char primitives using a boolean array.
 * The chars are represented by a boolean flag, each of which is an index in the array
 * that is also the char's numerical value.
 */
public final class charSet implements Set<Integer> {

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

    @Override
    public boolean contains(Object o) {
        if (! (o instanceof  Integer)) { return false;}
        return contains((char)o);
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

    public @NotNull Iterator<Integer> iterator() {
        return new Iterator<>() {
            int index = 0;
            final Integer[] array = toIntegerArray();
            public boolean hasNext() { return index < array.length; }
            public Integer next() { return array[index++]; }
        };
    }

    public char[] toCharArray() {
        final List<Character> list = new ArrayList<>();
        for (char i = 0; i < table.length; i++){
            if (table[i]){ list.add( i ); }
        }
        final char[] result = new char[list.size()];
        for  ( char i = 0; i < result.length; i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public Character[] toCharacterArray() {
        final Character[] result =  new Character[size];
        for (char arrayIndex = 0, resultIndex = 0; arrayIndex < table.length; arrayIndex++){
            if (table[arrayIndex]){ result[resultIndex++] = arrayIndex; }
        }
        return result;
    }

    public int[] toIntArray() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < table.length; i++){
            if (table[i]){ list.add( i ); }
        }
        final int[] result = new int[list.size()];
        for  ( char i = 0; i < result.length; i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public Integer[] toIntegerArray() {
        Integer[] result =  new Integer[size];
        for (int arrayIndex = 0, resultIndex = 0; arrayIndex < table.length; arrayIndex++){
            if (table[arrayIndex]){ result[resultIndex++] = arrayIndex; }
        }
        return result;
    }

    public Object @NotNull [] toArray() {
        return toArray(new Object[0]);
    }

    public <T> T @NotNull [] toArray(T[] a) {
        final Class<?> componentType = a.getClass().getComponentType();
        if (!componentType.isAssignableFrom(Integer.class)) {
            throw new ArrayStoreException(
                    "Array component type " + componentType.getTypeName() + " cannot store Integer");
        }
        final int _size = size;
        @SuppressWarnings("unchecked")
        final T[] result = ( a.length >= _size ) ? a
                : (T[]) java.lang.reflect.Array.newInstance(componentType, _size);

        final int[] source = toIntArray();
        int i; // we need access to this value after the for loop ends.
        for ( i = 0; i < source.length; i++){
            //noinspection unchecked
            result[i] = (T)((Integer)source[i]);
        }
        if (a.length > _size){
            result[i] = null;
        }
        return result;
    }

    @Override
    public boolean add( Integer ch) {
        return add((int)ch);
    }

    public boolean add(Character character) {
        return add((int)character);
    }

    public boolean add(char ch) {
        return add((int)ch);
    }

    public boolean add(int ch) {
        if (!table[ch]) { table[ch] = true; size++; return true; }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return remove((int)o);
    }

    public boolean remove(char ch) {
        return remove((int)ch);
    }

    public boolean remove(int ch) {
        if (table[ch]) { table[ch] = false; size--; return true; }
        return false;
    }

    public boolean containsAll(int[] intArray) {
        for  (int ch : intArray) {
            if (! containsUnchecked( ch) ) { return false; }
        }
        return true;
    }
    public boolean containsAll(char[] charArray) {
        for  (char ch : charArray) {
            if (! containsUnchecked((int)ch) ) { return false; }
        }
        return true;
    }

    // todo - this is Unverified code. Test it!
    public boolean containsAll(Collection<?> collection) {
        if (collection.isEmpty()) { return true; }
        // Fast path if the caller is also a charSet
        if (collection instanceof charSet other) {
            int minLen = Math.min(this.table.length, other.table.length);
            for (int i = 0; i < minLen; i++) {
                if (other.table[i] && !this.table[i]) return false;
            }
            // If other has true bits beyond our length, we cannot contain them
            for (int i = this.table.length; i < other.table.length; i++) {
                if (other.table[i]) return false;
            }
            return true;
        }

        // General path: verify element type and membership
        for (Object o : collection) {
            if (!(o instanceof Integer ch)) {
                throw new ClassCastException(
                        "Expected elements of type Integer but found: " + (o == null ? "null" : o.getClass().getName())
                );
            }
            if (!contains((int) ch)) return false;
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

    public boolean addAll(int[] intArray) {
        boolean setWasChanged = false;
        for (int c : intArray) {
            if (add(c)) { setWasChanged = true; }
        }
        return setWasChanged;
    }

    @Override
    public boolean addAll(Collection<? extends Integer> collection) {
        boolean setWasChanged = false;
        for (Integer c : collection) {
            if (add(c)) { setWasChanged = true; }
        }
        return setWasChanged;
    }

    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
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
