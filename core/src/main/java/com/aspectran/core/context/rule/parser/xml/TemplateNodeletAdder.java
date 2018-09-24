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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class TemplateNodeletAdder.
 * 
 * <p>Created: 2016. 01. 09</p>
 */
class TemplateNodeletAdder implements NodeletAdder {

    protected final ContextRuleAssistant assistant;

    /**
     * Instantiates a new TemplateNodeletAdder.
     *
     * @param assistant the assistant
     */
    TemplateNodeletAdder(ContextRuleAssistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public void process(String xpath, NodeletParser parser) {
        parser.setXpath(xpath + "/template");
        parser.addNodelet(attrs -> {
            String id = StringUtils.emptyToNull(attrs.get("id"));
            String engine = StringUtils.emptyToNull(attrs.get("engine"));
            String name = StringUtils.emptyToNull(attrs.get("name"));
            String file = StringUtils.emptyToNull(attrs.get("file"));
            String resource = StringUtils.emptyToNull(attrs.get("resource"));
            String url = StringUtils.emptyToNull(attrs.get("url"));
            String style = attrs.get("style");
            String encoding = attrs.get("encoding");
            Boolean noCache = BooleanUtils.toNullableBooleanObject(attrs.get("noCache"));

            TemplateRule templateRule = TemplateRule.newInstance(id, engine, name, file,
                    resource, url, null, style, encoding, noCache);
            parser.pushObject(templateRule);
        });
        parser.addNodeEndlet(text -> {
            TemplateRule templateRule = parser.popObject();
            TemplateRule.updateTemplateSource(templateRule, text);
            assistant.addTemplateRule(templateRule);
        });
    }

}