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
package com.aspectran.core.var.type;

import java.util.HashMap;
import java.util.Map;


/**
 * Action Type.
 * 
 * <p>Created: 2008. 03. 26 오전 12:58:38</p>
 */
public final class BeanProxyModeType extends Type {

	/** The "CGLIB" bean proxy mode type. */
	public static final BeanProxyModeType CGLIB;

	/** The "JDK" bean proxy mode type. */
	public static final BeanProxyModeType JDK;
	
	private static final Map<String, BeanProxyModeType> types;
	
	static {
		CGLIB = new BeanProxyModeType("cglib");
		JDK = new BeanProxyModeType("jdk");

		types = new HashMap<String, BeanProxyModeType>();
		types.put(CGLIB.toString(), CGLIB);
		types.put(JDK.toString(), JDK);
	}

	/**
	 * Instantiates a new bean proxy mode type.
	 * 
	 * @param type the type
	 */
	private BeanProxyModeType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>BeanProxyModeType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the action type
	 */
	public static BeanProxyModeType valueOf(String type) {
		if(type == null)
			return null;
		
		return types.get(type);
	}
}
