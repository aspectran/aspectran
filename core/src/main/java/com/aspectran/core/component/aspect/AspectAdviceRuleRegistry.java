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
package com.aspectran.core.component.aspect;

import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.ToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Class AspectAdviceRuleRegistry.
 */
public class AspectAdviceRuleRegistry implements Replicable<AspectAdviceRuleRegistry> {

    private Map<String, Object> settings;

    private List<SettingsAdviceRule> settingsAdviceRuleList;

    private List<AspectAdviceRule> beforeAdviceRuleList;

    private List<AspectAdviceRule> afterAdviceRuleList;

    private List<AspectAdviceRule> finallyAdviceRuleList;

    private List<ExceptionRule> exceptionRuleList;

    private int aspectRuleCount;

    public void register(AspectRule aspectRule) {
        if (aspectRule.getSettingsAdviceRule() != null) {
            addAspectAdviceRule(aspectRule.getSettingsAdviceRule());
        }
        if (aspectRule.getAspectAdviceRuleList() != null) {
            for (AspectAdviceRule aspectAdviceRule : aspectRule.getAspectAdviceRuleList()) {
                addAspectAdviceRule(aspectAdviceRule);
            }
        }
        if (aspectRule.getExceptionRule() != null) {
            addExceptionRule(aspectRule.getExceptionRule());
        }
        increaseAspectRuleCount();
    }

    public void registerDynamically(AspectRule aspectRule) {
        if (aspectRule.getSettingsAdviceRule() != null) {
            addAspectAdviceRule(aspectRule.getSettingsAdviceRule());
        }
        if (aspectRule.getAspectAdviceRuleList() != null) {
            for (AspectAdviceRule aspectAdviceRule : aspectRule.getAspectAdviceRuleList()) {
                if (aspectAdviceRule.getAspectAdviceType() != AspectAdviceType.BEFORE) {
                    addAspectAdviceRule(aspectAdviceRule);
                }
            }
        }
        if (aspectRule.getExceptionRule() != null) {
            addExceptionRule(aspectRule.getExceptionRule());
        }
        increaseAspectRuleCount();
    }

    @Override
    public AspectAdviceRuleRegistry replicate() {
        AspectAdviceRuleRegistry aarr = new AspectAdviceRuleRegistry();
        aarr.setAspectRuleCount(aspectRuleCount);
        if (settings != null) {
            Map<String, Object> newSettings = new HashMap<>(settings);
            aarr.setSettings(newSettings);
        }
        if (settingsAdviceRuleList != null) {
            aarr.setSettingsAdviceRuleList(new ArrayList<>(settingsAdviceRuleList));
        }
        if (beforeAdviceRuleList != null) {
            aarr.setBeforeAdviceRuleList(new LinkedList<>(beforeAdviceRuleList));
        }
        if (afterAdviceRuleList != null) {
            aarr.setAfterAdviceRuleList(new LinkedList<>(afterAdviceRuleList));
        }
        if (finallyAdviceRuleList != null) {
            aarr.setFinallyAdviceRuleList(new LinkedList<>(finallyAdviceRuleList));
        }
        if (exceptionRuleList != null) {
            aarr.setExceptionRuleList(new LinkedList<>(exceptionRuleList));
        }
        return aarr;
    }

    @SuppressWarnings("unchecked")
    public <T> T getSetting(String settingName) {
        return (settings != null ? (T)settings.get(settingName) : null);
    }

    private void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    private void addSettings(Map<String, Object> settings) {
        if (settings != null) {
            if (this.settings == null) {
                this.settings = new HashMap<>(settings);
            } else {
                this.settings.putAll(settings);
            }
        }
    }

    private void setSettingsAdviceRuleList(List<SettingsAdviceRule> settingsAdviceRuleList) {
        this.settingsAdviceRuleList = settingsAdviceRuleList;
        for (SettingsAdviceRule settingsAdviceRule : settingsAdviceRuleList) {
            addSettings(settingsAdviceRule.getSettings());
        }
    }

    public List<AspectAdviceRule> getBeforeAdviceRuleList() {
        return beforeAdviceRuleList;
    }

    private void setBeforeAdviceRuleList(List<AspectAdviceRule> beforeAdviceRuleList) {
        this.beforeAdviceRuleList = beforeAdviceRuleList;
    }

    public List<AspectAdviceRule> getAfterAdviceRuleList() {
        return afterAdviceRuleList;
    }

    private void setAfterAdviceRuleList(List<AspectAdviceRule> afterAdviceRuleList) {
        this.afterAdviceRuleList = afterAdviceRuleList;
    }

    public List<AspectAdviceRule> getFinallyAdviceRuleList() {
        return finallyAdviceRuleList;
    }

    private void setFinallyAdviceRuleList(List<AspectAdviceRule> finallyAdviceRuleList) {
        this.finallyAdviceRuleList = finallyAdviceRuleList;
    }

    private void addAspectAdviceRule(SettingsAdviceRule settingsAdviceRule) {
        if (settingsAdviceRuleList == null) {
            settingsAdviceRuleList = new ArrayList<>();
        }
        settingsAdviceRuleList.add(settingsAdviceRule);

        addSettings(settingsAdviceRule.getSettings());
    }

    private void addAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
        if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
            addBeforeAdviceRule(aspectAdviceRule);
        } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
            addAfterAdviceRule(aspectAdviceRule);
        } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
            addBeforeAdviceRule(aspectAdviceRule);
            addAfterAdviceRule(aspectAdviceRule);
        } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
            addFinallyAdviceRule(aspectAdviceRule);
        }
    }

    private void addBeforeAdviceRule(AspectAdviceRule aspectAdviceRule) {
        if (beforeAdviceRuleList == null) {
            beforeAdviceRuleList = new LinkedList<>();
            beforeAdviceRuleList.add(aspectAdviceRule);
        } else {
            int leftInt = aspectAdviceRule.getAspectRule().getOrder();
            int index = findLessThanIndex(beforeAdviceRuleList, leftInt);
            beforeAdviceRuleList.add(index, aspectAdviceRule);
        }
    }

    private void addAfterAdviceRule(AspectAdviceRule aspectAdviceRule) {
        if (afterAdviceRuleList == null) {
            afterAdviceRuleList = new LinkedList<>();
            afterAdviceRuleList.add(aspectAdviceRule);
        } else {
            int leftInt = aspectAdviceRule.getAspectRule().getOrder();
            int index = findGreaterThanOrEqualIndex(afterAdviceRuleList, leftInt);
            afterAdviceRuleList.add(index, aspectAdviceRule);
        }
    }

    private void addFinallyAdviceRule(AspectAdviceRule aspectAdviceRule) {
        if (finallyAdviceRuleList == null) {
            finallyAdviceRuleList = new LinkedList<>();
            finallyAdviceRuleList.add(aspectAdviceRule);
        } else {
            int leftInt = aspectAdviceRule.getAspectRule().getOrder();
            int index = findGreaterThanOrEqualIndex(finallyAdviceRuleList, leftInt);
            finallyAdviceRuleList.add(index, aspectAdviceRule);
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

    public int getAspectRuleCount() {
        return aspectRuleCount;
    }

    private void setAspectRuleCount(int aspectRuleCount) {
        this.aspectRuleCount = aspectRuleCount;
    }

    private void increaseAspectRuleCount() {
        this.aspectRuleCount++;
    }

    private int findLessThanIndex(List<AspectAdviceRule> adviceRuleList, int leftInt) {
        for (int i = 0; i < adviceRuleList.size(); i++) {
            int rightInt = adviceRuleList.get(i).getAspectRule().getOrder();
            if (leftInt < rightInt) {
                return i;
            }
        }
        return adviceRuleList.size();
    }

    private int findGreaterThanOrEqualIndex(List<AspectAdviceRule> adviceRuleList, int leftInt) {
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
        ToStringBuilder tsb = new ToStringBuilder(94);
        tsb.append("settings", settings);
        tsb.append("beforeAdvices", beforeAdviceRuleList);
        tsb.append("afterAdvices", afterAdviceRuleList);
        tsb.append("finallyAdvices", finallyAdviceRuleList);
        tsb.append("exceptionRules", exceptionRuleList);
        return tsb.toString();
    }

}
