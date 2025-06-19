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
import com.aspectran.core.context.rule.type.AdviceType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class AdviceRuleRegistry.
 */
public class AdviceRuleRegistry implements Replicable<AdviceRuleRegistry> {

    private List<SettingsAdviceRule> settingsAdviceRuleList;

    private List<AdviceRule> beforeAdviceRuleList;

    private List<AdviceRule> afterAdviceRuleList;

    private List<AdviceRule> finallyAdviceRuleList;

    private List<ExceptionRule> exceptionRuleList;

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

    public List<SettingsAdviceRule> getSettingsAdviceRuleList() {
        return settingsAdviceRuleList;
    }

    private void setSettingsAdviceRuleList(List<SettingsAdviceRule> settingsAdviceRuleList) {
        this.settingsAdviceRuleList = settingsAdviceRuleList;
    }

    public List<AdviceRule> getBeforeAdviceRuleList() {
        return beforeAdviceRuleList;
    }

    private void setBeforeAdviceRuleList(List<AdviceRule> beforeAdviceRuleList) {
        this.beforeAdviceRuleList = beforeAdviceRuleList;
    }

    public List<AdviceRule> getAfterAdviceRuleList() {
        return afterAdviceRuleList;
    }

    private void setAfterAdviceRuleList(List<AdviceRule> afterAdviceRuleList) {
        this.afterAdviceRuleList = afterAdviceRuleList;
    }

    public List<AdviceRule> getFinallyAdviceRuleList() {
        return finallyAdviceRuleList;
    }

    private void setFinallyAdviceRuleList(List<AdviceRule> finallyAdviceRuleList) {
        this.finallyAdviceRuleList = finallyAdviceRuleList;
    }

    public void addAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        if (settingsAdviceRuleList == null) {
            settingsAdviceRuleList = new ArrayList<>();
        }
        settingsAdviceRuleList.add(0, settingsAdviceRule);
    }

    public void addAdviceRule(@NonNull AdviceRule adviceRule) {
        if (adviceRule.getAdviceType() == AdviceType.BEFORE) {
            addBeforeAdviceRule(adviceRule);
        } else if (adviceRule.getAdviceType() == AdviceType.AFTER) {
            addAfterAdviceRule(adviceRule);
        } else if (adviceRule.getAdviceType() == AdviceType.AROUND) {
            addBeforeAdviceRule(adviceRule);
            addAfterAdviceRule(adviceRule);
        } else if (adviceRule.getAdviceType() == AdviceType.FINALLY) {
            addFinallyAdviceRule(adviceRule);
        }
    }

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
        exceptionRuleList.add(0, exceptionRule);
    }

    private int findLessThanIndex(@NonNull List<AdviceRule> adviceRuleList, int leftInt) {
        for (int i = 0; i < adviceRuleList.size(); i++) {
            int rightInt = adviceRuleList.get(i).getAspectRule().getOrder();
            if (leftInt < rightInt) {
                return i;
            }
        }
        return adviceRuleList.size();
    }

    private int findGreaterThanOrEqualIndex(@NonNull List<AdviceRule> adviceRuleList, int leftInt) {
        for (int i = 0; i < adviceRuleList.size(); i++) {
            int rightInt = adviceRuleList.get(i).getAspectRule().getOrder();
            if (leftInt >= rightInt) {
                return i;
            }
        }
        return adviceRuleList.size();
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
