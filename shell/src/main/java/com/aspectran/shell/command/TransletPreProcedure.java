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
package com.aspectran.shell.command;

import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.shell.activity.ShellActivity;
import com.aspectran.shell.console.PromptStringBuilder;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.aspectran.shell.console.ShellConsole.MASK_CHAR;

public class TransletPreProcedure {

    private final ShellConsole console;

    private final TransletRule transletRule;

    private final ParameterMap parameterMap;

    private final boolean procedural;

    private boolean readSimply;

    public TransletPreProcedure(@NonNull ShellConsole console, @NonNull TransletRule transletRule,
                                @NonNull ParameterMap parameterMap, boolean procedural) {
        this.console = console;
        this.transletRule = transletRule;
        this.parameterMap = parameterMap;
        this.procedural = procedural;
    }

    public void proceed() throws MissingMandatoryParametersException {
        determineSimpleReading();
        if (procedural) {
            printRequiredParameters();
            printRequiredAttributes();
        }
        readRequiredParameters();
    }

    private void determineSimpleReading() {
        ItemRuleMap attributeItemRuleMap = transletRule.getRequestRule().getAttributeItemRuleMap();
        if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
            readSimply = false;
        } else {
            ItemRuleMap parameterItemRuleMap = transletRule.getRequestRule().getParameterItemRuleMap();
            if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
                readSimply = isSimpleItemRules(parameterItemRuleMap.values());
            } else {
                readSimply = true;
            }
        }
    }

    private boolean isSimpleItemRules(@NonNull Collection<ItemRule> itemRules) {
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

    private void printDescription(String description) {
        if (description != null) {
            console.infoStyle();
            console.writeLine(description);
            console.resetStyle();
        }
    }

    public void printDescription(@NonNull ShellActivity activity) {
        if (activity.isVerbose()) {
            DescriptionRule descriptionRule = transletRule.getDescriptionRule();
            if (descriptionRule != null) {
                printDescription(DescriptionRule.render(descriptionRule, activity));
            }
        }
    }

    private void printRequiredParameters() {
        ItemRuleMap parameterItemRuleMap = transletRule.getRequestRule().getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            console.secondaryStyle();
            console.writeLine("Required parameters:");
            console.resetStyle();
            if (!readSimply) {
                writeItems(parameterItemRuleMap.values(), TokenType.PARAMETER);
            }
        }
    }

    private void printRequiredAttributes() {
        if (!readSimply) {
            ItemRuleMap attributeItemRuleMap = transletRule.getRequestRule().getAttributeItemRuleMap();
            if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
                console.secondaryStyle();
                console.writeLine("Required attributes:");
                console.resetStyle();
                writeItems(attributeItemRuleMap.values(), TokenType.ATTRIBUTE);
            }
        }
    }

    public void printSomeMandatoryParametersMissing(Collection<ItemRule> itemRules) {
        if (itemRules != null && !itemRules.isEmpty()) {
            console.dangerStyle();
            console.writeLine("Some mandatory parameters are missing:");
            for (ItemRule ir : itemRules) {
                console.warningStyle();
                console.write(" * ");
                console.resetStyle("bold");
                console.writeLine(ir.getName());
            }
            console.resetStyle();
        }
    }

    public void printSomeMandatoryAttributesMissing(Collection<ItemRule> itemRules) {
        if (itemRules != null && !itemRules.isEmpty()) {
            console.dangerStyle();
            console.writeLine("Some mandatory attributes are missing:");
            for (ItemRule ir : itemRules) {
                console.warningStyle();
                console.write(" * ");
                console.resetStyle("bold");
                console.writeLine(ir.getName());
            }
            console.resetStyle();
        }
    }

    private void readRequiredParameters() throws MissingMandatoryParametersException {
        Collection<ItemRule> itemRules;
        ItemRuleMap parameterItemRuleMap = transletRule.getRequestRule().getParameterItemRuleMap();
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
            console.warningStyle();
            console.writeLine("Missing mandatory parameters:");
            console.resetStyle();
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

    @Nullable
    private Collection<ItemRule> readEachParameter(@NonNull Collection<ItemRule> itemRules) {
        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        for (ItemRule ir : itemRules) {
            if (!ir.hasOnlyFixedValue()) {
                String value = readParameter(ir);
                if (StringUtils.hasLength(value)) {
                    parameterMap.setParameter(ir.getName(), value);
                } else if (ir.isMandatory()) {
                    missingItemRules.add(ir);
                }
            }
        }
        return (missingItemRules.isEmpty() ? null : missingItemRules);
    }

    private String readParameter(@NonNull ItemRule itemRule) {
        PromptStringBuilder psb = console.newPromptStringBuilder()
                .warningStyle()
                .append(getMandatoryMarker(itemRule.isMandatory()))
                .resetStyle("bold")
                .append(itemRule.getName())
                .resetStyle()
                .append(": ");

        Token[] tokens = itemRule.getAllTokens();
        if (tokens != null && tokens.length == 1) {
            Token token = tokens[0];
            if (token.getType() == TokenType.TEXT) {
                psb.defaultValue(token.getDefaultValue());
            } else if (token.getType() == TokenType.PARAMETER &&
                    token.getName().equals(itemRule.getName())) {
                psb.defaultValue(token.getDefaultValue());
            }
        }

        if (itemRule.isSecret()) {
            return console.readPassword(psb);
        } else {
            return console.readLine(psb);
        }
    }

    @Nullable
    private Collection<ItemRule> readEachToken(@NonNull Collection<ItemRule> itemRules) {
        console.secondaryStyle();
        console.writeLine("Enter a value for each token:");
        console.resetStyle();

        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        Map<Token, Set<ItemRule>> valueTokens = new LinkedHashMap<>();
        for (ItemRule itemRule : itemRules) {
            if (!itemRule.hasOnlyFixedValue()) {
                Token[] tokens = itemRule.getAllTokens();
                if (tokens == null || tokens.length == 0) {
                    Token t = new Token(TokenType.PARAMETER, itemRule.getName());
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
        }
        for (Map.Entry<Token, Set<ItemRule>> entry : valueTokens.entrySet()) {
            Token token = entry.getKey();
            Set<ItemRule> rules = entry.getValue();
            boolean secret = hasSecretItem(rules);
            PromptStringBuilder psb = console.newPromptStringBuilder()
                    .append("   ")
                    .infoStyle()
                    .append(String.valueOf(Token.PARAMETER_SYMBOL))
                    .append(String.valueOf(Token.BRACKET_OPEN))
                    .resetStyle("bold")
                    .append(token.getName())
                    .infoStyle()
                    .append(String.valueOf(Token.BRACKET_CLOSE))
                    .resetStyle()
                    .append(": ")
                    .defaultValue(token.getDefaultValue());
            String line;
            if (secret) {
                line = console.readPassword(psb);
            } else {
                line = console.readLine(psb);
            }
            if (StringUtils.hasLength(line)) {
                parameterMap.setParameter(token.getName(), line);
            } else {
                for (ItemRule ir : rules) {
                    if (ir.isMandatory()) {
                        missingItemRules.add(ir);
                    }
                }
            }
        }
        return (missingItemRules.isEmpty() ? null : missingItemRules);
    }

    private boolean hasSecretItem(@NonNull Collection<ItemRule> itemRules) {
        boolean secret = false;
        for (ItemRule ir : itemRules) {
            if (ir.isSecret()) {
                secret = true;
                break;
            }
        }
        return secret;
    }

    @NonNull
    private String getMandatoryMarker(boolean mandatory) {
        return (mandatory ? " * " : "   ");
    }

    private void writeItems(@NonNull Collection<ItemRule> itemRules, TokenType tokenType) {
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

    private void writeItem(@NonNull ItemRule itemRule, Token[] tokens) {
        console.warningStyle();
        console.write(getMandatoryMarker(itemRule.isMandatory()));
        console.resetStyle("bold");
        console.write(itemRule.getName());
        console.resetStyle();
        if (tokens != null && tokens.length > 0) {
            console.write(": ");
            if (itemRule.isSecret()) {
                console.write(StringUtils.repeat(MASK_CHAR, 8));
            } else {
                for (Token token : tokens) {
                    writeToken(token);
                }
            }
        }
        console.writeLine();
    }

    private void writeToken(@NonNull Token token) {
        if (token.getType() == TokenType.TEXT) {
            console.write(token.stringify());
        } else {
            String str = token.stringify();
            console.infoStyle();
            console.write(str.substring(0, 2));
            console.resetStyle("bold");
            console.write(str.substring(2, str.length() - 1));
            console.infoStyle();
            console.write(str.substring(str.length() - 1));
            console.resetStyle();
        }
    }

    @Nullable
    private Collection<ItemRule> checkRequiredParameters(@NonNull Collection<ItemRule> itemRules) {
        Set<ItemRule> missingItemRules = new LinkedHashSet<>();
        for (ItemRule itemRule : itemRules) {
            String[] values = parameterMap.getParameterValues(itemRule.getName());
            if (values == null || values.length == 0) {
                missingItemRules.add(itemRule);
            }
        }
        return (missingItemRules.isEmpty() ? null : missingItemRules);
    }

}
