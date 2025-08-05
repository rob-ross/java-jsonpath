package org.killeroonie.jsonpath;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.killeroonie.jsonpath.JSONPointer.Pair;
import org.killeroonie.jsonpath.exception.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.killeroonie.jsonpath.JSONPointer.UNDEFINED;

@SuppressWarnings("unused")
public class TestJSONPointer {

    @Test
    void test_match_to_pointer() {
    /*
    void test_match_to_pointer() {
    data = {"some": {"thing": "else"}}
    matches = list(jsonpath.finditer("$.some.thing", data))
    assert len(matches) == 1
    match = matches[0]
    pointer = match.pointer();
    assert pointer.resolve(data) == match.obj
    assert pointer.resolve({"some": {"thing": "foo"}}) == "foo"
     */
    }

    void test_pointer_repr() {
        /*
        data = {"some": {"thing": "else"}}
        matches = list(jsonpath.finditer("$.some.thing", data))
        assert len(matches) == 1
        match = matches[0]
        pointer = match.pointer();
        assert str(pointer) == "/some/thing"
         */
    }

    @Test
    void test_resolve_with_default() {
        Map<String, Object> data = new HashMap<>();
        data.put("some", Map.of("thing", "else"));
        JSONPointer jsonPointer = new JSONPointer("/some/other");
        assertNull(jsonPointer.resolve(data, null));
    }

    @Test
    void test_pointer_index_out_of_range() {
        long max_plus_one = JsonPathUtils.JSON_MAX_INT_INDEX + 1;
        long min_minus_one = JsonPathUtils.JSON_MIN_INT_INDEX - 1;
        assertThrows(JSONPointerException.class, ()-> new JSONPointer("/some/thing/%d".formatted(max_plus_one)));
        assertThrows(JSONPointerException.class, ()-> new JSONPointer("/some/thing/%d".formatted(min_minus_one)));
    }


    @Test
    void test_resolve_int_key() {
        Map<String, Object> data = new HashMap<>();
        data.put("some", Map.of("1", "thing"));
        JSONPointer jsonPointer = new JSONPointer("/some/1");
        assertEquals("thing", jsonPointer.resolve(data));
    }


    @Test
    void test_resolve_int_missing_key() {
        Map<String, Object> data = new HashMap<>();
        data.put("some", Map.of("1", "thing"));
        JSONPointer pointer = new JSONPointer("/some/2");
        assertThrows(JSONPointerKeyException.class, () -> pointer.resolve(data));
    }

    @Test
    void test_resolve_str_index() {
        Map<String, Object> data = new HashMap<>();
        data.put("some", List.of("a", "b", "c"));
        JSONPointer pointer = new JSONPointer("/some/1", List.of("some", "1"));
        assertEquals("b", pointer.resolve(data));
    }


    void test_keys_selector() {
/*          Map<String, Object> data = new HashMap<>();
            data.put("some", Map.of("thing", "else"));
            matches = list(jsonpath.finditer("$.some.~", data))
            assert len(matches) == 1
            match = matches[0]
            pointer = match.pointer();
            assert str(pointer) == "/some/~0thing"
            assert pointer.resolve(data) == "thing"*/
        }


    @Test
    void test_mapping_key_error() {
        Map<String, Object> data = new HashMap<>();
        data.put("some", Map.of("thing", "else"));
        JSONPointer pointer = new JSONPointer("/some/other");
        assertThrows(JSONPointerKeyException.class, () -> pointer.resolve(data));
    }


    @Test
    void test_sequence_type_error() {
        Map<String, Object> data = new HashMap<>();
        data.put("some", List.of("a", "b", "c"));
        JSONPointer pointer = new JSONPointer("/some/thing");
        assertThrows(JSONPointerTypeException.class, () -> pointer.resolve(data));
    }


    @Test
    void test_hyphen_index() {
        Map<String, Object> data = new HashMap<>();
        data.put("some", Map.of( "thing", List.of(1, 2, 3)));
        JSONPointer pointer = new JSONPointer("/some/thing/-");
        assertThrows(JSONPointerIndexException.class, () -> pointer.resolve(data));
    }


    @Test
    void test_negative_index() {
        Map<String, Object> data = new HashMap<>();
        data.put("some", Map.of( "thing", List.of(1, 2, 3)));
        JSONPointer pointer = new JSONPointer("/some/thing/-2");
        assertEquals( 2, pointer.resolve(data) );
    }


    @Test
    void test_resolve_with_parent() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        JSONPointer pointer = new JSONPointer("/some/thing");
        Pair<Object, Object> p = pointer.resolveParent(data);
        var parent = p.parent();
        var rv     = p.obj();
        assertEquals(data.get("some"), parent);
        assertEquals(data.get("some").get("thing"), rv);
    }


    @Test
    void test_resolve_with_missing_parent() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        JSONPointer pointer = new JSONPointer("");
        Pair<Object, Object> p = pointer.resolveParent(data);
        var parent = p.parent();
        var rv     = p.obj();
        assertNull(parent);
        assertEquals(data, rv);
    }


    @Test
    void test_resolve_with_missing_target() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        JSONPointer pointer = new JSONPointer("/some/other");
        Pair<Object, Object> p = pointer.resolveParent(data);
        var parent = p.parent();
        var rv     = p.obj();
        assertEquals(data.get("some"), parent);
        assertEquals(UNDEFINED, rv);
    }


    @Test
    void test_resolve_from_json_string() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        JSONPointer pointer = new JSONPointer("/some/thing");
        assertEquals(List.of(1, 2, 3), pointer.resolve(data));
        Pair<Object, Object> p = pointer.resolveParent(data);
        var parent = p.parent();
        var rv     = p.obj();
        assertEquals(Map.of("thing", List.of(1,2,3)), parent);
        assertEquals(List.of(1,2,3), rv);
    }


    @Test
    void test_resolve_from_file_like() throws JsonProcessingException {
        Map<String, Map<String, Object>> map = new HashMap<>();
        map.put("some", Map.of("thing", List.of(1, 2, 3)));

        // First test with the map directly
        Object data = map;
        JSONPointer pointer = new JSONPointer("/some/thing");
        assertEquals(List.of(1, 2, 3), pointer.resolve(data));
        ObjectMapper mapper = new ObjectMapper();
        // Now test with an InputStream (equivalent to Python's StringIO)
        String jsonString = mapper.writeValueAsString(map);
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
        data = inputStream;

        // Test resolve
        assertEquals(List.of(1, 2, 3), pointer.resolve(data));

        // Reset the stream position (equivalent to data.seek(0) in Python)
        inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
        data = inputStream;

        // Test resolveParent
        Pair<Object, Object> p = pointer.resolveParent(data);
        var parent = p.parent();
        var rv = p.obj();
        assertEquals(Map.of("thing", List.of(1,2,3)), parent);
        assertEquals(List.of(1,2,3), rv);
    }


    @Test
    void test_convenience_resolve() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        assertEquals(1, JsonPathUtils.resolve("/some/thing/0", data));
        assertThrows(JSONPointerResolutionException.class,  () -> JsonPathUtils.resolve("/some/thing/99", data));
    }

    @Test
    void test_convenience_resolve_default() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        assertEquals(0, JsonPathUtils.resolve("/some/thing/99", data, 0));
    }

    @Test
    void test_convenience_resolve_from_parts() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        assertEquals(1, JsonPathUtils.resolve(List.of("some", "thing", "0"), data ));
        assertThrows(JSONPointerResolutionException.class,
                () -> JsonPathUtils.resolve(List.of("some", "thing", "99"), data)
        );
    }

    @Test
    void test_convenience_resolve_default_from_parts() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        assertEquals(0, JsonPathUtils.resolve(List.of("some", "thing", "99"), data, 0));
    }

    @Test
    void test_pointer_from_parts() {
        List<Object> parts = List.of("some",  "thing", 0);
        JSONPointer pointer = JSONPointer.fromParts(parts);
        assertEquals("/some/thing/0",  pointer.toString() );
    }

    @Test
    void test_pointer_from_empty_parts() {
        List<Object> parts = Collections.emptyList();
        JSONPointer pointer = JSONPointer.fromParts(parts);
        assertEquals("",  pointer.toString() );
    }

    @Test
    void test_pointer_from_only_empty_string_parts() {
        List<String> parts = List.of("");
        JSONPointer pointer = JSONPointer.fromParts(parts);
        assertEquals("/",  pointer.toString() );
    }

    @Test
        void test_pointer_from_uri_encoded_parts() {
            List<Object> parts = List.of("some%20thing", "else", 0);
            JSONPointer pointer = JSONPointer.fromParts(parts, true, false);
            assertEquals("/some thing/else/0", pointer.toString() );
        }

    @Test
    void test_index_with_leading_zero() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        JSONPointer pointer = new JSONPointer("/some/thing/0");
        assertEquals(1, pointer.resolve(data));

        JSONPointer pointer2 = new JSONPointer("/some/thing/01");
        assertThrows(JSONPointerTypeException.class, () -> pointer2.resolve(data));

        JSONPointer pointer3 = new JSONPointer("/some/thing/00");
        assertThrows(JSONPointerTypeException.class, () -> pointer3.resolve(data));

        JSONPointer pointer4 = new JSONPointer("/some/thing/01");
        assertThrows(JSONPointerTypeException.class, () -> pointer4.resolveParent(data));
    }

    @Test
    void test_pointer_without_leading_slash() {
        assertThrows(JSONPointerException.class, () -> new JSONPointer("some/thing/0"));
        assertThrows(JSONPointerException.class, () -> new JSONPointer("nosuchthing"));
    }

    @Test
    void test_pointer_with_leading_whitespace() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        JSONPointer pointer = new JSONPointer("   /some/thing/0");
        assertEquals(1, pointer.resolve(data));
        assertEquals("/some/thing/0",  pointer.toString() );
    }

    @Test
    void test_pointer_parent() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3)));
        JSONPointer pointer = new JSONPointer("/some/thing/0");
        assertEquals(1, pointer.resolve(data));

        JSONPointer parent = pointer.parent();
        assertEquals("/some/thing", parent.toString() );
        assertEquals(List.of(1, 2, 3), parent.resolve(data));

        parent = parent.parent();
        assertEquals("/some", parent.toString() );
        assertEquals(Map.of("thing", List.of(1, 2, 3)), parent.resolve(data));

        parent = parent.parent();
        assertEquals("", parent.toString() );
        assertEquals(Map.of("some", Map.of("thing",List.of(1, 2, 3))), parent.resolve(data));

        parent = parent.parent();
        assertEquals("", parent.toString() );
        assertEquals(Map.of("some", Map.of("thing",List.of(1, 2, 3))), parent.resolve(data));

    }


    // Java does not support operator overloading, so this Python test is obviated
    void test_join_pointers_with_slash() {
        // Test that we can join a pointer to a relative path with the `/` operator.
        /*
        pointer = JSONPointer("/foo")
        assert str(pointer) == "/foo"
        assert str(pointer / "bar") == "/foo/bar"
        assert str(pointer / "baz") == "/foo/baz"
        assert str(pointer / "bar/baz") == "/foo/bar/baz"
        assert str(pointer / "bar/baz" / "0") == "/foo/bar/baz/0"
        assert str(pointer / "/bar") == "/bar"

        with pytest.raises(TypeError):
            pointer / 0
         */
    }


    @Test
    void test_join_pointers() {
        JSONPointer pointer = new JSONPointer("/foo");
        assertEquals("/foo", pointer.toString());
        assertEquals("/foo/bar", pointer.join("bar").toString());
        assertEquals("/foo/baz", pointer.join("baz").toString());
        assertEquals("/foo/bar/baz", pointer.join("bar/baz").toString());
        assertEquals("/foo/bar/baz",   pointer.join("bar", "baz").toString());
        assertEquals("/foo/bar/baz/0", pointer.join("bar/baz", "0").toString());
        assertEquals("/bar", pointer.join("/bar").toString());
        assertEquals("/bar/0", pointer.join("/bar", "0").toString());

        //Java's static typing won't even allow an int to be passed to join(), so this test is obviated.
//        with pytest.raises(TypeError):
//            pointer.join(0)  # type: ignore

    }


    @Test
    void test_pointer_exists() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("some", Map.of("thing", List.of(1, 2, 3) ) ) ;
        data.put("other", null);
        assertTrue(new JSONPointer("/some/thing").exists(data));
        assertTrue(new JSONPointer("/other").exists(data));
        assertFalse(new JSONPointer("/nosuchthing").exists(data));

    }

    @Test
    void test_non_standard_property_pointer() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("foo", Map.of("bar", List.of(1, 2, 3), "#baz", "hello" ) ) ;
        assertEquals("bar", new JSONPointer("/foo/#bar").resolve(data));
        assertEquals("hello", new JSONPointer("/foo/#baz").resolve(data));
    }

    @Test
    void test_non_standard_index_pointer() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put("foo", Map.of("bar", List.of(1, 2, 3), "#baz", "hello" ) ) ;
        assertEquals(1, new JSONPointer("/foo/bar/#1").resolve(data));
        assertThrows(JSONPointerIndexException.class, () -> new JSONPointer("/foo/bar/#9").resolve(data) );
    }


    @Test
    void test_trailing_slash() {
        Map<String, Map<String, Object>> data = new HashMap<>();
        data.put( "foo", Map.of("", List.of(1, 2, 3), " ", List.of(4, 5, 6) ) );
        assertEquals(List.of(1,2,3), new JSONPointer("/foo/").resolve(data));
        assertEquals(List.of(4,5,6), new JSONPointer("/foo/ ").resolve(data));
    }

}