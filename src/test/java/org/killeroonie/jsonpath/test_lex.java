package org.killeroonie.jsonpath;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.List;

/**
 * Port of test_lex.py from python-jsonpath
 */
public class test_lex {

    record TestCases(String description, List<Case> tests){}
    record Case(String description,
                String path,
                @JsonDeserialize(contentUsing = TokenWrapperDeserializer.class)
                List<Token> want) {

//        @JsonCreator
//        Case { }
    }
    static class TokenWrapperDeserializer extends JsonDeserializer<Token> {
        @Override
        public Token deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);

            // If it’s the wrapped form: { "Token": { ... } }
            if (node.isObject() && node.size() == 1 && node.has("Token")) {
                JsonNode inner = node.get("Token");
                // Delegate to the default binding for Token using the inner node
                JsonParser innerParser = inner.traverse(p.getCodec());
                innerParser.nextToken();
                return ctxt.readValue(innerParser, Token.class);
            }

            // If it’s already the direct form (in case you change JSON later): { ...fields... }
            JsonParser directParser = node.traverse(p.getCodec());
            directParser.nextToken();
            return ctxt.readValue(directParser, Token.class);
        }
    }


    static class TokenKindDeserializer extends JsonDeserializer<TokenKind> {
        @Override
        public TokenKind deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String raw = p.getText();
            String name = raw;
            if (name.startsWith("TOKEN_")) name = name.substring(6);
            // Map historical names to current enum constants
            if ("FAKE_ROOT".equals(name)) name = "PSEUDO_ROOT";
            if ("LIST_START".equals(name)) name = "LBRACKET";
            // Add other aliases as needed
            try {
                return TokenKind.valueOf(name);
            } catch (IllegalArgumentException ex) {
                // Fallback if unknown: you may prefer to throw
                return TokenKind.UNKNOWN;
            }
        }
    }

    static void load() {
        final String fileName = "test_lex.json";
        final ObjectMapper mapper = new ObjectMapper();
        // Register only for this test mapper
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(TokenKind.class, new TokenKindDeserializer());
        mapper.registerModule(mod);

        // Mix-in to ignore only the derived getters on Token
        abstract class TokenIgnoreDerivedProps {
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getLineNumber();
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getColumnNumber();
        }
        mapper.addMixIn(Token.class, TokenIgnoreDerivedProps.class);


        TestCases testCases = null;
        try(var inputStream = test_lex.class.getResourceAsStream(fileName)) {
            testCases = mapper.readValue(inputStream, new TypeReference<TestCases>() { });
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load " + fileName, e);
        }
        String prettyJson = null;
        System.out.println("Test Cases loaded: ");
        System.out.println("num test cases: " + testCases.tests.size());

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            prettyJson = mapper.writeValueAsString(testCases);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        System.out.println(prettyJson);
        System.out.println("num test cases: " + testCases.tests.size());
        System.out.println("tests: " + testCases.tests);
    }

    public static void main(String[] args) {
        load();
    }
}
