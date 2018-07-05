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
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
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

    private final Writer[] redirectionWriters;

    /**
     * Instantiates a new ShellActivity.
     *
     * @param service the shell service
     * @param redirectionWriters the redirection writers
     */
    public ShellActivity(ShellService service, Writer[] redirectionWriters) {
        super(service.getActivityContext());

        this.service = service;
        this.console = service.getConsole();
        this.redirectionWriters = redirectionWriters;
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
            setSessionAdapter(service.newSessionAdapter());

            RequestAdapter requestAdapter = new ShellRequestAdapter();
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

            ResponseAdapter responseAdapter = new ShellResponseAdapter(outputWriter);
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

            console.setStyle("WHITE", "underline");
            console.writeLine("Required parameters:");
            console.offStyle();

            for (ItemRule itemRule : parameterItemRuleList) {
                Token[] tokens = itemRule.getAllTokens();
                if (tokens == null) {
                    tokens = new Token[] { new Token(TokenType.PARAMETER, itemRule.getName()) };
                }

                String mandatoryMarker = itemRule.isMandatory() ? " * " : "   ";
                console.setStyle("GREEN");
                console.write(mandatoryMarker);
                console.offStyle();
                console.writeLine("%s: %s", itemRule.getName(), TokenParser.toString(tokens));
            }

            enterRequiredParameters(parameterItemRuleList);
        }
    }

    private void enterRequiredParameters(ItemRuleList parameterItemRuleList) {
        ItemRuleList missingItemRules1 = enterEachParameter(parameterItemRuleList);

        if (missingItemRules1 != null) {
            console.setStyle("YELLOW");
            console.writeLine("Missing required parameters.");
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
                terminate("Missing required parameters");
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

            console.setStyle("WHITE", "underline");
            console.writeLine("Required attributes:");
            console.offStyle();

            for (ItemRule itemRule : attributeItemRuleList) {
                Token[] tokens = itemRule.getAllTokens();
                if (tokens == null) {
                    tokens = new Token[] { new Token(TokenType.PARAMETER, itemRule.getName()) };
                }

                String mandatoryMarker = itemRule.isMandatory() ? " * " : "   ";
                console.setStyle("GREEN");
                console.write(mandatoryMarker);
                console.offStyle();
                console.writeLine("%s: %s", itemRule.getName(), TokenParser.toString(tokens));
            }

            enterRequiredAttributes(attributeItemRuleList);
        }
    }

    private void enterRequiredAttributes(ItemRuleList attributeItemRuleList) {
        ItemRuleList missingItemRules1 = enterEachParameter(attributeItemRuleList);

        if (missingItemRules1 != null) {
            console.setStyle("YELLOW");
            console.writeLine("Missing required attributes.");
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
                terminate("Missing required attributes");
            }
        }
    }

    private ItemRuleList enterEachParameter(ItemRuleList itemRuleList) {
        console.setStyle("underline");
        console.writeLine("Enter a value for each token:");
        console.offStyle();

        Set<ItemRule> missingItemRules = new LinkedHashSet<>(itemRuleList.size());

        try {
            Map<Token, Set<ItemRule>> inputTokens = new LinkedHashMap<>(itemRuleList.size());
            for (ItemRule itemRule : itemRuleList) {
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
                        if (!exists) {
                            Set<ItemRule> rules = new LinkedHashSet<>();
                            rules.add(itemRule);
                            inputTokens.put(t1, rules);
                        } else {
                            Set<ItemRule> rules = inputTokens.get(t1);
                            rules.add(itemRule);
                        }
                    }
                }
            }

            for (Map.Entry<Token, Set<ItemRule>> entry : inputTokens.entrySet()) {
                Token token = entry.getKey();
                Set<ItemRule> rules = entry.getValue();
                boolean security = false;
                for (ItemRule ir : rules) {
                    if (ir.isSecurity()) {
                        security = true;
                        break;
                    }
                }
                String value;
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

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Activity> T newActivity() {
        ShellActivity activity = new ShellActivity(service, redirectionWriters);
        activity.setIncluded(true);
        return (T)activity;
    }

}
