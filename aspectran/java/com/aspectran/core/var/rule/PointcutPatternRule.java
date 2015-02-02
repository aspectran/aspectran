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
package com.aspectran.core.var.rule;

import java.util.ArrayList;
import java.util.List;

public class PointcutPatternRule {
	
	public static final char POINTCUT_BEAN_DELIMITER = '@';
	
	public static final char POINTCUT_METHOD_DELIMITER = '^';

	public static final char JOINPOINT_SCOPE_DELIMITER = '$';
	
	private String patternString;
	
	private String transletNamePattern;
	
	private String beanOrActionIdPattern;

	private String beanMethodNamePattern;
	
	private List<PointcutPatternRule> excludePointcutPatternRuleList;
	
	public PointcutPatternRule() {
	}

	public String getPatternString() {
		return patternString;
	}

	public void setPatternString(String patternString) {
		this.patternString = patternString;
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
	
	public List<PointcutPatternRule> getExcludePointcutPatternRuleList() {
		return excludePointcutPatternRuleList;
	}

	public void setExcludePointcutPatternRuleList(List<PointcutPatternRule> minusPointcutPatternRuleList) {
		this.excludePointcutPatternRuleList = minusPointcutPatternRuleList;
	}
	
	public void addExcludePointcutPatternRule(PointcutPatternRule excludePointcutPatternRule) {
		if(excludePointcutPatternRuleList == null)
			excludePointcutPatternRuleList = new ArrayList<PointcutPatternRule>();
		
		excludePointcutPatternRuleList.add(excludePointcutPatternRule);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{translet=").append(transletNamePattern);
		sb.append(", bean=").append(beanOrActionIdPattern);
		sb.append(", method=").append(beanMethodNamePattern);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static String combinePatternString(String transletName, String beanId, String methodName) {
		StringBuilder sb = new StringBuilder();
		
		if(transletName != null)
			sb.append(transletName);
		
		if(beanId != null) {
			sb.append(POINTCUT_BEAN_DELIMITER);
			sb.append(beanId);
		}
		
		if(methodName != null) {
			sb.append(POINTCUT_METHOD_DELIMITER);
			sb.append(methodName);
		}
		
		return sb.toString();
	}
	
	public static String combinePatternString(String joinpointScope, String transletName, String beanId, String methodName) {
		StringBuilder sb = new StringBuilder();
		
		if(joinpointScope != null) {
			sb.append(joinpointScope);
			sb.append(JOINPOINT_SCOPE_DELIMITER);
		}
		
		if(transletName != null)
			sb.append(transletName);
		
		if(beanId != null) {
			sb.append(POINTCUT_BEAN_DELIMITER);
			sb.append(beanId);
		}
		
		if(methodName != null) {
			sb.append(POINTCUT_METHOD_DELIMITER);
			sb.append(methodName);
		}
		
		return sb.toString();
	}
	
	public static PointcutPatternRule parsePatternString(String patternString) {
		PointcutPatternRule pointcutPatternRule = new PointcutPatternRule();
		pointcutPatternRule.setPatternString(patternString);
		
		String transletNamePattern = null;
		String beanOrActionIdPattern = null;
		String beanMethodNamePattern = null;

		int actionDelimiterIndex = patternString.indexOf(POINTCUT_BEAN_DELIMITER);
		
		if(actionDelimiterIndex == -1)
			transletNamePattern = patternString;
		else if(actionDelimiterIndex == 0)
			beanOrActionIdPattern = patternString.substring(1);
		else {
			transletNamePattern = patternString.substring(0, actionDelimiterIndex);
			beanOrActionIdPattern = patternString.substring(actionDelimiterIndex + 1);
		}

		if(beanOrActionIdPattern != null) {
			int beanMethodDelimiterIndex = beanOrActionIdPattern.indexOf(POINTCUT_METHOD_DELIMITER);
			
			if(beanMethodDelimiterIndex == 0) {
				beanMethodNamePattern = beanOrActionIdPattern.substring(1);
				beanOrActionIdPattern = null;
			} else if(beanMethodDelimiterIndex > 0) {
				beanMethodNamePattern = beanOrActionIdPattern.substring(beanMethodDelimiterIndex + 1);
				beanOrActionIdPattern = beanOrActionIdPattern.substring(0, beanMethodDelimiterIndex);
			}
		}
		
		if(transletNamePattern != null)
			pointcutPatternRule.setTransletNamePattern(transletNamePattern);
		
		if(beanOrActionIdPattern != null)
			pointcutPatternRule.setBeanOrActionIdPattern(beanOrActionIdPattern);

		if(beanMethodNamePattern != null)
			pointcutPatternRule.setBeanMethodNamePattern(beanMethodNamePattern);
		
		return pointcutPatternRule;
	}
	
	public static PointcutPatternRule newInstance(String translet, String bean, String method) {
		PointcutPatternRule pointcutPatternRule = new PointcutPatternRule();

		if(translet != null && translet.length() > 0)						
			pointcutPatternRule.setTransletNamePattern(translet);
		if(bean != null && bean.length() > 0)
			pointcutPatternRule.setBeanOrActionIdPattern(bean);
		if(method != null && method.length() > 0)
			pointcutPatternRule.setBeanMethodNamePattern(method);
		
		return pointcutPatternRule;
	}
	
}
