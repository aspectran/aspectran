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
package com.aspectran.web.support.multipart.commons;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.web.activity.request.MultipartFormDataParser;

/**
 * A {@link com.aspectran.core.component.bean.ablility.FactoryBean} that creates and
 * configures a {@link CommonsMultipartFormDataParser}.
 * <p>This bean simplifies the integration of the multipart parser into the Aspectran
 * framework, allowing its properties to be set via bean configuration.
 * It also resolves the temporary directory path relative to the application's base path.
 *
 * @since 2.0.0
 */
public class CommonsMultipartFormDataParserFactoryBean extends CommonsMultipartFormDataParserFactory
        implements ApplicationAdapterAware, InitializableFactoryBean<MultipartFormDataParser> {

    private ApplicationAdapter applicationAdapter;

    private MultipartFormDataParser parser;

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    /**
     * Sets the temporary directory for uploaded files.
     * <p>If an {@link ApplicationAdapter} is available, the given path will be
     * resolved relative to the application's real path.
     * @param tempFileDir the path to the temporary directory
     */
    @Override
    public void setTempFileDir(String tempFileDir) {
        if (applicationAdapter != null) {
            super.setTempFileDir(applicationAdapter.getRealPath(tempFileDir).toString());
        } else {
            super.setTempFileDir(tempFileDir);
        }
    }

    /**
     * Initializes the bean by creating the {@link MultipartFormDataParser} instance.
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public void initialize() throws Exception {
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
