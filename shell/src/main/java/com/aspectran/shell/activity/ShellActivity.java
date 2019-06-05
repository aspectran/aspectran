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
package com.aspectran.shell.activity;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.MissingMandatoryAttributesException;
import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpression;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.StringOutputWriter;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.adapter.ShellRequestAdapter;
import com.aspectran.shell.adapter.ShellResponseAdapter;
import com.aspectran.shell.command.ConsoleTerminatedException;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.ShellService;

import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    private Writer outputWriter;

    private boolean simpleReading;

    /**
     * Instantiates a new ShellActivity.
     *
     * @param service the {@code ShellService} instance
     * @param console the {@code Console} instance
     */
    public ShellActivity(ShellService service, Console console) {
        super(service.getActivityContext());

        this.service = service;
        this.console = console;
    }

    public void setProcedural(boolean procedural) {
        this.procedural = procedural;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void setOutputWriter(Writer outputWriter) {
        this.outputWriter = outputWriter;
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

            if (outputWriter == null) {
                outputWriter = new StringOutputWriter();
            }

            ShellResponseAdapter responseAdapter = new ShellResponseAdapter(outputWriter);
            responseAdapter.setEncoding(console.getEncoding());
            setResponseAdapter(responseAdapter);

            super.adapt();
        } catch (Exception e) {
            throw new AdapterException("Failed to prepare adapters required for shell service activity", e);
        }
    }

    @Override
    protected void parseRequest() {
        showDescription();

        try {
            readParameters();
            parseDeclaredParameters();
        } catch (MissingMandatoryParametersException e) {
            ItemRuleList itemRuleList = e.getItemRuleList();
            console.setStyle("RED");
            console.writeLine("Required parameters are missing:");
            console.styleOff();
            for (ItemRule ir : itemRuleList) {
                console.setStyle("RED");
                console.write(" - ");
                console.setStyle("YELLOW");
                console.writeLine(ir.getName());
                console.styleOff();
            }
            terminate("Required parameters are missing");
        }

        try {
            readAttributes();
            parseDeclaredAttributes();
        } catch (MissingMandatoryAttributesException e) {
            ItemRuleList itemRuleList = e.getItemRuleList();
            console.setStyle("RED");
            console.writeLine("Required attributes are missing:");
            console.styleOff();
            for (ItemRule ir : itemRuleList) {
                console.setStyle("RED");
                console.write(" - ");
                console.setStyle("YELLOW");
                console.writeLine(ir.getName());
                console.styleOff();
            }
            terminate("Required attributes are missing");
        }
    }

    /**
     * Prints a description for the {@code Translet}.
     */
    private void showDescription() {
        if (service.isVerbose()) {
            String description = getTranslet().getDescription();
            if (description != null) {
                console.writeLine(description);
            }
        }
    }

    private boolean isSimpleItemRules(ItemRuleList itemRuleList) {
        for (ItemRule itemRule : itemRuleList) {
            if (itemRule.getType() != ItemType.SINGLE) {
                return false;
            }
            Token[] tokens = itemRule.getAllTokens();
            if (tokens != null && tokens.length > 0) {
                if (tokens.length == 1) {
                    Token t = tokens[0];
                    if (t.getType() != TokenType.TEXT) {
                        if (t.getType() != TokenType.PARAMETER ||
                                !itemRule.getName().equals(t.getName())) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private void determineSimpleReading(ItemRuleList itemRuleList) {
        simpleReading = isSimpleItemRules(itemRuleList);
    }

    /**
     * Read required input parameters.
     */
    private void readParameters() {
        ItemRuleMap itemRuleMap = getRequestRule().getParameterItemRuleMap();
        if (itemRuleMap != null && !itemRuleMap.isEmpty()) {
            ItemRuleList itemRuleList = new ItemRuleList(itemRuleMap.values());
            determineSimpleReading(itemRuleList);
            if (procedural) {
                console.setStyle("GREEN");
                console.writeLine("Required parameters:");
                console.styleOff();
                if (!simpleReading) {
                    writeItems(itemRuleList, TokenType.PARAMETER);
                }
            }
            readRequiredParameters(itemRuleList);
        }
    }

    private void readRequiredParameters(ItemRuleList itemRuleList) {
        ItemRuleList missingItemRules;
        if (procedural) {
            if (simpleReading) {
                missingItemRules = readEachParameter(itemRuleList);
            } else {
                missingItemRules = readEachToken(itemRuleList, true);
            }
        } else {
            missingItemRules = checkRequiredParameters(itemRuleList);
        }
        if (missingItemRules != null) {
            console.setStyle("YELLOW");
            console.writeLine("Missing required parameters:");
            console.styleOff();
            if (!simpleReading) {
                writeItems(missingItemRules, TokenType.PARAMETER);
            }
            ItemRuleList missingItemRules2;
            if (simpleReading) {
                missingItemRules2 = readEachParameter(missingItemRules);
            } else {
                missingItemRules2 = readEachToken(missingItemRules, true);
            }
            if (missingItemRules2 != null) {
                throw new MissingMandatoryParametersException(missingItemRules2);
            }
        }
    }

    /**
     * Read required input attributes.
     */
    private void readAttributes() {
        ItemRuleMap itemRuleMap = getRequestRule().getAttributeItemRuleMap();
        if (itemRuleMap != null && !itemRuleMap.isEmpty()) {
            ItemRuleList itemRuleList = new ItemRuleList(itemRuleMap.values());
            determineSimpleReading(itemRuleList);
            if (procedural) {
                console.setStyle("GREEN");
                console.writeLine("Required attributes:");
                console.styleOff();
                if (!simpleReading) {
                    writeItems(itemRuleList, TokenType.ATTRIBUTE);
                }
            }
            readRequiredAttributes(itemRuleList);
        }
    }

    private void readRequiredAttributes(ItemRuleList itemRuleList) {
        ItemRuleList missingItemRules;
        if (procedural) {
            if (simpleReading) {
                missingItemRules = readEachAttribute(itemRuleList);
            } else {
                missingItemRules = readEachToken(itemRuleList, false);
            }
        } else {
            missingItemRules = checkRequiredAttributes(itemRuleList);
        }
        if (missingItemRules != null) {
            console.setStyle("YELLOW");
            console.writeLine("Missing required attributes:");
            console.styleOff();
            if (!simpleReading) {
                writeItems(missingItemRules, TokenType.ATTRIBUTE);
            }
            ItemRuleList missingItemRules2;
            if (simpleReading) {
                missingItemRules2 = readEachParameter(missingItemRules);
            } else {
                missingItemRules2 = readEachToken(missingItemRules, false);
            }
            if (missingItemRules2 != null) {
                throw new MissingMandatoryAttributesException(missingItemRules2);
            }
        }
    }

    private ItemRuleList readEachParameter(ItemRuleList itemRuleList) {
        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        try {
            for (ItemRule ir : itemRuleList) {
                String value = readParameter(ir);
                if (StringUtils.hasLength(value)) {
                    getRequestAdapter().setParameter(ir.getName(), value);
                } else if (ir.isMandatory()) {
                    missingItemRules.add(ir);
                }
            }
        } catch (ConsoleTerminatedException e) {
            log.info("User interrupt occurred");
            terminate("User interrupt occurred");
        }
        return (missingItemRules.isEmpty() ? null : new ItemRuleList(missingItemRules));
    }

    private ItemRuleList readEachAttribute(ItemRuleList itemRuleList) {
        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        try {
            for (ItemRule ir : itemRuleList) {
                String value = readParameter(ir);
                if (StringUtils.hasLength(value)) {
                    getRequestAdapter().setAttribute(ir.getName(), value);
                } else if (ir.isMandatory()) {
                    missingItemRules.add(ir);
                }
            }
        } catch (ConsoleTerminatedException e) {
            log.info("User interrupt occurred");
            terminate("User interrupt occurred");
        }
        return (missingItemRules.isEmpty() ? null : new ItemRuleList(missingItemRules));
    }

    private String readParameter(ItemRule itemRule) {
        console.clearPrompt();
        console.setStyle("YELLOW");
        console.appendPrompt(getMandatoryMarker(itemRule.isMandatory()));
        console.styleOff();
        console.setStyle("bold");
        console.appendPrompt(itemRule.getName());
        console.styleOff();
        console.appendPrompt(": ");

        String defaultValue = null;
        Token[] tokens = itemRule.getAllTokens();
        if (tokens != null && tokens.length == 1) {
            Token token = tokens[0];
            if (token.getType() == TokenType.TEXT) {
                defaultValue = token.getDefaultValue();
            }
        }

        if (itemRule.isSecret()) {
            return console.readPassword(null, defaultValue);
        } else {
            return console.readLine(null, defaultValue);
        }
    }

    private ItemRuleList readEachToken(ItemRuleList itemRuleList, boolean forParameters) {
        console.setStyle("GREEN");
        console.writeLine("Enter a value for each token:");
        console.styleOff();

        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        try {
            Map<Token, Set<ItemRule>> valueTokens = new LinkedHashMap<>();
            for (ItemRule itemRule : itemRuleList) {
                Token[] tokens = itemRule.getAllTokens();
                if (forParameters) {
                    if (tokens == null || tokens.length == 0) {
                        Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                        tokens = new Token[] { t };
                    } else if (tokens.length == 1 && tokens[0].getType() == TokenType.TEXT) {
                        Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                        t.setDefaultValue(tokens[0].getDefaultValue());
                        tokens = new Token[] { t };
                    }
                }
                if (tokens != null) {
                    for (Token t1 : tokens) {
                        if (t1.getType() == TokenType.PARAMETER) {
                            boolean exists = false;
                            for (Token t2 : valueTokens.keySet()) {
                                if (t2.equals(t1)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (exists) {
                                Set<ItemRule> rules = valueTokens.get(t1);
                                rules.add(itemRule);
                            } else {
                                Set<ItemRule> rules = new LinkedHashSet<>();
                                rules.add(itemRule);
                                valueTokens.put(t1, rules);
                            }
                        }
                    }
                }
            }
            for (Map.Entry<Token, Set<ItemRule>> entry : valueTokens.entrySet()) {
                Token token = entry.getKey();
                Set<ItemRule> itemRules = entry.getValue();
                boolean secret = hasSecretItem(itemRules);
                if (!forParameters) {
                    String value = getRequestAdapter().getParameter(token.getName());
                    if (value != null) {
                        if (secret) {
                            value = StringUtils.repeat(Console.MASK_CHAR, value.length());
                        }
                        console.write("   ");
                        writeToken(token);
                        console.write(": ");
                        console.writeLine(value);
                        continue;
                    }
                }
                String defaultValue = token.getDefaultValue();
                console.clearPrompt();
                console.appendPrompt("   ");
                console.setStyle("CYAN");
                console.appendPrompt(String.valueOf(Token.PARAMETER_SYMBOL));
                console.appendPrompt(String.valueOf(Token.BRACKET_OPEN));
                console.styleOff();
                console.appendPrompt(token.getName());
                console.setStyle("CYAN");
                console.appendPrompt(String.valueOf(Token.BRACKET_CLOSE));
                console.styleOff();
                console.appendPrompt(": ");
                String line;
                if (secret) {
                    line = console.readPassword(null, defaultValue);
                } else {
                    line = console.readLine(null, defaultValue);
                }
                if (StringUtils.hasLength(line)) {
                    getRequestAdapter().setParameter(token.getName(), line);
                } else {
                    for (ItemRule ir : itemRules) {
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

    private boolean hasSecretItem(Collection<ItemRule> itemRules) {
        boolean secret = false;
        for (ItemRule ir : itemRules) {
            if (ir.isSecret()) {
                secret = true;
                break;
            }
        }
        return secret;
    }

    private String getMandatoryMarker(boolean mandatory) {
        return (mandatory ? " * " : "   ");
    }

    private void writeItems(ItemRuleList itemRuleList, TokenType tokenType) {
        for (ItemRule itemRule : itemRuleList) {
            if (simpleReading) {
                writeItem(itemRule, null);
            } else {
                Token[] tokens = itemRule.getAllTokens();
                if (tokens == null) {
                    Token t = new Token(tokenType, itemRule.getName());
                    tokens = new Token[] { t };
                }
                writeItem(itemRule, tokens);
            }
        }
    }

    private void writeItem(ItemRule itemRule, Token[] tokens) {
        console.setStyle("YELLOW");
        console.write(getMandatoryMarker(itemRule.isMandatory()));
        console.styleOff();
        console.setStyle("bold");
        console.write(itemRule.getName());
        console.styleOff();
        if (tokens != null && tokens.length > 0) {
            console.write(": ");
            for (Token token : tokens) {
                writeToken(token);
            }
        }
        console.writeLine();
    }

    private void writeToken(Token token) {
        if (token.getType() == TokenType.TEXT) {
            console.write(token.stringify());
        } else {
            String str = token.stringify();
            console.setStyle("CYAN");
            console.write(str.substring(0, 2));
            console.styleOff();
            console.write(str.substring(2, str.length() - 1));
            console.setStyle("CYAN");
            console.write(str.substring(str.length() - 1));
            console.styleOff();
        }
    }

    private ItemRuleList checkRequiredParameters(ItemRuleList itemRuleList) {
        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        ItemEvaluator evaluator = new ItemExpression(this);
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
        ItemEvaluator evaluator = new ItemExpression(this);
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
        ShellActivity activity = new ShellActivity(service, console);
        activity.setOutputWriter(outputWriter);
        activity.setIncluded(true);
        return (T)activity;
    }

}
