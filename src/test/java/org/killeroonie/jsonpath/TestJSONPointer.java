package org.killeroonie.jsonpath;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestJSONPointer {

    @Test
    void test_match_to_pointer() {
        Map<String, Object> map = new HashMap<>();
        map.put("some", Map.of("thing", "else"));
    }
    /*
    def test_match_to_pointer() -> None:
    data = {"some": {"thing": "else"}}
    matches = list(jsonpath.finditer("$.some.thing", data))
    assert len(matches) == 1
    match = matches[0]
    pointer = match.pointer()
    assert pointer.resolve(data) == match.obj
    assert pointer.resolve({"some": {"thing": "foo"}}) == "foo"
     */

    void test_pointer_repr() {}

    @Test
    void testResolveWithDefault() {
        Map<String, Object> map = new HashMap<>();
        map.put("some", Map.of("thing", "else"));
        JSONPointer jsonPointer = new JSONPointer("/some/other");
        assertNull(jsonPointer.resolve(map, null));
    }

    void test_pointer_index_out_fo_range() {}

    @Test
    void test_resolve_int_key() {
        Map<String, Object> map = new HashMap<>();
        map.put("some", Map.of("1", "thing"));

        JSONPointer jsonPointer = new JSONPointer("/some/1");
        assertEquals("thing", jsonPointer.resolve(map));
    }


}
