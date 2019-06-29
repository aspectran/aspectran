/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.apon;

import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.ArrayStack;
import com.aspectran.core.util.json.JsonReader;
import com.aspectran.core.util.json.JsonToken;

import java.io.IOException;
import java.io.StringReader;

/**
 * Converts JSON to APON.
 * 
 * <p>Created: 2015. 03. 16 PM 11:14:29</p>
 */
public class JsonToApon {

    public static Parameters from(String json) throws IOException {
        return from(json, null);
    }

    public static Parameters from(String json, @Nullable Parameters container) throws IOException {
        if (json == null) {
            throw new IllegalArgumentException("json must not be null");
        }
        ArrayStack<Parameters> stack = new ArrayStack<>();
        JsonReader reader = new JsonReader(new StringReader(json));
        String name = null;
        while (reader.hasNext()) {
            JsonToken nextToken = reader.peek();
            if (JsonToken.BEGIN_BLOCK == nextToken) {
                reader.beginBlock();
                if (container == null) {
                    container = new VariableParameters();
                    stack.push(container);
                }
                if (name != null) {
                    Parameters parameters = stack.peek();
                    Parameters subParameters = parameters.newParameters(name);
                    stack.push(subParameters);
                }
            } else if (JsonToken.END_BLOCK == nextToken) {
                Parameters parameters = stack.pop();
                name = parameters.getParent().getName();
            } else if (JsonToken.BEGIN_ARRAY == nextToken) {
                if (container == null) {
                    container = new ArrayParameters();
                    stack.push(container);
                    name = ArrayParameters.NONAME;
                }
            } else if(JsonToken.NAME == nextToken) {
                name = reader.nextName();
            } else if(JsonToken.STRING == nextToken) {
                String value =  reader.nextString();
                stack.peek().putValue(name, value);
            } else if(JsonToken.BOOLEAN == nextToken) {
                boolean value =  reader.nextBoolean();
                stack.peek().putValue(name, value);
            } else if(JsonToken.NUMBER == nextToken) {
                try {
                    int value = reader.nextInt();
                    stack.peek().putValue(name, value);
                } catch (NumberFormatException e0) {
                    try {
                        long value = reader.nextLong();
                        stack.peek().putValue(name, value);
                    } catch (NumberFormatException e1) {
                        double value = reader.nextDouble();
                        stack.peek().putValue(name, value);
                    }
                }
            }
        }
        return container;
    }

}
