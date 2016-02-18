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
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class MethodActionRule.
 * 
 * <p>Created: 2016. 2. 10.</p>
 * 
 * @since 2.0.0
 */
public class MethodActionRule {

	private Class<?> configBeanClass;

	private Method method;

	private boolean requiresTranslet;

	private AspectAdviceRule aspectAdviceRule;
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

	public Class<?> getConfigBeanClass() {
		return configBeanClass;
	}

	public void setConfigBeanClass(Class<?> configBeanClass) {
		this.configBeanClass = configBeanClass;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public boolean isRequiresTranslet() {
		return requiresTranslet;
	}

	public void setRequiresTranslet(boolean requiresTranslet) {
		this.requiresTranslet = requiresTranslet;
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
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("class", configBeanClass);
		tsb.append("method", method);
		tsb.append("aspectAdviceRule", aspectAdviceRule);
		tsb.append("aspectAdviceRuleRegistry", aspectAdviceRuleRegistry);
		return tsb.toString();
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
		methodActionRule.setConfigBeanClass(actionClass);
		methodActionRule.setMethod(method);

		return methodActionRule;
	}
	
}
