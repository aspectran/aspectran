/**
 * Copyright 2008-2016 Juho Jeong
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

import com.aspectran.core.context.rule.type.AutowireTargetType;

/**
 * The Class AutowireRule.
 * 
 * <p>Created: 2016. 2. 24.</p>
 * 
 * @since 2.0.0
 */
public class AutowireRule {

	private AutowireTargetType targetType;
	
	private Object target;
	
	private String qualifier;
	
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
		this.targetType = AutowireTargetType.FIELD;
	}

	public void setTarget(Method method) {
		this.target = method;
		this.targetType = AutowireTargetType.METHOD;
	}
	
	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
	
}
