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
package com.aspectran.core.activity.process.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.rule.MethodActionRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class MethodAction.
 * 
 * <p>Created: 2016. 2. 9.</p>
 *
 * @since 2.0.0
 */
public class MethodAction extends AbstractAction implements Executable {

	private static final Log log = LogFactory.getLog(MethodAction.class);

	private final MethodActionRule methodActionRule;

	private final Class<?> actionClass;

	private final Method method;

	private Boolean needTranslet;

	/**
	 * Instantiates a new BeanAction.
	 *
	 * @param methodActionRule the method action rule
	 * @param parent the parent
	 */
	public MethodAction(MethodActionRule methodActionRule, ActionList parent) {
		super(null, parent);

		this.methodActionRule = methodActionRule;
		this.actionClass = methodActionRule.getBeanClass();
		this.method = methodActionRule.getMethod();
	}

	@Override
	public Object execute(Activity activity) throws Exception {
		try {
			Object bean = activity.getBean(actionClass);

			Object result;

			if(needTranslet == null) {
				try {
					result = invokeMethod(activity, bean, this.method, true);
					needTranslet = Boolean.TRUE;
				} catch(NoSuchMethodException e) {
					if(log.isDebugEnabled()) {
						log.debug("Cannot find a method that requires a argument translet. So in the future will continue to call a method with no argument translet. methodActionRule " + methodActionRule);
					}
					
					needTranslet = Boolean.FALSE;
					result = invokeMethod(activity, bean, this.method, false);
				}
			} else {
				result = invokeMethod(activity, bean, this.method, needTranslet.booleanValue());
			}

			return result;
		} catch(Exception e) {
			log.error("Action execution error: methodActionRule " + methodActionRule + " Cause: " + e.toString());
			throw e;
		}
	}

	public static Object invokeMethod(Activity activity, Object bean, Method method, boolean needTranslet) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Object[] args;
		
		if(needTranslet) {
			args = new Object[] { activity.getTranslet() };
		} else {
			args = new Object[0];
		}

		return method.invoke(bean, args);
	}
	
	/**
	 * Gets the method action rule.
	 * 
	 * @return the method action rule
	 */
	public MethodActionRule getMethodActionRule() {
		return methodActionRule;
	}

	@Override
	public ActionList getParent() {
		return parent;
	}

	@Override
	public String getActionId() {
		return null;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public ActionType getActionType() {
		return ActionType.METHOD;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getActionRule() {
		return (T)methodActionRule;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("actionType", getActionType());
		tsb.append("methodActionRule", methodActionRule);
		return tsb.toString();
	}
	
}
