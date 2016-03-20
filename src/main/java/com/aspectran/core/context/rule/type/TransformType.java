/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.rule.type;

/**
 * The enum TransformType.
 * 
 * <p>Created: 2008. 04. 25 AM 16:47:38</p>
 */
public enum TransformType {

	XML("transform/xml"),
	XSL("transform/xsl"),
	TEXT("transform/text"),
	JSON("transform/json"),
	APON("transform/apon");

	private final String alias;

	private TransformType(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return this.alias;
	}

	/**
	 * Returns a <code>TransformType</code> with a value represented by the specified String.
	 *
	 * @param alias the specified String
	 * @return the transform type
	 */
	public static TransformType lookup(String alias) {
		for(TransformType type : TransformType.class.getEnumConstants()) {
			if(type.alias.equals(alias))
				return type;
		}
		return null;
	}

	/**
	 * Returns a <code>TransformType</code> with a value corresponding to the specified ContentType.
	 *
	 * @param contentType the content type
	 * @return the transform type
	 */
	public static TransformType lookup(ContentType contentType) {
		if(contentType == ContentType.TEXT_PLAIN)
			return TEXT;
		else if(contentType == ContentType.TEXT_XML)
			return XML;
		else if(contentType == ContentType.TEXT_JSON)
			return JSON;
		else if(contentType == ContentType.TEXT_APON)
			return APON;
		
		return null;
	}

}
