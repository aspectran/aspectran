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
import com.aspectran.core.context.builder.apon.params.JoinpointParameters;
import com.aspectran.core.context.builder.apon.params.PointTargetParameters;
import com.aspectran.core.context.builder.apon.params.PointcutParameters;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.BeanReferrerType;
import com.aspectran.core.context.rule.type.JoinpointType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.PointcutType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class AspectRule.
 * 
 * <pre>
 * &lt;joinpoint scope="translet"&gt;
 *   methods: [
 *     "GET"
 *     "POST"
 *     "PATCH"
 *     "PUT"
 *     "DELETE"
 *   ]
 *   headers: [
 * 	   "Origin"
 *   ]
 *   pointcut: {
 * 	   type: "wildcard"
 * 	   +: "/a/b@sample.bean1^method1"
 * 	   +: "/x/y@sample.bean2^method1"
 * 	   -: "/a/b/c@sample.bean3^method1"
 * 	   -: "/x/y/z@sample.bean4^method1"
 *   }
 *   pointcut: {
 * 	   type: "regexp"
 * 	   include: {
 *       translet: "/a/b"
 *       bean: "sample.bean1"
 *       method: "method1"
 *     }
 *     execlude: {
 * 	     translet: "/a/b/c"
 *       bean: "sample.bean3"
 *       method: "method1"
 *     }
 *   }
 * &lt;/joinpoint&gt;
 * </pre>
 */
public class AspectRule implements BeanReferenceInspectable {

	private static final BeanReferrerType BEAN_REFERRER_TYPE = BeanReferrerType.ASPECT_RULE;

	private String id;

	private JoinpointType joinpointType;
	
	private MethodType[] targetMethods;
	
	private String[] targetHeaders;
	
	private PointcutRule pointcutRule;
	
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

	public JoinpointType getJoinpointType() {
		return joinpointType;
	}

	public void setJoinpointScope(JoinpointType joinpointType) {
		this.joinpointType = joinpointType;
	}

	public MethodType[] getTargetMethods() {
		return targetMethods;
	}

	public void setTargetMethods(MethodType[] targetMethods) {
		this.targetMethods = targetMethods;
	}

	public String[] getTargetHeaders() {
		return targetHeaders;
	}

	public void setTargetHeaders(String[] targetHeaders) {
		this.targetHeaders = targetHeaders;
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
	
	public void addAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
		AspectAdviceRule.updateBeanActionClass(aspectAdviceRule);

		if(aspectAdviceRuleList == null) {
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
		tsb.append("joinpointScope", joinpointType);
		tsb.append("pointcutRule", pointcutRule);
		if(aspectTargetType == AspectTargetType.TRANSLET) {
			tsb.append("settingsAdviceRule", settingsAdviceRule);
			tsb.append("aspectAdviceRuleList", aspectAdviceRuleList);
		} else if(aspectTargetType == AspectTargetType.SCHEDULER) {
			tsb.append("aspectJobAdviceRuleList", aspectJobAdviceRuleList);
		}
		tsb.append("exceptionRule", exceptionRule);
		tsb.append("beanRelevanted", beanRelevanted);
		return tsb.toString();
	}
	
	public static AspectRule newInstance(String id) {
		AspectTargetType aspectTargetType;
		
		if(usedFor != null) {
			aspectTargetType = AspectTargetType.resolve(usedFor);
			if(aspectTargetType == null)
				throw new IllegalArgumentException("No aspect target type registered for '" + usedFor + "'.");
		} else {
			aspectTargetType = AspectTargetType.TRANSLET;
		}
		
		AspectRule aspectRule = new AspectRule();
		aspectRule.setId(id);
		aspectRule.setAspectTargetType(aspectTargetType);
		
		return aspectRule;
	}
	
	public static void updateJoinpointScope(AspectRule aspectRule, String scope) {
		JoinpointType joinpointScope;
		
		if(scope != null) {
			joinpointScope = JoinpointType.resolve(scope);
			if(joinpointScope == null)
				throw new IllegalArgumentException("No joinpoint scope type registered for '" + scope + "'.");
		} else {
			joinpointScope = JoinpointType.TRANSLET;
		}
		
		aspectRule.setJoinpointScope(joinpointScope);
	}
	
	public static void updateTargetMethods(AspectRule aspectRule, String[] methods) {
		MethodType[] targetMethods = null;
		if(methods != null) {
			List<MethodType> methodTypes = new ArrayList<>(methods.length);
			for(String method : methods) {
				MethodType methodType = MethodType.resolve(method);
				if(methodType == null) {
					throw new IllegalArgumentException("No request method type registered for '" + method + "'.");
				}
				methodTypes.add(methodType);
			}
			targetMethods = methodTypes.toArray(new MethodType[methodTypes.size()]);
		}
		aspectRule.setTargetMethods(targetMethods);
	}
	
	public static void updateTargetHeaders(AspectRule aspectRule, String[] headers) {
		String[] targetHeaders = null;
		if(headers != null) {
			List<String> headerList = new ArrayList<>(headers.length);
			for(String header : headers) {
				if(StringUtils.hasText(header)) {
					headerList.add(header);
				}
			}
			targetHeaders = headerList.toArray(new String[headerList.size()]);
		}
		aspectRule.setTargetHeaders(targetHeaders);
	}
	
	public static PointcutRule updateJoinpoint(AspectRule aspectRule, String type, Parameters joinpointParameters) {
		PointcutRule pointcutRule = null;
		Parameters pointcutParameters = joinpointParameters.getParameters(JoinpointParameters.pointcut);
		
		if(pointcutParameters != null) {
			PointcutType pointcutType = null;
			
			if(type == null) {
				type = pointcutParameters.getString(PointcutParameters.type);
			}
			if(type != null) {
				pointcutType = PointcutType.resolve(type);
				if(pointcutType == null) {
					throw new IllegalArgumentException("Unknown pointcut type '" + type + "'. Pointcut type for Translet must be 'wildcard' or 'regexp'.");
				}
			}
			
			pointcutRule = new PointcutRule(pointcutType);
			
			List<Parameters> targetParametersList = pointcutParameters.getParametersList(PointcutParameters.targets);
			if(targetParametersList != null) {
				for(Parameters targetParameters : targetParametersList) {
					addPointcutPatternRule(pointcutRule.touchPointcutPatternRuleList(), targetParameters);
					pointcutRule.touchTargetParametersList().add(targetParameters);
				}
			}
		}

		return pointcutRule;
	}


	private static List<PointcutPatternRule> addPointcutPatternRule(List<PointcutPatternRule> pointcutPatternRuleList, Parameters targetParameters) {
		String translet = targetParameters.getString(PointTargetParameters.translet);
		String bean = targetParameters.getString(PointTargetParameters.bean);
		String method = targetParameters.getString(PointTargetParameters.method);
		List<Parameters> excludeTargetParametersList = targetParameters.getParametersList(PointTargetParameters.excludeTargets);
		
		if(StringUtils.hasLength(translet) || StringUtils.hasLength(bean) || StringUtils.hasLength(method) || (excludeTargetParametersList != null && !excludeTargetParametersList.isEmpty())) {
			PointcutPatternRule pointcutPatternRule = PointcutPatternRule.newInstance(translet, bean, method);
			
			if(excludeTargetParametersList != null && !excludeTargetParametersList.isEmpty()) {
				for(Parameters excludeTargetParameters : excludeTargetParametersList) {
					addExcludePointcutPatternRule(pointcutPatternRule, excludeTargetParameters);
				}
			}
			
			pointcutPatternRuleList.add(pointcutPatternRule);
		}
		
		List<String> plusPatternStringList = targetParameters.getStringList(PointTargetParameters.pluses);
		List<String> minusPatternStringList = targetParameters.getStringList(PointTargetParameters.minuses);
		
		List<PointcutPatternRule> minusPointcutPatternRuleList = null;
		
		if(minusPatternStringList != null && !minusPatternStringList.isEmpty()) {
			minusPointcutPatternRuleList = new ArrayList<PointcutPatternRule>(minusPatternStringList.size());
			
			for(String patternString : minusPatternStringList) {
				PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePatternString(patternString);
				minusPointcutPatternRuleList.add(pointcutPatternRule);
			}
		}
		
		if(plusPatternStringList != null && !plusPatternStringList.isEmpty()) {
			for(String patternString : plusPatternStringList) {
				PointcutPatternRule pointcutPatternRule = PointcutPatternRule.parsePatternString(patternString);
				if(minusPointcutPatternRuleList != null)
					pointcutPatternRule.setExcludePointcutPatternRuleList(minusPointcutPatternRuleList);
				
				pointcutPatternRuleList.add(pointcutPatternRule);
			}
		}
		
		return pointcutPatternRuleList;
	}

	private static void addExcludePointcutPatternRule(PointcutPatternRule pointcutPatternRule, Parameters excludeTargetParameters) {
		String translet = excludeTargetParameters.getString(PointTargetParameters.translet);
		String bean = excludeTargetParameters.getString(PointTargetParameters.bean);
		String method = excludeTargetParameters.getString(PointTargetParameters.method);

		if(StringUtils.hasLength(translet) || StringUtils.hasLength(bean) || StringUtils.hasLength(method)) {
			PointcutPatternRule ppr = PointcutPatternRule.newInstance(translet, bean, method);
			pointcutPatternRule.addExcludePointcutPatternRule(ppr);
		}
	}

}
