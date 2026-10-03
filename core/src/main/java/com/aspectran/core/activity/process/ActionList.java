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
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.ArrayList;

/**
 * An ordered collection of {@link Executable} actions that are executed sequentially.
 * This class represents a single, flat sequence of operations within a larger,
 * potentially nested {@link ContentList}.
 *
 * <p>Actions within this list are executed in the order they are added, forming a fundamental
 * part of the translet's processing flow. It implements {@link HasActionRules} to allow
 * various types of actions to be dynamically added based on configuration rules.</p>
 *
 * <p>Created: 2008. 03. 23 AM 1:38:14</p>
 */
public class ActionList extends ArrayList<Executable> implements HasActionRules {

    @Serial
    private static final long serialVersionUID = 4636431127789162551L;

    private final boolean explicit;

    @Nullable
    private String name;

    /**
     * Instantiates a new ActionList.
     */
    public ActionList() {
        this(false);
    }

    /**
     * Instantiates a new ActionList.
     * @param explicit whether this action list was explicitly defined
     */
    public ActionList(boolean explicit) {
        this(5, explicit);
    }

    /**
     * Instantiates a new ActionList with a specific initial capacity.
     * @param initialCapacity the initial capacity
     * @param explicit whether this action list was explicitly defined
     */
    public ActionList(int initialCapacity, boolean explicit) {
        super(initialCapacity);
        this.explicit = explicit;
    }

    /**
     * Instantiates a new ActionList by copying another ActionList.
     * @param actionList the action list to copy
     */
    public ActionList(@NonNull ActionList actionList) {
        super(actionList);
        this.explicit = actionList.isExplicit();
        this.name = actionList.getName();
    }

    /**
     * Returns whether this action list was explicitly defined in the configuration.
     * @return true if the action list was explicit, false otherwise
     */
    public boolean isExplicit() {
        return explicit;
    }

    /**
     * Returns the name of this action list.
     * @return the name of the action list
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this action list.
     * @param name the name of the action list
     */
    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Override
    @NonNull
    public Executable putActionRule(@NonNull HeaderActionRule headerActionRule) {
        Assert.notNull(headerActionRule, "headerActionRule must not be null");
        Executable action = new HeaderAction(headerActionRule);
        add(action);
        return action;
    }

    @Override
    @NonNull
    public Executable putActionRule(@NonNull EchoActionRule echoActionRule) {
        Assert.notNull(echoActionRule, "echoActionRule must not be null");
        Executable action = new EchoAction(echoActionRule);
        add(action);
        return action;
    }

    @Override
    @NonNull
    public Executable putActionRule(@NonNull InvokeActionRule invokeActionRule) {
        Assert.notNull(invokeActionRule, "invokeActionRule must not be null");
        Executable action = new InvokeAction(invokeActionRule);
        add(action);
        return action;
    }

    @Override
    @NonNull
    public Executable putActionRule(@NonNull AnnotatedActionRule annotatedActionRule) {
        Assert.notNull(annotatedActionRule, "annotatedActionRule must not be null");
        Executable action = new AnnotatedAction(annotatedActionRule);
        add(action);
        return action;
    }

    @Override
    @NonNull
    public Executable putActionRule(@NonNull IncludeActionRule includeActionRule) {
        Assert.notNull(includeActionRule, "includeActionRule must not be null");
        Executable action = new IncludeAction(includeActionRule);
        add(action);
        return action;
    }

    @Override
    @NonNull
    public Executable putActionRule(@NonNull ChooseRule chooseRule) {
        Assert.notNull(chooseRule, "chooseRule must not be null");
        Executable action = new ChooseAction(chooseRule);
        add(action);
        return action;
    }

    @Override
    public void putActionRule(@NonNull Executable action) {
        Assert.notNull(action, "action must not be null");
        add(action);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("actions", this);
        return tsb.toString();
    }

    /**
     * A factory method to create a new instance of ActionList.
     * @param name the name of the action list
     * @return the new ActionList instance
     */
    @NonNull
    public static ActionList newInstance(@Nullable String name) {
        ActionList actionList = new ActionList(true);
        actionList.setName(name);
        return actionList;
    }

}
