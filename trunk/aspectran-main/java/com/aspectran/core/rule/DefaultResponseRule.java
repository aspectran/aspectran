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


/**
 * <p>Created: 2008. 04. 12 오후 12:06:15</p>
 */
public class DefaultResponseRule {

	protected String characterEncoding;
	
	protected String defaultContentType;
	
	protected DispatcherViewsRule dispatcherViewsRule;
	
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

	public String getDefaultContentType() {
		return defaultContentType;
	}

	public void setDefaultContentType(String defaultContentType) {
		this.defaultContentType = defaultContentType;
	}

	/**
	 * Gets the dispatcher views rule.
	 *
	 * @return the dispatcher views rule
	 */
	public DispatcherViewsRule getDispatcherViewsRule() {
		return dispatcherViewsRule;
	}

	/**
	 * Sets the dispatcher views rule.
	 *
	 * @param dispatcherViewsRule the new dispatcher views rule
	 */
	public void setDispatcherViewsRule(DispatcherViewsRule dispatcherViewsRule) {
		this.dispatcherViewsRule = dispatcherViewsRule;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[characterEncoding=").append(characterEncoding);
		sb.append(", defaultContentType=").append(defaultContentType);
		sb.append(", dispatcherViewsRule=").append(dispatcherViewsRule);
		sb.append("]");
		
		return sb.toString();
	}
}
