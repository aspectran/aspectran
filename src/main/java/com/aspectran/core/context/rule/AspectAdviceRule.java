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

import com.aspectran.core.activity.process.action.BeanAction;
import com.aspectran.core.activity.process.action.EchoAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class AspectAdviceRule.
 * 
 * <p>Created: 2008. 04. 01 PM 11:19:28</p>
 */
public class AspectAdviceRule implements ActionRuleApplicable {

	private final AspectRule aspectRule;
	
	private final String adviceBeanId;
	
	private final Class<?> adviceBeanClass;

	private final AspectAdviceType aspectAdviceType;
	
	private Executable action;
	
	public AspectAdviceRule(AspectRule aspectRule, AspectAdviceType aspectAdviceType) {
		this.aspectRule = aspectRule;
		this.adviceBeanId = aspectRule.getAdviceBeanId();
		this.adviceBeanClass = aspectRule.getAdviceBeanClass();
		this.aspectAdviceType = aspectAdviceType;
	}
	
	public String getAspectId() {
		return aspectRule.getId();
	}

	public AspectRule getAspectRule() {
		return aspectRule;
	}

	public String getAdviceBeanId() {
		return adviceBeanId;
	}

	public Class<?> getAdviceBeanClass() {
		return adviceBeanClass;
	}

	public AspectAdviceType getAspectAdviceType() {
		return aspectAdviceType;
	}

	@Override
	public void applyActionRule(EchoActionRule echoActionRule) {
		action = new EchoAction(echoActionRule, null);
	}

	@Override
	public void applyActionRule(BeanActionRule beanActionRule) {
		beanActionRule.setAspectAdviceRule(this);
		action = new BeanAction(beanActionRule, null);
	}

	@Override
	public void applyActionRule(MethodActionRule methodActionRule) {
		throw new UnsupportedOperationException(
				"Cannot apply Method Action Rule to Aspect Advice Rule. AspecetAdvice is not support MethodAction.");
	}

	@Override
	public void applyActionRule(IncludeActionRule includeActionRule) {
		throw new UnsupportedOperationException(
				"Cannot apply Include Action Rule to Aspect Advice Rule. AspecetAdvice is not support IncludeAction.");
	}
	
	public Executable getExecutableAction() {
		return action;
	}
	
	public ActionType getActionType() {
		if(action == null)
			return null;
		
		return action.getActionType();
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean preventRecursive) {
		ToStringBuilder tsb = new ToStringBuilder();
		if(aspectRule != null)
			tsb.append("aspectId", aspectRule.getId());
		tsb.append("aspectAdviceType", aspectAdviceType);
		if(!preventRecursive)
			tsb.append("action", action);
		return tsb.toString();
	}

	public static AspectAdviceRule newInstance(AspectRule aspectRule, AspectAdviceType aspectAdviceType) {
		AspectAdviceRule aspectAdviceRule = new AspectAdviceRule(aspectRule, aspectAdviceType);

		return aspectAdviceRule;
	}

	public static BeanActionRule updateBeanActionClass(AspectAdviceRule aspectAdviceRule) {
		if(aspectAdviceRule.getAdviceBeanId() != null) {
			if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
				BeanAction beanAction = (BeanAction)aspectAdviceRule.getExecutableAction();
				BeanActionRule beanActionRule = beanAction.getBeanActionRule();
				if(beanActionRule.getBeanId() == null) {
					String beanIdOrClass = aspectAdviceRule.getAdviceBeanId();
					Class<?> beanClass = aspectAdviceRule.getAdviceBeanClass();

					beanActionRule.setBeanId(beanIdOrClass);
					beanActionRule.setBeanClass(beanClass);

					return beanActionRule;
				}
			}
		}
		return null;
	}
	
}
