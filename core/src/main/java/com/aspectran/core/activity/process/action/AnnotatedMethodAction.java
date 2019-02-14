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
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.rule.AnnotatedMethodActionRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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

    /**
     * Instantiates a new AnnotatedMethodAction.
     *
     * @param annotatedMethodActionRule the annotated method action rule
     * @param parent the parent of this action
     */
    public AnnotatedMethodAction(AnnotatedMethodActionRule annotatedMethodActionRule, ActionList parent) {
        super(parent);

        this.annotatedMethodActionRule = annotatedMethodActionRule;
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        try {
            Object bean = activity.getConfiguredBean(annotatedMethodActionRule.getBeanClass());
            Method method = annotatedMethodActionRule.getMethod();
            AutowireRule autowireRule = annotatedMethodActionRule.getAutowireRule();
            return invokeMethod(activity, bean, method, autowireRule);
        } catch (Exception e) {
            log.error("Failed to execute annotated bean method action " + annotatedMethodActionRule);
            throw e;
        }
    }

    public static Object invokeMethod(Activity activity, Object bean, Method method, AutowireRule autowireRule)
            throws InvocationTargetException, IllegalAccessException {
        Translet translet = activity.getTranslet();
        String[] qualifiers = autowireRule.getQualifiers();
        String[] formats = autowireRule.getFormats();
        Class<?>[] argTypes = autowireRule.getTypes();
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            Class<?> type = argTypes[i];
            String name = qualifiers[i];
            String format = formats[i];
            args[i] = autowire(type, name, format, translet);
        }
        return method.invoke(bean, args);
    }

    private static Object autowire(Class<?> type, String name, String format, Translet translet) {
        Object result = null;
        if (translet != null) {
            if (type == Translet.class) {
                result = translet;
            } else if (type == String.class) {
                result = translet.getParameter(name);
            } else if (type == Date.class) {
                String value = translet.getParameter(name);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDateTime ldt = LocalDateTime.parse(value, formatter);
                result = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            } else if (type == Boolean.class) {
                String value = translet.getParameter(name);
                result = Boolean.valueOf(value);
            } else if (type == Number.class) {
                //TODO
            }
        }
        return result;
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
