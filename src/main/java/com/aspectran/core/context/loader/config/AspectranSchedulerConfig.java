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
package com.aspectran.core.context.loader.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class AspectranSchedulerConfig extends AbstractParameters implements Parameters {

	public static final ParameterDefine startDelaySeconds;
	public static final ParameterDefine waitOnShutdown;
	public static final ParameterDefine startup;
	
	private final static ParameterDefine[] parameterDefines;
	
	static {
		startDelaySeconds = new ParameterDefine("startDelaySeconds", ParameterValueType.INT);
		waitOnShutdown = new ParameterDefine("waitOnShutdown", ParameterValueType.BOOLEAN);
		startup = new ParameterDefine("startup", ParameterValueType.BOOLEAN);

		parameterDefines = new ParameterDefine[] {
				startDelaySeconds,
				waitOnShutdown,
				startup
		};
	}
	
	public AspectranSchedulerConfig() {
		super(parameterDefines);
	}
	
	public AspectranSchedulerConfig(String plaintext) {
		super(parameterDefines, plaintext);
	}
	
}
