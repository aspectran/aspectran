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
package com.aspectran.embed.adapter;

import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.embed.service.EmbeddedAspectran;

import java.io.Writer;

/**
 * The response adapter for an embedded Aspectran environment.
 * <p>This adapter extends {@link DefaultResponseAdapter} and is used to capture
 * the output of a translet execution. It directs all response content to a
 * provided {@link Writer} and defaults the content type to {@code text/plain}.
 * </p>
 *
 * @author Juho Jeong
 * @since 2016. 11. 26.
 * @see EmbeddedAspectran
 */
public class AspectranResponseAdapter extends DefaultResponseAdapter {

    /**
     * Creates a new {@code AspectranResponseAdapter} that writes to the specified writer.
     * @param outputWriter the writer to which response content will be written
     */
    public AspectranResponseAdapter(Writer outputWriter) {
        super(null, outputWriter);
        setContentType(ContentType.TEXT_PLAIN.toString());
    }

}
