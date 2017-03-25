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
import java.util.List;

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
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.StringUtils;
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
        setSessionAdapter(service.getSessionAdapter());

        this.service = service;
        this.consoleInout = service.getConsoleInout();
        this.redirectionWriters = redirectionWriters;
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
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
        } catch (Exception e) {
            throw new AdapterException("Could not adapt to console application activity.", e);
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

            consoleInout.writeLine("Required parameters:");

            for (ItemRule itemRule : parameterItemRuleList) {
                Token[] tokens = itemRule.getTokens();
                if (tokens == null) {
                    tokens = new Token[] { new Token(TokenType.PARAMETER, itemRule.getName()) };
                }

                String mandatoryMarker = itemRule.isMandatory() ? "*" : " ";
                consoleInout.writeLine(" %s %s: %s", mandatoryMarker, itemRule.getName(), TokenParser.toString(tokens));
            }

            enterRequiredParameters(parameterItemRuleList);
        }
    }

    private void enterRequiredParameters(ItemRuleList parameterItemRuleList) {
        ItemRuleList missingItemRules1 = enterEachParameter(parameterItemRuleList);

        if (missingItemRules1 != null) {
            consoleInout.writeLine("Please enter a value for all required parameters:");

            ItemRuleList missingItemRules2 = enterEachParameter(missingItemRules1);

            if (missingItemRules2 != null && missingItemRules1.size() == missingItemRules2.size()) {
                String[] itemNames = missingItemRules2.getItemNames();
                String missingParamNames = StringUtils.joinCommaDelimitedList(itemNames);
                consoleInout.writeLine("Missing required parameters: %s", missingParamNames);

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

            consoleInout.writeLine("Required attributes:");

            for (ItemRule itemRule : attributeItemRuleList) {
                Token[] tokens = itemRule.getTokens();
                if (tokens == null) {
                    tokens = new Token[] { new Token(TokenType.PARAMETER, itemRule.getName()) };
                }

                String mandatoryMarker = itemRule.isMandatory() ? "*" : " ";
                consoleInout.writeLine(" %s %s: %s", mandatoryMarker, itemRule.getName(), TokenParser.toString(tokens));
            }

            enterRequiredAttributes(attributeItemRuleList);
        }
    }

    private void enterRequiredAttributes(ItemRuleList attributeItemRuleList) {
        ItemRuleList missingItemRules1 = enterEachParameter(attributeItemRuleList);

        if (missingItemRules1 != null) {
            consoleInout.writeLine("Please enter a value for all required attributes:");

            ItemRuleList missingItemRules2 = enterEachParameter(missingItemRules1);

            if (missingItemRules2 != null && missingItemRules1.size() == missingItemRules2.size()) {
                String[] itemNames = missingItemRules2.getItemNames();
                String missingParamNames = StringUtils.joinCommaDelimitedList(itemNames);
                consoleInout.writeLine("Missing required attributes: %s", missingParamNames);

                terminate();
            }
        }
    }

    private ItemRuleList enterEachParameter(ItemRuleList itemRuleList) {
        consoleInout.writeLine("Enter a value for each token:");

        ItemRuleList missingItemRules = new ItemRuleList(itemRuleList.size());

        try {
            for (ItemRule itemRule : itemRuleList) {
                Token[] tokens = itemRule.getTokens();
                if (tokens == null || tokens.length == 0) {
                    Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                    tokens = new Token[] { t };
                }

                int inputCount = 0;

                for (Token token : tokens) {
                    if (token.getType() == TokenType.PARAMETER) {
                        String value;
                        if (itemRule.isSecurity()) {
                            value = consoleInout.readPassword("   %s: ", token.stringify());
                        } else {
                            value = consoleInout.readLine("   %s: ", token.stringify());
                        }
                        if (value != null && !value.isEmpty()) {
                            getRequestAdapter().setParameter(token.getName(), value);
                            inputCount++;
                        } else if (token.getValue() != null) {
                            getRequestAdapter().setParameter(token.getName(), token.getValue());
                            inputCount++;
                        }
                    }
                }

                if (itemRule.isMandatory() && inputCount == 0) {
                    missingItemRules.add(itemRule);
                }
            }
        } catch (ConsoleTerminatedException e) {
            log.info("User interrupt occurred.");
            throw new ActivityTerminatedException();
        }

        return (missingItemRules.isEmpty() ? null : missingItemRules);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Activity> T newActivity() {
        ConsoleActivity activity = new ConsoleActivity(service, redirectionWriters);
        activity.setIncluded(true);
        return (T)activity;
    }

}
