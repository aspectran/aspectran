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

public final class PointcutPatternOperationType extends Type {

	public static final PointcutPatternOperationType PLUS;

	public static final PointcutPatternOperationType MINUS;
	
	private static final Map<String, PointcutPatternOperationType> types;
	
	static {
		PLUS = new PointcutPatternOperationType("+");
		MINUS = new PointcutPatternOperationType("-");

		types = new HashMap<String, PointcutPatternOperationType>();
		types.put(PLUS.toString(), PLUS);
		types.put(MINUS.toString(), MINUS);
	}

	private PointcutPatternOperationType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>PointcutType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the content type
	 */
	public static PointcutPatternOperationType valueOf(String type) {
		if(type == null)
			return null;
		
		return types.get(type);
	}
	
}
