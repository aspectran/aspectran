/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.context.builder.apon;

import java.util.Iterator;
import java.util.List;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.apon.params.ActionParameters;
import com.aspectran.core.context.builder.apon.params.AdviceParameters;
import com.aspectran.core.context.builder.apon.params.AspectParameters;
import com.aspectran.core.context.builder.apon.params.AspectranParameters;
import com.aspectran.core.context.builder.apon.params.DispatchParameters;
import com.aspectran.core.context.builder.apon.params.ExceptionRaizedParameters;
import com.aspectran.core.context.builder.apon.params.JobParameters;
import com.aspectran.core.context.builder.apon.params.JoinpointParameters;
import com.aspectran.core.context.builder.apon.params.ResponseByContentTypeParameters;
import com.aspectran.core.context.builder.apon.params.TemplateParameters;
import com.aspectran.core.var.apon.Parameters;
import com.aspectran.core.var.rule.AspectAdviceRule;
import com.aspectran.core.var.rule.AspectJobAdviceRule;
import com.aspectran.core.var.rule.AspectRule;
import com.aspectran.core.var.rule.BeanActionRule;
import com.aspectran.core.var.rule.DispatchResponseRule;
import com.aspectran.core.var.rule.EchoActionRule;
import com.aspectran.core.var.rule.IncludeActionRule;
import com.aspectran.core.var.rule.ItemRule;
import com.aspectran.core.var.rule.ItemRuleMap;
import com.aspectran.core.var.rule.PointcutRule;
import com.aspectran.core.var.rule.ResponseByContentTypeRule;
import com.aspectran.core.var.rule.SettingsAdviceRule;
import com.aspectran.core.var.rule.ability.ActionRuleApplicable;
import com.aspectran.core.var.token.Token;
import com.aspectran.core.var.type.AspectAdviceType;
import com.aspectran.core.var.type.DefaultSettingType;
import com.aspectran.core.var.type.TokenType;

/**
 * AponAspectranContextBuilder.
 * 
 * <p>Created: 2015. 01. 27 오후 10:36:29</p>
 */
public class AponAssembler {
	
	private final ContextBuilderAssistant assistant;
	
	public AponAssembler(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}
	
	public void assembleAspectran(Parameters aspectranParameters) {
		assembleDefaultSettings(aspectranParameters.getParameters(AspectranParameters.setting));
		assembleTypeAlias(aspectranParameters.getParameters(AspectranParameters.typeAlias));
		assembleAspectRule(aspectranParameters.getParametersList(AspectranParameters.aspects));
	}
	
	public void assembleDefaultSettings(Parameters parameters) {
		if(parameters == null)
			return;
		
		Iterator<String> iter = parameters.getParameterNameSet().iterator();
		
		while(iter.hasNext()) {
			String name = iter.next();
			
			DefaultSettingType settingType = null;
			
			if(name != null) {
				settingType = DefaultSettingType.valueOf(name);
				
				if(settingType == null)
					throw new IllegalArgumentException("Unknown setting name '" + name + "'");
			}
			
			assistant.putSetting(settingType, parameters.getString(name));
		}
	}
	
	public void assembleTypeAlias(Parameters parameters) {
		if(parameters == null)
			return;
		
		Iterator<String> iter = parameters.getParameterNameSet().iterator();
		
		while(iter.hasNext()) {
			String alias = iter.next();
			assistant.addTypeAlias(alias, parameters.getString(alias));
		}
	}
	
	public void assembleAspectRule(List<Parameters> aspectParametersList) {
		for(Parameters aspectParameters : aspectParametersList) {
			assembleAspectRule(aspectParameters);
		}
	}
	
	public void assembleAspectRule(Parameters aspectParameters) {
		String id = aspectParameters.getString(AspectParameters.id);
		String useFor = aspectParameters.getString(AspectParameters.useFor);
		AspectRule aspectRule = AspectRule.newInstance(id, useFor);

		Parameters joinpointParams = aspectParameters.getParameters(AspectParameters.jointpoint);
		String scope = joinpointParams.getString(JoinpointParameters.scope);
		AspectRule.updateJoinpointScope(aspectRule, scope);

		Parameters pointcutParams = joinpointParams.getParameters(JoinpointParameters.pointcut);
		PointcutRule pointcutRule = PointcutRule.newInstance(aspectRule, null, pointcutParams);
		aspectRule.setPointcutRule(pointcutRule);

		Parameters settingParams = aspectParameters.getParameters(AspectParameters.setting);
		if(settingParams != null) {
			SettingsAdviceRule settingsAdviceRule = SettingsAdviceRule.newInstance(aspectRule, settingParams);
			aspectRule.setSettingsAdviceRule(settingsAdviceRule);
		}
		
		Parameters adviceParams = aspectParameters.getParameters(AspectParameters.advice);
		String adviceBeanId = adviceParams.getString(AdviceParameters.bean);
		if(adviceBeanId != null) {
			aspectRule.setAdviceBeanId(adviceBeanId);
			assistant.putBeanReference(adviceBeanId, aspectRule);
		}
		
		List<Parameters> beforeActionParamsList = adviceParams.getParametersList(AdviceParameters.beforeActions);
		if(beforeActionParamsList != null && beforeActionParamsList.size() > 0) {
			for(Parameters actionParameters : beforeActionParamsList) {
				AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.BEFORE);
				assembleActionRule(actionParameters, aspectAdviceRule);
				aspectRule.addAspectAdviceRule(aspectAdviceRule);
			}
		}
		
		List<Parameters> afterActionParamsList = adviceParams.getParametersList(AdviceParameters.afterActions);
		if(afterActionParamsList != null && afterActionParamsList.size() > 0) {
			for(Parameters actionParameters : afterActionParamsList) {
				AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AFTER);
				assembleActionRule(actionParameters, aspectAdviceRule);
				aspectRule.addAspectAdviceRule(aspectAdviceRule);
			}
		}

		List<Parameters> aroundActionParamsList = adviceParams.getParametersList(AdviceParameters.aroundActions);
		if(aroundActionParamsList != null && aroundActionParamsList.size() > 0) {
			for(Parameters actionParameters : aroundActionParamsList) {
				AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AROUND);
				assembleActionRule(actionParameters, aspectAdviceRule);
				aspectRule.addAspectAdviceRule(aspectAdviceRule);
			}
		}

		List<Parameters> finallyActionParamsList = adviceParams.getParametersList(AdviceParameters.finallyActions);
		if(finallyActionParamsList != null && finallyActionParamsList.size() > 0) {
			for(Parameters actionParameters : finallyActionParamsList) {
				AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AROUND);
				assembleActionRule(actionParameters, aspectAdviceRule);
				aspectRule.addAspectAdviceRule(aspectAdviceRule);
			}
		}

		Parameters exceptionRaizedParams = adviceParams.getParameters(AdviceParameters.exceptionRaized);
		if(exceptionRaizedParams != null) {
//			actions = new ParameterDefine("action", new ActionParameters(), true);
//			responseByContentType = new ParameterDefine("responseByContentType", new ResponseByContentTypeParameters());
//			defaultResponse = new ParameterDefine("defaultResponse", new DefaultResponseParameters());

			AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.EXCPETION_RAIZED);
			List<Parameters> actionParamsList = exceptionRaizedParams.getParametersList(ExceptionRaizedParameters.actions);
			
			
		}
		
		List<Parameters> jobParamsList = adviceParams.getParametersList(AdviceParameters.jobs);
		if(jobParamsList != null && jobParamsList.size() > 0) {
			for(Parameters jobParameters : jobParamsList) {
				String translet = jobParameters.getString(JobParameters.translet);
				boolean disabled = jobParameters.getBoolean(JobParameters.disabled);
				AspectJobAdviceRule ajar = AspectJobAdviceRule.newInstance(aspectRule, translet, disabled);
				aspectRule.addAspectJobAdviceRule(ajar);
			}
		}

		//		bean = new ParameterDefine("bean", ParameterValueType.STRING);
//		beforeActions = new ParameterDefine("before", new ActionParameters(), true);
//		afterActions = new ParameterDefine("after", new ActionParameters(), true);
//		aroundActions = new ParameterDefine("around", new ActionParameters(), true);
//		finallyActions = new ParameterDefine("finally", new ActionParameters(), true);
//		exceptionRaized = new ParameterDefine("exceptionRaized", new ExceptionRaizedParameters());
//		jobs = new ParameterDefine("job", new JobParameters(), true);
		
		assistant.addAspectRule(aspectRule);
	}
	
	public ResponseByContentTypeRule assembleResponseByContentTypeRule(Parameters responseByContentTypeParameters) {
		ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
		
//		exceptionType = new ParameterDefine("exceptionType", ParameterValueType.STRING);
//		dispatchs = new ParameterDefine("dispatch", new DispatchParameters(), true);
//		transforms = new ParameterDefine("transform", new TransformParameters(), true);
//		redirects = new ParameterDefine("redirect", new RedirectParameters(), true);
//		forwards = new ParameterDefine("forward", new ForwardParameters(), true);
		
		String exceptionType = responseByContentTypeParameters.getString(ResponseByContentTypeParameters.exceptionType);
		rbctr.setExceptionType(exceptionType);
		
		List<Parameters> dispatchParamsList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.dispatchs);
		if(dispatchParamsList != null && dispatchParamsList.size() > 0) {
			for(Parameters dispatchParameters : dispatchParamsList) {
				DispatchResponseRule drr = assembleDispatchResponseRule(dispatchParameters);
				rbctr.applyResponseRule(drr);
			}
		}
		
		List<Parameters> transformParamsList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.transforms);
		List<Parameters> redirectParamsList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.redirects);
		List<Parameters> tranformParamsList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.transforms);
		
		
		return rbctr;
	}
	
	public DispatchResponseRule assembleDispatchResponseRule(Parameters dispatchParameters) {
		String contentType = dispatchParameters.getString(DispatchParameters.contentType);
		String characterEncoding = dispatchParameters.getString(DispatchParameters.characterEncoding);
		Parameters templateParams = dispatchParameters.getParameters(DispatchParameters.template);
		List<Parameters> actionParamsList = dispatchParameters.getParametersList(DispatchParameters.actions);
		
		DispatchResponseRule drr = DispatchResponseRule.newInstance(contentType, characterEncoding);
		
		if(actionParamsList != null && actionParamsList.size()> 0) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParamsList) {
				assembleActionRule(actionParameters, actionList);
			}
			drr.setActionList(actionList);
		}

		if(templateParams != null) {
			String file = templateParams.getString(TemplateParameters.file);
			String url = templateParams.getString(TemplateParameters.url);
			String content = templateParams.getText(TemplateParameters.content);
			String encoding = templateParams.getString(TemplateParameters.encoding);
			boolean noCache = templateParams.getBoolean(TemplateParameters.noCache);
			DispatchResponseRule.updateTemplate(drr, file, content, encoding, noCache);
		}
		
		return drr;
	}
	
	public void assembleActionRule(Parameters actionParameters, ActionRuleApplicable actionRuleApplicable) {
		String id = actionParameters.getString(ActionParameters.id);
		String beanId = actionParameters.getString(ActionParameters.beanId);
		String methodName = actionParameters.getString(ActionParameters.methodName);
		List<Parameters> argumentParamsList = actionParameters.getParametersList(ActionParameters.arguments);
		List<Parameters> propertyParamsList = actionParameters.getParametersList(ActionParameters.properties);
		String include = actionParameters.getString(ActionParameters.include);
		List<Parameters> echoParamsList = actionParameters.getParametersList(ActionParameters.echo);
		boolean hidden = actionParameters.getBoolean(ActionParameters.include);
		
		if(beanId != null && methodName != null) {
			BeanActionRule beanActionRule = BeanActionRule.newInstance(id, beanId, methodName, hidden);
			ItemRuleMap argumentItemRuleMap = assembleItemRuleMap(argumentParamsList);
			if(argumentItemRuleMap != null) {
				beanActionRule.setArgumentItemRuleMap(argumentItemRuleMap);
			}
			ItemRuleMap propertyItemRuleMap = assembleItemRuleMap(propertyParamsList);
			if(propertyItemRuleMap != null) {
				beanActionRule.setPropertyItemRuleMap(propertyItemRuleMap);
			}
			actionRuleApplicable.applyBeanActionRule(beanActionRule);
			assistant.putBeanReference(beanId, beanActionRule);
		} else if(echoParamsList != null) {
			EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
			ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(echoParamsList);
			echoActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
			actionRuleApplicable.applyEchoActionRule(echoActionRule);
		} else if(include != null) {
			IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, include, hidden);
			ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(echoParamsList);
			includeActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
			actionRuleApplicable.applyIncludeActionRule(includeActionRule);
		}
	}
	
	public ItemRuleMap assembleItemRuleMap(List<Parameters> itemParamsList) {
		ItemRuleMap itemRuleMap = ItemRule.toItemRuleMap(itemParamsList);
		
		if(itemRuleMap != null) {
			for(ItemRule itemRule : itemRuleMap) {
				Iterator<Token[]> iter = ItemRule.tokenIterator(itemRule);
				
				if(iter != null) {
					while(iter.hasNext()) {
						for(Token token : iter.next()) {
							if(token.getType() == TokenType.REFERENCE_BEAN) {
								assistant.putBeanReference(token.getName(), itemRule);
							}
						}
					}
				}
			}
		}
		
		return itemRuleMap;
	}
	
}
