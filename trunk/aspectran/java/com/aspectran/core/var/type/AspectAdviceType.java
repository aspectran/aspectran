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


public final class AspectAdviceType extends Type {

	public static final AspectAdviceType SETTINGS;

	public static final AspectAdviceType TRIGGER;
	
	public static final AspectAdviceType BEFORE;
	
	public static final AspectAdviceType AFTER;
	
	public static final AspectAdviceType AROUND;
	
	public static final AspectAdviceType FINALLY;
	
	public static final AspectAdviceType EXCPETION_RAIZED;
	
	public static final AspectAdviceType JOB;
	
	private static final Map<String, AspectAdviceType> types;
	
	static {
		SETTINGS = new AspectAdviceType("settings");
		TRIGGER = new AspectAdviceType("trigger");
		BEFORE = new AspectAdviceType("before");
		AFTER = new AspectAdviceType("after");
		AROUND = new AspectAdviceType("around");
		FINALLY = new AspectAdviceType("finally");
		EXCPETION_RAIZED = new AspectAdviceType("exceptionRaized");
		JOB = new AspectAdviceType("trigger");

		types = new HashMap<String, AspectAdviceType>();
		types.put(SETTINGS.toString(), SETTINGS);
		types.put(TRIGGER.toString(), TRIGGER);
		types.put(BEFORE.toString(), BEFORE);
		types.put(AFTER.toString(), AFTER);
		types.put(AROUND.toString(), AROUND);
		types.put(FINALLY.toString(), FINALLY);
		types.put(EXCPETION_RAIZED.toString(), EXCPETION_RAIZED);
		types.put(JOB.toString(), JOB);
	}

	private AspectAdviceType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>AdviceType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the content type
	 */
	public static AspectAdviceType valueOf(String type) {
		if(type == null)
			return null;
		
		return types.get(type);
	}
}
