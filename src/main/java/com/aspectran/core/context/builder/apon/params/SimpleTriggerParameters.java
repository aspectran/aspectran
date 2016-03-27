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
package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;

public class SimpleTriggerParameters extends AbstractParameters {

	public static final ParameterDefine withIntervalInMilliseconds;
	public static final ParameterDefine withIntervalInMinutes;
	public static final ParameterDefine withIntervalInSeconds;
	public static final ParameterDefine withIntervalInHours;
	public static final ParameterDefine withRepeatCount;
	public static final ParameterDefine repeatForever;

	private final static ParameterDefine[] parameterDefines;
	
	static {
		withIntervalInMilliseconds = new ParameterDefine("withIntervalInMilliseconds", ParameterValueType.INT);
		withIntervalInMinutes = new ParameterDefine("withIntervalInMinutes", ParameterValueType.INT);
		withIntervalInSeconds = new ParameterDefine("withIntervalInSeconds", ParameterValueType.INT);
		withIntervalInHours = new ParameterDefine("withIntervalInHours", ParameterValueType.INT);
		withRepeatCount = new ParameterDefine("withRepeatCount", ParameterValueType.INT);
		repeatForever = new ParameterDefine("repeatForever", ParameterValueType.BOOLEAN);
		
		parameterDefines = new ParameterDefine[] {
				withIntervalInMilliseconds,
				withIntervalInMinutes,
				withIntervalInSeconds,
				withIntervalInHours,
				withRepeatCount,
				repeatForever
		};
	}
	
	public SimpleTriggerParameters() {
		super(parameterDefines);
	}
	
	public SimpleTriggerParameters(String text) {
		super(parameterDefines, text);
	}
	
}
