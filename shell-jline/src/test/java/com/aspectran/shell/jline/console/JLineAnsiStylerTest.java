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
package com.aspectran.shell.jline.console;

import org.junit.Test;

import java.io.IOException;

/**
 * <p>Created: 2017. 11. 19.</p>
 */
public class JLineAnsiStylerTest {

    @Test
    public void testAnsiColor() throws IOException {
        String s1 = "Ansi 8 colors {{white,bg:black}} Black {{black,bg:red}} Red {{white,bg:green}} Green {{black,bg:yellow}} Yellow {{bg:blue}} Blue {{bg:magenta}} Magenta {{bg:cyan}} Cyan {{black,bg:white}} White {{off}}";
        System.out.println(JLineAnsiStyler.parse(s1));
    }

}