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
package com.aspectran.core.context.rule;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class PointcutPatternRule.
 */
public class PointcutPatternRule {
	
	private static final char POINTCUT_BEAN_CLASS_DELIMITER = '@';
	
	private static final char POINTCUT_METHOD_NAME_DELIMITER = '^';

	//private static final char JOINPOINT_SCOPE_DELIMITER = '$';
	
	private PointcutType pointcutType;
	
	private String patternString;
	
	private String transletNamePattern;
	
	private String beanIdPattern;

	private String classNamePattern;

	private String methodNamePattern;
	
	private int matchedTransletCount;
	
	private int matchedBeanCount;

	private int matchedClassCount;

	private int matchedMethodCount;

	private List<PointcutPatternRule> excludePointcutPatternRuleList;
	
	public PointcutPatternRule() {
	}
	
	public PointcutType getPointcutType() {
		return pointcutType;
	}

	protected void setPointcutType(PointcutType pointcutType) {
		this.pointcutType = pointcutType;
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

	public String getBeanIdPattern() {
		return beanIdPattern;
	}

	public void setBeanIdPattern(String beanIdPattern) {
		this.beanIdPattern = beanIdPattern;
	}

	public String getClassNamePattern() {
		return classNamePattern;
	}

	public void setClassNamePattern(String classNamePattern) {
		this.classNamePattern = classNamePattern;
	}

	public String getMethodNamePattern() {
		return methodNamePattern;
	}

	public void setMethodNamePattern(String methodNamePattern) {
		this.methodNamePattern = methodNamePattern;
	}
	
	public List<PointcutPatternRule> getExcludePointcutPatternRuleList() {
		return excludePointcutPatternRuleList;
	}

	public void setExcludePointcutPatternRuleList(List<PointcutPatternRule> excludePointcutPatternRuleList) {
		this.excludePointcutPatternRuleList = excludePointcutPatternRuleList;
	}
	
	public void addExcludePointcutPatternRule(PointcutPatternRule excludePointcutPatternRule) {
		if(excludePointcutPatternRuleList == null)
			excludePointcutPatternRuleList = new ArrayList<PointcutPatternRule>();
		
		excludePointcutPatternRuleList.add(excludePointcutPatternRule);
	}

	public int getMatchedTransletCount() {
		return matchedTransletCount;
	}

	public void increaseMatchedTransletCount() {
		matchedTransletCount++;
	}

	public int getMatchedBeanCount() {
		return matchedBeanCount;
	}

	public void increaseMatchedBeanCount() {
		matchedBeanCount++;
	}

	public int getMatchedClassCount() {
		return matchedClassCount;
	}

	public void increaseMatchedClassCount() {
		matchedClassCount++;
	}

	public int getMatchedMethodCount() {
		return matchedMethodCount;
	}

	public void increaseMatchedMethodCount() {
		matchedMethodCount++;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("translet", transletNamePattern);
		tsb.append("bean", beanIdPattern);
		tsb.append("class", classNamePattern);
		tsb.append("method", methodNamePattern);
		return tsb.toString();
	}
	
	public static String combinePatternString(String transletName, String beanId, String className, String methodName) {
		StringBuilder sb = new StringBuilder();
		
		if(transletName != null)
			sb.append(transletName);

		if(beanId != null) {
			sb.append(POINTCUT_BEAN_CLASS_DELIMITER);
			sb.append(beanId);
		} else if(className != null) {
			sb.append(POINTCUT_BEAN_CLASS_DELIMITER);
			sb.append(BeanRule.CLASS_DIRECTIVE_PREFIX);
			sb.append(className);
		}

		if(methodName != null) {
			sb.append(POINTCUT_METHOD_NAME_DELIMITER);
			sb.append(methodName);
		}
		
		return sb.toString();
	}

	/*
	public static String combinePatternString(JoinpointScopeType joinpointScope, String transletName, String beanId, String className, String methodName) {
		StringBuilder sb = new StringBuilder();
		
		if(joinpointScope != null) {
			sb.append(joinpointScope);
			sb.append(JOINPOINT_SCOPE_DELIMITER);
		}
		
		if(transletName != null)
			sb.append(transletName);

		if(beanId != null) {
			sb.append(POINTCUT_BEAN_CLASS_DELIMITER);
			sb.append(beanId);
		} else if(className != null) {
			sb.append(POINTCUT_BEAN_CLASS_DELIMITER);
			sb.append(BeanRule.CLASS_DIRECTIVE_PREFIX);
			sb.append(className);
		}

		if(methodName != null) {
			sb.append(POINTCUT_METHOD_NAME_DELIMITER);
			sb.append(methodName);
		}
		
		return sb.toString();
	}
	*/
	
	public static PointcutPatternRule parsePatternString(String patternString) {
		PointcutPatternRule ppr = new PointcutPatternRule();
		ppr.setPatternString(patternString);
		
		String transletNamePattern = null;
		String beanIdPattern = null;
		String methodNamePattern = null;

		int actionDelimiterIndex = patternString.indexOf(POINTCUT_BEAN_CLASS_DELIMITER);
		
		if(actionDelimiterIndex == -1)
			transletNamePattern = patternString;
		else if(actionDelimiterIndex == 0)
			beanIdPattern = patternString.substring(1);
		else {
			transletNamePattern = patternString.substring(0, actionDelimiterIndex);
			beanIdPattern = patternString.substring(actionDelimiterIndex + 1);
		}

		if(beanIdPattern != null) {
			int beanMethodDelimiterIndex = beanIdPattern.indexOf(POINTCUT_METHOD_NAME_DELIMITER);
			
			if(beanMethodDelimiterIndex == 0) {
				methodNamePattern = beanIdPattern.substring(1);
				beanIdPattern = null;
			} else if(beanMethodDelimiterIndex > 0) {
				methodNamePattern = beanIdPattern.substring(beanMethodDelimiterIndex + 1);
				beanIdPattern = beanIdPattern.substring(0, beanMethodDelimiterIndex);
			}
		}
		
		if(transletNamePattern != null)
			ppr.setTransletNamePattern(transletNamePattern);
		
		if(beanIdPattern != null) {
			if(beanIdPattern.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
				String className = beanIdPattern.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
				if(className.length() > 0) {
					ppr.setClassNamePattern(className);
				}
			} else {
				ppr.setBeanIdPattern(beanIdPattern);
			}
		}

		if(methodNamePattern != null)
			ppr.setMethodNamePattern(methodNamePattern);
		
		return ppr;
	}
	
	public static PointcutPatternRule newInstance(String translet, String bean, String method) {
		PointcutPatternRule ppr = new PointcutPatternRule();

		if(translet != null && translet.length() > 0)						
			ppr.setTransletNamePattern(translet);
		if(bean != null && bean.length() > 0) {
			if(bean.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
				String className = bean.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
				if(className.length() > 0) {
					ppr.setClassNamePattern(className);
				}
			} else {
				ppr.setBeanIdPattern(bean);
			}
		}
		if(method != null && method.length() > 0)
			ppr.setMethodNamePattern(method);
		
		return ppr;
	}
	
}
