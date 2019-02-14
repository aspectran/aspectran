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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.rule.AnnotatedMethodActionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The AnnotatedMethodAction that invoking method in the bean instance.
 * 
 * <p>Created: 2016. 2. 9.</p>
 *
 * @since 2.0.0
 */
public class AnnotatedMethodAction extends AbstractAction {

    private static final Log log = LogFactory.getLog(AnnotatedMethodAction.class);

    private final AnnotatedMethodActionRule annotatedMethodActionRule;

    private final Class<?> beanClass;

    /**
     * Instantiates a new AnnotatedMethodAction.
     *
     * @param annotatedMethodActionRule the annotated method action rule
     * @param parent the parent of this action
     */
    public AnnotatedMethodAction(AnnotatedMethodActionRule annotatedMethodActionRule, ActionList parent) {
        super(parent);

        this.annotatedMethodActionRule = annotatedMethodActionRule;
        this.beanClass = annotatedMethodActionRule.getBeanClass();
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        try {
            Object bean = activity.getConfiguredBean(beanClass);
            return invokeMethod(activity, bean, annotatedMethodActionRule.getMethod(),
                    annotatedMethodActionRule.isRequiresTranslet());
        } catch (Exception e) {
            log.error("Failed to execute annotated bean method action " + annotatedMethodActionRule);
            throw e;
        }
    }

    public static Object invokeMethod(Activity activity, Object bean, Method method, boolean requiresTranslet)
            throws IllegalAccessException, InvocationTargetException {
        Object[] args;
        if (requiresTranslet) {
            args = new Object[] { activity.getTranslet() };
        } else {
            args = MethodUtils.EMPTY_OBJECT_ARRAY;
        }
        return method.invoke(bean, args);
    }

    /**
     * Returns the annotated bean method action rule.
     *
     * @return the annotated bean method action rule
     */
    public AnnotatedMethodActionRule getAnnotatedMethodActionRule() {
        return annotatedMethodActionRule;
    }

    @Override
    public String getActionId() {
        return annotatedMethodActionRule.getActionId();
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ANNOTATED_METHOD;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getActionRule() {
        return (T) annotatedMethodActionRule;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("actionType", getActionType());
        tsb.append("annotatedMethodActionRule", annotatedMethodActionRule);
        return tsb.toString();
    }

}
