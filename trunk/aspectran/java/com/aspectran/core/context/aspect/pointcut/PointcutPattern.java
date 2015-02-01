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

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.var.rule.PointcutPatternRule;
import com.aspectran.core.var.type.PointcutPatternOperationType;

public class PointcutPattern {
	
	private PointcutPatternOperationType pointcutPatternOperationType;
	
	private String transletNamePattern;
	
	private String beanOrActionIdPattern;

	private String beanMethodNamePattern;
	
	private List<PointcutPattern> minusPointcutPatternList;
	
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
	
	public List<PointcutPattern> getMinusPointcutPatternList() {
		return minusPointcutPatternList;
	}

	public void setMinusPointcutPatternList(
			List<PointcutPattern> withoutPointcutPatternList) {
		this.minusPointcutPatternList = withoutPointcutPatternList;
	}
	
	public void addMinusPointcutPattern(PointcutPattern minusPointcutPattern) {
		if(minusPointcutPatternList == null)
			minusPointcutPatternList = new ArrayList<PointcutPattern>();
		
		minusPointcutPatternList.add(minusPointcutPattern);
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
		String beanOrActionIdPattern = null;
		String beanMethodNamePattern = null;

		int actionDelimiterIndex = pattern.indexOf(AspectranConstant.POINTCUT_BEAN_DELIMITER);
		
		if(actionDelimiterIndex == -1)
			transletNamePattern = pattern;
		else if(actionDelimiterIndex == 0)
			beanOrActionIdPattern = pattern.substring(1);
		else {
			transletNamePattern = pattern.substring(0, actionDelimiterIndex);
			beanOrActionIdPattern = pattern.substring(actionDelimiterIndex + 1);
		}

		if(beanOrActionIdPattern != null) {
			int beanMethodDelimiterIndex = beanOrActionIdPattern.indexOf(AspectranConstant.POINTCUT_METHOD_DELIMITER);
			
			if(beanMethodDelimiterIndex == 0) {
				beanMethodNamePattern = beanOrActionIdPattern.substring(1);
				beanOrActionIdPattern = null;
			} else if(beanMethodDelimiterIndex > 0) {
				beanMethodNamePattern = beanOrActionIdPattern.substring(beanMethodDelimiterIndex + 1);
				beanOrActionIdPattern = beanOrActionIdPattern.substring(0, beanMethodDelimiterIndex);
			}
		}
		
		if(transletNamePattern != null)
			pointcutPattern.setTransletNamePattern(transletNamePattern);
		
		if(beanOrActionIdPattern != null)
			pointcutPattern.setBeanOrActionIdPattern(beanOrActionIdPattern);

		if(beanMethodNamePattern != null)
			pointcutPattern.setBeanMethodNamePattern(beanMethodNamePattern);
		
		return pointcutPattern;
	}
	
}
