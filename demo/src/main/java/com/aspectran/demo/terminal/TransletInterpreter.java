/*
 * Copyright (c) 2008-2020 The Aspectran Project
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
package com.aspectran.demo.terminal;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.RequestToGet;
import com.aspectran.core.component.bean.annotation.RequestToPost;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.json.JsonWriter;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component("/terminal")
@Bean("transletInterpreter")
public class TransletInterpreter extends InstantActivitySupport {

    private final Logger logger = LoggerFactory.getLogger(TransletInterpreter.class);

    @RequestToGet("/query/@{_translet_}")
    @Transform(type = TransformType.TEXT, contentType = "application/json")
    public void query(Translet translet) throws IOException {
        String transletName = translet.getAttribute("_translet_");
        if (StringUtils.isEmpty(transletName)) {
            return;
        }

        TransletRule transletRule = getActivityContext().getTransletRuleRegistry().getTransletRule(transletName);
        if (transletRule == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Translet not found: " + transletName);
            }

            JsonWriter jsonWriter = new JsonWriter(translet.getResponseAdapter().getWriter());
            jsonWriter.beginObject();
            jsonWriter.writeName("translet");
            jsonWriter.writeNull();
            jsonWriter.writeName("request");
            jsonWriter.writeNull();
            jsonWriter.writeName("response");
            jsonWriter.writeNull();
            jsonWriter.endObject();
            return;
        }

        ItemRuleMap parameterItemRuleMap = transletRule.getRequestRule().getParameterItemRuleMap();
        ItemRuleMap attributeItemRuleMap = transletRule.getRequestRule().getAttributeItemRuleMap();

        JsonWriter jsonWriter = new JsonWriter(translet.getResponseAdapter().getWriter());
        jsonWriter.beginObject();
        jsonWriter.writeName("translet");
        jsonWriter.beginObject();
        jsonWriter.writeName("name");
        jsonWriter.write(transletRule.getName());
        if (transletRule.getDescriptionRule() != null) {
            String description = DescriptionRule.render(transletRule.getDescriptionRule(), getCurrentActivity());
            jsonWriter.writeName("description");
            jsonWriter.write(description);
        }
        jsonWriter.endObject();
        jsonWriter.writeName("request");
        jsonWriter.beginObject();
        if (parameterItemRuleMap != null) {
            jsonWriter.writeName("parameters");
            jsonWriter.beginObject();
            jsonWriter.writeName("items");
            jsonWriter.write(toListForItems(parameterItemRuleMap.values()));
            jsonWriter.writeName("tokens");
            jsonWriter.write(toListForTokens(parameterItemRuleMap.values()));
            jsonWriter.endObject();
        }
        if (attributeItemRuleMap != null) {
            jsonWriter.writeName("attributes");
            jsonWriter.beginObject();
            jsonWriter.writeName("items");
            jsonWriter.write(toListForItems(attributeItemRuleMap.values()));
            jsonWriter.writeName("tokens");
            jsonWriter.write(toListForTokens(attributeItemRuleMap.values()));
            jsonWriter.endObject();
        }
        jsonWriter.endObject();
        jsonWriter.writeName("response");
        jsonWriter.beginObject();
        if (transletRule.getResponseRule().getResponse() != null) {
            jsonWriter.writeName("contentType");
            jsonWriter.writeValue(transletRule.getResponseRule().getResponse().getContentType());
        }
        jsonWriter.endObject();
        jsonWriter.endObject();
    }

    @RequestToPost("/exec/@{_translet_}")
    public void execute(Translet translet) {
        String transletName = translet.getAttribute("_translet_");
        if (StringUtils.isEmpty(transletName)) {
            return;
        }

        try {
            instantActivity(transletName);
        } catch (Exception e) {
            logger.error("Failed to execute translet: " + transletName, e);
        }
    }

    private List<Map<String, Object>> toListForTokens(Collection<ItemRule> itemRules) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<Token, Set<ItemRule>> inputTokens = new LinkedHashMap<>();
        for (ItemRule itemRule : itemRules) {
            Token[] tokens = itemRule.getAllTokens();
            if (tokens == null || tokens.length == 0) {
                Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                tokens = new Token[] { t };
            }
            for (Token t1 : tokens) {
                if (t1.getType() == TokenType.PARAMETER) {
                    boolean exists = false;
                    for (Token t2 : inputTokens.keySet()) {
                        if (t2.equals(t1)) {
                            exists = true;
                            break;
                        }
                    }
                    if (exists) {
                        Set<ItemRule> rules = inputTokens.get(t1);
                        rules.add(itemRule);
                    } else {
                        Set<ItemRule> rules = new LinkedHashSet<>();
                        rules.add(itemRule);
                        inputTokens.put(t1, rules);
                    }
                }
            }
        }
        for (Map.Entry<Token, Set<ItemRule>> entry : inputTokens.entrySet()) {
            Token token = entry.getKey();
            Set<ItemRule> rules = entry.getValue();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", token.getName());
            map.put("defaultValue", token.getDefaultValue());
            map.put("string", token.stringify());

            boolean secret = false;
            for (ItemRule ir : rules) {
                if (ir.isSecret()) {
                    secret = true;
                    break;
                }
            }
            map.put("secret", secret);

            boolean mandatory = false;
            for (ItemRule ir : rules) {
                if (ir.isMandatory()) {
                    mandatory = true;
                    break;
                }
            }
            map.put("mandatory", mandatory);

            map.put("items", toListForItems(rules));
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> toListForItems(Collection<ItemRule> itemRules) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ItemRule itemRule : itemRules) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("type", itemRule.getType().toString());
            map.put("name", itemRule.getName());
            map.put("value", itemRule.getValue());
            map.put("mandatory", itemRule.isMandatory());
            map.put("secret", itemRule.isSecret());

            Token[] tokens = itemRule.getAllTokens();
            if (tokens == null) {
                Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                tokens = new Token[] { t };
            }
            map.put("tokenString", TokenParser.toString(tokens));

            String[] tokenNames = new String[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                tokenNames[i] = tokens[i].getName();
            }
            map.put("tokenNames", tokenNames);

            list.add(map);
        }
        return list;
    }

}
