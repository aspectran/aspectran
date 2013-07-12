/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.base.variable;

import java.util.Map;


/**
 * <p>Created: 2008. 06. 11 오후 8:55:13</p>
 */
public class AttributeMap extends ValueMap {

	/** @serial */
	static final long serialVersionUID = -5350170143494113143L;

	public AttributeMap() {
		super();
	}
	
	public AttributeMap(Map<String, Object> map) {
		super(map);
	}
	
}
