package org.killeroonie.jsonpath.lex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.killeroonie.jsonpath.Token;
import org.killeroonie.jsonpath.TokenKind;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Helper {

    // TestCases is the top-level JSON object
    record TestCases(String description, List<Case> tests){}

    record Case(String description, String path, boolean isInvalid, String exception, List<Token> want) {

        /**
         * Convenience constructor for a valid test case that is expected to pass.
         */
        public Case(String description, String path, List<Token> want) {
            this(description, path, false, null, want);
        }

        /**
         * Convenience constructor for a test case that is expected to fail with an exception.
         */
        public Case(String description, String path, String exception) {
            this(description, path, true, exception, java.util.Collections.emptyList());
        }
    }

    ////////////////////////////////////////////////////////////////////
    /// HELPERS
    ////////////////////////////////////////////////////////////////////

    static TestCases load(final String fileName) {
        final ObjectMapper mapper = new ObjectMapper();

        JsonDeserializer<TokenKind> jsonDeserializer = new JsonDeserializer<>() {
            @Override
            public TokenKind deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String name = p.getText();
                // Here we can tweak differences between the name of the TOKEN_XXX string constants in the Python version
                // (from where the test data originates) and our Enum TokenKind implementation.
                // Strip "TOKEN" prefix from the serialized name.
                if (name.startsWith("TOKEN_")) name = name.substring(6);
                // Map historical names to current enum constants
                if ("KEYS".equals(name)) name = "KEY_SELECTOR";
                //if ("LIST_START".equals(name)) name = "LBRACKET";
                // Add other aliases as needed
                try {
                    return TokenKind.valueOf(name);
                } catch (IllegalArgumentException ex) {
                    // Fallback if unknown: you may prefer to throw
                    return TokenKind.UNKNOWN;
                }
            }
        };
        // Register only for this test mapper
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(TokenKind.class, jsonDeserializer);
        mapper.registerModule(mod);

        // Mix-in to ignore only the derived getters on Token
        abstract class TokenIgnoreDerivedProps {
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getLineNumber();
            @com.fasterxml.jackson.annotation.JsonIgnore abstract int getColumnNumber();
        }
        mapper.addMixIn(Token.class, TokenIgnoreDerivedProps.class);

        TestCases testCases;
        try(InputStream inputStream = test_lex.class.getResourceAsStream(fileName)) {
            testCases = mapper.readValue(inputStream, new TypeReference<TestCases>() { });
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load " + fileName, e);
        }

        return testCases;
    }


}
