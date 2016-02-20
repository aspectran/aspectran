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

import com.aspectran.core.context.aspect.pointcut.Pointcut;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.AspectTargetType;
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.context.rule.type.JoinpointScopeType;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class AspectRule.
 */
public class AspectRule implements BeanReferenceInspectable {

	private static final BeanReferrerType BEAN_REFERABLE_RULE_TYPE = BeanReferrerType.ASPECT_RULE;

	private String id;

	private AspectTargetType aspectTargetType;
	
	private JoinpointScopeType joinpointScope;
	
	private PointcutRule pointcutRule;
	
	private Pointcut pointcut;
	
	private String adviceBeanId;

	private Class<?> adviceBeanClass;
	
	private SettingsAdviceRule settingsAdviceRule;
	
	private List<AspectAdviceRule> aspectAdviceRuleList;
	
	private List<AspectJobAdviceRule> aspectJobAdviceRuleList; // for scheduling aspects
	
	private ExceptionHandlingRule exceptionHandlingRule;
	
	private boolean beanRelevanted;
	
	private String description;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AspectTargetType getAspectTargetType() {
		return aspectTargetType;
	}

	public void setAspectTargetType(AspectTargetType aspectTargetType) {
		this.aspectTargetType = aspectTargetType;
	}

	public JoinpointScopeType getJoinpointScope() {
		return joinpointScope;
	}

	public void setJoinpointScope(JoinpointScopeType joinpointScope) {
		this.joinpointScope = joinpointScope;
	}

	public PointcutRule getPointcutRule() {
		return pointcutRule;
	}

	public void setPointcutRule(PointcutRule pointcutRule) {
		this.pointcutRule = pointcutRule;
	}

	public Pointcut getPointcut() {
		return pointcut;
	}

	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	public String getAdviceBeanId() {
		return adviceBeanId;
	}

	public void setAdviceBeanId(String adviceBeanId) {
		this.adviceBeanId = adviceBeanId;
	}

	public Class<?> getAdviceBeanClass() {
		return adviceBeanClass;
	}

	public void setAdviceBeanClass(Class<?> adviceBeanClass) {
		this.adviceBeanClass = adviceBeanClass;
	}

	public SettingsAdviceRule getSettingsAdviceRule() {
		return settingsAdviceRule;
	}

	public void setSettingsAdviceRule(SettingsAdviceRule settingsAdviceRule) {
		this.settingsAdviceRule = settingsAdviceRule;
	}

	public List<AspectAdviceRule> getAspectAdviceRuleList() {
		return aspectAdviceRuleList;
	}

	public void setAspectAdviceRuleList(List<AspectAdviceRule> aspectAdviceRuleList) {
		this.aspectAdviceRuleList = aspectAdviceRuleList;
	}
	
	public void addAspectAdviceRule(AspectAdviceRule aar) {
		if(aspectAdviceRuleList == null)
			aspectAdviceRuleList = new ArrayList<AspectAdviceRule>();
		
		aspectAdviceRuleList.add(aar);
	}

	public List<AspectJobAdviceRule> getAspectJobAdviceRuleList() {
		return aspectJobAdviceRuleList;
	}

	public void setAspectJobAdviceRuleList(List<AspectJobAdviceRule> aspectJobAdviceRuleList) {
		this.aspectJobAdviceRuleList = aspectJobAdviceRuleList;
	}
	
	public void addAspectJobAdviceRule(AspectJobAdviceRule atar) {
		if(aspectJobAdviceRuleList == null)
			aspectJobAdviceRuleList = new ArrayList<AspectJobAdviceRule>();
		
		aspectJobAdviceRuleList.add(atar);
	}

	public ExceptionHandlingRule getExceptionHandlingRule() {
		return exceptionHandlingRule;
	}

	public void setExceptionHandlingRule(ExceptionHandlingRule exceptionHandlingRule) {
		this.exceptionHandlingRule = exceptionHandlingRule;
	}
	
	public void addExceptionHandlingRule(ResponseByContentTypeRule responseByContentTypeRule) {
		if(exceptionHandlingRule == null)
			exceptionHandlingRule = new ExceptionHandlingRule();
		
		exceptionHandlingRule.putResponseByContentTypeRule(responseByContentTypeRule);
	}

	public boolean isBeanRelevanted() {
		return beanRelevanted;
	}

	public void setBeanRelevanted(boolean beanRelevanted) {
		this.beanRelevanted = beanRelevanted;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public BeanReferrerType getBeanReferrerType() {
		return BEAN_REFERABLE_RULE_TYPE;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("id", id);
		tsb.append("for", aspectTargetType);
		tsb.append("joinpointScope", joinpointScope);
		tsb.append("pointcutRule", pointcutRule);
		if(aspectTargetType == AspectTargetType.TRANSLET) {
			tsb.append("settingsAdviceRule", settingsAdviceRule);
			tsb.append("aspectAdviceRuleList", aspectAdviceRuleList);
		} else if(aspectTargetType == AspectTargetType.SCHEDULER) {
			tsb.append("aspectJobAdviceRuleList", aspectJobAdviceRuleList);
		}
		tsb.append("exceptionHandlingRule", exceptionHandlingRule);
		tsb.append("beanRelevanted", beanRelevanted);
		return tsb.toString();
	}
	
	public static AspectRule newInstance(String id, String useFor) {
		AspectTargetType aspectTargetType = null;
		
		if(useFor != null) {
			aspectTargetType = AspectTargetType.lookup(useFor);
			if(aspectTargetType == null)
				throw new IllegalArgumentException("No aspect target type registered for '" + useFor + "'.");
		} else {
			aspectTargetType = AspectTargetType.TRANSLET;
		}
		
		AspectRule aspectRule = new AspectRule();
		aspectRule.setId(id);
		aspectRule.setAspectTargetType(aspectTargetType);
		
		return aspectRule;
	}
	
	public static void updateJoinpointScope(AspectRule aspectRule, String scope) {
		JoinpointScopeType joinpointScope = null;
		
		if(scope != null) {
			joinpointScope = JoinpointScopeType.lookup(scope);
			if(joinpointScope == null)
				throw new IllegalArgumentException("No joinpoint scope type registered for '" + scope + "'.");
		} else {
			joinpointScope = JoinpointScopeType.TRANSLET;
		}
		
		aspectRule.setJoinpointScope(joinpointScope);
	}
	
}
