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
package com.aspectran.core.context.type;

import java.util.HashMap;
import java.util.Map;


public final class JoinpointTargetType extends Type {

	public static final JoinpointTargetType TRANSLET;
	
	public static final JoinpointTargetType SCHEDULER;
	
	private static final Map<String, JoinpointTargetType> types;
	
	static {
		TRANSLET = new JoinpointTargetType("translet");
		SCHEDULER = new JoinpointTargetType("scheduler");

		types = new HashMap<String, JoinpointTargetType>();
		types.put(TRANSLET.toString(), TRANSLET);
		types.put(SCHEDULER.toString(), SCHEDULER);
	}

	private JoinpointTargetType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>JoinpointTargetType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the content type
	 */
	public static JoinpointTargetType valueOf(String type) {
		if(type == null)
			return null;
		
		return types.get(type);
	}
}
