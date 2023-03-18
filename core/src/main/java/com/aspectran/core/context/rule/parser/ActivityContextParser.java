/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.params.AspectranParameters;

/**
 * The Interface ActivityContextParser.
 *
 * <p>Created: 2008. 06. 14 PM 8:53:29</p>
 */
public interface ActivityContextParser {

    ActivityRuleAssistant getContextRuleAssistant();

    void setEncoding(String encoding);

    void setUseXmlToApon(boolean useXmlToApon);

    void setDebugMode(boolean debugMode);

    ActivityRuleAssistant parse(String[] contextRules) throws ActivityContextParserException;

    ActivityRuleAssistant parse(AspectranParameters aspectranParameters) throws ActivityContextParserException;

}
