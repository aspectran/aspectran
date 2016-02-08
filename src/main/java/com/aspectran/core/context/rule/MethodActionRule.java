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

import java.lang.reflect.Method;

import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;

/**
 * The Class BeanActionRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:50:35</p>
 */
public class MethodActionRule {

	private Class<?> actionClass;

	private Method method;

	private AspectAdviceRule aspectAdviceRule;
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

	public Class<?> getActionClass() {
		return actionClass;
	}

	public void setActionClass(Class<?> actionClass) {
		this.actionClass = actionClass;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	/**
	 * Gets the aspect advice rule.
	 *
	 * @return the aspect advice rule
	 */
	public AspectAdviceRule getAspectAdviceRule() {
		return aspectAdviceRule;
	}

	/**
	 * Sets the aspect advice rule.
	 *
	 * @param aspectAdviceRule the new aspect advice rule
	 */
	public void setAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
		this.aspectAdviceRule = aspectAdviceRule;
	}

	/**
	 * Gets the aspect advice rule registry.
	 *
	 * @return the aspect advice rule registry
	 */
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	/**
	 * Sets the aspect advice rule registry.
	 *
	 * @param aspectAdviceRuleRegistry the new aspect advice rule registry
	 */
	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{class=").append(actionClass);
		sb.append(", method=").append(method.getName());
		sb.append("}");
		
		return sb.toString();
	}

	/**
	 * Returns a new derived instance of MethodActionRule.
	 *
	 * @param actionClass the action class
	 * @param method the method
	 * @return the method action rule
	 */
	public static MethodActionRule newInstance(Class<?> actionClass, Method method) {
		MethodActionRule methodActionRule = new MethodActionRule();
		methodActionRule.setActionClass(actionClass);
		methodActionRule.setMethod(method);

		return methodActionRule;
	}
	
}
