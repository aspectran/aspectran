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
package com.aspectran.utils.apon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-06-28</p>
 */
class ArrayParametersTest {

    @Test
    void readArrayParameters() throws AponParseException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  param1: 111\n");
        sb.append("  param2: 222\n");
        sb.append("}\n");
        sb.append("{\n");
        sb.append("  param3: 333\n");
        sb.append("  param4: 444\n");
        sb.append("}\n");

        ArrayParameters arrayParameters = new ArrayParameters(sb.toString());
        String s1 = arrayParameters.toString();

        AponReader aponReader = new AponReader(sb.toString());
        String s2 = aponReader.read(new ArrayParameters()).toString();

        assertEquals(s1, s2);
    }

}
