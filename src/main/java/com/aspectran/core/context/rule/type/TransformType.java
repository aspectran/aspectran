/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.rule.type;

import java.util.HashMap;
import java.util.Map;


/**
 * Transformation type for repsponse.
 * 
 * <p>Created: 2008. 04. 25 오전 16:47:38</p>
 */
public final class TransformType extends Type {

	/** The "transform/xml" transform type. */
	public static final TransformType XML_TRANSFORM;

	/** The "transform/xsl" transform type. */
	public static final TransformType XSL_TRANSFORM;

	/** The "transform/text" transform type. */
	public static final TransformType TEXT_TRANSFORM;
	
	/** The "transform/json" transform type. */
	public static final TransformType JSON_TRANSFORM;

	/** The "transform/apon" transform type. */
	public static final TransformType APON_TRANSFORM;
	
	private static final Map<String, TransformType> types;
	
	static {
		XML_TRANSFORM = new TransformType("transform/xml");
		XSL_TRANSFORM = new TransformType("transform/xsl");
		TEXT_TRANSFORM = new TransformType("transform/text");
		JSON_TRANSFORM = new TransformType("transform/json");
		APON_TRANSFORM = new TransformType("transform/apon");

		types = new HashMap<String, TransformType>();
		types.put(XML_TRANSFORM.toString(), XML_TRANSFORM);
		types.put(XSL_TRANSFORM.toString(), XSL_TRANSFORM);
		types.put(TEXT_TRANSFORM.toString(), TEXT_TRANSFORM);
		types.put(JSON_TRANSFORM.toString(), JSON_TRANSFORM);
		types.put(APON_TRANSFORM.toString(), APON_TRANSFORM);
	}

	/**
	 * Instantiates a new transform type.
	 * 
	 * @param type the type
	 */
	private TransformType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>TransformType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the transform type
	 */
	public static TransformType valueOf(String type) {
		if(type == null)
			return null;
		
		return types.get(type);
	}
	
	public static TransformType valueOf(ContentType contentType) {
		if(contentType == ContentType.TEXT_PLAIN)
			return TEXT_TRANSFORM;
		else if(contentType == ContentType.TEXT_XML)
			return XML_TRANSFORM;
		else if(contentType == ContentType.TEXT_JSON)
			return JSON_TRANSFORM;
		else if(contentType == ContentType.TEXT_APON)
			return APON_TRANSFORM;
		
		return null;
	}

	/**
	 * Returns an array containing the constants of this type, in the order they are declared.
	 *
	 * @return the string[]
	 */
	public static String[] values() {
		return types.keySet().toArray(new String[types.size()]);
	}

}
