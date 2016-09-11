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
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.context.rule.type.JoinpointType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class AspectRule.
 * 
 * <pre>
 * &lt;aspect id="sampleAspect" order="0" isolated="true"&gt;
 *   &lt;joinpoint type="translet"&gt;
 *     methods: [
 *       "GET"
 *       "POST"
 *       "PATCH"
 *       "PUT"
 *       "DELETE"
 *     ]
 *     headers: [
 *       "Origin"
 *     ]
 *     pointcut: {
 *       type: "wildcard"
 *       +: "/a/b@sample.bean1^method1"
 *       +: "/x/y@sample.bean2^method1"
 *       -: "/a/b/c@sample.bean3^method1"
 *       -: "/x/y/z@sample.bean4^method1"
 *     }
 *     pointcut: {
 *       type: "regexp"
 *       include: {
 *         translet: "/a/b"
 *         bean: "sample.bean1"
 *         method: "method1"
 *       }
 *       execlude: {
 *         translet: "/a/b/c"
 *         bean: "sample.bean3"
 *         method: "method1"
 *       }
 *     }
 *   &lt;/joinpoint&gt;
 *   &lt;settings&gt;
 *   &lt;/settings&gt;
 *   &lt;advice&gt;
 *   &lt;/advice&gt;
 *   &lt;exception&gt;
 *   &lt;/exception&gt;
 * &lt;aspect&gt;
 * </pre>
 */
public class AspectRule implements BeanReferenceInspectable {

	private static final BeanReferrerType BEAN_REFERRER_TYPE = BeanReferrerType.ASPECT_RULE;

	private String id;

	/**
	 * The lowest value has highest priority.
	 * Normally starting with 0, with Integer.MAX_VALUE indicating the greatest value.
	 */
	private int order = Integer.MAX_VALUE;

	private Boolean isolated;

	private JoinpointRule joinpointRule;

	private Pointcut pointcut;

	private String adviceBeanId;

	private Class<?> adviceBeanClass;
	
	private SettingsAdviceRule settingsAdviceRule;
	
	private List<AspectAdviceRule> aspectAdviceRuleList;
	
	private ExceptionRule exceptionRule;
	
	private boolean beanRelevanted;
	
	private String description;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	private void setOrder(int order) {
		this.order = order;
	}

	public Boolean getIsolated() {
		return isolated;
	}

	public boolean isIsolated() {
		return BooleanUtils.toBoolean(isolated, false);
	}

	private void setIsolated(Boolean isolated) {
		this.isolated = isolated;
	}

	public JoinpointRule getJoinpointRule() {
		return joinpointRule;
	}

	private void setJoinpointRule(JoinpointRule joinpointRule) {
		this.joinpointRule = joinpointRule;
	}

	public JoinpointType getJoinpointType() {
		return (joinpointRule != null ? joinpointRule.getJoinpointType() : null);
	}

	public MethodType[] getTargetMethods() {
		return (joinpointRule != null ? joinpointRule.getTargetMethods() : null);
	}

	public String[] getTargetHeaders() {
		return (joinpointRule != null ? joinpointRule.getTargetHeaders() : null);
	}

	public PointcutRule getPointcutRule() {
		return (joinpointRule != null ? joinpointRule.getPointcutRule() : null);
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
	
	public void addAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
		AspectAdviceRule.updateBeanActionClass(aspectAdviceRule);

		if (aspectAdviceRuleList == null) {
			aspectAdviceRuleList = new ArrayList<AspectAdviceRule>();
		}
		aspectAdviceRuleList.add(aspectAdviceRule);
	}

	public ExceptionRule getExceptionRule() {
		return exceptionRule;
	}

	public void setExceptionRule(ExceptionRule exceptionRule) {
		this.exceptionRule = exceptionRule;
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
		return BEAN_REFERRER_TYPE;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("id", id);
		if (order != Integer.MAX_VALUE) {
			tsb.append("order", order);
		}
		tsb.append("isolated", isolated);
		tsb.append("joinpointRule", joinpointRule);
		tsb.append("settingsAdviceRule", settingsAdviceRule);
		tsb.append("aspectAdviceRuleList", aspectAdviceRuleList);
		tsb.append("exceptionRule", exceptionRule);
		tsb.append("beanRelevanted", beanRelevanted);
		return tsb.toString();
	}
	
	public static AspectRule newInstance(String id, String order, Boolean isolated) {
		if (id == null) {
			throw new IllegalArgumentException("The 'aspect' element requires an 'id' attribute.");
		}

		AspectRule aspectRule = new AspectRule();
		aspectRule.setId(id);
		aspectRule.setIsolated(isolated);

		if (!StringUtils.isEmpty(order)) {
			try {
				aspectRule.setOrder(Integer.parseInt(order));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("The 'order' attribute on an 'aspect' element must be a valid integer.");
			}
		}

		return aspectRule;
	}
	
	public static void updateJoinpoint(AspectRule aspectRule, String type, String text) {
		JoinpointRule joinpointRule = JoinpointRule.newInstance();
		JoinpointRule.updateJoinpointType(joinpointRule, type);
		JoinpointRule.updateJoinpoint(joinpointRule, text);
		aspectRule.setJoinpointRule(joinpointRule);
	}
	
	public static void updateJoinpoint(AspectRule aspectRule, Parameters joinpointParameters) {
		JoinpointRule joinpointRule = JoinpointRule.newInstance();
		JoinpointRule.updateJoinpoint(joinpointRule, joinpointParameters);
		aspectRule.setJoinpointRule(joinpointRule);
	}

}
