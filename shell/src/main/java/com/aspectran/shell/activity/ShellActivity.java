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
package com.aspectran.shell.activity;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.adapter.ShellRequestAdapter;
import com.aspectran.shell.adapter.ShellResponseAdapter;
import com.aspectran.shell.command.ConsoleTerminatedException;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.console.MultiWriter;
import com.aspectran.shell.service.ShellService;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class ShellActivity.
 *
 * @since 2016. 1. 18.
 */
public class ShellActivity extends CoreActivity {

    private static final Log log = LogFactory.getLog(ShellActivity.class);

    private final ShellService service;

    private final Console console;

    private boolean procedural;

    private ParameterMap parameterMap;

    private Writer[] redirectionWriters;

    /**
     * Instantiates a new ShellActivity.
     *
     * @param service the shell service
     */
    public ShellActivity(ShellService service) {
        super(service.getActivityContext());

        this.service = service;
        this.console = service.getConsole();
    }

    public void setProcedural(boolean procedural) {
        this.procedural = procedural;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void setRedirectionWriters(Writer[] redirectionWriters) {
        this.redirectionWriters = redirectionWriters;
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
            setSessionAdapter(service.newSessionAdapter());

            ShellRequestAdapter requestAdapter = new ShellRequestAdapter();
            if (parameterMap != null) {
                requestAdapter.setParameterMap(parameterMap);
            }
            requestAdapter.setEncoding(console.getEncoding());
            setRequestAdapter(requestAdapter);

            Writer outputWriter;
            if (redirectionWriters == null) {
                outputWriter = console.getUnclosableWriter();
            } else {
                List<Writer> writerList = new ArrayList<>(redirectionWriters.length + 1);
                writerList.add(console.getUnclosableWriter());
                Collections.addAll(writerList, redirectionWriters);
                outputWriter = new MultiWriter(writerList.toArray(new Writer[0]));
            }

            ShellResponseAdapter responseAdapter = new ShellResponseAdapter(outputWriter);
            responseAdapter.setEncoding(console.getEncoding());
            setResponseAdapter(responseAdapter);

            super.adapt();
        } catch (Exception e) {
            throw new AdapterException("Failed to specify adapters required for shell service activity", e);
        }
    }

    @Override
    protected void parseRequest() {
        showDescription();

        receiveParameters();
        parseDeclaredParameters();

        receiveAttributes();
        parseDeclaredAttributes();
    }

    /**
     * Prints a description for the {@code Translet}.
     */
    private void showDescription() {
        if (service.isVerbose()) {
            String description = getTranslet().getDescription();
            if (description != null) {
                console.writeLine(description);
                console.flush();
            }
        }
    }

    /**
     * Receive required input parameters.
     */
    private void receiveParameters() {
        ItemRuleMap parameterItemRuleMap = getRequestRule().getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            ItemRuleList parameterItemRuleList = new ItemRuleList(parameterItemRuleMap.values());
            if (procedural) {
                console.setStyle("GREEN");
                console.writeLine("Required parameters:");
                console.offStyle();

                for (ItemRule itemRule : parameterItemRuleList) {
                    Token[] tokens = itemRule.getAllTokens();
                    if (tokens == null) {
                        Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                        t.setDefaultValue(itemRule.getDefaultValue());
                        tokens = new Token[]{t};
                    }
                    String mandatoryMarker = itemRule.isMandatory() ? " * " : "   ";
                    console.setStyle("YELLOW");
                    console.write(mandatoryMarker);
                    console.offStyle();
                    console.setStyle("bold");
                    console.write("%s: ", itemRule.getName());
                    console.offStyle();
                    console.writeLine(TokenParser.toString(tokens));
                }
            }
            enterRequiredParameters(parameterItemRuleList);
        }
    }

    private void enterRequiredParameters(ItemRuleList parameterItemRuleList) {
        ItemRuleList missingItemRules1;
        if (procedural) {
            missingItemRules1 = enterEachParameter(parameterItemRuleList);
        } else {
            missingItemRules1 = checkRequiredParameter(parameterItemRuleList);
        }
        if (missingItemRules1 != null) {
            console.setStyle("YELLOW");
            console.writeLine("Required parameters are missing.");
            console.offStyle();

            ItemRuleList missingItemRules2 = enterEachParameter(missingItemRules1);
            if (missingItemRules2 != null) {
                String[] itemNames = missingItemRules2.getItemNames();
                console.setStyle("RED");
                console.writeLine("Missing required parameters:");
                console.setStyle("WHITE");
                for (String name : itemNames) {
                    console.writeLine("   %s", name);
                }
                console.offStyle();
                terminate("Required parameters are missing");
            }
        }
    }

    /**
     * Receive required input attributes.
     */
    private void receiveAttributes() {
        ItemRuleMap attributeItemRuleMap = getRequestRule().getAttributeItemRuleMap();
        if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
            ItemRuleList attributeItemRuleList = new ItemRuleList(attributeItemRuleMap.values());
            if (procedural) {
                console.setStyle("GREEN");
                console.writeLine("Required attributes:");
                console.offStyle();

                for (ItemRule itemRule : attributeItemRuleList) {
                    Token[] tokens = itemRule.getAllTokens();
                    if (tokens == null) {
                        Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                        t.setDefaultValue(itemRule.getDefaultValue());
                        tokens = new Token[]{t};
                    }
                    String mandatoryMarker = itemRule.isMandatory() ? " * " : "   ";
                    console.setStyle("YELLOW");
                    console.write(mandatoryMarker);
                    console.offStyle();
                    console.setStyle("bold");
                    console.write("%s: ", itemRule.getName());
                    console.offStyle();
                    console.writeLine(TokenParser.toString(tokens));
                }
            }
            enterRequiredAttributes(attributeItemRuleList);
        }
    }

    private void enterRequiredAttributes(ItemRuleList attributeItemRuleList) {
        ItemRuleList missingItemRules1;
        if (procedural) {
            missingItemRules1 = enterEachParameter(attributeItemRuleList);
        } else {
            missingItemRules1 = checkRequiredAttributes(attributeItemRuleList);
        }
        if (missingItemRules1 != null) {
            console.setStyle("YELLOW");
            console.writeLine("Required attributes are missing.");
            console.offStyle();

            ItemRuleList missingItemRules2 = enterEachParameter(missingItemRules1);
            if (missingItemRules2 != null) {
                String[] itemNames = missingItemRules2.getItemNames();
                console.setStyle("RED");
                console.writeLine("Missing required attributes:");
                console.setStyle("WHITE");
                for (String name : itemNames) {
                    console.writeLine("   %s", name);
                }
                console.offStyle();
                terminate("Required attributes are missing");
            }
        }
    }

    private ItemRuleList enterEachParameter(ItemRuleList itemRuleList) {
        console.setStyle("GREEN");
        console.writeLine("Enter a value for each token:");
        console.offStyle();

        Set<ItemRule> missingItemRules = new LinkedHashSet<>();

        try {
            Map<Token, Set<ItemRule>> inputTokens = new LinkedHashMap<>();
            for (ItemRule itemRule : itemRuleList) {
                Token[] tokens = itemRule.getAllTokens();
                if (tokens == null || tokens.length == 0) {
                    Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                    t.setDefaultValue(itemRule.getDefaultValue());
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
                String value = getRequestAdapter().getParameter(token.getName());
                if (value != null) {
                    console.writeLine("   %s: %s", token.stringify(), value);
                    continue;
                }
                Set<ItemRule> rules = entry.getValue();
                boolean security = false;
                for (ItemRule ir : rules) {
                    if (ir.isSecurity()) {
                        security = true;
                        break;
                    }
                }
                if (security) {
                    value = console.readPassword("   %s: ", token.stringify());
                } else {
                    value = console.readLine("   %s: ", token.stringify());
                }
                if (value == null || value.isEmpty()) {
                    value = token.getDefaultValue();
                }
                if (value != null) {
                    getRequestAdapter().setParameter(token.getName(), value);
                } else {
                    for (ItemRule ir : rules) {
                        if (ir.isMandatory()) {
                            missingItemRules.add(ir);
                        }
                    }
                }
            }
        } catch (ConsoleTerminatedException e) {
            log.info("User interrupt occurred");
            terminate("User interrupt occurred");
        }

        return (missingItemRules.isEmpty() ? null : new ItemRuleList(missingItemRules));
    }

    private ItemRuleList checkRequiredParameter(ItemRuleList itemRuleList) {
        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        ItemEvaluator evaluator = new ItemExpressionParser(this);
        for (ItemRule itemRule : itemRuleList) {
            String[] values = evaluator.evaluateAsStringArray(itemRule);
            if (values != null && values.length > 0) {
                getRequestAdapter().setParameter(itemRule.getName(), values);
            } else {
                missingItemRules.add(itemRule);
            }
        }
        return (missingItemRules.isEmpty() ? null : new ItemRuleList(missingItemRules));
    }

    private ItemRuleList checkRequiredAttributes(ItemRuleList itemRuleList) {
        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        ItemEvaluator evaluator = new ItemExpressionParser(this);
        for (ItemRule itemRule : itemRuleList) {
            Object value = evaluator.evaluate(itemRule);
            if (value != null) {
                getRequestAdapter().setAttribute(itemRule.getName(), value);
            } else {
                missingItemRules.add(itemRule);
            }
        }
        return (missingItemRules.isEmpty() ? null : new ItemRuleList(missingItemRules));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Activity> T newActivity() {
        ShellActivity activity = new ShellActivity(service);
        activity.setRedirectionWriters(redirectionWriters);
        activity.setIncluded(true);
        return (T)activity;
    }

}
