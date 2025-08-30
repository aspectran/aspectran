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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class TemplateNodeParser.
 *
 * <p>Created: 2016. 01. 09</p>
 */
class TemplateNodeletAdder implements NodeletAdder {

    @Override
    public void addTo(NodeletGroup group) {
        group.child("template")
            .nodelet(attrs -> {
                String id = StringUtils.emptyToNull(attrs.get("id"));
                String engine = StringUtils.emptyToNull(attrs.get("engine"));
                String name = StringUtils.emptyToNull(attrs.get("name"));
                String file = StringUtils.emptyToNull(attrs.get("file"));
                String resource = StringUtils.emptyToNull(attrs.get("resource"));
                String url = StringUtils.emptyToNull(attrs.get("url"));
                String style = attrs.get("style");
                String contentType = attrs.get("contentType");
                String encoding = attrs.get("encoding");
                Boolean noCache = BooleanUtils.toNullableBooleanObject(attrs.get("noCache"));

                TemplateRule templateRule = TemplateRule.newInstance(id, engine, name, file,
                        resource, url, style, null, contentType, encoding, noCache);

                AspectranNodeParser.current().pushObject(templateRule);
            })
            .endNodelet(text -> {
                TemplateRule templateRule = AspectranNodeParser.current().popObject();

                TemplateRule.updateTemplateSource(templateRule, text);
                AspectranNodeParser.current().getAssistant().addTemplateRule(templateRule);
            });
    }

}
