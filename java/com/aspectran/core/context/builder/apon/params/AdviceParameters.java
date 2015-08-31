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
package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

public class AdviceParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine bean;
	public static final ParameterDefine beforeAdvice;
	public static final ParameterDefine afterAdvice;
	public static final ParameterDefine aroundAdvice;
	public static final ParameterDefine finallyAdvice;
	public static final ParameterDefine jobs;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		bean = new ParameterDefine("bean", ParameterValueType.STRING);
		beforeAdvice = new ParameterDefine("before", AdviceActionParameters.class);
		afterAdvice = new ParameterDefine("after", AdviceActionParameters.class);
		aroundAdvice = new ParameterDefine("around", AdviceActionParameters.class);
		finallyAdvice = new ParameterDefine("finally", AdviceActionParameters.class);
		jobs = new ParameterDefine("job", JobParameters.class, true, true);
		
		parameterDefines = new ParameterDefine[] {
				bean,
				beforeAdvice,
				afterAdvice,
				aroundAdvice,
				finallyAdvice,
				jobs
		};
	}
	
	public AdviceParameters() {
		super(parameterDefines);
	}
	
	public AdviceParameters(String text) {
		super(parameterDefines, text);
	}
	
}
