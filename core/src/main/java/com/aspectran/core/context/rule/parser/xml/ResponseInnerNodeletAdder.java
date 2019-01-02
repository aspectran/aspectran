/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class ResponseInnerNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class ResponseInnerNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ActionNodeletAdder actionNodeletAdder = nodeParser.getActionNodeletAdder();
        ItemNodeletAdder itemNodeletAdder = nodeParser.getItemNodeletAdder();
        ContextRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/transform");
        parser.addNodelet(attrs -> {
            String type = attrs.get("type");
            String contentType = attrs.get("contentType");
            String encoding = attrs.get("encoding");
            Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));
            Boolean pretty = BooleanUtils.toNullableBooleanObject(attrs.get("pretty"));

            TransformRule tr = TransformRule.newInstance(type, contentType, encoding, defaultResponse, pretty);
            parser.pushObject(tr);

            ActionList actionList = new ActionList();
            parser.pushObject(actionList);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            TransformRule tr = parser.popObject();

            if (!actionList.isEmpty()) {
                tr.setActionList(actionList);
            }

            ResponseRuleApplicable applicable = parser.peekObject();
            applicable.applyResponseRule(tr);
        });
        parser.setXpath(xpath + "/transform/template");
        parser.addNodelet(attrs -> {
            String engine = attrs.get("engine");
            String name = attrs.get("name");
            String file = attrs.get("file");
            String resource = attrs.get("resource");
            String url = attrs.get("url");
            String style = attrs.get("style");
            String encoding = attrs.get("encoding");
            Boolean noCache = BooleanUtils.toNullableBooleanObject(attrs.get("noCache"));

            TemplateRule templateRule = TemplateRule.newInstanceForBuiltin(engine, name, file, resource, url,
                    null, style, encoding, noCache);
            parser.pushObject(templateRule);
        });
        parser.addNodeEndlet(text -> {
            TemplateRule templateRule = parser.popObject();
            TransformRule transformRule = parser.peekObject(1);

            TemplateRule.updateTemplateSource(templateRule, text);
            transformRule.setTemplateRule(templateRule);

            assistant.resolveBeanClass(templateRule);
        });
        parser.setXpath(xpath + "/transform/call");
        parser.addNodelet(attrs -> {
            String template = StringUtils.emptyToNull(attrs.get("template"));

            TransformRule transformRule = parser.peekObject(1);
            TransformRule.updateTemplateId(transformRule, template);
        });
        parser.setXpath(xpath + "/dispatch");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String dispatcher = attrs.get("dispatcher");
            String contentType = attrs.get("contentType");
            String encoding = attrs.get("encoding");
            Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

            DispatchResponseRule drr = DispatchResponseRule.newInstance(name, dispatcher, contentType, encoding, defaultResponse);
            parser.pushObject(drr);

            ActionList actionList = new ActionList();
            parser.pushObject(actionList);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            DispatchResponseRule drr = parser.popObject();

            if (!actionList.isEmpty()) {
                drr.setActionList(actionList);
            }

            ResponseRuleApplicable applicable = parser.peekObject();
            applicable.applyResponseRule(drr);
        });
        parser.setXpath(xpath + "/redirect");
        parser.addNodelet(attrs -> {
            String contentType = attrs.get("contentType");
            String path = attrs.get("path");
            String encoding = attrs.get("encoding");
            Boolean excludeNullParameters = BooleanUtils.toNullableBooleanObject(attrs.get("excludeNullParameters"));
            Boolean excludeEmptyParameters = BooleanUtils.toNullableBooleanObject(attrs.get("excludeEmptyParameters"));
            Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

            RedirectResponseRule rrr = RedirectResponseRule.newInstance(contentType, path, encoding, excludeNullParameters,
                    excludeEmptyParameters, defaultResponse);
            parser.pushObject(rrr);

            ActionList actionList = new ActionList();
            parser.pushObject(actionList);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            RedirectResponseRule rrr = parser.popObject();

            if (rrr.getPath() == null) {
                throw new IllegalArgumentException("The <redirect> element requires a 'path' attribute");
            }
            if (!actionList.isEmpty()) {
                rrr.setActionList(actionList);
            }

            ResponseRuleApplicable applicable = parser.peekObject();
            applicable.applyResponseRule(rrr);

            assistant.resolveBeanClass(rrr.getPathTokens());
        });
        parser.setXpath(xpath + "/redirect/parameters");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        parser.addNodelet(itemNodeletAdder);
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            RedirectResponseRule rrr = parser.peekObject(1);
            irm = assistant.profiling(irm, rrr.getParameterItemRuleMap());
            rrr.setParameterItemRuleMap(irm);
        });
        parser.setXpath(xpath + "/forward");
        parser.addNodelet(attrs -> {
            String contentType = attrs.get("contentType");
            String transletName = attrs.get("translet");
            String method = attrs.get("method");
            Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

            transletName = assistant.applyTransletNamePattern(transletName);

            ForwardResponseRule frr = ForwardResponseRule.newInstance(contentType, transletName, method, defaultResponse);
            parser.pushObject(frr);

            ActionList actionList = new ActionList();
            parser.pushObject(actionList);
        });
        parser.addNodeEndlet(text -> {
            ActionList actionList = parser.popObject();
            ForwardResponseRule frr = parser.popObject();

            if (!actionList.isEmpty()) {
                frr.setActionList(actionList);
            }

            ResponseRuleApplicable applicable = parser.peekObject();
            applicable.applyResponseRule(frr);
        });
        parser.addNodelet(actionNodeletAdder);
        parser.setXpath(xpath + "/forward/attributes");
        parser.addNodelet(attrs -> {
            ItemRuleMap irm = new ItemRuleMap();
            irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
            parser.pushObject(irm);
        });
        parser.addNodelet(itemNodeletAdder);
        parser.addNodeEndlet(text -> {
            ItemRuleMap irm = parser.popObject();
            ForwardResponseRule frr = parser.peekObject(1);
            irm = assistant.profiling(irm, frr.getAttributeItemRuleMap());
            frr.setAttributeItemRuleMap(irm);
        });
    }

}