/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.activity.process.action;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.var.ValueMap;
import com.aspectran.core.var.rule.BeanActionRule;
import com.aspectran.core.var.rule.ItemRule;
import com.aspectran.core.var.rule.ItemRuleMap;
import com.aspectran.core.var.token.ItemTokenExpression;
import com.aspectran.core.var.token.ItemTokenExpressor;
import com.aspectran.core.var.type.ActionType;

/**
 * <p>Created: 2008. 03. 22 오후 5:50:35</p>
 */
public class BeanAction extends AbstractAction implements Executable {

	private final Logger logger = LoggerFactory.getLogger(BeanAction.class);

	private final BeanActionRule beanActionRule;
	
	/**
	 * Instantiates a new bean action.
	 *
	 * @param beanActionRule the bean action rule
	 * @param parent the parent
	 */
	public BeanAction(BeanActionRule beanActionRule, ActionList parent) {
		super(beanActionRule.getActionId(), parent);
		this.beanActionRule = beanActionRule;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#execute(org.jhlabs.translets.action.Translet)
	 */
	public Object execute(CoreActivity activity) throws Exception {
		try {
			String beanId = beanActionRule.getBeanId();
			Object bean = null;
			
			if(beanId != null)
				bean = activity.getBean(beanId);
			else if(beanActionRule.getAspectAdviceRule() != null) {
				String aspectId = beanActionRule.getAspectAdviceRule().getAspectId();
				bean = activity.getAspectAdviceBean(aspectId);
			}
				
			String methodName = beanActionRule.getMethodName();
			ItemRuleMap propertyItemRuleMap = beanActionRule.getPropertyItemRuleMap();
			ItemRuleMap argumentItemRuleMap = beanActionRule.getArgumentItemRuleMap();

			return invokeMethod(activity, bean, methodName, propertyItemRuleMap, argumentItemRuleMap);
		} catch(Exception e) {
			logger.error("action execution error: beanActionRule " + beanActionRule + " Cause: " + e.toString());
			throw e;
		}
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
		return beanActionRule.getHidden();
	}

	public ActionType getActionType() {
		return ActionType.BEAN;
	}
	
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return beanActionRule.getAspectAdviceRuleRegistry();
	}
	
	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		beanActionRule.setAspectAdviceRuleRegistry(aspectAdviceRuleRegistry);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{fullActionId=").append(fullActionId);
		sb.append(", actionType=").append(getActionType());
		sb.append(", beanActionRule=").append(beanActionRule.toString());
		sb.append("}");

		return sb.toString();
	}
	
	public static Object invokeMethod(CoreActivity activity, Object bean, String methodName, ItemRuleMap propertyItemRuleMap, ItemRuleMap argumentItemRuleMap) throws Exception {
		ItemTokenExpressor expressor = new ItemTokenExpression(activity);

		if(propertyItemRuleMap != null) {
			ValueMap valueMap = expressor.express(propertyItemRuleMap);
			
			// set properties for ActionBean
			for(Map.Entry<String, Object> entry : valueMap.entrySet()) {
				BeanUtils.setObject(bean, entry.getKey(), entry.getValue());
			}
		}

		Class<?>[] parameterTypes = null;
		Object[] args = null;
		
		if(argumentItemRuleMap != null && argumentItemRuleMap.size() > 0) {
			ValueMap valueMap = expressor.express(argumentItemRuleMap);
			
			parameterTypes = new Class<?>[argumentItemRuleMap.size() + 1];
			args = new Object[parameterTypes.length];
			
			parameterTypes[0] = activity.getTransletInterfaceClass();
			args[0] = activity.getSuperTranslet();
			
			Iterator<ItemRule> iter = argumentItemRuleMap.iterator();
			int i = 1;
			
			while(iter.hasNext()) {
				ItemRule ir = iter.next();
				Object o = valueMap.get(ir.getName());
				
				parameterTypes[i] = o.getClass();
				args[i] = o;
				
				i++;
			}
		} else {
			parameterTypes = new Class<?>[] { activity.getTransletInterfaceClass() };
			args = new Object[] { activity.getSuperTranslet() };
		}
		
		Object result = MethodUtils.invokeMethod(bean, methodName, args, parameterTypes);
		
		return result;
	}
	
}
