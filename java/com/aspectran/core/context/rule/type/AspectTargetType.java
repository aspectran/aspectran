/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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


public final class AspectTargetType extends Type {

	public static final AspectTargetType TRANSLET;
	
	public static final AspectTargetType SCHEDULER;
	
	private static final Map<String, AspectTargetType> types;
	
	static {
		TRANSLET = new AspectTargetType("translet");
		SCHEDULER = new AspectTargetType("scheduler");

		types = new HashMap<String, AspectTargetType>();
		types.put(TRANSLET.toString(), TRANSLET);
		types.put(SCHEDULER.toString(), SCHEDULER);
	}

	private AspectTargetType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>AspectTargetType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the content type
	 */
	public static AspectTargetType valueOf(String type) {
		if(type == null)
			return null;
		
		return types.get(type);
	}
}
