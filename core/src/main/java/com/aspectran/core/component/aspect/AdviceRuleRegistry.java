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
package com.aspectran.core.component.aspect;

import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A registry that collects and organizes advice rules based on their type
 * (e.g., before, after, finally) and exception handling rules.
 *
 * <p>This class is used to aggregate all rules from aspects that match a
 * specific join point. It correctly handles the order of execution for advice
 * based on the aspect's order of precedence.
 * </p>
 */
public class AdviceRuleRegistry implements Replicable<AdviceRuleRegistry> {

    private List<SettingsAdviceRule> settingsAdviceRuleList;

    private List<AdviceRule> beforeAdviceRuleList;

    private List<AdviceRule> afterAdviceRuleList;

    private List<AdviceRule> finallyAdviceRuleList;

    private List<ExceptionRule> exceptionRuleList;

    /**
     * Extracts and registers all advice rules and exception handling rules
     * from the given {@link AspectRule}.
     * @param aspectRule the aspect rule to register
     */
    public void register(AspectRule aspectRule) {
        if (aspectRule != null) {
            if (aspectRule.getSettingsAdviceRule() != null) {
                addAdviceRule(aspectRule.getSettingsAdviceRule());
            }
            if (aspectRule.getAdviceRuleList() != null) {
                for (AdviceRule adviceRule : aspectRule.getAdviceRuleList()) {
                    addAdviceRule(adviceRule);
                }
            }
            if (aspectRule.getExceptionRule() != null) {
                addExceptionRule(aspectRule.getExceptionRule());
            }
        }
    }

    /**
     * Merges all rules from another registry into this one.
     * @param adviceRuleRegistry the source registry to merge from
     */
    public void merge(@NonNull AdviceRuleRegistry adviceRuleRegistry) {
        if (adviceRuleRegistry.getSettingsAdviceRuleList() != null) {
            for (SettingsAdviceRule sar : adviceRuleRegistry.getSettingsAdviceRuleList()) {
                addAdviceRule(sar);
            }
        }
        if (adviceRuleRegistry.getBeforeAdviceRuleList() != null) {
            for (AdviceRule aar : adviceRuleRegistry.getBeforeAdviceRuleList()) {
                addBeforeAdviceRule(aar);
            }
        }
        if (adviceRuleRegistry.getAfterAdviceRuleList() != null) {
            for (AdviceRule aar : adviceRuleRegistry.getAfterAdviceRuleList()) {
                addAfterAdviceRule(aar);
            }
        }
        if (adviceRuleRegistry.getFinallyAdviceRuleList() != null) {
            for (AdviceRule aar : adviceRuleRegistry.getFinallyAdviceRuleList()) {
                addFinallyAdviceRule(aar);
            }
        }
        if (adviceRuleRegistry.getExceptionRuleList() != null) {
            for (ExceptionRule er : adviceRuleRegistry.getExceptionRuleList()) {
                addExceptionRule(er);
            }
        }
    }

    /**
     * Creates a shallow copy of this registry.
     * The rule lists themselves are new, but the rule objects within them are not cloned.
     * @return a replicated {@code AdviceRuleRegistry}
     */
    @Override
    public AdviceRuleRegistry replicate() {
        AdviceRuleRegistry adviceRuleRegistry = new AdviceRuleRegistry();
        if (settingsAdviceRuleList != null) {
            adviceRuleRegistry.setSettingsAdviceRuleList(new ArrayList<>(settingsAdviceRuleList));
        }
        if (beforeAdviceRuleList != null) {
            adviceRuleRegistry.setBeforeAdviceRuleList(new LinkedList<>(beforeAdviceRuleList));
        }
        if (afterAdviceRuleList != null) {
            adviceRuleRegistry.setAfterAdviceRuleList(new LinkedList<>(afterAdviceRuleList));
        }
        if (finallyAdviceRuleList != null) {
            adviceRuleRegistry.setFinallyAdviceRuleList(new LinkedList<>(finallyAdviceRuleList));
        }
        if (exceptionRuleList != null) {
            adviceRuleRegistry.setExceptionRuleList(new LinkedList<>(exceptionRuleList));
        }
        return adviceRuleRegistry;
    }

    /**
     * Retrieves the list of settings advice rules.
     * @return a list of {@link SettingsAdviceRule} objects representing the settings advice rules
     */
    public List<SettingsAdviceRule> getSettingsAdviceRuleList() {
        return settingsAdviceRuleList;
    }

    private void setSettingsAdviceRuleList(List<SettingsAdviceRule> settingsAdviceRuleList) {
        this.settingsAdviceRuleList = settingsAdviceRuleList;
    }

    /**
     * Retrieves the list of 'before' advice rules.
     * This list contains advice rules that are executed before the target join point.
     * @return a list of {@link AdviceRule} objects representing the 'before' advice rules
     */
    public List<AdviceRule> getBeforeAdviceRuleList() {
        return beforeAdviceRuleList;
    }

    private void setBeforeAdviceRuleList(List<AdviceRule> beforeAdviceRuleList) {
        this.beforeAdviceRuleList = beforeAdviceRuleList;
    }

    /**
     * Retrieves the list of 'after' advice rules.
     * This list contains advice rules that are executed after the target join point.
     * @return a list of {@link AdviceRule} objects representing the 'after' advice rules
     */
    public List<AdviceRule> getAfterAdviceRuleList() {
        return afterAdviceRuleList;
    }

    private void setAfterAdviceRuleList(List<AdviceRule> afterAdviceRuleList) {
        this.afterAdviceRuleList = afterAdviceRuleList;
    }

    /**
     * Retrieves the list of 'finally' advice rules.
     * This list contains advice rules that are always executed after the target join point,
     * regardless of whether an exception is thrown or not.
     * @return a list of {@link AdviceRule} objects representing the 'finally' advice rules
     */
    public List<AdviceRule> getFinallyAdviceRuleList() {
        return finallyAdviceRuleList;
    }

    private void setFinallyAdviceRuleList(List<AdviceRule> finallyAdviceRuleList) {
        this.finallyAdviceRuleList = finallyAdviceRuleList;
    }

    /**
     * Adds the specified {@link SettingsAdviceRule} to the list of settings advice rules.
     * If the list is not already initialized, it will be created as a linked list.
     * The newly added rule will be inserted at the beginning of the list.
     * @param settingsAdviceRule the {@link SettingsAdviceRule} to be added to the settings advice rule list
     */
    public void addAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        if (settingsAdviceRuleList == null) {
            settingsAdviceRuleList = new LinkedList<>();
        }
        settingsAdviceRuleList.addFirst(settingsAdviceRule);
    }

    /**
     * Adds an advice rule, dispatching it to the appropriate list based on its type.
     * 'AROUND' advice is added to both the 'before' and 'after' lists to wrap the
     * join point execution.
     * @param adviceRule the advice rule to add
     */
    public void addAdviceRule(@NonNull AdviceRule adviceRule) {
        switch (adviceRule.getAdviceType()) {
            case BEFORE -> addBeforeAdviceRule(adviceRule);
            case AFTER -> addAfterAdviceRule(adviceRule);
            case AROUND -> {
                addBeforeAdviceRule(adviceRule);
                addAfterAdviceRule(adviceRule);
            }
            case FINALLY -> addFinallyAdviceRule(adviceRule);
        }
    }

    /**
     * Adds a 'before' advice rule, inserting it into the list according to the
     * aspect's order of precedence (ascending).
     * @param adviceRule the advice rule to add
     */
    private void addBeforeAdviceRule(AdviceRule adviceRule) {
        if (beforeAdviceRuleList == null) {
            beforeAdviceRuleList = new LinkedList<>();
            beforeAdviceRuleList.add(adviceRule);
        } else {
            int leftInt = adviceRule.getAspectRule().getOrder();
            int index = findLessThanIndex(beforeAdviceRuleList, leftInt);
            beforeAdviceRuleList.add(index, adviceRule);
        }
    }

    /**
     * Adds an 'after' advice rule, inserting it into the list according to the
     * aspect's order of precedence (descending for 'after' advice to create a
     * nested "onion-like" execution chain).
     * @param adviceRule the advice rule to add
     */
    private void addAfterAdviceRule(AdviceRule adviceRule) {
        if (afterAdviceRuleList == null) {
            afterAdviceRuleList = new LinkedList<>();
            afterAdviceRuleList.add(adviceRule);
        } else {
            int leftInt = adviceRule.getAspectRule().getOrder();
            int index = findGreaterThanOrEqualIndex(afterAdviceRuleList, leftInt);
            afterAdviceRuleList.add(index, adviceRule);
        }
    }

    /**
     * Adds a 'finally' advice rule, inserting it into the list according to the
     * aspect's order of precedence (descending).
     * @param adviceRule the advice rule to add
     */
    private void addFinallyAdviceRule(AdviceRule adviceRule) {
        if (finallyAdviceRuleList == null) {
            finallyAdviceRuleList = new LinkedList<>();
            finallyAdviceRuleList.add(adviceRule);
        } else {
            int leftInt = adviceRule.getAspectRule().getOrder();
            int index = findGreaterThanOrEqualIndex(finallyAdviceRuleList, leftInt);
            finallyAdviceRuleList.add(index, adviceRule);
        }
    }

    /**
     * Retrieves the list of exception handling rules.
     * @return a list of {@link ExceptionRule} objects representing the exception handling rules
     */
    public List<ExceptionRule> getExceptionRuleList() {
        return exceptionRuleList;
    }

    private void setExceptionRuleList(List<ExceptionRule> exceptionRuleList) {
        this.exceptionRuleList = exceptionRuleList;
    }

    private void addExceptionRule(ExceptionRule exceptionRule) {
        if (exceptionRuleList == null) {
            exceptionRuleList = new LinkedList<>();
        }
        exceptionRuleList.addFirst(exceptionRule);
    }

    private int findLessThanIndex(@NonNull List<AdviceRule> adviceRuleList, int leftInt) {
        int index = 0;
        for (AdviceRule rule : adviceRuleList) {
            int rightInt = rule.getAspectRule().getOrder();
            if (leftInt < rightInt) {
                return index;
            }
            index++;
        }
        return index;
    }

    private int findGreaterThanOrEqualIndex(@NonNull List<AdviceRule> adviceRuleList, int leftInt) {
        int index = 0;
        for (AdviceRule rule : adviceRuleList) {
            int rightInt = rule.getAspectRule().getOrder();
            if (leftInt >= rightInt) {
                return index;
            }
            index++;
        }
        return index;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("settings", settingsAdviceRuleList);
        tsb.append("before", beforeAdviceRuleList);
        tsb.append("after", afterAdviceRuleList);
        tsb.append("finally", finallyAdviceRuleList);
        tsb.append("exceptions", exceptionRuleList);
        return tsb.toString();
    }

}
