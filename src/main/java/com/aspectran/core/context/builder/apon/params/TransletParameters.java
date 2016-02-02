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
import com.aspectran.core.util.apon.Parameters;

public class TransletParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine description;
	public static final ParameterDefine name;
	public static final ParameterDefine scan;
	public static final ParameterDefine mask;
	public static final ParameterDefine restVerb;
	public static final ParameterDefine request;
	public static final ParameterDefine contents1;
	public static final ParameterDefine contents2;
	public static final ParameterDefine responses;
	public static final ParameterDefine exception;
	public static final ParameterDefine actions;
	public static final ParameterDefine transform;
	public static final ParameterDefine dispatch;
	public static final ParameterDefine redirect;
	public static final ParameterDefine forward;

	private static final ParameterDefine[] parameterDefines;
	
	static {
		description = new ParameterDefine("description", ParameterValueType.TEXT);
		name = new ParameterDefine("name", ParameterValueType.STRING);
		scan = new ParameterDefine("scan", ParameterValueType.STRING);
		mask = new ParameterDefine("mask", ParameterValueType.STRING);
		restVerb = new ParameterDefine("restVerb", ParameterValueType.STRING);
		request = new ParameterDefine("request", RequestParameters.class);
		contents1 = new ParameterDefine("contents", ContentsParameters.class);
		contents2 = new ParameterDefine("content", ContentParameters.class, true, true);
		responses = new ParameterDefine("response", ResponseParameters.class, true, true);
		exception = new ParameterDefine("exception", ExceptionParameters.class, true, true);
		actions = new ParameterDefine("action", ActionParameters.class, true, true);
		transform = new ParameterDefine("transform", TransformParameters.class);
		dispatch = new ParameterDefine("dispatch", DispatchParameters.class);
		redirect = new ParameterDefine("redirect", RedirectParameters.class);
		forward = new ParameterDefine("forward", ForwardParameters.class);
		
		parameterDefines = new ParameterDefine[] {
				description,
				name,
				scan,
				mask,
				restVerb,
				request,
				contents1,
				contents2,
				responses,
				exception,
				actions,
				transform,
				dispatch,
				redirect,
				forward
			};
	}
	
	public TransletParameters() {
		super(parameterDefines);
	}
	
	public TransletParameters(String text) {
		super(parameterDefines, text);
	}
	
}
