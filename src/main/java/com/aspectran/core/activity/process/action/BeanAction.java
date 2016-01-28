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
import java.util.Iterator;
import java.util.Map;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.expr.ItemTokenExpression;
import com.aspectran.core.context.expr.ItemTokenExpressor;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.variable.ValueMap;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class BeanAction.
 * 
 * <p>Created: 2008. 03. 22 PM 5:50:35</p>
 */
public class BeanAction extends AbstractAction implements Executable {

	private static final Log log = LogFactory.getLog(BeanAction.class);

	private final BeanActionRule beanActionRule;
	
	private final String beanId;

	private final String methodName;
	
	private final ItemRuleMap propertyItemRuleMap;
	
	private final ItemRuleMap argumentItemRuleMap;
	
	private final AspectAdviceRule aspectAdviceRule;
	
	private Boolean needTranslet;
	
	/**
	 * Instantiates a new BeanAction.
	 *
	 * @param beanActionRule the bean action rule
	 * @param parent the parent
	 */
	public BeanAction(BeanActionRule beanActionRule, ActionList parent) {
		super(beanActionRule.getActionId(), parent);
		this.beanActionRule = beanActionRule;
		this.beanId = beanActionRule.getBeanId();
		this.methodName = beanActionRule.getMethodName();
		
		if(beanActionRule.getPropertyItemRuleMap() != null && !beanActionRule.getPropertyItemRuleMap().isEmpty())
			this.propertyItemRuleMap = beanActionRule.getPropertyItemRuleMap();
		else
			this.propertyItemRuleMap = null;

		if(beanActionRule.getArgumentItemRuleMap() != null && !beanActionRule.getArgumentItemRuleMap().isEmpty())
			this.argumentItemRuleMap = beanActionRule.getArgumentItemRuleMap();
		else
			this.argumentItemRuleMap = null;
		
		if(beanActionRule.getAspectAdviceRule() != null)
			this.aspectAdviceRule = beanActionRule.getAspectAdviceRule();
		else
			this.aspectAdviceRule = null;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#execute(org.jhlabs.translets.action.Translet)
	 */
	public Object execute(Activity activity) throws Exception {
		try {
			Object bean = null;
			
			if(beanId != null)
				bean = activity.getBean(beanId);
			else if(aspectAdviceRule != null)
				bean = activity.getAspectAdviceBean(aspectAdviceRule.getAspectId());

			ItemTokenExpressor expressor = null;
			
			if(propertyItemRuleMap != null || argumentItemRuleMap != null)
				expressor = new ItemTokenExpression(activity);
			
			if(propertyItemRuleMap != null) {
				ValueMap valueMap = expressor.express(propertyItemRuleMap);
				
				// set properties for ActionBean
				for(Map.Entry<String, Object> entry : valueMap.entrySet()) {
					BeanUtils.setObject(bean, entry.getKey(), entry.getValue());
				}
			}
			
			Object result;

			if(needTranslet == null) {
				try {
					result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, expressor, true);
					needTranslet = Boolean.TRUE;
				} catch(NoSuchMethodException e) {
					if(log.isDebugEnabled()) {
						log.debug("Cannot find a method that requires a argument translet. So in the future will continue to call a method with no argument translet. beanActionRule " + beanActionRule);
					}
					
					needTranslet = Boolean.FALSE;
					result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, expressor, false);
				}
			} else {
				result = invokeMethod(activity, bean, methodName, argumentItemRuleMap, expressor, needTranslet.booleanValue());
			}

			return result;
		} catch(Exception e) {
			log.error("action execution error: beanActionRule " + beanActionRule + " Cause: " + e.toString());
			throw e;
		}
	}
	
	public static Object invokeMethod(Activity activity, Object bean, String methodName, ItemRuleMap argumentItemRuleMap, ItemTokenExpressor expressor, boolean needTranslet) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class<?>[] argsTypes = null;
		Object[] argsObjects = null;
		
		if(argumentItemRuleMap != null) {
			ValueMap valueMap = expressor.express(argumentItemRuleMap);

			int argIndex;
			
			if(needTranslet) {
				argIndex = 1;
				argsTypes = new Class<?>[argumentItemRuleMap.size() + argIndex];
				argsObjects = new Object[argsTypes.length];
				argsTypes[0] = activity.getTransletInterfaceClass();
				argsObjects[0] = activity.getTranslet();
			} else {
				argIndex = 0;
				argsTypes = new Class<?>[argumentItemRuleMap.size()];
				argsObjects = new Object[argsTypes.length];
			}
			
			Iterator<ItemRule> iter = argumentItemRuleMap.iterator();
			
			while(iter.hasNext()) {
				ItemRule ir = iter.next();
				Object o = valueMap.get(ir.getName());

				argsTypes[argIndex] = ItemRule.getClassOfValue(ir, o);
				argsObjects[argIndex] = o;
				
				argIndex++;
			}
		} else {
			if(needTranslet) {
				argsTypes = new Class<?>[] { activity.getTransletInterfaceClass() };
				argsObjects = new Object[] { activity.getTranslet() };
			}
		}
		
		Object result = MethodUtils.invokeMethod(bean, methodName, argsObjects, argsTypes);
		
		return result;
	}
	
	/**
	 * Gets the bean action rule.
	 * 
	 * @return the bean action rule
	 */
	public BeanActionRule getBeanActionRule() {
		return beanActionRule;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#getParent()
	 */
	public ActionList getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#getId()
	 */
	public String getActionId() {
		return beanActionRule.getActionId();
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#isHidden()
	 */
	public boolean isHidden() {
		return beanActionRule.isHidden();
	}

	public ActionType getActionType() {
		return ActionType.BEAN;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getActionRule() {
		return (T)beanActionRule;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{actionType=").append(getActionType());
		sb.append(", beanActionRule=").append(beanActionRule.toString());
		sb.append("}");

		return sb.toString();
	}
	
}
