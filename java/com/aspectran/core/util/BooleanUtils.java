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
package com.aspectran.core.util;


/**
 * Miscellaneous {@link Boolean} utility methods.
 * 
 * @author Jeong Ju Ho
 */
public class BooleanUtils {
	
	public static Boolean toNullableBooleanObject(String booleanString) {
		if(booleanString == null)
			return null;
		
		return Boolean.valueOf(booleanString);
	}
	
	public static boolean toBoolean(Boolean bool) {
		return toBoolean(bool, false);
	}
	
	public static boolean toBoolean(Boolean bool, boolean defaultValue) {
		if(bool == null)
			return defaultValue;
		
		return bool.booleanValue();
	}
}
