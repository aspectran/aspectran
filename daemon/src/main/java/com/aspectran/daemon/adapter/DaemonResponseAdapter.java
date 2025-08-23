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
package com.aspectran.daemon.adapter;

import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.context.rule.type.ContentType;

import java.io.Writer;

/**
 * The response adapter for the daemon environment.
 * <p>This class is a specialization of {@link DefaultResponseAdapter} that directs
 * all response output to a provided {@link Writer}, such as a log file or console.
 * It automatically sets the content type to {@code text/plain}.
 * </p>
 *
 * @author Juho Jeong
 * @since 2017. 12. 12.
 */
public class DaemonResponseAdapter extends DefaultResponseAdapter {

    /**
     * Creates a new {@code DaemonResponseAdapter} that writes to the specified writer.
     * @param outputWriter the writer to which response content will be written
     */
    public DaemonResponseAdapter(Writer outputWriter) {
        super(null, outputWriter);
        setContentType(ContentType.TEXT_PLAIN.toString());
    }

}
