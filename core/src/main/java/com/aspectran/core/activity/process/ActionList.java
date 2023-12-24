/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.activity.process.action.AnnotatedAction;
import com.aspectran.core.activity.process.action.ChooseAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.process.action.HeaderAction;
import com.aspectran.core.activity.process.action.IncludeAction;
import com.aspectran.core.activity.process.action.InvokeAction;
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.utils.ToStringBuilder;

import java.util.ArrayList;

/**
 * The set of actions is called a Content or ActionList.
 *
 * <p>Created: 2008. 03. 23 AM 1:38:14</p>
 */
public class ActionList extends ArrayList<Executable> implements ActionRuleApplicable {

    private static final long serialVersionUID = 4636431127789162551L;

    private final boolean explicit;

    private String name;

    public ActionList(boolean explicit) {
        super(5);

        this.explicit = explicit;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Executable applyActionRule(HeaderActionRule headerActionRule) {
        Executable action = new HeaderAction(headerActionRule);
        add(action);
        return action;
    }

    @Override
    public Executable applyActionRule(EchoActionRule echoActionRule) {
        Executable action = new EchoAction(echoActionRule);
        add(action);
        return action;
    }

    @Override
    public Executable applyActionRule(InvokeActionRule invokeActionRule) {
        Executable action = new InvokeAction(invokeActionRule);
        add(action);
        return action;
    }

    @Override
    public Executable applyActionRule(AnnotatedActionRule annotatedActionRule) {
        Executable action = new AnnotatedAction(annotatedActionRule);
        add(action);
        return action;
    }

    @Override
    public Executable applyActionRule(IncludeActionRule includeActionRule) {
        Executable action = new IncludeAction(includeActionRule);
        add(action);
        return action;
    }

    @Override
    public Executable applyActionRule(ChooseRule chooseRule) {
        Executable action = new ChooseAction(chooseRule);
        add(action);
        return action;
    }

    @Override
    public void applyActionRule(Executable action) {
        add(action);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("actions", this);
        return tsb.toString();
    }

    public static ActionList newInstance(String name) {
        ActionList actionList = new ActionList(true);
        actionList.setName(name);
        return actionList;
    }

}
