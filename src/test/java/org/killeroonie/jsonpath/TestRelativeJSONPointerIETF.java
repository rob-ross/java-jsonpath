package org.killeroonie.jsonpath;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases from draft-hha-relative-json-pointer-00.<p>
 *
 * The test cases defined here are taken from draft-hha-relative-json-pointer-00.
 * The appropriate Revised BSD License is included below.<p>
 *
 * Copyright (c) 2023 IETF Trust and the persons identified as authors of the
 * code. All rights reserved. Redistribution and use in source and binary forms,
 * with or without modification, are permitted provided that the following
 * conditions are met:<p>
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of Internet Society, IETF or IETF Trust, nor the names of
 *   specific contributors, may be used to endorse or promote products derived
 *   from this software without specific prior written permission.<p>
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class TestRelativeJSONPointerIETF {
    record Case(String pointer, String rel, Object want) {};
    static final Map<String, Object> DOCUMENT = new LinkedHashMap<>();
    static {
        DOCUMENT.put("foo", List.of("bar", "baz", "biz"));
        DOCUMENT.put("highly", Map.of("nested", Map.of("objects", true)));
    }
    static Stream<Arguments> testCases() {
        Object[][] testData = {
                { new Case( "/foo/1", "0", "baz") },
                { new Case( "/foo/1", "1/0", "bar") },
                { new Case( "/foo/1", "0-1", "bar") },
                { new Case( "/foo/1", "2/highly/nested/objects", true) },
                { new Case( "/foo/1", "0#", 1) },
                { new Case( "/foo/1", "0+1#", 2) },
                { new Case( "/foo/1", "1#", "foo") },
                { new Case( "/highly/nested", "0/objects", true) },
                { new Case( "/highly/nested", "1/nested/objects", true) },
                { new Case( "/highly/nested", "2/foo/0", "bar") },
                { new Case( "/highly/nested", "0#", "nested") },
                { new Case( "/highly/nested", "1#", "highly") },
        };
        
        return Arrays.stream(testData).map(Arguments::of);
    }
    @ParameterizedTest
    @MethodSource("testCases")
    void test_relative_pointer_ietf_examples(Case testCase) {
        JSONPointer pointer = new JSONPointer(testCase.pointer);
        RelativeJSONPointer rel = new RelativeJSONPointer(testCase.rel);
        JSONPointer rel_pointer = rel.to(pointer);

        assertEquals(testCase.want, rel_pointer.resolve(DOCUMENT), testCase.toString() );
        assertEquals(rel_pointer, pointer.to( testCase.rel), testCase.toString());
        assertEquals(testCase.rel, rel.toString(), testCase.toString());
    }
}
