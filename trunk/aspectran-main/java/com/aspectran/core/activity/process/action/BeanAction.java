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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.base.rule.BeanActionRule;
import com.aspectran.base.rule.ItemRule;
import com.aspectran.base.rule.ItemRuleMap;
import com.aspectran.base.token.expression.ValueExpression;
import com.aspectran.base.token.expression.ValueExpressor;
import com.aspectran.base.util.BeanUtils;
import com.aspectran.base.util.MethodUtils;
import com.aspectran.base.variable.ValueMap;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ActionPathMaker;
import com.aspectran.core.translet.Translet;

/**
 * <p>Created: 2008. 03. 22 오후 5:50:35</p>
 */
public class BeanAction implements Executable {

	private final Log log = LogFactory.getLog(BeanAction.class);
	
	private final BeanActionRule beanActionRule;

	private final ActionList parent;

	/**
	 * Instantiates a new bean action.
	 *
	 * @param beanActionRule the bean action rule
	 * @param parent the parent
	 */
	public BeanAction(BeanActionRule beanActionRule, ActionList parent) {
		this.beanActionRule = beanActionRule;
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#execute(org.jhlabs.translets.action.Translet)
	 */
	public Object execute(Activity activity) throws ActionExecutionException {
		try {
			String beanId = beanActionRule.getBeanId();
			String methodName = beanActionRule.getMethodName();
			ItemRuleMap propertyItemRuleMap = beanActionRule.getPropertyItemRuleMap();
			ItemRuleMap argumentItemRuleMap = beanActionRule.getArgumentItemRuleMap();

			return invokeMethod(activity, beanId, methodName, propertyItemRuleMap, argumentItemRuleMap);
		} catch(Exception e) {
			log.error("Execute error: BeanAction " + beanActionRule.toString());
			throw new ActionExecutionException(this, e);
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
	public String getId() {
		if(beanActionRule == null)
			return null;

		return beanActionRule.getId();
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.engine.process.action.Executable#isHidden()
	 */
	public boolean isHidden() {
		if(beanActionRule.getHidden() == Boolean.TRUE)
			return true;
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{actionPath=").append(ActionPathMaker.concatActionPath(parent.getContentId(), beanActionRule.getId()));
		sb.append(", beanActionRule=").append(beanActionRule.toString());
		sb.append("}");

		return sb.toString();
	}
	
	public static Object invokeMethod(Activity activity, String beanId, String methodName, ItemRuleMap propertyItemRuleMap, ItemRuleMap argumentItemRuleMap) throws Exception {
		Object bean = activity.getBean(beanId);
		
		ValueExpressor expressor = new ValueExpression(activity);

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
			
			parameterTypes[0] = Translet.class;
			args[0] = activity.getActivityTranslet();
			
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
			parameterTypes = new Class<?>[] { Translet.class };
			args = new Object[] { activity.getActivityTranslet() };
		}
		
		Object result = MethodUtils.invokeMethod(bean, methodName, args, parameterTypes);
		
		return result;
		
	}
}
