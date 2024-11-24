/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

        assert map != null;
        for (Map.Entry<Token, String> entry : map.entrySet()) {
            Token token = entry.getKey();
            String value = entry.getValue();
            //System.out.println(token.getName() + " : " + value);
            assertEquals(token.getName(), value);
        }
    }

}
