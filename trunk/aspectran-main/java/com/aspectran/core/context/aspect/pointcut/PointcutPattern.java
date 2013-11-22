/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.context.aspect.pointcut;

import com.aspectran.core.var.type.PointcutPatternOperationType;

public class PointcutPattern {
	
	private PointcutPatternOperationType pointcutPatternOperationType;
	
	private String transletNamePattern;
	
	private String actionNamePattern;

	private String beanMethodNamePattern;
	
	public PointcutPatternOperationType getPointcutPatternOperationType() {
		return pointcutPatternOperationType;
	}

	public void setPointcutPatternOperationType(PointcutPatternOperationType pointcutPatternOperationType) {
		this.pointcutPatternOperationType = pointcutPatternOperationType;
	}

	public String getTransletNamePattern() {
		return transletNamePattern;
	}

	public void setTransletNamePattern(String transletNamePattern) {
		this.transletNamePattern = transletNamePattern;
	}

	public String getActionNamePattern() {
		return actionNamePattern;
	}

	public void setActionNamePattern(String actionIdPattern) {
		this.actionNamePattern = actionIdPattern;
	}

	public String getBeanMethodNamePattern() {
		return beanMethodNamePattern;
	}

	public void setBeanMethodNamePattern(String beanMethodNamePattern) {
		this.beanMethodNamePattern = beanMethodNamePattern;
	}
	
}
