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

import java.util.HashMap;
import java.util.Map;

/**
 * Type of BeanProxifier.
 * 
 * <p>Created: 2008. 03. 26 AM 12:58:38</p>
 */
public final class BeanProxifierType extends Type {

	/** The "CGLIB" bean proxifier. */
	public static final BeanProxifierType CGLIB;

	/**
	 * The "JAVASSIST" bean proxifier.
	 * @since 1.1.0 
	 */
	public static final BeanProxifierType JAVASSIST;
	
	/** The "JDK" bean proxifier. */
	public static final BeanProxifierType JDK;
	
	private static final Map<String, BeanProxifierType> types;
	
	static {
		CGLIB = new BeanProxifierType("cglib");
		JAVASSIST = new BeanProxifierType("javassist");
		JDK = new BeanProxifierType("jdk");

		types = new HashMap<String, BeanProxifierType>();
		types.put(CGLIB.toString(), CGLIB);
		types.put(JAVASSIST.toString(), JAVASSIST);
		types.put(JDK.toString(), JDK);
	}

	/**
	 * Instantiates a new BeanProxifierType.
	 * 
	 * @param type the type
	 */
	private BeanProxifierType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>BeanProxifierType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the action type
	 */
	public static BeanProxifierType valueOf(String type) {
		if(type == null)
			return null;
		
		return types.get(type);
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
