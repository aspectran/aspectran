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
package com.aspectran.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * <p>Created: 2019/12/02</p>
 */
class ProcessRunnerTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessRunner runner = new ProcessRunner();
        StringWriter writer = new StringWriter();
        PrintWriter errOut = new PrintWriter(writer);
        runner.run(new String[] {"echo", "hello"}, errOut);
    }

}
