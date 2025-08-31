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

import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.ability.HasResponseRules;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class ResponseInnerNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class ResponseInnerNodeletAdder implements NodeletAdder {

    private static final ResponseInnerNodeletAdder INSTANCE = new ResponseInnerNodeletAdder();

    static ResponseInnerNodeletAdder instance() {
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("transform")
            .nodelet(attrs -> {
                String format = attrs.get("format");
                String contentType = attrs.get("contentType");
                String encoding = attrs.get("encoding");
                Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));
                Boolean pretty = BooleanUtils.toNullableBooleanObject(attrs.get("pretty"));

                TransformRule transformRule = TransformRule.newInstance(format, contentType, encoding, defaultResponse, pretty);
                AspectranNodeParser.current().pushObject(transformRule);
            })
            .endNodelet(text -> {
                TransformRule transformRule = AspectranNodeParser.current().popObject();
                HasResponseRules applicable = AspectranNodeParser.current().peekObject();
                applicable.putResponseRule(transformRule);
            })
            .child("template")
                .nodelet(attrs -> {
                    String id = attrs.get("id");
                    String engine = attrs.get("engine");
                    String name = attrs.get("name");
                    String file = attrs.get("file");
                    String resource = attrs.get("resource");
                    String url = attrs.get("url");
                    String style = attrs.get("style");
                    String contentType = attrs.get("contentType");
                    String encoding = attrs.get("encoding");
                    Boolean noCache = BooleanUtils.toNullableBooleanObject(attrs.get("noCache"));

                    TemplateRule templateRule = TemplateRule.newInstanceForBuiltin(id, engine, name, file, resource, url,
                            style, null, contentType, encoding, noCache);
                    AspectranNodeParser.current().pushObject(templateRule);
                })
                .endNodelet(text -> {
                    TemplateRule templateRule = AspectranNodeParser.current().popObject();
                    TransformRule transformRule = AspectranNodeParser.current().peekObject();

                    TemplateRule.updateTemplateSource(templateRule, text);
                    transformRule.setTemplateRule(templateRule);

                    AspectranNodeParser.current().getAssistant().resolveBeanClass(templateRule);
                })
            .parent()
        .parent().child("dispatch")
            .nodelet(attrs -> {
                String name = attrs.get("name");
                String dispatcher = attrs.get("dispatcher");
                String contentType = attrs.get("contentType");
                String encoding = attrs.get("encoding");
                Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

                DispatchRule dispatchRule = DispatchRule.newInstance(name, dispatcher, contentType, encoding, defaultResponse);
                AspectranNodeParser.current().pushObject(dispatchRule);
            })
            .endNodelet(text -> {
                DispatchRule dispatchRule = AspectranNodeParser.current().popObject();
                HasResponseRules applicable = AspectranNodeParser.current().peekObject();
                applicable.putResponseRule(dispatchRule);
            })
        .parent().child("forward")
            .nodelet(attrs -> {
                String contentType = attrs.get("contentType");
                String transletName = attrs.get("translet");
                String method = attrs.get("method");
                Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

                transletName = AspectranNodeParser.current().getAssistant().applyTransletNamePattern(transletName);

                ForwardRule forwardRule = ForwardRule.newInstance(contentType, transletName, method, defaultResponse);
                AspectranNodeParser.current().pushObject(forwardRule);
            })
            .endNodelet(text -> {
                ForwardRule forwardRule = AspectranNodeParser.current().popObject();
                HasResponseRules applicable = AspectranNodeParser.current().peekObject();
                applicable.putResponseRule(forwardRule);
            })
            .child("attributes")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .mount(ItemNodeletGroup.instance())
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    ForwardRule forwardRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, forwardRule.getAttributeItemRuleMap());
                    forwardRule.setAttributeItemRuleMap(irm);
                })
            .parent()
        .parent().child("redirect")
            .nodelet(attrs -> {
                String contentType = attrs.get("contentType");
                String path = attrs.get("path");
                String encoding = attrs.get("encoding");
                Boolean excludeNullParameters = BooleanUtils.toNullableBooleanObject(attrs.get("excludeNullParameters"));
                Boolean excludeEmptyParameters = BooleanUtils.toNullableBooleanObject(attrs.get("excludeEmptyParameters"));
                Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

                RedirectRule redirectRule = RedirectRule.newInstance(contentType, path, encoding, excludeNullParameters,
                        excludeEmptyParameters, defaultResponse);
                AspectranNodeParser.current().pushObject(redirectRule);
            })
            .endNodelet(text -> {
                RedirectRule redirectRule = AspectranNodeParser.current().popObject();

                if (redirectRule.getPath() == null) {
                    throw new IllegalRuleException("The <redirect> element requires a 'path' attribute");
                }

                HasResponseRules applicable = AspectranNodeParser.current().peekObject();
                applicable.putResponseRule(redirectRule);

                AspectranNodeParser.current().getAssistant().resolveBeanClass(redirectRule.getPathTokens());
            })
            .child("parameters")
                .nodelet(attrs -> {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.setProfile(StringUtils.emptyToNull(attrs.get("profile")));
                    AspectranNodeParser.current().pushObject(irm);
                })
                .mount(ItemNodeletGroup.instance())
                .endNodelet(text -> {
                    ItemRuleMap irm = AspectranNodeParser.current().popObject();
                    RedirectRule redirectRule = AspectranNodeParser.current().peekObject();
                    irm = AspectranNodeParser.current().getAssistant().profiling(irm, redirectRule.getParameterItemRuleMap());
                    redirectRule.setParameterItemRuleMap(irm);
                });
    }

}
