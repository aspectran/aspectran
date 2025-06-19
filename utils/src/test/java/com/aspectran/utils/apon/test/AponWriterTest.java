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
package com.aspectran.utils.apon.test;

import com.aspectran.utils.apon.AponWriter;

import java.io.PrintWriter;

class AponWriterTest {

    public static void main(String[] args) {
        try {
            AponWriter aponWriter = new AponWriter(new PrintWriter(System.out));
            aponWriter.comment("\ncomment line-1\ncomment line-2\ncomment line-3\n");
            aponWriter.enableValueTypeHints(false);
            aponWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
