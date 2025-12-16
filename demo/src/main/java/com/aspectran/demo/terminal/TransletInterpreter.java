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
package com.aspectran.demo.terminal;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.RequestToGet;
import com.aspectran.core.component.bean.annotation.RequestToPost;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.json.JsonWriter;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final String COMMANDS_PREFIX = "/commands/";

    @RequestToGet("/query/@{_translet_}")
    @Transform(format = FormatType.TEXT, contentType = "application/json")
    public void query(@NonNull Translet translet) throws IOException {
        String requestName = translet.getAttribute("_translet_");
        if (StringUtils.isEmpty(requestName)) {
            return;
        }

        requestName = COMMANDS_PREFIX + requestName;
        TransletRule transletRule = getActivityContext().getTransletRuleRegistry().getTransletRule(requestName);
        if (transletRule == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No translet {}", requestName);
            }

            new JsonWriter(translet.getResponseAdapter().getWriter())
                .beginObject()
                .name("translet").value(null)
                .name("request").value(null)
                .name("response").value(null)
                .endObject();
            return;
        }

        ItemRuleMap parameterItemRuleMap = transletRule.getRequestRule().getParameterItemRuleMap();
        ItemRuleMap attributeItemRuleMap = transletRule.getRequestRule().getAttributeItemRuleMap();

        JsonWriter jsonWriter = new JsonWriter(translet.getResponseAdapter().getWriter())
            .beginObject()
            .name("translet")
            .beginObject()
            .name("name").value(transletRule.getName());
        if (transletRule.getDescriptionRule() != null) {
            String description = DescriptionRule.render(transletRule.getDescriptionRule(), getCurrentActivity());
            jsonWriter.name("description").value(description);
        }
        jsonWriter.endObject()
            .name("request")
            .beginObject();
        if (parameterItemRuleMap != null) {
            jsonWriter.name("parameters")
                .beginObject()
                .name("items").value(toListForItems(parameterItemRuleMap.values()))
                .name("tokens").value(toListForTokens(parameterItemRuleMap.values()))
                .endObject();
        }
        if (attributeItemRuleMap != null) {
            jsonWriter.name("attributes")
                .beginObject()
                .name("items").value(toListForItems(attributeItemRuleMap.values()))
                .name("tokens").value(toListForTokens(attributeItemRuleMap.values()))
                .endObject();
        }
        jsonWriter.endObject()
            .name("response")
            .beginObject();
        if (transletRule.getResponseRule().getResponse() != null) {
            jsonWriter.name("contentType").value(transletRule.getResponseRule().getResponse().getContentType());
        }
        jsonWriter.endObject().endObject();
    }

    @RequestToPost("/exec/@{_translet_}")
    public void execute(@NonNull Translet translet) {
        String requestName = translet.getAttribute("_translet_");
        if (StringUtils.isEmpty(requestName)) {
            return;
        }

        try {
            requestName = COMMANDS_PREFIX + requestName;
            instantActivity(requestName);
        } catch (Exception e) {
            logger.error("Failed to execute translet: {}", requestName, e);
        }
    }

    @NonNull
    private List<Map<String, Object>> toListForTokens(@NonNull Collection<ItemRule> itemRules) {
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

    @NonNull
    private List<Map<String, Object>> toListForItems(@NonNull Collection<ItemRule> itemRules) {
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
