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
package com.aspectran.scheduler.adapter;

import com.aspectran.core.adapter.BasicResponseAdapter;
import com.aspectran.core.context.rule.type.ContentType;

/**
 * The Class QuartzJobResponseAdapter.
 * 
 * @since 2013. 11. 20.
 */
public class QuartzJobResponseAdapter extends BasicResponseAdapter {

    public QuartzJobResponseAdapter() {
        super(null);

        setContentType(ContentType.TEXT_PLAIN.toString());
        setWriter(new QuartzJobOutputWriter());
    }

}
