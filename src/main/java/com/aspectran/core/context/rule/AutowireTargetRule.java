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
package com.aspectran.core.context.rule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.aspectran.core.component.expr.token.Token;
import com.aspectran.core.context.rule.type.AutowireTargetType;

/**
 * The Class AutowireRule.
 * 
 * <p>Created: 2016. 2. 24.</p>
 * 
 * @since 2.0.0
 */
public class AutowireTargetRule {

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

}
