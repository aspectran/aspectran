/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.MissingMandatoryAttributesException;
import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.OutputStringWriter;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.shell.adapter.ShellRequestAdapter;
import com.aspectran.shell.adapter.ShellResponseAdapter;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.console.ShellConsoleClosedException;
import com.aspectran.shell.service.ShellService;

import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * An activity that processes a shell command.
 *
 * @since 2016. 1. 18.
 */
public class ShellActivity extends CoreActivity {

    private static final Logger logger = LoggerFactory.getLogger(ShellActivity.class);

    private final ShellService shellService;

    private final ShellConsole console;

    private boolean procedural;

    private ParameterMap parameterMap;

    private Writer outputWriter;

    private boolean readSimply;

    /**
     * Instantiates a new ShellActivity.
     * @param shellService the {@code ShellService} instance
     * @param console the {@code Console} instance
     */
    public ShellActivity(ShellService shellService, ShellConsole console) {
        super(shellService.getActivityContext());

        this.shellService = shellService;
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
            setSessionAdapter(shellService.newSessionAdapter());

            ShellRequestAdapter requestAdapter = new ShellRequestAdapter(getTranslet().getRequestMethod());
            requestAdapter.setEncoding(console.getEncoding());
            setRequestAdapter(requestAdapter);

            if (outputWriter == null) {
                outputWriter = new OutputStringWriter();
            }
            ShellResponseAdapter responseAdapter = new ShellResponseAdapter(outputWriter);
            responseAdapter.setEncoding(console.getEncoding());
            setResponseAdapter(responseAdapter);
        } catch (Exception e) {
            throw new AdapterException("Failed to adapt for Shell Activity", e);
        }

        if (getParentActivity() == null && getSessionAdapter() instanceof DefaultSessionAdapter) {
            ((DefaultSessionAdapter)getSessionAdapter()).getSessionAgent().access();
        }

        super.adapt();
    }

    @Override
    protected void parseRequest() throws ActivityTerminatedException, RequestParseException {
        if (parameterMap != null) {
            ((ShellRequestAdapter)getRequestAdapter()).setParameterMap(parameterMap);
        }

        if (procedural) {
            showDescription();
        }

        try {
            determineSimpleReading();
            if (procedural) {
                printRequiredParameters();
                printRequiredAttributes();
            }
            readRequiredParameters();
            parseDeclaredParameters();
        } catch (MissingMandatoryParametersException e) {
            Collection<ItemRule> itemRules = e.getItemRules();
            console.setStyle("RED");
            console.writeLine("Required parameters are missing:");
            console.clearStyle();
            for (ItemRule ir : itemRules) {
                console.setStyle("RED");
                console.write(" - ");
                console.setStyle("YELLOW");
                console.writeLine(ir.getName());
                console.clearStyle();
            }
            terminate("Required parameters are missing");
        }

        try {
            parseDeclaredAttributes();
        } catch (MissingMandatoryAttributesException e) {
            Collection<ItemRule> itemRules = e.getItemRules();
            console.setStyle("RED");
            console.writeLine("Required attributes are missing:");
            console.clearStyle();
            for (ItemRule ir : itemRules) {
                console.setStyle("RED");
                console.write(" - ");
                console.setStyle("YELLOW");
                console.writeLine(ir.getName());
                console.clearStyle();
            }
            terminate("Required attributes are missing");
        }

        super.parseRequest();
    }

    @Override
    protected void release() {
        if (getParentActivity() == null && getSessionAdapter() instanceof DefaultSessionAdapter) {
            ((DefaultSessionAdapter)getSessionAdapter()).getSessionAgent().complete();
        }

        super.release();
    }

    /**
     * Prints a description for the {@code Translet}.
     */
    private void showDescription() {
        if (shellService.isVerbose()) {
            String description = getTranslet().getDescription();
            if (description != null) {
                console.writeLine(description);
            }
        }
    }

    private void determineSimpleReading() {
        ItemRuleMap attributeItemRuleMap = getRequestRule().getAttributeItemRuleMap();
        if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
            readSimply = false;
        } else {
            ItemRuleMap parameterItemRuleMap = getRequestRule().getParameterItemRuleMap();
            if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
                readSimply = isSimpleItemRules(parameterItemRuleMap.values());
            } else {
                readSimply = true;
            }
        }
    }

    private boolean isSimpleItemRules(Collection<ItemRule> itemRules) {
        for (ItemRule itemRule : itemRules) {
            if (itemRule.getType() != ItemType.SINGLE) {
                return false;
            }
            Token[] tokens = itemRule.getAllTokens();
            if (tokens != null && tokens.length > 0) {
                if (tokens.length == 1) {
                    Token token = tokens[0];
                    if (token.getType() != TokenType.TEXT) {
                        if (token.getType() != TokenType.PARAMETER ||
                                !token.getName().equals(itemRule.getName())) {
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

    private void printRequiredParameters() {
        ItemRuleMap parameterItemRuleMap = getRequestRule().getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            console.setStyle("GREEN");
            console.writeLine("Required parameters:");
            console.clearStyle();
            if (!readSimply) {
                writeItems(parameterItemRuleMap.values(), TokenType.PARAMETER);
            }
        }
    }

    private void printRequiredAttributes() {
        if (!readSimply) {
            ItemRuleMap attributeItemRuleMap = getRequestRule().getAttributeItemRuleMap();
            if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
                console.setStyle("GREEN");
                console.writeLine("Required attributes:");
                console.clearStyle();
                writeItems(attributeItemRuleMap.values(), TokenType.ATTRIBUTE);
            }
        }
    }

    private void readRequiredParameters() throws ActivityTerminatedException, MissingMandatoryParametersException {
        Collection<ItemRule> itemRules;
        ItemRuleMap parameterItemRuleMap = getRequestRule().getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            itemRules = parameterItemRuleMap.values();
        } else {
            return;
        }
        Collection<ItemRule> missingItemRules;
        if (procedural) {
            if (readSimply) {
                missingItemRules = readEachParameter(itemRules);
            } else {
                missingItemRules = readEachToken(itemRules);
            }
        } else {
            missingItemRules = checkRequiredParameters(itemRules);
        }
        if (missingItemRules != null) {
            console.setStyle("YELLOW");
            console.writeLine("Missing required parameters:");
            console.clearStyle();
            if (!readSimply) {
                writeItems(missingItemRules, TokenType.PARAMETER);
            }
            Collection<ItemRule> missingItemRules2;
            if (readSimply) {
                missingItemRules2 = readEachParameter(missingItemRules);
            } else {
                missingItemRules2 = readEachToken(missingItemRules);
            }
            if (missingItemRules2 != null) {
                throw new MissingMandatoryParametersException(missingItemRules2);
            }
        }
    }

    private Collection<ItemRule> readEachParameter(Collection<ItemRule> itemRules)
            throws ActivityTerminatedException {
        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        try {
            for (ItemRule ir : itemRules) {
                String value = readParameter(ir);
                if (StringUtils.hasLength(value)) {
                    getRequestAdapter().setParameter(ir.getName(), value);
                } else if (ir.isMandatory()) {
                    missingItemRules.add(ir);
                }
            }
        } catch (ShellConsoleClosedException e) {
            logger.info("User interrupt occurred");
            terminate("User interrupt occurred");
        }
        return (missingItemRules.isEmpty() ? null : missingItemRules);
    }

    private String readParameter(ItemRule itemRule) {
        console.clearPrompt();
        console.setStyle("YELLOW");
        console.appendPrompt(getMandatoryMarker(itemRule.isMandatory()));
        console.clearStyle();
        console.setStyle("bold");
        console.appendPrompt(itemRule.getName());
        console.clearStyle();
        console.appendPrompt(": ");

        String defaultValue = null;
        Token[] tokens = itemRule.getAllTokens();
        if (tokens != null && tokens.length == 1) {
            Token token = tokens[0];
            if (token.getType() == TokenType.TEXT) {
                defaultValue = token.getDefaultValue();
            } else if (token.getType() == TokenType.PARAMETER &&
                    token.getName().equals(itemRule.getName())) {
                defaultValue = token.getDefaultValue();
            }
        }

        if (itemRule.isSecret()) {
            return console.readPassword(null, defaultValue);
        } else {
            return console.readLine(null, defaultValue);
        }
    }

    private Collection<ItemRule> readEachToken(Collection<ItemRule> itemRules) throws ActivityTerminatedException {
        console.setStyle("GREEN");
        console.writeLine("Enter a value for each token:");
        console.clearStyle();

        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        try {
            Map<Token, Set<ItemRule>> valueTokens = new LinkedHashMap<>();
            for (ItemRule itemRule : itemRules) {
                Token[] tokens = itemRule.getAllTokens();
                if (tokens == null || tokens.length == 0) {
                    Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                    tokens = new Token[] { t };
                } else if (tokens.length == 1 && tokens[0].getType() == TokenType.TEXT) {
                    Token t = new Token(TokenType.PARAMETER, itemRule.getName());
                    t.setDefaultValue(tokens[0].getDefaultValue());
                    tokens = new Token[] { t };
                }
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
            for (Map.Entry<Token, Set<ItemRule>> entry : valueTokens.entrySet()) {
                Token token = entry.getKey();
                Set<ItemRule> itemRuleSet = entry.getValue();
                boolean secret = hasSecretItem(itemRuleSet);
                console.clearPrompt();
                console.appendPrompt("   ");
                console.setStyle("CYAN");
                console.appendPrompt(String.valueOf(Token.PARAMETER_SYMBOL));
                console.appendPrompt(String.valueOf(Token.BRACKET_OPEN));
                console.clearStyle();
                console.appendPrompt(token.getName());
                console.setStyle("CYAN");
                console.appendPrompt(String.valueOf(Token.BRACKET_CLOSE));
                console.clearStyle();
                console.appendPrompt(": ");
                String line;
                if (secret) {
                    line = console.readPassword(null, token.getDefaultValue());
                } else {
                    line = console.readLine(null, token.getDefaultValue());
                }
                if (StringUtils.hasLength(line)) {
                    getRequestAdapter().setParameter(token.getName(), line);
                } else {
                    for (ItemRule ir : itemRuleSet) {
                        if (ir.isMandatory()) {
                            missingItemRules.add(ir);
                        }
                    }
                }
            }
        } catch (ShellConsoleClosedException e) {
            logger.info("User interrupt occurred");
            terminate("User interrupt occurred");
        }
        return (missingItemRules.isEmpty() ? null : missingItemRules);
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

    private void writeItems(Collection<ItemRule> itemRules, TokenType tokenType) {
        for (ItemRule itemRule : itemRules) {
            if (readSimply) {
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
        console.clearStyle();
        console.setStyle("bold");
        console.write(itemRule.getName());
        console.clearStyle();
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
            console.clearStyle();
            console.write(str.substring(2, str.length() - 1));
            console.setStyle("CYAN");
            console.write(str.substring(str.length() - 1));
            console.clearStyle();
        }
    }

    private Collection<ItemRule> checkRequiredParameters(Collection<ItemRule> itemRules) {
        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        ItemEvaluator evaluator = new ItemEvaluation(this);
        for (ItemRule itemRule : itemRules) {
            String[] values = getRequestAdapter().getParameterValues(itemRule.getName());
            if (values == null || values.length == 0) {
                values = evaluator.evaluateAsStringArray(itemRule);
                if (values != null && values.length > 0) {
                    getRequestAdapter().setParameter(itemRule.getName(), values);
                } else {
                    missingItemRules.add(itemRule);
                }
            }
        }
        return (missingItemRules.isEmpty() ? null : missingItemRules);
    }

}
