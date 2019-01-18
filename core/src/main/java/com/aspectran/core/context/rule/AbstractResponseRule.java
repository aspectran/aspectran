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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.util.BooleanUtils;

import java.util.Collection;

public abstract class AbstractResponseRule implements ActionRuleApplicable {

    private Boolean defaultResponse;

    private ActionList actionList;

    /**
     * Returns whether the default response.
     *
     * @return whether the default response
     */
    public Boolean getDefaultResponse() {
        return defaultResponse;
    }

    /**
     * Returns whether the default response.
     *
     * @return true, if is default response
     */
    public boolean isDefaultResponse() {
        return BooleanUtils.toBoolean(defaultResponse);
    }

    /**
     * Sets whether the default response.
     *
     * @param defaultResponse whether the default response
     */
    public void setDefaultResponse(Boolean defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    public ActionList getActionList() {
        return actionList;
    }

    public void setActionList(ActionList actionList) {
        this.actionList = actionList;
    }

    @Override
    public Executable applyActionRule(BeanMethodActionRule beanMethodActionRule) {
        return touchActionList().applyActionRule(beanMethodActionRule);
    }

    @Override
    public Executable applyActionRule(ConfigBeanMethodActionRule configBeanMethodActionRule) {
        return touchActionList().applyActionRule(configBeanMethodActionRule);
    }

    @Override
    public Executable applyActionRule(IncludeActionRule includeActionRule) {
        return touchActionList().applyActionRule(includeActionRule);
    }

    @Override
    public Executable applyActionRule(EchoActionRule echoActionRule) {
        return touchActionList().applyActionRule(echoActionRule);
    }

    @Override
    public Executable applyActionRule(HeaderActionRule headerActionRule) {
        return touchActionList().applyActionRule(headerActionRule);
    }

    @Override
    public void applyActionRule(Executable action) {
        touchActionList().applyActionRule(action);
    }

    @Override
    public void applyActionRule(Collection<Executable> actionList) {
        touchActionList().addAll(actionList);
    }

    /**
     * Returns the action list.
     * If not yet instantiated then create a new one.
     *
     * @return the action list
     */
    private ActionList touchActionList() {
        if (actionList == null) {
            actionList = new ActionList(false);
        }
        return actionList;
    }

}
