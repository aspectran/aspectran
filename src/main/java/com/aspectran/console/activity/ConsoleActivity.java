/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.console.activity;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.console.adapter.ConsoleRequestAdapter;
import com.aspectran.console.adapter.ConsoleResponseAdapter;
import com.aspectran.console.inout.ConsoleInout;
import com.aspectran.console.inout.ConsoleTerminatedException;
import com.aspectran.console.inout.MultiWriter;
import com.aspectran.console.service.ConsoleAspectranService;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.component.expr.token.Token;
import com.aspectran.core.component.expr.token.TokenParser;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class ConsoleActivity.
 *
 * @since 2016. 1. 18.
 */
public class ConsoleActivity extends CoreActivity {

    private static final Log log = LogFactory.getLog(ConsoleActivity.class);

    private final ConsoleAspectranService service;

    private final ConsoleInout consoleInout;

    private final Writer[] redirectionWriters;

    /**
     * Instantiates a new ConsoleActivity.
     *
     * @param service the console aspectran service
     * @param redirectionWriters the redirection writers
     */
    public ConsoleActivity(ConsoleAspectranService service, Writer[] redirectionWriters) {
        super(service.getActivityContext());

        this.service = service;
        this.consoleInout = service.getConsoleInout();
        this.redirectionWriters = redirectionWriters;
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
            setSessionAdapter(service.newSessionAdapter());

            RequestAdapter requestAdapter = new ConsoleRequestAdapter();
            requestAdapter.setCharacterEncoding(consoleInout.getEncoding());
            setRequestAdapter(requestAdapter);

            Writer outputWriter;
            if (redirectionWriters == null) {
                outputWriter = consoleInout.getUnclosableWriter();
            } else {
                List<Writer> writerList = new ArrayList<>(redirectionWriters.length + 1);
                writerList.add(consoleInout.getUnclosableWriter());
                Collections.addAll(writerList, redirectionWriters);
                outputWriter = new MultiWriter(writerList.toArray(new Writer[writerList.size()]));
            }

            ResponseAdapter responseAdapter = new ConsoleResponseAdapter(outputWriter);
            responseAdapter.setCharacterEncoding(consoleInout.getEncoding());
            setResponseAdapter(responseAdapter);

            super.adapt();
        } catch (Exception e) {
            throw new AdapterException("Failed to specify adapters for console service activity", e);
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
        if (service.isShowDescription()) {
            String description = getTranslet().getDescription();
            if (description != null) {
                consoleInout.writeLine(description);
                consoleInout.flush();
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

            consoleInout.setStyle("WHITE");
            consoleInout.writeLine("Required parameters:");
            consoleInout.offStyle();

            for (ItemRule itemRule : parameterItemRuleList) {
                Token[] tokens = itemRule.getAllTokens();
                if (tokens == null) {
                    tokens = new Token[] { new Token(TokenType.PARAMETER, itemRule.getName()) };
                }

                String mandatoryMarker = itemRule.isMandatory() ? " * " : "   ";
                consoleInout.setStyle("YELLOW");
                consoleInout.write(mandatoryMarker);
                consoleInout.offStyle();
                consoleInout.writeLine("%s: %s", itemRule.getName(), TokenParser.toString(tokens));
            }

            enterRequiredParameters(parameterItemRuleList);
        }
    }

    private void enterRequiredParameters(ItemRuleList parameterItemRuleList) {
        ItemRuleList missingItemRules1 = enterEachParameter(parameterItemRuleList);

        if (missingItemRules1 != null) {
            consoleInout.setStyle("YELLOW");
            consoleInout.writeLine("Please enter a value for all required parameters:");
            consoleInout.offStyle();

            ItemRuleList missingItemRules2 = enterEachParameter(missingItemRules1);

            if (missingItemRules2 != null) {
                String[] itemNames = missingItemRules2.getItemNames();
                consoleInout.setStyle("RED");
                consoleInout.writeLine("Missing required parameters:");
                consoleInout.offStyle();
                for (String name : itemNames) {
                    consoleInout.writeLine("   %s", name);
                }
                terminate();
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

            consoleInout.setStyle("WHITE");
            consoleInout.writeLine("Required attributes:");
            consoleInout.offStyle();

            for (ItemRule itemRule : attributeItemRuleList) {
                Token[] tokens = itemRule.getAllTokens();
                if (tokens == null) {
                    tokens = new Token[] { new Token(TokenType.PARAMETER, itemRule.getName()) };
                }

                String mandatoryMarker = itemRule.isMandatory() ? " * " : "   ";
                consoleInout.setStyle("YELLOW");
                consoleInout.write(mandatoryMarker);
                consoleInout.offStyle();
                consoleInout.writeLine("%s: %s", itemRule.getName(), TokenParser.toString(tokens));
            }

            enterRequiredAttributes(attributeItemRuleList);
        }
    }

    private void enterRequiredAttributes(ItemRuleList attributeItemRuleList) {
        ItemRuleList missingItemRules1 = enterEachParameter(attributeItemRuleList);

        if (missingItemRules1 != null) {
            consoleInout.setStyle("YELLOW");
            consoleInout.writeLine("Please enter a value for all required attributes:");
            consoleInout.offStyle();

            ItemRuleList missingItemRules2 = enterEachParameter(missingItemRules1);

            if (missingItemRules2 != null) {
                String[] itemNames = missingItemRules2.getItemNames();
                consoleInout.setStyle("RED");
                consoleInout.writeLine("Missing required attributes:");
                consoleInout.offStyle();
                for (String name : itemNames) {
                    consoleInout.writeLine("   %s", name);
                }
                terminate();
            }
        }
    }

    private ItemRuleList enterEachParameter(ItemRuleList itemRuleList) {
        consoleInout.setStyle("bold");
        consoleInout.writeLine("Enter a value for each token:");
        consoleInout.offStyle();

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
                    value = consoleInout.readPassword("   %s: ", token.stringify());
                } else {
                    value = consoleInout.readLine("   %s: ", token.stringify());
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
            throw new ActivityTerminatedException();
        }

        return (missingItemRules.isEmpty() ? null : new ItemRuleList(missingItemRules));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Activity> T newActivity() {
        ConsoleActivity activity = new ConsoleActivity(service, redirectionWriters);
        activity.setIncluded(true);
        return (T)activity;
    }

}
