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
package com.aspectran.web.support.multipart.inmemory;

import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.web.activity.request.MultipartFormDataParser;

/**
 * A {@link com.aspectran.core.component.bean.ablility.FactoryBean} that creates and
 * configures an {@link InMemoryMultipartFormDataParser}.
 * <p>This bean simplifies the integration of the in-memory multipart parser into the
 * Aspectran framework, allowing its properties to be set via bean configuration.
 *
 * @since 5.1.0
 */
public class InMemoryMultipartFormDataParserFactoryBean extends InMemoryMultipartFormDataParserFactory
        implements InitializableFactoryBean<MultipartFormDataParser> {

    private MultipartFormDataParser parser;

    /**
     * Initializes the bean by creating the {@link MultipartFormDataParser} instance.
     */
    @Override
    public void initialize() {
        if (parser == null) {
            parser = createMultipartFormDataParser();
        }
    }

    /**
     * Returns the created {@link MultipartFormDataParser} instance.
     * @return the parser instance
     */
    @Override
    public MultipartFormDataParser getObject() {
        return parser;
    }

}
