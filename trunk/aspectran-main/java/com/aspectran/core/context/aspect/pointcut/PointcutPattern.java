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

import com.aspectran.core.type.PointcutPatternOperationType;

public class PointcutPattern {
	
	public static final String ACTION_SEPARATOR = "@";

	private PointcutPatternOperationType pointcutPatternOperationType;
	
	private String transletNamePattern;
	
	private String actionIdPattern;

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

	public String getActionIdPattern() {
		return actionIdPattern;
	}

	public void setActionIdPattern(String actionIdPattern) {
		this.actionIdPattern = actionIdPattern;
	}
	
}
