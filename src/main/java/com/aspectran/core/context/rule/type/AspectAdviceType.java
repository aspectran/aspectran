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

/**
 * Types of advice include "around," "before" and "after" advice.
 * <pre>
 * Before advice: Advice that executes before a join point.
 * After advice: Advice to be executed after a join point completes normally.
 * Finally advice: Advice to be executed regardless of the means by which a join point exits (normal or exceptional return).
 * Around advice: Before advice + After advice
 * Job advice: Only used for Scheduler.
 * </pre>
 * @author Juho Jeong
 */
public final class AspectAdviceType extends Type {

	public static final AspectAdviceType SETTINGS;

	public static final AspectAdviceType BEFORE;
	
	public static final AspectAdviceType AFTER;
	
	public static final AspectAdviceType AROUND;
	
	public static final AspectAdviceType FINALLY;
	
	public static final AspectAdviceType JOB;
	
	private static final Map<String, AspectAdviceType> types;
	
	static {
		SETTINGS = new AspectAdviceType("settings");
		BEFORE = new AspectAdviceType("before");
		AFTER = new AspectAdviceType("after");
		AROUND = new AspectAdviceType("around");
		FINALLY = new AspectAdviceType("finally");
		JOB = new AspectAdviceType("job");

		types = new HashMap<String, AspectAdviceType>();
		types.put(SETTINGS.toString(), SETTINGS);
		types.put(BEFORE.toString(), BEFORE);
		types.put(AFTER.toString(), AFTER);
		types.put(AROUND.toString(), AROUND);
		types.put(FINALLY.toString(), FINALLY);
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
	
	/**
	 * Returns an array containing the constants of this type, in the order they are declared.
	 *
	 * @return the string[]
	 */
	public static String[] values() {
		return types.keySet().toArray(new String[types.size()]);
	}
	
}
