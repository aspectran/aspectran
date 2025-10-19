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

import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.parser.xml.AspectranNodeParser;
import com.aspectran.core.context.rule.parsing.RuleParsingContext;

/**
 * Defines the contract for a parser that reads Aspectran's configuration rules
 * and populates a {@link com.aspectran.core.context.rule.parsing.RuleParsingContext}.
 * <p>Implementations of this interface are responsible for handling different
 * configuration formats (e.g., XML, APON) and sources.</p>
 *
 * <p>Created: 2008. 06. 14 PM 8:53:29</p>
 */
public interface ActivityContextRuleParser extends AutoCloseable {

    /**
     * Returns the rule parsing context.
     * @return the rule parsing context
     */
    RuleParsingContext getRuleParsingContext();

    /**
     * Sets the character encoding to be used for parsing.
     * @param encoding the character encoding
     */
    void setEncoding(String encoding);

    /**
     * Sets whether to use APON to load XML-based configuration.
     * @param useXmlToApon true to use APON for XML
     */
    void setUseXmlToApon(boolean useXmlToApon);

    /**
     * Sets whether to run in debug mode.
     * @param debugMode true to run in debug mode
     */
    void setDebugMode(boolean debugMode);

    /**
     * Parses the specified configuration files.
     * @param contextRules the context rules
     * @return the rule parsing context
     * @throws ActivityContextRuleParserException if an error occurs during parsing
     */
    RuleParsingContext parse(String[] contextRules) throws ActivityContextRuleParserException;

    /**
     * Parses the specified Aspectran parameters.
     * @param aspectranParameters the aspectran parameters
     * @return the rule parsing context
     * @throws ActivityContextRuleParserException if an error occurs during parsing
     */
    RuleParsingContext parse(AspectranParameters aspectranParameters) throws ActivityContextRuleParserException;

    /**
     * Returns the XML node parser for Aspectran.
     * @return the aspectran node parser
     */
    AspectranNodeParser getAspectranNodeParser();

}
