package org.killeroonie.jsonpath;

import org.junit.jupiter.api.Test;
import org.killeroonie.jsonpath.exception.RelativeJSONPointerIndexException;
import org.killeroonie.jsonpath.exception.RelativeJSONPointerSyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestRelativeJSONPointer {
    
    @Test
    void test_syntax_error() {
        assertThrows(RelativeJSONPointerSyntaxException.class, () -> new RelativeJSONPointer("foo"));
    }


    @Test
    void test_origin_leading_zero() {
        assertThrows(RelativeJSONPointerSyntaxException.class, () -> new RelativeJSONPointer("01"));
    }


    @Test
    void test_origin_beyond_pointer() {
        JSONPointer pointer = new JSONPointer("/foo/bar/0");
        var rel = new RelativeJSONPointer("9/foo");
        assertThrows(RelativeJSONPointerIndexException.class, () -> rel.to(pointer) );
    }

    @Test
    void test_equality() {
        var rel = new RelativeJSONPointer("1/foo");
        assertEquals(new RelativeJSONPointer("1/foo"), rel);
    }


    @Test
    void test_zero_index_offset() {
        assertThrows(RelativeJSONPointerSyntaxException.class, () -> new RelativeJSONPointer("1-0") );
        assertThrows(RelativeJSONPointerSyntaxException.class, () -> new RelativeJSONPointer("1+0") );
    }


    @Test
    void test_negative_index_offset() {
        JSONPointer pointer = new JSONPointer("/foo/1");
        var rel = new RelativeJSONPointer("0-2");
        assertThrows(RelativeJSONPointerIndexException.class, () -> rel.to(pointer) );
    }

}
