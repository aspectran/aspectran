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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.util.ToStringBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * The Class AutowireRule.
 * 
 * <p>Created: 2016. 2. 24.</p>
 * 
 * @since 2.0.0
 */
public class AutowireRule implements BeanReferenceInspectable {

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.AUTOWIRE_RULE;

    private AutowireTargetType targetType;

    private Object target;

    private Class<?>[] types;

    private String[] qualifiers;

    private Token token;

    private boolean required;

    public AutowireTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(AutowireTargetType targetType) {
        this.targetType = targetType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getTarget() {
        return (T)target;
    }

    public void setTarget(Field field) {
        this.target = field;
    }

    public void setTarget(Method method) {
        this.target = method;
    }

    public Class<?>[] getTypes() {
        return types;
    }

    public void setTypes(Class<?>... types) {
        this.types = types;
    }

    public String[] getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(String... qualifiers) {
        this.qualifiers = qualifiers;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public BeanRefererType getBeanRefererType() {
        return BEAN_REFERER_TYPE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("targetType", targetType);
        tsb.append("target", target);
        tsb.append("types", types);
        tsb.append("qualifiers", qualifiers);
        tsb.append("token", token);
        tsb.append("required", required);
        return tsb.toString();
    }

}
