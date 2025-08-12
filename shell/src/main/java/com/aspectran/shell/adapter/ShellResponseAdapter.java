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
 * Shell response adapter that writes translet output to the interactive console or a provided writer.
 * <p>
 * Created and configured by {@link com.aspectran.shell.activity.ShellActivity}, this adapter
 * delegates rendering to the {@link ShellConsole} while also supporting redirection to an
 * arbitrary {@link Writer} when requested by the user.
 * </p>
 */
public class ShellResponseAdapter extends DefaultResponseAdapter {

    /**
     * Create a new ShellResponseAdapter that renders through the given console/writer.
     * @param console the shell console used for styling and terminal output
     * @param writer the writer to receive output (may be a redirected destination)
     */
    public ShellResponseAdapter(ShellConsole console, Writer writer) {
        super(console, writer);
    }

}
