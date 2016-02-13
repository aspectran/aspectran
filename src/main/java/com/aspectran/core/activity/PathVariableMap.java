/**
 * Copyright 2008-2016 Juho Jeong
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

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.TokenType;

/**
 * The Class PathVariableMap.
 *
 * <p>Created: 2016. 2. 13.</p>
 */
public class PathVariableMap extends HashMap<Token, String> {

    private final TransletRule transletRule;

    public PathVariableMap(TransletRule transletRule) {
        super(transletRule.getNameTokens().length);
        this.transletRule = transletRule;
    }

    public TransletRule getTransletRule() {
        return transletRule;
    }

    protected void apply(Translet translet) {
        for(Map.Entry<Token, String> entry : entrySet()) {
            Token token = entry.getKey();
            if(token.getType() == TokenType.PARAMETER) {
                translet.getRequestAdapter().setParameter(token.getName(), entry.getValue());
            } else {
                translet.getRequestAdapter().setAttribute(token.getName(), entry.getValue());
            }
        }
    }

}
