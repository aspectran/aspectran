/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.web.activity.request.MultipartFormDataParser;

/**
 * The Class InMemoryMultipartFormDataParserFactoryBean.
 *
 * @since 5.1.0
 */
public class InMemoryMultipartFormDataParserFactoryBean
        extends InMemoryMultipartFormDataParserFactory
        implements FactoryBean<MultipartFormDataParser> {

    @Override
    public MultipartFormDataParser getObject() {
        return createMultipartFormDataParser();
    }

}
