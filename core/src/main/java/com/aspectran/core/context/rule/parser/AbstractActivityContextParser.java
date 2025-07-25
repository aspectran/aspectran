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
package com.aspectran.core.context.rule.parser;

import com.aspectran.core.context.rule.appender.FileRuleAppender;
import com.aspectran.core.context.rule.appender.ResourceRuleAppender;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.type.AppendableFileFormatType;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractActivityContextParser.
 *
 * <p>Created: 2008. 06. 14 PM 8:53:29</p>
 */
public abstract class AbstractActivityContextParser implements ActivityContextParser {

    /** Logger available to subclasses */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ActivityRuleAssistant assistant;

    private String encoding;

    private boolean useXmlToApon;

    private boolean debugMode;

    public AbstractActivityContextParser(ActivityRuleAssistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public ActivityRuleAssistant getContextRuleAssistant() {
        return assistant;
    }

    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    protected boolean isUseXmlToApon() {
        return useXmlToApon;
    }

    @Override
    public void setUseXmlToApon(boolean useXmlToApon) {
        this.useXmlToApon = useXmlToApon;
    }

    protected boolean isDebugMode() {
        return debugMode;
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    protected RuleAppender resolveAppender(@NonNull String classpathOrFilePath) {
        RuleAppender appender;
        if (classpathOrFilePath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String resource = classpathOrFilePath.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            appender = new ResourceRuleAppender(resource, assistant.getClassLoader());
        } else if (classpathOrFilePath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            String filePath = classpathOrFilePath.substring(ResourceUtils.FILE_URL_PREFIX.length());
            appender = new FileRuleAppender(filePath);
        } else {
            appender = new FileRuleAppender(assistant.getBasePath(), classpathOrFilePath);
        }
        if (classpathOrFilePath.toLowerCase().endsWith(".apon")) {
            appender.setAppendableFileFormatType(AppendableFileFormatType.APON);
        } else {
            appender.setAppendableFileFormatType(AppendableFileFormatType.XML);
        }
        return appender;
    }

}
