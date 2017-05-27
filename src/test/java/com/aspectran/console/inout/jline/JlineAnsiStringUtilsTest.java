/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.console.inout.jline;

import java.io.IOException;

import org.jline.utils.AttributedStringBuilder;
import org.junit.Test;

/**
 * <p>Created: 2017. 5. 21.</p>
 */
public class JlineAnsiStringUtilsTest {

    @Test
    public void testAnsiColor() throws IOException {
        String s1 = "Ansi 8 colors {{white,bg:black}} Black {{black,bg:red}} Red {{white,bg:green}} Green {{black,bg:yellow}} Yellow {{bg:blue}} Blue {{bg:magenta}} Magenta {{bg:cyan}} Cyan {{black,bg:white}} White {{off}}";
        AttributedStringBuilder asb1 = JlineAnsiStringUtils.parse(s1);
        System.out.println(asb1.toAnsi());
    }

}