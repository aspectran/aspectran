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

public class BeanParameters extends AbstractParameters {

	public static final ParameterDefine description;
	public static final ParameterDefine id;
	public static final ParameterDefine className;
	public static final ParameterDefine scan;
	public static final ParameterDefine mask;
	public static final ParameterDefine scope;
	public static final ParameterDefine singleton;
	public static final ParameterDefine offerBean;
	public static final ParameterDefine offerMethod;
	public static final ParameterDefine initMethod;
	public static final ParameterDefine destroyMethod;
	public static final ParameterDefine factoryMethod;
	public static final ParameterDefine lazyInit;
	public static final ParameterDefine important;
	public static final ParameterDefine constructor;
	public static final ParameterDefine properties;
	public static final ParameterDefine filter;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		description = new ParameterDefine("description", ParameterValueType.TEXT);
		id = new ParameterDefine("id", ParameterValueType.STRING);
		className = new ParameterDefine("class", ParameterValueType.STRING);
		scan = new ParameterDefine("scan", ParameterValueType.STRING);
		mask = new ParameterDefine("mask", ParameterValueType.STRING);
		scope = new ParameterDefine("scope", ParameterValueType.STRING);
		singleton = new ParameterDefine("singleton", ParameterValueType.BOOLEAN);
		offerBean = new ParameterDefine("offerBean", ParameterValueType.STRING);
		offerMethod = new ParameterDefine("offerMethod", ParameterValueType.STRING);
		initMethod = new ParameterDefine("initMethod", ParameterValueType.STRING);
		destroyMethod = new ParameterDefine("destroyMethod", ParameterValueType.STRING);
		factoryMethod = new ParameterDefine("factoryMethod", ParameterValueType.STRING);
		lazyInit = new ParameterDefine("lazyInit", ParameterValueType.BOOLEAN);
		important = new ParameterDefine("important", ParameterValueType.BOOLEAN);
		constructor = new ParameterDefine("constructor", ConstructorParameters.class);
		properties = new ParameterDefine("property", ItemHolderParameters.class);
		filter = new ParameterDefine("filter", FilterParameters.class);
		
		parameterDefines = new ParameterDefine[] {
				description,
				id,
				className,
				scan,
				mask,
				scope,
				singleton,
				offerBean,
				offerMethod,
				initMethod,
				destroyMethod,
				factoryMethod,
				lazyInit,
				important,
				constructor,
				properties,
				filter
			};
	}
	
	public BeanParameters() {
		super(parameterDefines);
	}
	
	public BeanParameters(String text) {
		super(parameterDefines, text);
	}
	
}
