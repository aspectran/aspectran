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
package com.aspectran.shell.console;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * The Class UnclosablePrintWriter.
 * 
 * @since 2016. 1. 22.
 */
public class UnclosablePrintWriter extends PrintWriter {

    public UnclosablePrintWriter(OutputStream out, String encoding) throws UnsupportedEncodingException {
        this(new BufferedWriter(new OutputStreamWriter(out, encoding)));
    }

    public UnclosablePrintWriter(Writer writer) {
        super(writer, true);
    }

    @Override
    public void close() {
        // Do not close the shell output stream until the application is terminated.
    }

}
