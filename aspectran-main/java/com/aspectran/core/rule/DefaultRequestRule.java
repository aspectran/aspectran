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
 * <p>Created: 2008. 04. 12 오전 11:48:14</p>
 * @deprecated
 */
public class DefaultRequestRule {
	
	protected String characterEncoding;
	
	protected MultipartRequestRule multipartRequestRule;

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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[characterEncoding=").append(characterEncoding);
		sb.append(", MultipartRequestRule=").append("[");
		sb.append(multipartRequestRule).append("]");
		sb.append("]");
		
		return sb.toString();
	}
}
