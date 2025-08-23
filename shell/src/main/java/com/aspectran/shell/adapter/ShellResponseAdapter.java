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
package com.aspectran.shell.adapter;

import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.shell.console.ShellConsole;

import java.io.Writer;

/**
 * The response adapter for the interactive shell environment.
 * <p>This adapter directs response output to the {@link ShellConsole}. It is
 * configured with a {@link Writer} that typically points to the console, but can
 * also be redirected to a file or other destination. The {@link ShellConsole}
 * itself is provided as the adaptee for potential access to console-specific features.
 * </p>
 *
 * @author Juho Jeong
 * @since 2017. 3. 4.
 */
public class ShellResponseAdapter extends DefaultResponseAdapter {

    /**
     * Creates a new {@code ShellResponseAdapter}.
     * @param console the shell console, which serves as the adaptee
     * @param writer the writer to which response content will be written
     */
    public ShellResponseAdapter(ShellConsole console, Writer writer) {
        super(console, writer);
    }

}
