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
package com.aspectran.core.context.rule;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;

/**
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class TransletRule implements ActionRuleApplicable, ResponseRuleApplicable, AspectAdviceSupport {

	private String name;

	private RequestRule requestRule;
	
	private ContentList contentList;

	private boolean explicitContent;

	private ResponseRule responseRule;
	
	/** The response rule list of child translet. */
	private List<ResponseRule> responseRuleList;
	
	private boolean implicitResponse;

	private ResponseByContentTypeRuleMap exceptionHandlingRuleMap;
	
	private Class<? extends Translet> transletInterfaceClass;
	
	private Class<? extends CoreTranslet> transletImplementClass;

	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	/**
	 * Instantiates a new translet rule.
	 */
	public TransletRule() {
	}

	/**
	 * Gets the translet name.
	 * 
	 * @return the translet name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the tranlset name.
	 * 
	 * @param name the new translet name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the request rule.
	 * 
	 * @return the request rule
	 */
	public RequestRule getRequestRule() {
		return requestRule;
	}

	/**
	 * Sets the request rule.
	 * 
	 * @param requestRule the new request rule
	 */
	public void setRequestRule(RequestRule requestRule) {
		this.requestRule = requestRule;
	}

	/**
	 * Gets the content list.
	 * 
	 * @return the content list
	 */
	public ContentList getContentList() {
		return contentList;
	}

	/**
	 * Sets the content list.
	 * 
	 * @param contentList the new content list
	 */
	public void setContentList(ContentList contentList) {
		this.contentList = contentList;
		
		if(contentList != null)
			explicitContent = true;
	}

	public ContentList touchContentList(boolean explicitContent) {
		this.explicitContent = explicitContent;
		return touchContentList();
	}
	
	public synchronized ContentList touchContentList() {
		if(contentList == null) {
			contentList = new ContentList();
			contentList.setOmittable(Boolean.TRUE);
		}
		
		return contentList;
	}

	public boolean isExplicitContent() {
		return explicitContent;
	}

	public void applyActionRule(EchoActionRule echoActionRule) {
		touchActionList().applyActionRule(echoActionRule);
	}

	public void applyActionRule(BeanActionRule beanActionRule) {
		touchActionList().applyActionRule(beanActionRule);
	}

	public void applyActionRule(IncludeActionRule includeActionRule) {
		touchActionList().applyActionRule(includeActionRule);
	}
	
	private ActionList touchActionList() {
		touchContentList();
		
		if(contentList.size() == 1) {
			return contentList.get(0);
		} else {
			return contentList.newActionList(true);
		}
	}
	
	/**
	 * Gets the response rule.
	 * 
	 * @return the response rule
	 */
	public ResponseRule getResponseRule() {
		return responseRule;
	}
	
	/**
	 * Sets the response rule.
	 * 
	 * @param responseRule the new response rule
	 */
	public void setResponseRule(ResponseRule responseRule) {
		this.responseRule = responseRule;
		implicitResponse = false;
	}
	
	public List<ResponseRule> getResponseRuleList() {
		return responseRuleList;
	}

	public void setResponseRuleList(List<ResponseRule> responseRuleList) {
		this.responseRuleList = responseRuleList;
		implicitResponse = false;
	}
	
	public void addResponseRule(ResponseRule responseRule) {
		if(responseRuleList == null)
			responseRuleList = new ArrayList<ResponseRule>();
		
		responseRuleList.add(responseRule);
		implicitResponse = false;
	}

	public Response applyResponseRule(TransformRule tr) {
		if(responseRule == null)
			responseRule = new ResponseRule();
		
		implicitResponse = true;

		return responseRule.applyResponseRule(tr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param drr the drr
	 * 
	 * @return the dispatch response
	 */
	public Response applyResponseRule(DispatchResponseRule drr) {
		if(responseRule == null)
			responseRule = new ResponseRule();

		implicitResponse = true;

		return responseRule.applyResponseRule(drr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param rrr the rrr
	 * 
	 * @return the redirect response
	 */
	public Response applyResponseRule(RedirectResponseRule rrr) {
		if(responseRule == null)
			responseRule = new ResponseRule();
		
		implicitResponse = true;
		
		return responseRule.applyResponseRule(rrr);
	}
	
	/**
	 * Adds the response rule.
	 * 
	 * @param frr the frr
	 * 
	 * @return the forward response
	 */
	public Response applyResponseRule(ForwardResponseRule frr) {
		if(responseRule == null)
			responseRule = new ResponseRule();
		
		implicitResponse = true;

		return responseRule.applyResponseRule(frr);
	}
	
	private void addActionList(ActionList actionList) {
		if(actionList == null)
			return;
		
		touchContentList();		
		contentList.add(actionList);
	}
	
	public void determineResponseRule() {
		if(responseRule == null) {
			responseRule = new ResponseRule();
		} else {
			if(responseRule.getResponse() != null)
				addActionList(responseRule.getResponse().getActionList());
		
			adoptTransletName(this, responseRule);
		}

		setResponseRuleList(null);
	}

	public boolean isImplicitResponse() {
		return implicitResponse;
	}

	public ResponseByContentTypeRuleMap getExceptionHandlingRuleMap() {
		return exceptionHandlingRuleMap;
	}

	public void setExceptionHandlingRuleMap(ResponseByContentTypeRuleMap responseByContentTypeRuleMap) {
		this.exceptionHandlingRuleMap = responseByContentTypeRuleMap;
	}

	public void addExceptionHandlingRule(ResponseByContentTypeRule responseByContentTypeRule) {
		if(exceptionHandlingRuleMap == null)
			exceptionHandlingRuleMap = new ResponseByContentTypeRuleMap();
		
		exceptionHandlingRuleMap.putResponseByContentTypeRule(responseByContentTypeRule);
	}
	
	public Class<? extends Translet> getTransletInterfaceClass() {
		return transletInterfaceClass;
	}

	public void setTransletInterfaceClass(Class<? extends Translet> transletInterfaceClass) {
		this.transletInterfaceClass = transletInterfaceClass;
	}

	public Class<? extends CoreTranslet> getTransletImplementClass() {
		return transletImplementClass;
	}

	public void setTransletImplementClass(Class<? extends CoreTranslet> transletImplementClass) {
		this.transletImplementClass = transletImplementClass;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(boolean clone) throws CloneNotSupportedException {
		if(clone && aspectAdviceRuleRegistry != null)
			return (AspectAdviceRuleRegistry)aspectAdviceRuleRegistry.clone();
		
		return aspectAdviceRuleRegistry;
	}

	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}
	
	public List<AspectAdviceRule> getBeforeAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getAfterAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getAfterAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getFinallyAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getExceptionRaizedAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name=").append(name);
		sb.append(", requestRule=").append(requestRule);
		sb.append(", responseRule=").append(responseRule);
		if(exceptionHandlingRuleMap != null)
			sb.append(", exceptionHandlingRuleMap=").append(exceptionHandlingRuleMap);
		if(transletInterfaceClass != null)
			sb.append(", transletInterfaceClass=").append(transletInterfaceClass);
		if(transletImplementClass != null)
			sb.append(", transletInstanceClass=").append(transletImplementClass);
		if(aspectAdviceRuleRegistry != null)
			sb.append(", aspectAdviceRuleRegistry=").append(aspectAdviceRuleRegistry);
		if(explicitContent)
		sb.append(", explicitContent=").append(explicitContent);
		if(implicitResponse)
			sb.append(", implicitResponse=").append(implicitResponse);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static TransletRule newSubTransletRule(TransletRule transletRule, ResponseRule responseRule) {
		RequestRule newRequestRule = new RequestRule();
		if(newRequestRule != null)
			newRequestRule = newRequestRule.clone();
		
		ContentList newContentList = transletRule.getContentList();
		if(newContentList != null)
			newContentList = (ContentList)newContentList.clone();
		
		TransletRule newTransletRule = new TransletRule();
		newTransletRule.setName(transletRule.getName());
		newTransletRule.setRequestRule(newRequestRule);
		newTransletRule.setContentList(newContentList);
		newTransletRule.setResponseRule(responseRule);
		newTransletRule.setExceptionHandlingRuleMap(transletRule.getExceptionHandlingRuleMap());
		newTransletRule.setTransletInterfaceClass(transletRule.getTransletInterfaceClass());
		newTransletRule.setTransletImplementClass(transletRule.getTransletImplementClass());
		
		return newTransletRule;
	}
	
	protected static void adoptTransletName(TransletRule transletRule, ResponseRule responseRule) {
		String responseName = responseRule.getName();
		
		if(responseName != null && responseName.length() > 0) {
			String transletName = transletRule.getName();

			if(responseName.charAt(0) == AspectranConstant.TRANSLET_NAME_EXTENSION_DELIMITER) {
				transletName += responseName;
			} else {
				transletName += AspectranConstant.TRANSLET_NAME_SEPARATOR + responseName;
			}
			
			transletRule.setName(transletName);
		}
	}
	
	protected static void unadoptTransletName(TransletRule transletRule, ResponseRule responseRule) {
		String responseName = responseRule.getName();

		if(responseName != null && responseName.length() > 0) {
			String transletName = transletRule.getName();
		
			if(transletName.endsWith(responseName)) {
				transletName = transletName.substring(0, transletName.length() - responseName.length());
			}
			
			transletRule.setName(transletName);
		}
	}
	
	public static TransletRule newInstance(String name) {
		if(name == null)
			throw new IllegalArgumentException("The <translet> element requires a name attribute.");

		TransletRule transletRule = new TransletRule();
		transletRule.setName(name);

		return transletRule;
	}
	
}
