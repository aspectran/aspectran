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
package com.aspectran.web.servlet.support.multipart.standard;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.web.activity.request.MultipartFormDataParser;

/**
 * A {@link com.aspectran.core.component.bean.ablility.FactoryBean} that creates and
 * configures a {@link StandardServletMultipartFormDataParser}.
 *
 * @since 9.7.0
 */
public class StandardServletMultipartFormDataParserFactoryBean extends StandardServletMultipartFormDataParserFactory
        implements ApplicationAdapterAware, InitializableFactoryBean<MultipartFormDataParser> {

    private ApplicationAdapter applicationAdapter;

    private MultipartFormDataParser parser;

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    @Override
    public void setTempDir(String tempDir) {
        if (applicationAdapter != null) {
            super.setTempDir(applicationAdapter.getRealPath(tempDir).toString());
        } else {
            super.setTempDir(tempDir);
        }
    }

    @Override
    public void initialize() throws Exception {
        if (parser == null) {
            parser = createMultipartFormDataParser();
        }
    }

    @Override
    public MultipartFormDataParser getObject() {
        return parser;
    }

}
