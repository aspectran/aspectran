/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.activity.request;

import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.Tokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * <p>Created: 2016. 3. 1.</p>
 */
class PathVariableMapTest {

    @Test
    void testNewInstance() {
        String transletNamePattern = "/aaa/${bbb1}/bbb2/ccc/${ddd:eee}/fff/@{ggg:ggg}";
        String requestName = "/aaa/bbb1/bbb2/ccc/ddd/fff/";

        List<Token> tokenList = Tokenizer.tokenize(transletNamePattern, false);
        Token[] nameTokens = tokenList.toArray(new Token[0]);

        Map<Token, String> map = PathVariableMap.parse(nameTokens, requestName);

        assertNotNull(map);
        for (Map.Entry<Token, String> entry : map.entrySet()) {
            Token token = entry.getKey();
            String value = entry.getValue();
            assertEquals(token.getName(), value);
        }
    }

    @Test
    void testMismatchCases() {
        // Prefix mismatch
        Token[] tokens = Tokenizer.tokenize("/users/${id}", false).toArray(new Token[0]);
        assertNull(PathVariableMap.parse(tokens, "/api/users/123"));

        // Literal mismatch in between
        tokens = Tokenizer.tokenize("/users/${id}/edit", false).toArray(new Token[0]);
        assertNull(PathVariableMap.parse(tokens, "/users/123/view"));
    }

    @Test
    void testExtractWithTrailingPath() {
        Token[] tokens = Tokenizer.tokenize("/users/${id}/profile", false).toArray(new Token[0]);
        Map<Token, String> map = PathVariableMap.parse(tokens, "/users/123/profile/extra");
        assertNotNull(map);
        assertEquals("123", map.values().iterator().next());
    }

    @Test
    void testDefaultValues() {
        Token[] tokens = Tokenizer.tokenize("/items/${id:defaultId}", false).toArray(new Token[0]);
        Map<Token, String> map = PathVariableMap.parse(tokens, "/items/");
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("defaultId", map.values().iterator().next());
    }

}
