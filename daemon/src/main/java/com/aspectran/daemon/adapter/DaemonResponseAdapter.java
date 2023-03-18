/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.daemon.adapter;

import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.type.ContentType;

import java.io.Writer;

/**
 * Adapt Daemon Response to Core {@link ResponseAdapter}.
 */
public class DaemonResponseAdapter extends DefaultResponseAdapter {

    /**
     * Instantiates a new DaemonResponseAdapter.
     * @param outputWriter the writer to output
     */
    public DaemonResponseAdapter(Writer outputWriter) {
        super(null, outputWriter);
        setContentType(ContentType.TEXT_PLAIN.toString());
    }

}
