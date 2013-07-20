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
package com.aspectran.core.context.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.rule.BeanRuleMap;
import com.aspectran.core.rule.DefaultRequestRule;
import com.aspectran.core.rule.DefaultResponseRule;
import com.aspectran.core.rule.ExceptionHandleRule;
import com.aspectran.core.rule.TicketCheckRule;
import com.aspectran.core.rule.TicketCheckcaseRule;
import com.aspectran.core.rule.TicketCheckcaseRuleMap;
import com.aspectran.core.ticket.TicketCheckActionList;
import com.aspectran.core.util.ArrayStack;

/**
 * <p>Created: 2008. 04. 01 오후 10:25:35</p>
 */
public class BakContextBuilderAssistant {

	/** The use namespaces. */
	private boolean useNamespaces;

	/** The nullable content id. */
	private boolean nullableContentId = true;
	
	/** The nullable action id. */
	private boolean nullableActionId = true;
	
	/** The nullable bean id. */
	private boolean nullableBeanId = true;

	/** The service root path. */
	private String serviceRootPath;
	
	/** The service name. */
	private String serviceName;
	
	/** The request uri pattern. */
	private String requestUriPattern;
	
	/** The reqeust uri pattern prefix. */
	private String reqeustUriPatternPrefix;

	/** The reqeust uri pattern suffix. */
	private String reqeustUriPatternSuffix;

	/** The ticket checkcase rule map. */
	private TicketCheckcaseRuleMap ticketCheckcaseRuleMap;
	
	/** The ticket check action list. */
	private TicketCheckActionList ticketCheckActionList;
	
	/** The generic request rule. */
	private DefaultRequestRule genericRequestRule;

	/** The generic response rule. */
	private DefaultResponseRule genericResponseRule;
	
	/** The generic exception rule. */
	private ExceptionHandleRule genericExceptionRule;

	/** The bean map resources. */
	private List<ContextResourceFactory> beanMapResources;

	/** The translet map resources. */
	private List<ContextResourceFactory> transletMapResources;
	
	/** The bean rule map. */
	private BeanRuleMap beanRuleMap;
	
	/** The object stack. */
	private ArrayStack objectStack;
	
	/** The namespace. */
	private String namespace;
	
	/** The type aliases. */
	private Map<String, String> typeAliases;

	/**
	 * Instantiates a new translets config.
	 */
	public BakContextBuilderAssistant() {
		this.transletMapResources = new ArrayList<ContextResourceFactory>();
		this.objectStack = new ArrayStack(); 
		this.typeAliases = new HashMap<String, String>();
	}

	/**
	 * Push object.
	 * 
	 * @param item the item
	 */
	public void pushObject(Object item) {
		objectStack.push(item);
	}
	
	/**
	 * Pop object.
	 * 
	 * @return the object
	 */
	public Object popObject() {
		return objectStack.pop();
	}
	
	/**
	 * Peek object.
	 * 
	 * @return the object
	 */
	public Object peekObject() {
		return objectStack.peek();
	}
	
	/**
	 * Clear object stack.
	 */
	public void clearObjectStack() {
		objectStack.clear();
	}
	
	/**
	 * Clear type aliases.
	 */
	public void clearTypeAliases() {
		typeAliases.clear();
	}

	/**
	 * Adds the type alias.
	 * 
	 * @param alias the alias
	 * @param type the type
	 */
	public void addTypeAlias(String alias, String type) {
		typeAliases.put(alias, type);
	}
	
	/**
	 * Gets the alias type.
	 * 
	 * @param alias the alias
	 * 
	 * @return the alias type
	 */
	public String getAliasType(String alias) {
		return typeAliases.get(alias);
	}
	
	/**
	 * Apply namespace for a translet.
	 * 
	 * @param path the path
	 * 
	 * @return the string
	 */
	public String applyTransletNamespace(String path) {
		if(useNamespaces) {
			if(namespace != null && !path.startsWith(URI_SEPARATOR)) {
				StringBuilder sb = new StringBuilder(64);
				sb.append(namespace).append(URI_SEPARATOR).append(path);
				return sb.toString();
			}
		}
		
		return path;
	}

	/**
	 * Apply namespace for a ticket or a plugin.
	 * 
	 * @param name the path
	 * 
	 * @return the string
	 */
	public String applyNamespace(String name) {
		if(useNamespaces) {
			if(namespace != null) {
				StringBuilder sb = new StringBuilder(64);
				sb.append(namespace).append(NAMESPACE_SEPARATOR).append(name);
				return sb.toString();
			}
		}
		
		return name;
	}
	
	/**
	 * Sets the namespace.
	 * 
	 * @param namespace the new namespace
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Checks if is use namespaces.
	 * 
	 * @return true, if is usenamespaces
	 */
	public boolean isUseNamespaces() {
		return useNamespaces;
	}

	/**
	 * Sets the use namespaces.
	 * 
	 * @param useNamespaces the new use namespaces
	 */
	public void setUseNamespaces(boolean useNamespaces) {
		this.useNamespaces = useNamespaces;
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

	/**
	 * Sets the allow null action id.
	 * 
	 * @param nullableActionId the new allow null action id
	 */
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

	/**
	 * Sets the nullable bean id.
	 *
	 * @param nullableBeanId the new nullable bean id
	 */
	public void setNullableBeanId(boolean nullableBeanId) {
		this.nullableBeanId = nullableBeanId;
	}

	/**
	 * Gets the service root path.
	 * 
	 * @return the service root path
	 */
	public String getServiceRootPath() {
		return serviceRootPath;
	}

	/**
	 * Sets the service root path.
	 * 
	 * @param serviceRootPath the new service root path
	 */
	public void setServiceRootPath(String serviceRootPath) {
		this.serviceRootPath = serviceRootPath;
	}

	/**
	 * Gets the service name.
	 * 
	 * @return the service name
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Sets the service name.
	 * 
	 * @param serviceName the new service name
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Gets the uri pattern.
	 * 
	 * @return the uri pattern
	 */
	public String getRequestUriPattern() {
		return requestUriPattern;
	}

	/**
	 * Sets the uri pattern.
	 * 
	 * @param uriPattern the new uri pattern
	 */
	public void setRequestUriPattern(String uriPattern) {
		this.requestUriPattern = uriPattern;
		
		if(uriPattern != null) {
			int index = uriPattern.indexOf(WILDCARD_CHAR);
			
			if(index != -1) {
				if(index == 0) {
					reqeustUriPatternSuffix = uriPattern.substring(WILDCARD_CHAR.length());
				} else if(index == (uriPattern.length() - 1)) {
					reqeustUriPatternPrefix = uriPattern.substring(0, uriPattern.length() - WILDCARD_CHAR.length());
				} else {
					reqeustUriPatternPrefix = uriPattern.substring(0, index);
					reqeustUriPatternSuffix = uriPattern.substring(index + 1);
				}
			}
		}
	}
	
	/**
	 * Sets the uri pattern.
	 * 
	 * @param uriPatternPrefix the uri pattern prefix
	 * @param uriPatternSuffix the uri pattern suffix
	 */
	public void setRequestUriPattern(String uriPatternPrefix, String uriPatternSuffix) {
		requestUriPattern = uriPatternPrefix + WILDCARD_CHAR + uriPatternSuffix;
	}
	
	/**
	 * Sets the uri prefix pattern.
	 * 
	 * @param requestUriPatternPrefix the new uri pattern prefix
	 */
	public void setRequestUriPatternPrefix(String requestUriPatternPrefix) {
		this.reqeustUriPatternPrefix = requestUriPatternPrefix;
		
		if(reqeustUriPatternSuffix != null)
			setRequestUriPattern(requestUriPatternPrefix, reqeustUriPatternSuffix);
	}
	
	/**
	 * Sets the uri suffix pattern.
	 * 
	 * @param requestUriPatternSuffix the new uri pattern suffix
	 */
	public void setRequestUriPatternSuffix(String requestUriPatternSuffix) {
		this.reqeustUriPatternSuffix = requestUriPatternSuffix;
		
		if(reqeustUriPatternPrefix != null)
			setRequestUriPattern(reqeustUriPatternPrefix, requestUriPatternSuffix);
	}
	
	/**
	 * Gets the uri pattern prefix.
	 * 
	 * @return the uri pattern prefix
	 */
	public String getRequestUriPatternPrefix() {
		return reqeustUriPatternPrefix;
	}

	/**
	 * Gets the uri pattern suffix.
	 * 
	 * @return the uri pattern suffix
	 */
	public String getRequestUriPatternSuffix() {
		return reqeustUriPatternSuffix;
	}

	/**
	 * Gets the generic request rule.
	 * 
	 * @return the generic request rule
	 */
	public DefaultRequestRule getGenericRequestRule() {
		return genericRequestRule;
	}

	/**
	 * Sets the generic request rule.
	 * 
	 * @param genericRequestRule the new generic request rule
	 */
	public void setGenericRequestRule(DefaultRequestRule genericRequestRule) {
		this.genericRequestRule = genericRequestRule;
	}

	/**
	 * Gets the generic response rule.
	 * 
	 * @return the generic response rule
	 */
	public DefaultResponseRule getGenericResponseRule() {
		return genericResponseRule;
	}

	/**
	 * Sets the generic response rule.
	 * 
	 * @param genericResponseRule the new generic response rule
	 */
	public void setGenericResponseRule(DefaultResponseRule genericResponseRule) {
		this.genericResponseRule = genericResponseRule;
	}

	/**
	 * Gets the generic exception rule.
	 * 
	 * @return the generic exception rule
	 */
	public ExceptionHandleRule getGenericExceptionRule() {
		return genericExceptionRule;
	}

	/**
	 * Sets the generic exception rule.
	 * 
	 * @param genericExceptionRule the new generic exception rule
	 */
	public void setGenericExceptionRule(ExceptionHandleRule genericExceptionRule) {
		this.genericExceptionRule = genericExceptionRule;
	}

	/**
	 * Gets the bean map resources.
	 * 
	 * @return the bean map resources
	 */
	public List<ContextResourceFactory> getBeanMapResources() {
		return beanMapResources;
	}

	/**
	 * Adds the bean map resource.
	 * 
	 * @param mapResource the map resource
	 */
	public void addBeanMapResource(ContextResourceFactory mapResource) {
		if(beanMapResources == null)
			beanMapResources = new ArrayList<ContextResourceFactory>();
		
		beanMapResources.add(mapResource);
	}

	/**
	 * Gets the translet map resources.
	 * 
	 * @return the translet map resources
	 */
	public List<ContextResourceFactory> getTransletMapResources() {
		return transletMapResources;
	}
	
	/**
	 * Adds the translet map resource.
	 * 
	 * @param mapResource the map resource
	 */
	public void addTransletMapResource(ContextResourceFactory mapResource) {
		if(transletMapResources == null)
			transletMapResources = new ArrayList<ContextResourceFactory>();
		
		transletMapResources.add(mapResource);
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
		File file;

		if(serviceRootPath != null && !filePath.startsWith("/"))
			file = new File(serviceRootPath, filePath);
		else
			file = new File(filePath);
		
		return file;
	}
	
	/**
	 * To service uri.
	 * 
	 * @param transletPath the translet path
	 * 
	 * @return the string
	 */
	public String toRequestUri(String transletPath) {
		return BakContextBuilderAssistant.toRequestUri(reqeustUriPatternPrefix, reqeustUriPatternSuffix, transletPath);
	}
	
	/**
	 * To translet path.
	 * 
	 * @param uriPatternPrefix the uri pattern prefix
	 * @param uriPatternSuffix the uri pattern suffix
	 * @param serviceUri the service uri
	 * 
	 * @return the string
	 */
	public static String toTransletPath(String uriPatternPrefix, String uriPatternSuffix, String serviceUri) {
		if(uriPatternPrefix == null || uriPatternSuffix == null)
			return serviceUri;
		
		int beginIndex;
		int endIndex;
		
		if(uriPatternPrefix != null && serviceUri.startsWith(uriPatternPrefix))
			beginIndex = uriPatternPrefix.length();
		else
			beginIndex = 0;
		
		if(uriPatternSuffix != null && serviceUri.endsWith(uriPatternSuffix))
			endIndex = serviceUri.length() - uriPatternSuffix.length();
		else
			endIndex = serviceUri.length();
		
		return serviceUri.substring(beginIndex, endIndex);
	}
	
	/**
	 * To service uri.
	 * 
	 * @param requestUriPatternPrefix the uri pattern prefix
	 * @param requestUriPatternSuffix the uri pattern suffix
	 * @param transletPath the translet path
	 * 
	 * @return the string
	 */
	public static String toRequestUri(String requestUriPatternPrefix, String requestUriPatternSuffix, String transletPath) {
		if(requestUriPatternPrefix == null || requestUriPatternSuffix == null || transletPath == null)
			return transletPath;
		
		StringBuilder sb = new StringBuilder(128);
		
		if(requestUriPatternPrefix != null)
			sb.append(requestUriPatternPrefix);

		sb.append(transletPath);
		
		if(requestUriPatternSuffix != null)
			sb.append(requestUriPatternSuffix);
		
		return sb.toString();
	}
}
