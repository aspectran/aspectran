/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.base.context.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.aspectran.base.io.Resource;
import com.aspectran.base.rule.ActivityRule;
import com.aspectran.base.rule.BeanRule;
import com.aspectran.base.rule.BeanRuleMap;
import com.aspectran.base.rule.DefaultRequestRule;
import com.aspectran.base.rule.DefaultResponseRule;
import com.aspectran.base.rule.ExceptionHandleRule;
import com.aspectran.base.rule.MultiActivityTransletRuleMap;
import com.aspectran.base.rule.TicketCheckRule;
import com.aspectran.base.rule.TicketCheckcaseRule;
import com.aspectran.base.rule.TicketCheckcaseRuleMap;
import com.aspectran.base.rule.TransletRule;
import com.aspectran.base.rule.TransletRuleMap;
import com.aspectran.core.activity.ticket.TicketCheckActionList;

/**
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class AspectranContextBuilderAssistant extends AbstractContextBuilderAssistant {

	/** The use namespaces. */
	//private boolean useNamespaces;

	/** The nullable content id. */
	private boolean nullableContentId = true;
	
	/** The nullable action id. */
	private boolean nullableActionId = true;
	
	private boolean nullableBeanId = true;

	private boolean multiActivityEnable = false;
	
	/** The service root path. */
	private String activityRootPath;

	private ActivityRule activityRule;
	
	/** The ticket checkcase rule map. */
	private TicketCheckcaseRuleMap ticketCheckcaseRuleMap;
	
	/** The ticket check action list. */
	private TicketCheckActionList ticketCheckActionList;

	/** The generic request rule. */
	private DefaultRequestRule defaultRequestRule;

	/** The generic response rule. */
	private DefaultResponseRule defaultResponseRule;
	
	/** The generic exception rule. */
	private ExceptionHandleRule defaultExceptionRule;

	/** The translet map resources. */
	private List<Resource> resources;
	
	/** The bean rule map. */
	private BeanRuleMap beanRuleMap;
	
	private TransletRuleMap transletRuleMap;

	private MultiActivityTransletRuleMap multiActivityTransletRuleMap;
	
	/**
	 * Instantiates a new translets config.
	 */
	public AspectranContextBuilderAssistant(String serviceRootPath) {
		this.activityRootPath = serviceRootPath;
	}
	
	/**
	 * 상속.
	 * Instantiates a new activity context builder assistant.
	 *
	 * @param assistant the assistant
	 */
	public AspectranContextBuilderAssistant(AspectranContextBuilderAssistant assistant) {
		setSettings(assistant.getSettings());
		activityRootPath = assistant.getActivityRootPath();
		activityRule = assistant.getActivityRule();
		ticketCheckcaseRuleMap = assistant.getTicketCheckcaseRuleMap();
		defaultRequestRule = assistant.getDefaultRequestRule();
		defaultResponseRule = assistant.getDefaultResponseRule();
		defaultExceptionRule = assistant.getDefaultExceptionRule();
		
		if(assistant.getTicketCheckActionList() != null) {
			ticketCheckActionList = new TicketCheckActionList(assistant.getTicketCheckActionList());
		}
		
		transletRuleMap = assistant.getTransletRuleMap();
	}
	
	public void applySettings() {
		useNamespaces = isSettedTrue(AspectranContextConstant.USE_NAMESPACES_SETTING);
		nullableContentId = isSettedTrue(AspectranContextConstant.NULLABLE_CONTENT_ID_SETTING);
		nullableActionId = isSettedTrue(AspectranContextConstant.NULLABLE_ACTION_ID_SETTING);
		multiActivityEnable = isSettedTrue(AspectranContextConstant.MULTI_ACTIVITY_ENABLE);
	}
	
	/**
	 * Apply namespace for a translet.
	 * 
	 * @param transletName the name
	 * 
	 * @return the string
	 */
	public String applyNamespaceForTranslet(String transletName) {
		StringBuilder sb = new StringBuilder();
		
		if(activityRule.getTransletNamePatternPrefix() != null)
			sb.append(activityRule.getTransletNamePatternPrefix());
		
		if(useNamespaces && namespace != null) {
			sb.append(namespace);
			sb.append(AspectranContextConstant.TRANSLET_NAME_SEPARATOR);
		}
			
		sb.append(transletName);
		
		if(activityRule.getTransletNamePatternSuffix() != null)
			sb.append(activityRule.getTransletNamePatternSuffix());
		
		return sb.toString();
	}
	
	public String applyNamespaceForBean(String beanId) {
		if(!useNamespaces || namespace == null)
			return beanId;
		
		StringBuilder sb = new StringBuilder();
		sb.append(namespace);
		sb.append(AspectranContextConstant.BEAN_ID_SEPARATOR);
		sb.append(beanId);
		return sb.toString();
	}
	
	public String replaceTransletNameSuffix(String name, String transletNameSuffix) {
		if(activityRule.getTransletNamePatternSuffix() == null)
			return name + transletNameSuffix;
		
		int index = name.indexOf(activityRule.getTransletNamePatternSuffix());
		
		StringBuilder sb = new StringBuilder();
		sb.append(name.substring(0, index));
		sb.append(AspectranContextConstant.TRANSLET_NAME_SUFFIX_SEPARATOR);
		sb.append(transletNameSuffix);
		
		return sb.toString();
	}
	
	/**
	 * Checks if is allow null content id.
	 * 
	 * @return true, if is allow null content id
	 */
	public boolean isNullableContentId() {
		return nullableContentId;
	}

	/**
	 * Sets the allow null content id.
	 * 
	 * @param nullableContentId the new allow null content id
	 */
	public void setNullableContentId(boolean nullableContentId) {
		this.nullableContentId = nullableContentId;
	}

	/**
	 * Checks if is allow null action id.
	 * 
	 * @return true, if is allow null action id
	 */
	public boolean isNullableActionId() {
		return nullableActionId;
	}
	
	public void setNullableActionId(boolean nullableActionId) {
		this.nullableActionId = nullableActionId;
	}

	/**
	 * Checks if is nullable bean id.
	 *
	 * @return true, if is nullable bean id
	 */
	public boolean isNullableBeanId() {
		return nullableBeanId;
	}

	public boolean isMultiActivityEnable() {
		return multiActivityEnable;
	}

	public void setMultiActivityEnable(boolean multiActivityEnable) {
		this.multiActivityEnable = multiActivityEnable;
	}

	public void setNullableBeanId(boolean nullableBeanId) {
		this.nullableBeanId = nullableBeanId;
	}

	public String getActivityRootPath() {
		return activityRootPath;
	}
	
	public ActivityRule getActivityRule() {
		return activityRule;
	}

	public void setActivityRule(ActivityRule activityRule) {
		activityRule.setActivityRootPath(activityRootPath);
		this.activityRule = activityRule;
	}

	/**
	 * Gets the generic request rule.
	 * 
	 * @return the generic request rule
	 */
	public DefaultRequestRule getDefaultRequestRule() {
		return defaultRequestRule;
	}

	/**
	 * Sets the generic request rule.
	 * 
	 * @param defaultRequestRule the new generic request rule
	 */
	public void setDefaultRequestRule(DefaultRequestRule defaultRequestRule) {
		if(this.defaultRequestRule.getCharacterEncoding() != null && defaultRequestRule.getCharacterEncoding() == null)
			defaultRequestRule.setCharacterEncoding(this.defaultRequestRule.getCharacterEncoding());
		
		if(this.defaultRequestRule.getMultipartRequestRule() != null && defaultRequestRule.getMultipartRequestRule() == null)
			defaultRequestRule.setMultipartRequestRule(this.defaultRequestRule.getMultipartRequestRule());
		
		this.defaultRequestRule = defaultRequestRule;
	}

	/**
	 * Gets the generic response rule.
	 * 
	 * @return the generic response rule
	 */
	public DefaultResponseRule getDefaultResponseRule() {
		return defaultResponseRule;
	}

	/**
	 * Sets the generic response rule.
	 * 
	 * @param defaultResponseRule the new generic response rule
	 */
	public void setDefaultResponseRule(DefaultResponseRule defaultResponseRule) {
		if(this.defaultResponseRule.getCharacterEncoding() != null && defaultResponseRule.getCharacterEncoding() == null)
			defaultResponseRule.setCharacterEncoding(this.defaultResponseRule.getCharacterEncoding());
		
		if(this.defaultResponseRule.getDefaultContentType() != null && defaultResponseRule.getDefaultContentType() == null)
			defaultResponseRule.setDefaultContentType(this.defaultResponseRule.getDefaultContentType());

		this.defaultResponseRule = defaultResponseRule;
	}

	/**
	 * Gets the generic exception rule.
	 * 
	 * @return the generic exception rule
	 */
	public ExceptionHandleRule getDefaultExceptionRule() {
		return defaultExceptionRule;
	}

	/**
	 * Sets the generic exception rule.
	 * 
	 * @param defaultExceptionRule the new generic exception rule
	 */
	public void setDefaultExceptionRule(ExceptionHandleRule defaultExceptionRule) {
		this.defaultExceptionRule = defaultExceptionRule;
	}

	/**
	 * Gets the translet map resources.
	 * 
	 * @return the translet map resources
	 */
	public List<Resource> getResources() {
		return resources;
	}
	
	/**
	 * Adds the translet map resource.
	 * 
	 * @param resource the map resource
	 */
	public void addResource(Resource resource) {
		if(resources == null)
			resources = new ArrayList<Resource>();
		
		resources.add(resource);
	}
	
	/**
	 * Gets the bean rule map.
	 * 
	 * @return the bean rule map
	 */
	public BeanRuleMap getBeanRuleMap() {
		return beanRuleMap;
	}

	/**
	 * Sets the bean rule map.
	 * 
	 * @param beanRuleMap the new bean rule map
	 */
	public void setBeanRuleMap(BeanRuleMap beanRuleMap) {
		this.beanRuleMap = beanRuleMap;
	}
	
	/**
	 * Adds the bean rule.
	 *
	 * @param beanRule the bean rule
	 */
	public void addBeanRule(BeanRule beanRule) {
		if(beanRuleMap == null)
			beanRuleMap = new BeanRuleMap();
		
		beanRuleMap.putBeanRule(beanRule);
	}
	
	/**
	 * Gets the ticket checkcase rule map.
	 *
	 * @return the ticket checkcase rule map
	 */
	public TicketCheckcaseRuleMap getTicketCheckcaseRuleMap() {
		return ticketCheckcaseRuleMap;
	}
	
	public TicketCheckcaseRule getTicketCheckcaseRule(String checkcaseId) {
		if(ticketCheckcaseRuleMap == null)
			return null;
		
		return ticketCheckcaseRuleMap.get(checkcaseId);
	}

	/**
	 * Sets the ticket checkcase rule map.
	 *
	 * @param ticketCheckcaseRuleMap the new ticket checkcase rule map
	 */
	public void setTicketCheckcaseRuleMap(TicketCheckcaseRuleMap ticketCheckcaseRuleMap) {
		this.ticketCheckcaseRuleMap = ticketCheckcaseRuleMap;
	}

	/**
	 * Adds the ticket checkcase rule map.
	 *
	 * @param ticketCheckcaseRule the ticket checkcase rule
	 */
	public void addTicketCheckcaseRule(TicketCheckcaseRule ticketCheckcaseRule) {
		if(ticketCheckcaseRuleMap == null)
			ticketCheckcaseRuleMap = new TicketCheckcaseRuleMap();
		
		ticketCheckcaseRuleMap.putTicketCheckcaseRule(ticketCheckcaseRule);
	}
	
	/**
	 * Gets the ticket bean action list.
	 *
	 * @return the ticket bean action list
	 */
	public TicketCheckActionList getTicketCheckActionList() {
		return ticketCheckActionList;
	}
	
	/**
	 * Sets the ticket bean action list.
	 *
	 * @param ticketCheckActionList the new ticket bean action list
	 */
	public void setTicketCheckActionList(TicketCheckActionList ticketCheckActionList) {
		this.ticketCheckActionList = ticketCheckActionList;
	}
	
	/**
	 * Adds the ticket bean action.
	 *
	 * @param ticketCheckRule the ticket bean action rule
	 */
	public void addTicketCheckAction(TicketCheckRule ticketCheckRule) {
		if(ticketCheckActionList == null)
			ticketCheckActionList = new TicketCheckActionList();
		
		ticketCheckActionList.addTicketCheckAction(ticketCheckRule);
	}
	
	/**
	 * To real path as file.
	 * 
	 * @param filePath the file path
	 * 
	 * @return the file
	 */
	public File toRealPathAsFile(String filePath) {
		if(activityRule == null)
			return null;

		return activityRule.toRealPathAsFile(filePath);
	}
	
	public TransletRuleMap getTransletRuleMap() {
		return transletRuleMap;
	}
	
	public MultiActivityTransletRuleMap getMultiActivityTransletRuleMap() {
		return multiActivityTransletRuleMap;
	}

	public void addTransletRule(TransletRule transletRule) {
		if(transletRuleMap == null)
			transletRuleMap = new TransletRuleMap();
		
		transletRuleMap.put(transletRule.getName(), transletRule);
	}
	
	public void addMultiActivityTransletRule(String transletName, String responseId, TransletRule transletRule) {
		if(multiActivityTransletRuleMap == null)
			multiActivityTransletRuleMap = new MultiActivityTransletRuleMap();

		multiActivityTransletRuleMap.putMultiActivityTransletRule(transletName, responseId, transletRule);
	}
	
}
