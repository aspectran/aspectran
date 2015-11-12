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
 * The Class PointcutType.
 */
public final class PointcutType extends Type {

	public static final PointcutType WILDCARD;

	public static final PointcutType REGEXP;
	
	public static final PointcutType SIMPLE_TRIGGER; // for scheduler

	public static final PointcutType CRON_TRIGGER; // for scheduler
	
	private static final Map<String, PointcutType> types;
	
	static {
		WILDCARD = new PointcutType("wildcard");
		REGEXP = new PointcutType("regexp");
		SIMPLE_TRIGGER = new PointcutType("simpleTrigger");
		CRON_TRIGGER = new PointcutType("cronTrigger");

		types = new HashMap<String, PointcutType>();
		types.put(WILDCARD.toString(), WILDCARD);
		types.put(REGEXP.toString(), REGEXP);
		types.put(SIMPLE_TRIGGER.toString(), SIMPLE_TRIGGER);
		types.put(CRON_TRIGGER.toString(), CRON_TRIGGER);
	}

	private PointcutType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>PointcutType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the content type
	 */
	public static PointcutType valueOf(String type) {
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
