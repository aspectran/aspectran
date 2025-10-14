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

/**
 * Manages the pre-execution procedure for a translet in an interactive shell.
 * <p>This class is responsible for checking for mandatory parameters and, if running
 * in procedural mode, interactively prompting the user to enter values for them
 * before the translet is executed. It helps ensure that a translet receives all
 * its required inputs when invoked from the shell.</p>
 *
 * <p>Created: 2017. 11. 10.</p>
 */
public class TransletPreProcedure {

    private final ShellConsole console;

    private final TransletRule transletRule;

    private final ParameterMap parameterMap;

    private final boolean procedural;

    private boolean readSimply;

    /**
     * Instantiates a new TransletPreProcedure.
     * @param console the shell console
     * @param transletRule the rule of the translet to be executed
     * @param parameterMap the parameter map to be populated
     * @param procedural {@code true} to enable interactive prompting for parameters;
     *      {@code false} to only check for missing mandatory parameters
     */
    public TransletPreProcedure(@NonNull ShellConsole console, @NonNull TransletRule transletRule,
                                @NonNull ParameterMap parameterMap, boolean procedural) {
        this.console = console;
        this.transletRule = transletRule;
        this.parameterMap = parameterMap;
        this.procedural = procedural;
    }

    /**
     * Executes the pre-procedure.
     * @throws MissingMandatoryParametersException if mandatory parameters are still missing after the procedure
     */
    public void proceed() throws MissingMandatoryParametersException {
        determineSimpleReading();
        if (procedural) {
            printRequiredParameters();
            printRequiredAttributes();
        }
        readRequiredParameters();
    }

    /**
     * Determines whether to use a simple one-by-one parameter prompt or a more
     * complex token-based prompt.
     */
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

    /**
     * Checks if all item rules are simple enough for single-line reading.
     * @param itemRules the collection of item rules
     * @return {@code true} if all items are simple, {@code false} otherwise
     */
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

    /**
     * Prints a description to the console with info styling.
     * @param description the description to print
     */
    private void printDescription(String description) {
        if (description != null) {
            console.getStyler().infoStyle();
            console.writeLine(description);
            console.getStyler().resetStyle();
        }
    }

    /**
     * Renders and prints the translet's description if verbose mode is active.
     * @param activity the current shell activity
     */
    public void printDescription(@NonNull ShellActivity activity) {
        if (activity.isVerbose()) {
            DescriptionRule descriptionRule = transletRule.getDescriptionRule();
            if (descriptionRule != null) {
                printDescription(DescriptionRule.render(descriptionRule, activity));
            }
        }
    }

    /**
     * Prints the list of required parameters.
     */
    private void printRequiredParameters() {
        ItemRuleMap parameterItemRuleMap = transletRule.getRequestRule().getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            console.getStyler().secondaryStyle();
            console.writeLine("Required parameters:");
            console.getStyler().resetStyle();
            if (!readSimply) {
                writeItems(parameterItemRuleMap.values(), TokenType.PARAMETER);
            }
        }
    }

    /**
     * Prints the list of required attributes.
     */
    private void printRequiredAttributes() {
        if (!readSimply) {
            ItemRuleMap attributeItemRuleMap = transletRule.getRequestRule().getAttributeItemRuleMap();
            if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
                console.getStyler().secondaryStyle();
                console.writeLine("Required attributes:");
                console.getStyler().resetStyle();
                writeItems(attributeItemRuleMap.values(), TokenType.ATTRIBUTE);
            }
        }
    }

    /**
     * Prints a message indicating that some mandatory parameters are missing.
     * @param itemRules the collection of missing mandatory item rules
     */
    public void printSomeMandatoryParametersMissing(Collection<ItemRule> itemRules) {
        if (itemRules != null && !itemRules.isEmpty()) {
            console.getStyler().dangerStyle();
            console.writeLine("Some mandatory parameters are missing:");
            for (ItemRule ir : itemRules) {
                console.getStyler().warningStyle();
                console.write(" * ");
                console.getStyler().resetStyle("bold");
                console.writeLine(ir.getName());
            }
            console.getStyler().resetStyle();
        }
    }

    /**
     * Prints a message indicating that some mandatory attributes are missing.
     * @param itemRules the collection of missing mandatory item rules
     */
    public void printSomeMandatoryAttributesMissing(Collection<ItemRule> itemRules) {
        if (itemRules != null && !itemRules.isEmpty()) {
            console.getStyler().dangerStyle();
            console.writeLine("Some mandatory attributes are missing:");
            for (ItemRule ir : itemRules) {
                console.getStyler().warningStyle();
                console.write(" * ");
                console.getStyler().resetStyle("bold");
                console.writeLine(ir.getName());
            }
            console.getStyler().resetStyle();
        }
    }

    /**
     * Reads all required parameters, prompting the user if in procedural mode.
     * @throws MissingMandatoryParametersException if mandatory parameters are missing
     */
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
            console.getStyler().warningStyle();
            console.writeLine("Missing mandatory parameters:");
            console.getStyler().resetStyle();
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

    /**
     * Interactively reads each parameter from the user.
     * @param itemRules the item rules for the parameters to read
     * @return a collection of mandatory item rules that are still missing, or {@code null}
     */
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

    /**
     * Prompts the user and reads a single parameter value.
     * @param itemRule the item rule for the parameter
     * @return the value entered by the user
     */
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

    /**
     * Interactively reads a value for each distinct parameter token.
     * @param itemRules the item rules for the parameters
     * @return a collection of mandatory item rules that are still missing, or {@code null}
     */
    @Nullable
    private Collection<ItemRule> readEachToken(@NonNull Collection<ItemRule> itemRules) {
        console.getStyler().secondaryStyle();
        console.writeLine("Enter a value for each token:");
        console.getStyler().resetStyle();

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

    /**
     * Checks if any of the item rules are for a secret (password) value.
     * @param itemRules the collection of item rules
     * @return {@code true} if a secret item is found, {@code false} otherwise
     */
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

    /**
     * Returns a marker string for mandatory items.
     * @param mandatory whether the item is mandatory
     * @return an asterisk for mandatory items, spaces otherwise
     */
    @NonNull
    private String getMandatoryMarker(boolean mandatory) {
        return (mandatory ? " * " : "   ");
    }

    /**
     * Writes a formatted list of items to the console.
     * @param itemRules the item rules to write
     * @param tokenType the token type
     */
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

    /**
     * Writes a single formatted item to the console.
     * @param itemRule the item rule to write
     * @param tokens the tokens associated with the item
     */
    private void writeItem(@NonNull ItemRule itemRule, Token[] tokens) {
        console.getStyler().warningStyle();
        console.write(getMandatoryMarker(itemRule.isMandatory()));
        console.getStyler().resetStyle("bold");
        console.write(itemRule.getName());
        console.getStyler().resetStyle();
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

    /**
     * Writes a single token to the console with appropriate styling.
     * @param token the token to write
     */
    private void writeToken(@NonNull Token token) {
        if (token.getType() == TokenType.TEXT) {
            console.write(token.stringify());
        } else {
            String str = token.stringify();
            console.getStyler().infoStyle();
            console.write(str.substring(0, 2));
            console.getStyler().resetStyle("bold");
            console.write(str.substring(2, str.length() - 1));
            console.getStyler().infoStyle();
            console.write(str.substring(str.length() - 1));
            console.getStyler().resetStyle();
        }
    }

    /**
     * Checks for missing mandatory parameters without prompting the user.
     * @param itemRules the item rules to check
     * @return a collection of mandatory item rules that are missing, or {@code null}
     */
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
