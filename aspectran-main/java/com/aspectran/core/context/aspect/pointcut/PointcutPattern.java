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

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.var.rule.PointcutPatternRule;
import com.aspectran.core.var.type.PointcutPatternOperationType;

public class PointcutPattern {
	
	private PointcutPatternOperationType pointcutPatternOperationType;
	
	private String transletNamePattern;
	
	private String beanOrActionIdPattern;

	private String beanMethodNamePattern;
	
	public PointcutPattern() {
	}
	
	public PointcutPattern(PointcutPatternRule pointcutPatternRule) {
		this.pointcutPatternOperationType = pointcutPatternRule.getPointcutPatternOperationType();
		this.transletNamePattern = pointcutPatternRule.getTransletNamePattern();
		this.beanOrActionIdPattern = pointcutPatternRule.getBeanOrActionIdPattern();
		this.beanMethodNamePattern = pointcutPatternRule.getBeanMethodNamePattern();
	}
	
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

	public String getBeanOrActionIdPattern() {
		return beanOrActionIdPattern;
	}

	public void setBeanOrActionIdPattern(String beanOrActionIdPattern) {
		this.beanOrActionIdPattern = beanOrActionIdPattern;
	}

	public String getBeanMethodNamePattern() {
		return beanMethodNamePattern;
	}

	public void setBeanMethodNamePattern(String beanMethodNamePattern) {
		this.beanMethodNamePattern = beanMethodNamePattern;
	}
	
	public static String combinePatternString(String transletName, String beanId, String methodName) {
		StringBuilder sb = new StringBuilder();
		
		if(transletName != null)
			sb.append(transletName);
		
		if(beanId != null) {
			sb.append(AspectranConstant.POINTCUT_BEAN_DELIMITER);
			sb.append(beanId);
		}
		
		if(methodName != null) {
			sb.append(AspectranConstant.POINTCUT_METHOD_DELIMITER);
			sb.append(methodName);
		}
		
		return sb.toString();
	}
	
	public static String combinePatternString(String joinpointScope, String transletName, String beanId, String methodName) {
		StringBuilder sb = new StringBuilder();
		
		if(joinpointScope != null) {
			sb.append(joinpointScope);
			sb.append(AspectranConstant.JOINPOINT_SCOPE_DELIMITER);
		}
		
		if(transletName != null)
			sb.append(transletName);
		
		if(beanId != null) {
			sb.append(AspectranConstant.POINTCUT_BEAN_DELIMITER);
			sb.append(beanId);
		}
		
		if(methodName != null) {
			sb.append(AspectranConstant.POINTCUT_METHOD_DELIMITER);
			sb.append(methodName);
		}
		
		return sb.toString();
	}
	
	public static PointcutPattern createPointcutPattern(PointcutPatternOperationType pointcutPatternOperationType, String pattern) {
		PointcutPattern pointcutPattern = new PointcutPattern();
		pointcutPattern.setPointcutPatternOperationType(pointcutPatternOperationType);
		
		String transletNamePattern = null;
		String actionNamePattern = null;
		String beanMethodNamePattern = null;

		int actionDelimiterIndex = pattern.indexOf(AspectranConstant.POINTCUT_BEAN_DELIMITER);
		
		if(actionDelimiterIndex == -1)
			transletNamePattern = pattern;
		else if(actionDelimiterIndex == 0)
			actionNamePattern = pattern.substring(1);
		else {
			transletNamePattern = pattern.substring(0, actionDelimiterIndex);
			actionNamePattern = pattern.substring(actionDelimiterIndex + 1);
		}

		if(actionNamePattern != null) {
			int beanMethodDelimiterIndex = actionNamePattern.indexOf(AspectranConstant.POINTCUT_METHOD_DELIMITER);
			
			if(beanMethodDelimiterIndex == 0) {
				beanMethodNamePattern = actionNamePattern.substring(1);
				actionNamePattern = null;
			} else if(beanMethodDelimiterIndex > 0) {
				beanMethodNamePattern = actionNamePattern.substring(beanMethodDelimiterIndex + 1);
				actionNamePattern = actionNamePattern.substring(0, beanMethodDelimiterIndex);
			}
		}
		
		if(transletNamePattern != null)
			pointcutPattern.setTransletNamePattern(transletNamePattern);
		
		if(actionNamePattern != null)
			pointcutPattern.setBeanOrActionIdPattern(actionNamePattern);

		if(beanMethodNamePattern != null)
			pointcutPattern.setBeanMethodNamePattern(beanMethodNamePattern);
		
		return pointcutPattern;
	}
	
}
