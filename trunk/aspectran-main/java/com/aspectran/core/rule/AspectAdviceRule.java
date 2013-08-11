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
import com.aspectran.core.type.AspectAdviceType;

/**
 * <p>Created: 2008. 04. 01 오후 11:19:28</p>
 */
public class AspectAdviceRule {

	private String aspectId;
	
	private AspectAdviceType aspectAdviceType;
	
	private Executable action;
	
	private ResponseByContentTypeRule responseByContentTypeRule;

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

	public void setEchoAction(EchoActionRule echoActionRule) {
		action = new EchoAction(echoActionRule, null);
	}

	public void setBeanAction(BeanActionRule beanActionRule) {
		action = new BeanAction(beanActionRule, null);
	}
	
	public Executable getAction() {
		return action;
	}

	public ResponseByContentTypeRule getResponseByContentTypeRule() {
		return responseByContentTypeRule;
	}

	public void setResponseByContentTypeRule(ResponseByContentTypeRule responseByContentTypeRule) {
		this.responseByContentTypeRule = responseByContentTypeRule;
	}

	
}
