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
package com.aspectran.core.scheduler.adapter;

import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.type.ContentType;
import com.aspectran.utils.io.OutputStringWriter;

/**
 * A {@link ResponseAdapter} implementation for a scheduled job environment.
 * <p>Since a scheduled job does not have a traditional client to send a response to,
 * this adapter captures any output from the translet execution into an in-memory
 * {@link OutputStringWriter}. This captured output can then be used for logging or
 * other processing, as seen in {@link com.aspectran.core.scheduler.activity.ActivityJobReporter}.</p>
 *
 * @since 2013. 11. 20.
 */
public class QuartzJobResponseAdapter extends DefaultResponseAdapter {

    /**
     * Instantiates a new QuartzJobResponseAdapter.
     * <p>It initializes with a {@code null} adaptee and sets up an
     * in-memory {@link OutputStringWriter} to capture the response.</p>
     */
    public QuartzJobResponseAdapter() {
        super(null);

        setContentType(ContentType.TEXT_PLAIN.toString());
        setWriter(new OutputStringWriter(768));
    }

}
