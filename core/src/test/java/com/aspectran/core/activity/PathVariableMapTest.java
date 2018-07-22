/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.activity;

import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.Tokenizer;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * <p>Created: 2016. 3. 1.</p>
 */
public class PathVariableMapTest {

    @Test
    public void testNewInstance() {
        String transletNamePattern = "/aaa/${bbb1}/bbb2/ccc/${ddd:eee}/fff/@{ggg:ggg}";
        String requestTransletName = "/aaa/bbb1/bbb2/ccc/ddd/fff/";

        List<Token> tokenList = Tokenizer.tokenize(transletNamePattern, false);
        Token[] nameTokens = tokenList.toArray(new Token[0]);

        Map<Token, String> map = PathVariableMap.newInstance(nameTokens, requestTransletName);

        assertNotNull(map);

        for (Map.Entry<Token, String> entry : map.entrySet()) {
            Token token = entry.getKey();
            String value = entry.getValue();
            assertEquals(token.getName(), value);
        }
    }

}