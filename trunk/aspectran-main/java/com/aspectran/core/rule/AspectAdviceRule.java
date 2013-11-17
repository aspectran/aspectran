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
package com.aspectran.core.rule;

import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.rule.ability.ActionSettable;
import com.aspectran.core.type.AspectAdviceType;

/**
 * <p>Created: 2008. 04. 01 오후 11:19:28</p>
 */
public class AspectAdviceRule implements ActionSettable {

	private String aspectId;
	
	private AspectAdviceType aspectAdviceType;
	
	private String adviceBeanId;
	
	private Executable action;
	
	private ResponseByContentTypeRuleMap responseByContentTypeRuleMap;

	public String getAspectId() {
		return aspectId;
	}

	public void setAspectId(String aspectId) {
		this.aspectId = aspectId;
	}

	public AspectAdviceType getAspectAdviceType() {
		return aspectAdviceType;
	}

	public void setAspectAdviceType(AspectAdviceType aspectAdviceType) {
		this.aspectAdviceType = aspectAdviceType;
	}

	public String getAdviceBeanId() {
		return adviceBeanId;
	}

	public void setAdviceBeanId(String adviceBeanId) {
		this.adviceBeanId = adviceBeanId;
	}

	public void setEchoAction(EchoActionRule echoActionRule) {
		action = new EchoAction(echoActionRule, null);
	}

	public void setBeanAction(BeanActionRule beanActionRule) {
		beanActionRule.setAspectAdviceRule(this);
		action = new BeanAction(beanActionRule, null);
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

		sb.append("{aspectId=").append(aspectId);
		sb.append(", aspectAdviceType=").append(aspectAdviceType);
		sb.append(", action=").append(action);
		sb.append(", responseByContentTypeRuleMap=").append(responseByContentTypeRuleMap);
		sb.append("}");
		
		return sb.toString();
	}
	
}
