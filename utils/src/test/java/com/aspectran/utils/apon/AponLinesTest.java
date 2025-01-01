/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AponLinesTest {

    @Test
    void test1() {
        AponLines aponLines1 = new AponLines()
                .line("name", "value")
                .line("number", 9999)
                .line("text", "(")
                .line("|text-line-1")
                .line("|text-line-2")
                .line(")")
                .line("block", "{")
                .line("nested", "block")
                .line("}")
                .line("count: [")
                .line("1")
                .line("2")
                .line("3")
                .line("]")
                .line("array: [")
                .line("{")
                .line("block1-in-array", 1)
                .line("}")
                .line("{")
                .line("block2-in-array", 2)
                .line("}")
                .line("]")
                ;
        String apon1 = aponLines1.toString();
        //System.out.println(apon1);

        AponLines aponLines2 = new AponLines()
                .line("name", "value")
                .line("number", 9999)
                .text("text")
                .line("text-line-1")
                .line("text-line-2")
                .end()
                .block("block")
                .line("nested", "block")
                .end()
                .array("count")
                .line("1")
                .line("2")
                .line("3")
                .end()
                .array("array")
                .block()
                .line("block1-in-array", 1)
                .end()
                .block()
                .line("block2-in-array", 2)
                .end()
                .end()
                ;
        String apon2 = aponLines2.toString();
        //System.out.println(apon2);

        assertEquals(apon1, apon2);
    }

    @Test
    void test2() throws IOException {
        AponLines aponLines1 = new AponLines()
                .line("name", "value")
                .line("number", 9999)
                .line("text", "(")
                .line("|text-line-1")
                .line("|text-line-2")
                .line(")")
                .line("block", "{")
                .line("nested", "block")
                .line("}")
                .line("count: [")
                .line("1")
                .line("2")
                .line("3")
                .line("]")
                .line("array: [")
                .line("{")
                .line("block1-in-array", 1)
                .line("}")
                .line("{")
                .line("block2-in-array", 2)
                .line("}")
                .line("]")
                ;
        String apon1 = aponLines1.format();
        //System.out.println(apon1);

        AponLines aponLines2 = new AponLines()
                .line("name", "value")
                .line("number", 9999)
                .text("text")
                .line("text-line-1")
                .line("text-line-2")
                .end()
                .block("block")
                .line("nested", "block")
                .end()
                .array("count")
                .line("1")
                .line("2")
                .line("3")
                .end()
                .array("array")
                .block()
                .line("block1-in-array", 1)
                .end()
                .block()
                .line("block2-in-array", 2)
                .end()
                .end()
                ;
        String apon2 = aponLines2.format();
        //System.out.println(apon2);

        assertEquals(apon1, apon2);
    }

}
