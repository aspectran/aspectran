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
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.ability.HasResponseRules;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
import org.jspecify.annotations.NonNull;

/**
 * A {@code NodeletAdder} for parsing response-related elements such as
 * {@code <transform>}, {@code <dispatch>}, {@code <forward>}, and {@code <redirect>}.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class ResponseInnerNodeletAdder implements NodeletAdder {

    private static volatile ResponseInnerNodeletAdder INSTANCE;

    static ResponseInnerNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (ResponseInnerNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ResponseInnerNodeletAdder();
                }
            }
        }
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
                AspectranNodeParsingContext.pushObject(transformRule);
            })
            .endNodelet(text -> {
                TransformRule transformRule = AspectranNodeParsingContext.popObject();
                HasResponseRules hasResponseRules = AspectranNodeParsingContext.peekObject();
                hasResponseRules.putResponseRule(transformRule);
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
                    AspectranNodeParsingContext.pushObject(templateRule);
                })
                .endNodelet(text -> {
                    TemplateRule templateRule = AspectranNodeParsingContext.popObject();
                    TransformRule transformRule = AspectranNodeParsingContext.peekObject();

                    TemplateRule.updateTemplateSource(templateRule, text);
                    transformRule.setTemplateRule(templateRule);

                    AspectranNodeParsingContext.getCurrentRuleParsingContext().resolveBeanClass(templateRule);
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
                AspectranNodeParsingContext.pushObject(dispatchRule);
            })
            .endNodelet(text -> {
                DispatchRule dispatchRule = AspectranNodeParsingContext.popObject();
                HasResponseRules hasResponseRules = AspectranNodeParsingContext.peekObject();
                hasResponseRules.putResponseRule(dispatchRule);
            })
        .parent().child("forward")
            .nodelet(attrs -> {
                String contentType = attrs.get("contentType");
                String transletName = attrs.get("translet");
                String method = attrs.get("method");
                Boolean defaultResponse = BooleanUtils.toNullableBooleanObject(attrs.get("default"));

                transletName = AspectranNodeParsingContext.getCurrentRuleParsingContext().applyTransletNamePattern(transletName);

                ForwardRule forwardRule = ForwardRule.newInstance(contentType, transletName, method, defaultResponse);
                AspectranNodeParsingContext.pushObject(forwardRule);
            })
            .with(AttributesNodeletAdder.instance())
            .endNodelet(text -> {
                ForwardRule forwardRule = AspectranNodeParsingContext.popObject();
                HasResponseRules hasResponseRules = AspectranNodeParsingContext.peekObject();
                hasResponseRules.putResponseRule(forwardRule);
            })
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
                AspectranNodeParsingContext.pushObject(redirectRule);
            })
            .with(ParametersNodeletAdder.instance())
            .endNodelet(text -> {
                RedirectRule redirectRule = AspectranNodeParsingContext.popObject();

                if (redirectRule.getPath() == null) {
                    throw new IllegalRuleException("The <redirect> element requires a 'path' attribute");
                }

                HasResponseRules hasResponseRules = AspectranNodeParsingContext.peekObject();
                hasResponseRules.putResponseRule(redirectRule);

                AspectranNodeParsingContext.getCurrentRuleParsingContext().resolveBeanClass(redirectRule.getPathTokens());
            });
    }

}
