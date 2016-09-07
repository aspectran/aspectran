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
package com.aspectran.core.util.apon.test;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;

/**
 * 고객정보 레코드를 정의합니다.
 * 임의로 APON의 Parameters 객체를 이용합니다. 
 */
public class Customer extends AbstractParameters implements Parameters {

	/** 고객번호 */
	public static final ParameterDefinition id;
	
	/** 이름 */
	public static final ParameterDefinition name;
	
	/** 나이 */
	public static final ParameterDefinition age;
	
	/** 에피소드 */
	public static final ParameterDefinition episode;
	
	/** 승인 여부 */
	public static final ParameterDefinition approved;
	
	private static final ParameterDefinition[] parameterDefinitions;
	
	static {
		id = new ParameterDefinition("id", ParameterValueType.STRING);
		name = new ParameterDefinition("name", ParameterValueType.STRING);
		age = new ParameterDefinition("age", ParameterValueType.INT);
		episode = new ParameterDefinition("epsode", ParameterValueType.TEXT);
		approved = new ParameterDefinition("approved", ParameterValueType.BOOLEAN);
		
		parameterDefinitions = new ParameterDefinition[] {
			id,
			name,
			age,
			episode,
			approved
		};
	}
	
	/**
	 * Instantiates a new customer.
	 */
	public Customer() {
		super(parameterDefinitions);
	}
	
	/**
	 * Instantiates a new customer.
	 *
	 * @param text the Text string of APON format
	 */
	public Customer(String text) {
		super(parameterDefinitions, text);
	}
	
}
