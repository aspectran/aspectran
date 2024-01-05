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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletParser;
import com.aspectran.utils.nodelet.SubnodeParser;

/**
 * The Class ResponseInnerNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class ResponseInnerNodeParser implements SubnodeParser {

    @Override
    public void parse(@NonNull String xpath, @NonNull NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActivityRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/transform");
        parser.addNodelet(attrs -> {
            String format = attrs.get("format");
            String contentType = attrs.get("contentType");
            String encoding = attrs.get("encoding");
            Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));
            Boolean pretty = BooleanUtils.toNullableBooleanObject(attrs.get("pretty"));

            TransformRule transformRule = TransformRule.newInstance(format, contentType, encoding, defaultResponse, pretty);
            parser.pushObject(transformRule);
        });
        parser.addEndNodelet(text -> {
            TransformRule transformRule = parser.popObject();
            ResponseRuleApplicable applicable = parser.peekObject();
            applicable.applyResponseRule(transformRule);
        });
        parser.setXpath(xpath + "/transform/template");
        parser.addNodelet(attrs -> {
            String id = attrs.get("id");
            String engine = attrs.get("engine");
            String name = attrs.get("name");
            String file = attrs.get("file");
            String resource = attrs.get("resource");
            String url = attrs.get("url");
            String style = attrs.get("style");
            String encoding = attrs.get("encoding");
            Boolean noCache = BooleanUtils.toNullableBooleanObject(attrs.get("noCache"));

            TemplateRule templateRule = TemplateRule.newInstanceForBuiltin(id, engine, name, file, resource, url,
                    style, null, encoding, noCache);
            parser.pushObject(templateRule);
        });
        parser.addEndNodelet(text -> {
            TemplateRule templateRule = parser.popObject();
            TransformRule transformRule = parser.peekObject();

            TemplateRule.updateTemplateSource(templateRule, text);
            transformRule.setTemplateRule(templateRule);

            assistant.resolveBeanClass(templateRule);
        });
        parser.setXpath(xpath + "/dispatch");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String dispatcher = attrs.get("dispatcher");
            String contentType = attrs.get("contentType");
            String encoding = attrs.get("encoding");
            Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

            DispatchRule dispatchRule = DispatchRule.newInstance(name, dispatcher, contentType, encoding, defaultResponse);
            parser.pushObject(dispatchRule);
        });
        parser.addEndNodelet(text -> {
            DispatchRule dispatchRule = parser.popObject();
            ResponseRuleApplicable applicable = parser.peekObject();
            applicable.applyResponseRule(dispatchRule);
        });
        parser.setXpath(xpath + "/forward");
        parser.addNodelet(attrs -> {
            String contentType = attrs.get("contentType");
            String transletName = attrs.get("translet");
            String method = attrs.get("method");
            Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

            transletName = assistant.applyTransletNamePattern(transletName);

            ForwardRule forwardRule = ForwardRule.newInstance(contentType, transletName, method, defaultResponse);
            parser.pushObject(forwardRule);
        });
        parser.addEndNodelet(text -> {
            ForwardRule forwardRule = parser.popObject();
            ResponseRuleApplicable applicable = parser.peekObject();
            applicable.applyResponseRule(forwardRule);
        });
        parser.setXpath(xpath + "/forward/attributes");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.parseItemNode();
        parser.addEndNodelet(text -> {
            ItemRuleMap irm = parser.popObject();
            ForwardRule forwardRule = parser.peekObject();
            irm = assistant.profiling(irm, forwardRule.getAttributeItemRuleMap());
            forwardRule.setAttributeItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/redirect");
        parser.addNodelet(attrs -> {
            String contentType = attrs.get("contentType");
            String path = attrs.get("path");
            String encoding = attrs.get("encoding");
            Boolean excludeNullParameters = BooleanUtils.toNullableBooleanObject(attrs.get("excludeNullParameters"));
            Boolean excludeEmptyParameters = BooleanUtils.toNullableBooleanObject(attrs.get("excludeEmptyParameters"));
            Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

            RedirectRule redirectRule = RedirectRule.newInstance(contentType, path, encoding, excludeNullParameters,
                    excludeEmptyParameters, defaultResponse);
            parser.pushObject(redirectRule);
        });
        parser.addEndNodelet(text -> {
            RedirectRule redirectRule = parser.popObject();

            if (redirectRule.getPath() == null) {
                throw new IllegalRuleException("The <redirect> element requires a 'path' attribute");
            }

            ResponseRuleApplicable applicable = parser.peekObject();
            applicable.applyResponseRule(redirectRule);

            assistant.resolveBeanClass(redirectRule.getPathTokens());
        });
        parser.setXpath(xpath + "/redirect/parameters");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        nodeParser.parseItemNode();
        parser.addEndNodelet(text -> {
            ItemRuleMap irm = parser.popObject();
            RedirectRule redirectRule = parser.peekObject();
            irm = assistant.profiling(irm, redirectRule.getParameterItemRuleMap());
            redirectRule.setParameterItemRuleMap(irm);
        });
    }

}
