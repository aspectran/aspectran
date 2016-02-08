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
package com.aspectran.core.context.aspect.pointcut;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.AspectranConstants;
import com.aspectran.core.context.rule.PointcutPatternRule;

/**
 * The Class AbstractPointcut.
 */
public abstract class AbstractPointcut {
	
	protected final List<PointcutPatternRule> pointcutPatternRuleList;
	
	protected final boolean existsBeanMethodNamePattern;

	public AbstractPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		this.pointcutPatternRuleList = pointcutPatternRuleList;
		
		if(pointcutPatternRuleList != null) {
			boolean existsBeanMethodNamePattern = false;
			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(ppr.getBeanMethodNamePattern() != null) {
					existsBeanMethodNamePattern = true;
					break;
				}
			}
			this.existsBeanMethodNamePattern = existsBeanMethodNamePattern;
		} else {
			this.existsBeanMethodNamePattern = false;
		}
	}

	public List<PointcutPatternRule> getPointcutPatternRuleList() {
		return pointcutPatternRuleList;
	}

	public void addPointcutPatternRule(List<PointcutPatternRule> pointcutPatternList) {
		if(pointcutPatternList == null) {
			pointcutPatternList = new ArrayList<PointcutPatternRule>(pointcutPatternList);
			return;
		}

		pointcutPatternList.addAll(pointcutPatternList);
	}
	
	public boolean isExistsBeanMethodNamePattern() {
		return existsBeanMethodNamePattern;
	}

	public boolean matches(String transletName) {
		return matches(transletName, null, null);
	}

	public boolean matches(String transletName, String beanId) {
		return matches(transletName, beanId, null);
	}
	
	public boolean matches(String transletName, String beanId, String beanMethodName) {
		if(pointcutPatternRuleList != null) {
			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(matches(ppr, transletName, beanId, beanMethodName)) {
					List<PointcutPatternRule> epprList = ppr.getExcludePointcutPatternRuleList();
					
					if(epprList != null) {
						for(PointcutPatternRule eppr : epprList) {
							if(matches(eppr, transletName, beanId, beanMethodName)) {
								return false;
							}
						}
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 비교 항목의 값이 null이면 참으로 간주함.
	 *
	 * @param pointcutPatternRule the pointcut pattern
	 * @param transletName the translet name
	 * @param beanId the bean or action id
	 * @param beanMethodName the bean method name
	 * @return true, if successful
	 */
	protected boolean matches(PointcutPatternRule pointcutPatternRule, String transletName, String beanId, String beanMethodName) {
		if((transletName == null && pointcutPatternRule.getTransletNamePattern() != null) ||
				(beanId == null && pointcutPatternRule.getBeanIdPattern() != null) ||
				(beanMethodName == null && pointcutPatternRule.getBeanMethodNamePattern() != null))
			return false;
		
		return exists(pointcutPatternRule, transletName, beanId, beanMethodName);
	}

	public boolean exists(String transletName) {
		return exists(transletName, null, null);
	}
	
	public boolean exists(String transletName, String beanId) {
		return exists(transletName, beanId, null);
	}
	
	public boolean exists(String transletName, String beanId, String beanMethodName) {
		if(pointcutPatternRuleList != null) {
			for(PointcutPatternRule ppr : pointcutPatternRuleList) {
				if(exists(ppr, transletName, beanId, beanMethodName)) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * 비교 항목의 값이 null이면 참으로 간주함.
	 *
	 * @param pointcutPatternRule the pointcut pattern
	 * @param transletName the translet name
	 * @param beanId the bean or action id
	 * @param beanMethodName the bean method name
	 * @return true, if successful
	 */
	protected boolean exists(PointcutPatternRule pointcutPatternRule, String transletName, String beanId, String beanMethodName) {
		boolean matched = true;
		
		if(transletName != null && pointcutPatternRule.getTransletNamePattern() != null) {
			matched = patternMatches(pointcutPatternRule.getTransletNamePattern(), transletName, AspectranConstants.TRANSLET_NAME_SEPARATOR);
		}
		
		if(matched && beanId != null && pointcutPatternRule.getBeanIdPattern() != null) {
			matched = patternMatches(pointcutPatternRule.getBeanIdPattern(), beanId, AspectranConstants.ID_SEPARATOR);
		}
		
		if(matched && beanMethodName != null && pointcutPatternRule.getBeanMethodNamePattern() != null) {
			matched = patternMatches(pointcutPatternRule.getBeanMethodNamePattern(), beanMethodName);
		}
		
		return matched;
	}
	
	abstract public boolean patternMatches(String pattern, String str);
	
	abstract public boolean patternMatches(String pattern, String str, char separator);

	public void clear() {
	}
	
}
