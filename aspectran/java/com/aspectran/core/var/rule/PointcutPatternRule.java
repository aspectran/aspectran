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

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.var.type.AspectTargetType;
import com.aspectran.core.var.type.PointcutPatternOperationType;

public class PointcutPatternRule {
	
	private PointcutPatternOperationType pointcutPatternOperationType;
	
	private String transletNamePattern;
	
	private String beanOrActionIdPattern;

	private String beanMethodNamePattern;
	
	private String patternString;
	
	private List<PointcutPatternRule> minusPointcutPatternRuleList;

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
	
	public String getPatternString() {
		return patternString;
	}

	public void setPatternString(String patternString) {
		this.patternString = patternString;
	}
	
	public List<PointcutPatternRule> getMinusPointcutPatternRuleList() {
		return minusPointcutPatternRuleList;
	}

	public void setMinusPointcutPatternRuleList(List<PointcutPatternRule> minusPointcutPatternRuleList) {
		this.minusPointcutPatternRuleList = minusPointcutPatternRuleList;
	}
	
	public void addExcludePointcutPatternRule(PointcutPatternRule minusPointcutPatternRule) {
		if(minusPointcutPatternRuleList == null)
			minusPointcutPatternRuleList = new ArrayList<PointcutPatternRule>();
		
		minusPointcutPatternRuleList.add(minusPointcutPatternRule);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{operation=").append(pointcutPatternOperationType);
		sb.append(", translet=").append(transletNamePattern);
		sb.append(", bean=").append(beanOrActionIdPattern);
		sb.append(", method=").append(beanMethodNamePattern);
		sb.append(", patternString=").append(patternString);
		sb.append("}");
		
		return sb.toString();
	}
	
	public static PointcutPatternRule newInstance(AspectRule aspectRule, String translet, String bean, String method, String patternString) {
		PointcutPatternRule pointcutPatternRule = new PointcutPatternRule();

		if(aspectRule.getAspectTargetType() == AspectTargetType.TRANSLET) {
			pointcutPatternRule.setPointcutPatternOperationType(PointcutPatternOperationType.PLUS);
		
			if(StringUtils.hasLength(translet) || StringUtils.hasLength(bean) || StringUtils.hasLength(method)) {
				if(translet != null && translet.length() == 0)						
					pointcutPatternRule.setTransletNamePattern(translet);
				if(bean != null && bean.length() == 0)
					pointcutPatternRule.setBeanOrActionIdPattern(bean);
				if(method != null && method.length() == 0)
					pointcutPatternRule.setBeanMethodNamePattern(method);
			} else if(StringUtils.hasText(patternString)) {
				pointcutPatternRule.setPatternString(patternString);
			}
		}
		
		return pointcutPatternRule;
	}
	
}
