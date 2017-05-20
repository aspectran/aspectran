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
package com.aspectran.core.activity.process;

import java.util.ArrayList;

import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.HeadingAction;
import com.aspectran.core.activity.process.action.IncludeAction;
import com.aspectran.core.activity.process.action.MethodAction;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.HeadingActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.MethodActionRule;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The set of actions is called a Content or ActionList.
 * 
 * <p>Created: 2008. 03. 23 AM 1:38:14</p>
 */
public class ActionList extends ArrayList<Executable> implements ActionRuleApplicable {

    /** @serial */
    private static final long serialVersionUID = 4636431127789162551L;

    private String name;

    private Boolean hidden;

    private Boolean omittable;

    /**
     * Instantiates a new ActionList.
     */
    public ActionList() {
        super(5);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHidden() {
        return BooleanUtils.toBoolean(hidden);
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isOmittable() {
        return BooleanUtils.toBoolean(omittable);
    }

    public Boolean getOmittable() {
        return omittable;
    }

    public void setOmittable(Boolean omittable) {
        this.omittable = omittable;
    }

    public int getVisibleCount() {
        int count = 0;
        for (Executable action : this) {
            if (!action.isHidden()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void applyActionRule(BeanActionRule beanActionRule) {
        BeanAction beanAction = new BeanAction(beanActionRule, this);
        add(beanAction);
    }

    @Override
    public void applyActionRule(MethodActionRule methodActionRule) {
        MethodAction methodAction = new MethodAction(methodActionRule, this);
        add(methodAction);
    }

    @Override
    public void applyActionRule(IncludeActionRule includeActionRule) {
        IncludeAction includeAction = new IncludeAction(includeActionRule, this);
        add(includeAction);
    }

    @Override
    public void applyActionRule(EchoActionRule echoActionRule) {
        EchoAction echoAction = new EchoAction(echoActionRule, this);
        add(echoAction);
    }

    @Override
    public void applyActionRule(HeadingActionRule headingActionRule) {
        HeadingAction headingAction = new HeadingAction(headingActionRule, this);
        add(headingAction);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("hidden", hidden);
        tsb.append("omittable", omittable);
        tsb.append("actions", this);
        return tsb.toString();
    }

    public static ActionList newInstance(String name, Boolean omittable, Boolean hidden) {
        ActionList actionList = new ActionList();
        actionList.setName(name);
        actionList.setOmittable(omittable);
        actionList.setHidden(hidden);
        return actionList;
    }

}
