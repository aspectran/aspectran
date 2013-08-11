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

import java.util.List;

import com.aspectran.core.context.aspect.AspectAdviceRegistry;
import com.aspectran.core.type.RequestMethodType;


/**
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class RequestRule implements AspectAdviceSupport {

	public static final String CHARACTER_ENCODING_SETTING = "characterEncoding";
	
	private String characterEncoding;
	
	private MultipartRequestRule multipartRequestRule;

	private RequestMethodType method;

	private ItemRuleMap attributeItemRuleMap;

	private FileItemRuleMap fileItemRuleMap;
	
	private AspectAdviceRegistry aspectAdviceRegistry;
	
	public RequestRule() {
	}
	
	public RequestRule(DefaultRequestRule drr) {
		characterEncoding = drr.getCharacterEncoding();
		multipartRequestRule = drr.getMultipartRequestRule();
	}
	
	/**
	 * Gets the character encoding.
	 * 
	 * @return the character encoding
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	/**
	 * Sets the character encoding.
	 * 
	 * @param characterEncoding the new character encoding
	 */
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	/**
	 * Gets the multipart request rule.
	 * 
	 * @return the multipart request rule
	 */
	public MultipartRequestRule getMultipartRequestRule() {
		return multipartRequestRule;
	}

	/**
	 * Sets the multipart request rule.
	 * 
	 * @param multipartRequestRule the new multipart request rule
	 */
	public void setMultipartRequestRule(MultipartRequestRule multipartRequestRule) {
		this.multipartRequestRule = multipartRequestRule;
	}
	
	/**
	 * Gets the request method type.
	 * 
	 * @return the request method type
	 */
	public RequestMethodType getMethod() {
		return method;
	}

	/**
	 * Sets the request method type.
	 * 
	 * @param method the request method type
	 */
	public void setMethod(RequestMethodType method) {
		this.method = method;
	}

//
//	/**
//	 * Gets the ticket rule.
//	 * 
//	 * @param ticketId the ticket id
//	 * 
//	 * @return the ticket rule
//	 */
//	public TicketBeanActionRule getTicketBeanActionRule(String ticketId) {
//		return ticketBeanActionRuleMap.get(ticketId);
//	}
//
//	/**
//	 * Adds the ticket rule.
//	 * 
//	 * @param ticketRule the ticket rule
//	 */
//	public void addTicketBeanActionRule(TicketBeanActionRule ticketRule) {
//		if(ticketBeanActionRuleMap == null)
//			ticketBeanActionRuleMap = new TicketBeanActionRuleMap();
//		
//		ticketBeanActionRuleMap.putTicketRule(ticketRule);
//	}
//
//	/**
//	 * Gets the ticket rule map.
//	 * 
//	 * @return the ticket rule map
//	 */
//	public TicketBeanActionRuleMap getTicketBeanActionRuleMap() {
//		return ticketBeanActionRuleMap;
//	}
//
//	/**
//	 * Sets the ticket rule map.
//	 * 
//	 * @param ticketBeanActionRuleMap the new ticket rule map
//	 */
//	public void setTicketBeanActionRuleMap(TicketBeanActionRuleMap ticketBeanActionRuleMap) {
//		this.ticketBeanActionRuleMap = ticketBeanActionRuleMap;
//	}

	/**
	 * Gets the parameter rule map for attributes.
	 * 
	 * @return the parameter rule map for attributes
	 */
	public ItemRuleMap getAttributeItemRuleMap() {
		return attributeItemRuleMap;
	}

	/**
	 * Sets the parameter rule map for attributes.
	 * 
	 * @param parameterRuleMap the new parameter rule map for attributes
	 */
	public void setAttributeItemRuleMap(ItemRuleMap attributeItemRuleMap) {
		this.attributeItemRuleMap = attributeItemRuleMap;
	}

	/**
	 * Gets the file item rule map.
	 * 
	 * @return the file item rule map
	 */
	public FileItemRuleMap getFileItemRuleMap() {
		return fileItemRuleMap;
	}

	/**
	 * Sets the file item rule map.
	 * 
	 * @param fileItemRuleMap the new file item rule map
	 */
	public void setFileItemRuleMap(FileItemRuleMap fileItemRuleMap) {
		this.fileItemRuleMap = fileItemRuleMap;
	}
	
	/**
	 * Adds the parameter rule for attributes.
	 * 
	 * @param attributeItemRule the parameter rule for attributes
	 */
	public void addAttributeItemRule(ItemRule attributeItemRule) {
		if(attributeItemRuleMap == null) 
			attributeItemRuleMap = new ItemRuleMap();
		
		attributeItemRuleMap.putItemRule(attributeItemRule);
	}
	
	/**
	 * Adds the file item rule.
	 * 
	 * @param fileItemRule the file item rule
	 */
	public void addFileItemRule(FileItemRule fileItemRule) {
		if(fileItemRuleMap == null) 
			fileItemRuleMap = new FileItemRuleMap();
		
		fileItemRuleMap.putFileItemRule(fileItemRule);
	}
	
	/**
	 * Gets the multipart temporary file path.
	 * 
	 * @return the multipart temporary file path
	 */
	public String getMultipartTemporaryFilePath() {
		if(multipartRequestRule == null)
			return null;
		
		return multipartRequestRule.getTemporaryFilePath();
	}

	/**
	 * Gets the max multipart request size.
	 * 
	 * @return the max multipart request size
	 */
	public long getMaxMultipartRequestSize() {
		if(multipartRequestRule == null)
			return 0;
		
		return multipartRequestRule.getMaxRequestSize();
	}
	
	public AspectAdviceRegistry getAspectAdviceRegistry() {
		return aspectAdviceRegistry;
	}

	public void setAspectAdviceRegistry(AspectAdviceRegistry aspectAdviceRegistry) {
		this.aspectAdviceRegistry = aspectAdviceRegistry;
		
		String characterEncoding = (String)aspectAdviceRegistry.getSetting(CHARACTER_ENCODING_SETTING);
		
		if(this.characterEncoding != null)
			this.characterEncoding = characterEncoding;
	}
	
	public List<AspectAdviceRule> getBeforeAdviceRuleList() {
		if(aspectAdviceRegistry == null)
			return null;
		
		return aspectAdviceRegistry.getBeforeAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getAfterAdviceRuleList() {
		if(aspectAdviceRegistry == null)
			return null;
		
		return aspectAdviceRegistry.getAfterAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getFinallyAdviceRuleList() {
		if(aspectAdviceRegistry == null)
			return null;
		
		return aspectAdviceRegistry.getFinallyAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getExceptionRaizedAdviceRuleList() {
		if(aspectAdviceRegistry == null)
			return null;
		
		return aspectAdviceRegistry.getExceptionRaizedAdviceRuleList();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{method=").append(method);
		sb.append(", characterEncoding=").append(characterEncoding);
		
		if(attributeItemRuleMap != null) {
			sb.append(", attributes=[");
			int sbLength = sb.length();

			for(String name : attributeItemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}

			sb.append("]");
		}
		
		if(fileItemRuleMap != null) {
			sb.append(", fileItems=[");
			int sbLength = sb.length();
			
			for(String name : fileItemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}
			
			sb.append("]");
		}
		
		sb.append("}");
		
		return sb.toString();
	}
}
