/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.type.AspectAdviceType;

/**
 * <p>Created: 2008. 04. 01 오후 11:19:28</p>
 */
public class AspectAdviceRule implements ActionRuleApplicable {

	private final AspectRule aspectRule;
	
	private final String adviceBeanId;
	
	private final AspectAdviceType aspectAdviceType;
	
	private Executable action;
	
	private ResponseByContentTypeRuleMap responseByContentTypeRuleMap;

	public AspectAdviceRule(AspectRule aspectRule, AspectAdviceType aspectAdviceType) {
		this.aspectRule = aspectRule;
		this.adviceBeanId = aspectRule.getAdviceBeanId();
		this.aspectAdviceType = aspectAdviceType;
	}
	
	public String getAspectId() {
		return aspectRule.getId();
	}

	public AspectRule getAspectRule() {
		return aspectRule;
	}

	public String getAdviceBeanId() {
		return adviceBeanId;
	}

	public AspectAdviceType getAspectAdviceType() {
		return aspectAdviceType;
	}

	public void applyEchoActionRule(EchoActionRule echoActionRule) {
		action = new EchoAction(echoActionRule, null);
	}

	public void applyBeanActionRule(BeanActionRule beanActionRule) {
		beanActionRule.setAspectAdviceRule(this);
		action = new BeanAction(beanActionRule, null);
	}
	
	public void applyIncludeActionRule(IncludeActionRule includeActionRule) {
		throw new UnsupportedOperationException("There is nothing that can be apply to IncludeActionRule. The aspecet-advice is not support include-action.");
	}
	
	public Executable getExecutableAction() {
		return action;
	}

	public ResponseByContentTypeRuleMap getResponseByContentTypeRuleMap() {
		return responseByContentTypeRuleMap;
	}

	public void setResponseByContentTypeRuleMap(ResponseByContentTypeRuleMap responseByContentTypeRuleMap) {
		this.responseByContentTypeRuleMap = responseByContentTypeRuleMap;
	}
	
	public void addResponseByContentTypeRule(ResponseByContentTypeRule responseByContentTypeRule) {
		if(responseByContentTypeRuleMap == null)
			responseByContentTypeRuleMap = new ResponseByContentTypeRuleMap();
		
		responseByContentTypeRuleMap.putResponseByContentTypeRule(responseByContentTypeRule);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{aspectId=").append(aspectRule != null ? aspectRule.getId() : null);
		sb.append(", aspectAdviceType=").append(aspectAdviceType);
		sb.append(", action=").append(action);
		sb.append(", responseByContentTypeRuleMap=").append(responseByContentTypeRuleMap);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static AspectAdviceRule newInstance(AspectRule aspectRule, AspectAdviceType aspectAdviceType) {
		AspectAdviceRule aspectAdviceRule = new AspectAdviceRule(aspectRule, aspectAdviceType);

		return aspectAdviceRule;
	}
//
//	public static void updateAction(AspectAdviceRule aspectAdviceRule, Parameters actionParameters) {
////		id = new ParameterDefine("id", ParameterValueType.STRING);
////		beanId = new ParameterDefine("beanId", ParameterValueType.STRING);
////		method = new ParameterDefine("method", ParameterValueType.STRING);
////		arguments = new ParameterDefine("argument", new ItemParameters(), true);
////		properties = new ParameterDefine("property", new ItemParameters(), true);
////		include = new ParameterDefine("include", ParameterValueType.STRING);
////		echo = new ParameterDefine("echo", new GenericParameters());
////		attributes = new ParameterDefine("attribute", new ItemParameters(), true);
////		hidden = new ParameterDefine("hidden", ParameterValueType.BOOLEAN);
//
//		String id = actionParameters.getString(ActionParameters.id);
//		String beanId = actionParameters.getString(ActionParameters.beanId);
//		String methodName = actionParameters.getString(ActionParameters.methodName);
//		String include = actionParameters.getString(ActionParameters.include);
//		boolean hidden = actionParameters.getBoolean(ActionParameters.include);
//		
//		if(beanId != null && methodName != null) {
//			BeanActionRule beanActionRule = BeanActionRule.newInstance(id, beanId, methodName, hidden);
//		}
//	}
	
}
