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
package com.aspectran.core.context.rule.parser;

import com.aspectran.core.context.rule.appender.FileRuleAppender;
import com.aspectran.core.context.rule.appender.ResourceRuleAppender;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.AppenderFileFormatType;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractActivityContextParser.
 * 
 * <p>Created: 2008. 06. 14 PM 8:53:29</p>
 */
public abstract class AbstractActivityContextParser implements ActivityContextParser {

    protected final Log log = LogFactory.getLog(getClass());

    private final ContextRuleAssistant assistant;

    private String encoding;

    private boolean hybridLoad;

    public AbstractActivityContextParser(ContextRuleAssistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public ContextRuleAssistant getContextRuleAssistant() {
        return assistant;
    }

    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isHybridLoad() {
        return hybridLoad;
    }

    @Override
    public void setHybridLoad(boolean hybridLoad) {
        this.hybridLoad = hybridLoad;
    }

    protected RuleAppender resolveAppender(String rootConfigLocation) {
        RuleAppender appender;
        if (rootConfigLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String resource = rootConfigLocation.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            appender = new ResourceRuleAppender(resource, assistant.getClassLoader());
        } else if (rootConfigLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            String filePath = rootConfigLocation.substring(ResourceUtils.FILE_URL_PREFIX.length());
            appender = new FileRuleAppender(filePath);
        } else {
            appender = new FileRuleAppender(assistant.getBasePath(), rootConfigLocation);
        }
        if (rootConfigLocation.toLowerCase().endsWith(".apon")) {
            appender.setAppenderFileFormatType(AppenderFileFormatType.APON);
        } else {
            appender.setAppenderFileFormatType(AppenderFileFormatType.XML);
        }
        return appender;
    }

}
