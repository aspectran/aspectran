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

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.DefaultSettings;
import com.aspectran.core.context.builder.Importable;
import com.aspectran.core.context.builder.apon.params.ActionParameters;
import com.aspectran.core.context.builder.apon.params.AdviceParameters;
import com.aspectran.core.context.builder.apon.params.AspectParameters;
import com.aspectran.core.context.builder.apon.params.AspectranParameters;
import com.aspectran.core.context.builder.apon.params.BeanParameters;
import com.aspectran.core.context.builder.apon.params.ContentParameters;
import com.aspectran.core.context.builder.apon.params.ContentsParameters;
import com.aspectran.core.context.builder.apon.params.DispatchParameters;
import com.aspectran.core.context.builder.apon.params.ExceptionParameters;
import com.aspectran.core.context.builder.apon.params.ExceptionRaizedParameters;
import com.aspectran.core.context.builder.apon.params.ForwardParameters;
import com.aspectran.core.context.builder.apon.params.ImportParameters;
import com.aspectran.core.context.builder.apon.params.JobParameters;
import com.aspectran.core.context.builder.apon.params.JoinpointParameters;
import com.aspectran.core.context.builder.apon.params.RedirectParameters;
import com.aspectran.core.context.builder.apon.params.RequestParameters;
import com.aspectran.core.context.builder.apon.params.ResponseByContentTypeParameters;
import com.aspectran.core.context.builder.apon.params.ResponseParameters;
import com.aspectran.core.context.builder.apon.params.TemplateParameters;
import com.aspectran.core.context.builder.apon.params.TransformParameters;
import com.aspectran.core.context.builder.apon.params.TransletParameters;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectJobAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseByContentTypeRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.Parameters;

/**
 * AponAspectranContextBuilder.
 * 
 * <p>Created: 2015. 01. 27 오후 10:36:29</p>
 */
public class AspectranAponAssembler {
	
	private final ContextBuilderAssistant assistant;
	
	private final String encoding;
	
	public AspectranAponAssembler(ContextBuilderAssistant assistant, String encoding) {
		this.assistant = assistant;
		this.encoding = encoding;
	}
	
	public void assembleAspectran(Parameters aspectranParameters) throws ClassNotFoundException, IOException, CloneNotSupportedException {
		Parameters settingParameters = aspectranParameters.getParameters(AspectranParameters.setting);
		if(settingParameters != null)
			assembleDefaultSettings(settingParameters);

		Parameters typeAliasParameters = aspectranParameters.getParameters(AspectranParameters.typeAlias);
		if(typeAliasParameters != null)
			assembleTypeAlias(typeAliasParameters);
		
		List<Parameters> aspectParametersList = aspectranParameters.getParametersList(AspectranParameters.aspects);
		if(aspectParametersList != null) {
			for(Parameters aspectParameters : aspectParametersList) {
				assembleAspectRule(aspectParameters);
			}
		}

		List<Parameters> beanParametersList = aspectranParameters.getParametersList(AspectranParameters.beans);
		if(beanParametersList != null) {
			for(Parameters beanParameters : beanParametersList) {
				assembleBeanRule(beanParameters);
			}
		}

		List<Parameters> transletParametersList = aspectranParameters.getParametersList(AspectranParameters.translets);
		if(transletParametersList != null) {
			for(Parameters transletParameters : transletParametersList) {
				assembleTransletRule(transletParameters);
			}
		}
		
		List<Parameters> importParametersList = aspectranParameters.getParametersList(AspectranParameters.imports);
		if(importParametersList != null) {
			for(Parameters importParameters : importParametersList) {
				assembleImport(importParameters);
			}
		}
	}
	
	public void assembleImport(Parameters importParameters) throws CloneNotSupportedException, IOException, ClassNotFoundException {
		String resource = importParameters.getString(ImportParameters.resource);
		String file = importParameters.getString(ImportParameters.file);
		String url = importParameters.getString(ImportParameters.url);
		
		Importable importable = Importable.newInstance(assistant, resource, file, url);
		
		DefaultSettings defaultSettings = (DefaultSettings)assistant.getDefaultSettings().clone();
		
		Reader reader = importable.getReader(encoding);
		AponReader aponReader = new AponReader();
		Parameters aspectranParameters = aponReader.read(reader, new AspectranParameters());
		reader.close();
		
		AspectranAponAssembler aponAssembler = new AspectranAponAssembler(assistant, encoding);
		aponAssembler.assembleAspectran(aspectranParameters);

		assistant.setDefaultSettings(defaultSettings);
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

	public void assembleTransletRule(Parameters transletParameters) throws CloneNotSupportedException {
		String name = transletParameters.getString(TransletParameters.name);
		TransletRule transletRule = TransletRule.newInstance(name);
		
		Parameters requestParamters = transletParameters.getParameters(TransletParameters.request);
		if(requestParamters != null) {
			RequestRule requestRule = assembleRequestRule(requestParamters);
			transletRule.setRequestRule(requestRule);
		}
		
		Parameters contentsParameters = transletParameters.getParameters(TransletParameters.contents1);
		if(contentsParameters != null) {
			ContentList contentList = assembleContentList(contentsParameters);
			transletRule.setContentList(contentList);
		}
		
		List<Parameters> contentParametersList = transletParameters.getParametersList(TransletParameters.contents2);
		if(contentParametersList != null && !contentParametersList.isEmpty()) {
			ContentList contentList = transletRule.touchContentList();
			for(Parameters contentParamters : contentParametersList) {
				ActionList actionList = assembleActionList(contentParamters, contentList);
				contentList.addActionList(actionList);
			}
		}
		
		List<Parameters> responseParametersList = transletParameters.getParametersList(TransletParameters.responses);
		if(responseParametersList != null) {
			for(Parameters responseParamters : responseParametersList) {
				ResponseRule responseRule = assembleResponseRule(responseParamters);
				transletRule.addResponseRule(responseRule);
			}
		}
		
		Parameters exceptionParameters = transletParameters.getParameters(TransletParameters.exception);
		if(exceptionParameters != null) {
			List<Parameters> rrtrParametersList = exceptionParameters.getParametersList(ExceptionParameters.responseByContentTypes);
			if(rrtrParametersList != null && !rrtrParametersList.isEmpty()) {
				for(Parameters rrtrParameters : rrtrParametersList) {
					ResponseByContentTypeRule rrtr = assembleResponseByContentTypeRule(rrtrParameters);
					transletRule.addExceptionHandlingRule(rrtr);
				}
			}
		}
		
		List<Parameters> actionParametersList = transletParameters.getParametersList(TransletParameters.actions);
		if(actionParametersList != null) {
			for(Parameters actionParameters : actionParametersList) {
				assembleActionRule(actionParameters, transletRule);
			}
		}
		
		List<Parameters> transformParamsList = transletParameters.getParametersList(TransletParameters.transforms);
		if(transformParamsList != null && !transformParamsList.isEmpty()) {
			assembleTransformRule(transformParamsList, transletRule);
		}
		
		List<Parameters> dispatchParamsList = transletParameters.getParametersList(TransletParameters.dispatchs);
		if(dispatchParamsList != null && !dispatchParamsList.isEmpty()) {
			assembleDispatchResponseRule(dispatchParamsList, transletRule);
		}

		List<Parameters> redirectParamsList = transletParameters.getParametersList(TransletParameters.redirects);
		if(redirectParamsList != null && !redirectParamsList.isEmpty()) {
			assembleRedirectResponseRule(redirectParamsList, transletRule);
		}
		
		List<Parameters> forwardParamsList = transletParameters.getParametersList(TransletParameters.forwards);
		if(forwardParamsList != null && !forwardParamsList.isEmpty()) {
			assembleForwardResponseRule(forwardParamsList, transletRule);
		}

		assistant.addTransletRule(transletRule);
	}
	
	public ResponseRule assembleResponseRule(Parameters responseParameters) {
		String name = responseParameters.getString(ResponseParameters.name);
		String characterEncoding = responseParameters.getString(ResponseParameters.characterEncoding);

		ResponseRule responseRule = ResponseRule.newInstance(name, characterEncoding);
		
		List<Parameters> transformParamsList = responseParameters.getParametersList(ResponseParameters.transforms);
		if(transformParamsList != null && !transformParamsList.isEmpty()) {
			assembleTransformRule(transformParamsList, responseRule);
		}
		
		List<Parameters> dispatchParamsList = responseParameters.getParametersList(ResponseParameters.dispatchs);
		if(dispatchParamsList != null && !dispatchParamsList.isEmpty()) {
			assembleDispatchResponseRule(dispatchParamsList, responseRule);
		}

		List<Parameters> redirectParamsList = responseParameters.getParametersList(ResponseParameters.redirects);
		if(redirectParamsList != null && !redirectParamsList.isEmpty()) {
			assembleRedirectResponseRule(redirectParamsList, responseRule);
		}
		
		List<Parameters> forwardParamsList = responseParameters.getParametersList(ResponseParameters.forwards);
		if(forwardParamsList != null && !forwardParamsList.isEmpty()) {
			assembleForwardResponseRule(forwardParamsList, responseRule);
		}
		
		return responseRule;
	}
	
	public ContentList assembleContentList(Parameters contentsParameters) {
		String name = contentsParameters.getString(ContentsParameters.name);
		Boolean omittable = contentsParameters.getBoolean(ContentsParameters.omittable);
		List<Parameters> contentParametersList = contentsParameters.getParametersList(ContentsParameters.contents);
		
		ContentList contentList = ContentList.newInstance(name, omittable);
		
		if(contentParametersList != null) {
			for(Parameters contentParamters : contentParametersList) {
				ActionList actionList = assembleActionList(contentParamters, contentList);
				contentList.addActionList(actionList);
			}
		}
		
		return contentList;
	}
	
	public ActionList assembleActionList(Parameters contentParameters, ContentList contentList) {
		String id = contentParameters.getString(ContentParameters.id);
		String name = contentParameters.getString(ContentParameters.name);
		Boolean omittable = contentParameters.getBoolean(ContentParameters.omittable);
		Boolean hidden = contentParameters.getBoolean(ContentParameters.hidden);
		List<Parameters> actionParamsList = contentParameters.getParametersList(ContentParameters.actions);
		
		ActionList actionList = ActionList.newInstance(id, name, omittable, hidden, contentList);

		if(actionParamsList != null && !actionParamsList.isEmpty()) {
			for(Parameters actionParameters : actionParamsList) {
				assembleActionRule(actionParameters, actionList);
			}
		}
		
		return actionList;
	}
	
	public RequestRule assembleRequestRule(Parameters requestParameters) {
		String method = requestParameters.getString(RequestParameters.method);
		String characterEncoding = requestParameters.getString(RequestParameters.characterEncoding);
		List<Parameters> attributeParamsList = requestParameters.getParametersList(RequestParameters.attributes);
		
		RequestRule requestRule = RequestRule.newInstance(method, characterEncoding);

		ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(attributeParamsList);
		if(attributeItemRuleMap != null) {
			requestRule.setAttributeItemRuleMap(attributeItemRuleMap);
		}

		return requestRule;
	}
	
	public void assembleBeanRule(Parameters beanParameters) throws ClassNotFoundException, IOException {
		String id = beanParameters.getString(BeanParameters.id);
		String className = beanParameters.getString(BeanParameters.className);
		String scope = beanParameters.getString(BeanParameters.scope);
		Boolean singleton = beanParameters.getBoolean(BeanParameters.singleton);
		String factoryMethod = beanParameters.getString(BeanParameters.factoryMethod);
		String initMethod = beanParameters.getString(BeanParameters.initMethod);
		String destroyMethod = beanParameters.getString(BeanParameters.destroyMethod);
		Boolean lazyInit = beanParameters.getBoolean(BeanParameters.lazyInit);
		Boolean important = beanParameters.getBoolean(BeanParameters.important);
		List<Parameters> constructorArgumentParamsList = beanParameters.getParametersList(BeanParameters.constructor);
		List<Parameters> propertyParamsList = beanParameters.getParametersList(BeanParameters.properties);
		
		if(id != null) {
			id = assistant.applyNamespaceForBean(id);
		}
		
		ItemRuleMap constructorArgumentItemRuleMap = assembleItemRuleMap(constructorArgumentParamsList);
		ItemRuleMap propertyItemRuleMap = assembleItemRuleMap(propertyParamsList);

		BeanRule[] beanRules = BeanRule.newInstance(assistant.getClassLoader(), id, className, scope, singleton, factoryMethod, initMethod, destroyMethod, lazyInit, important);
		
		if(beanRules.length == 1) {
			if(constructorArgumentItemRuleMap != null)
				beanRules[0].setConstructorArgumentItemRuleMap(constructorArgumentItemRuleMap);
			if(propertyItemRuleMap != null)
				beanRules[0].setPropertyItemRuleMap(propertyItemRuleMap);
			assistant.addBeanRule(beanRules[0]);
		} else if(beanRules.length > 1) {
			for(BeanRule beanRule : beanRules) {
				if(constructorArgumentItemRuleMap != null)
					beanRule.setConstructorArgumentItemRuleMap(constructorArgumentItemRuleMap);
				if(propertyItemRuleMap != null)
					beanRule.setPropertyItemRuleMap(propertyItemRuleMap);
				assistant.addBeanRule(beanRule);
			}
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
		
		Parameters beforeActionParams = adviceParams.getParameters(AdviceParameters.beforeAction);
		if(beforeActionParams != null) {
			AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.BEFORE);
			assembleActionRule(beforeActionParams, aspectAdviceRule);
			aspectRule.addAspectAdviceRule(aspectAdviceRule);
		}
		
		Parameters afterActionParams = adviceParams.getParameters(AdviceParameters.afterAction);
		if(afterActionParams != null) {
			AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AFTER);
			assembleActionRule(afterActionParams, aspectAdviceRule);
			aspectRule.addAspectAdviceRule(aspectAdviceRule);
		}

		Parameters aroundActionParams = adviceParams.getParameters(AdviceParameters.aroundAction);
		if(aroundActionParams != null) {
			AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AROUND);
			assembleActionRule(aroundActionParams, aspectAdviceRule);
			aspectRule.addAspectAdviceRule(aspectAdviceRule);
		}

		Parameters finallyActionParams = adviceParams.getParameters(AdviceParameters.finallyAction);
		if(finallyActionParams != null) {
			AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AROUND);
			assembleActionRule(finallyActionParams, aspectAdviceRule);
			aspectRule.addAspectAdviceRule(aspectAdviceRule);
		}

		Parameters exceptionRaizedParams = adviceParams.getParameters(AdviceParameters.exceptionRaized);
		if(exceptionRaizedParams != null) {
			AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.EXCPETION_RAIZED);

			Parameters actionParams = exceptionRaizedParams.getParameters(ExceptionRaizedParameters.action);
			if(actionParams != null) {
				assembleActionRule(actionParams, aspectAdviceRule);
			}

			List<Parameters> rrtrParametersList = exceptionRaizedParams.getParametersList(ExceptionRaizedParameters.responseByContentTypes);
			if(rrtrParametersList != null && rrtrParametersList.size() > 0) {
				for(Parameters rrtrParameters : rrtrParametersList) {
					ResponseByContentTypeRule rrtr = assembleResponseByContentTypeRule(rrtrParameters);
					aspectAdviceRule.addResponseByContentTypeRule(rrtr);
				}
			}
			
			aspectRule.addAspectAdviceRule(aspectAdviceRule);
		}
		
		List<Parameters> jobParamsList = adviceParams.getParametersList(AdviceParameters.jobs);
		if(jobParamsList != null && jobParamsList.size() > 0) {
			for(Parameters jobParameters : jobParamsList) {
				String translet = jobParameters.getString(JobParameters.translet);
				Boolean disabled = jobParameters.getBoolean(JobParameters.disabled);
				
				translet = assistant.getFullTransletName(translet);
				
				AspectJobAdviceRule ajar = AspectJobAdviceRule.newInstance(aspectRule, translet, disabled);
				aspectRule.addAspectJobAdviceRule(ajar);
			}
		}
		
		assistant.addAspectRule(aspectRule);
	}
	
	public ResponseByContentTypeRule assembleResponseByContentTypeRule(Parameters responseByContentTypeParameters) {
		ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
		
		String exceptionType = responseByContentTypeParameters.getString(ResponseByContentTypeParameters.exceptionType);
		rbctr.setExceptionType(exceptionType);
		
		List<Parameters> transformParamsList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.transforms);
		if(transformParamsList != null && !transformParamsList.isEmpty()) {
			assembleTransformRule(transformParamsList, rbctr);
		}
		
		List<Parameters> dispatchParamsList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.dispatchs);
		if(dispatchParamsList != null && !dispatchParamsList.isEmpty()) {
			assembleDispatchResponseRule(dispatchParamsList, rbctr);
		}

		List<Parameters> redirectParamsList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.redirects);
		if(redirectParamsList != null && !redirectParamsList.isEmpty()) {
			assembleRedirectResponseRule(redirectParamsList, rbctr);
		}
		
		List<Parameters> forwardParamsList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.forwards);
		if(forwardParamsList != null && !forwardParamsList.isEmpty()) {
			assembleForwardResponseRule(forwardParamsList, rbctr);
		}
		
		return rbctr;
	}
	
	public void assembleTransformRule(List<Parameters> transformParamsList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters transformParameters : transformParamsList) {
			TransformRule tr = assembleTransformRule(transformParameters);
			responseRuleApplicable.applyResponseRule(tr);
		}
	}
	
	public void assembleDispatchResponseRule(List<Parameters> dispatchParamsList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters dispatchParameters : dispatchParamsList) {
			DispatchResponseRule drr = assembleDispatchResponseRule(dispatchParameters);
			responseRuleApplicable.applyResponseRule(drr);
		}
	}
	
	public void assembleRedirectResponseRule(List<Parameters> redirectParamsList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters redirectParameters : redirectParamsList) {
			RedirectResponseRule rrr = assembleRedirectResponseRule(redirectParameters);
			responseRuleApplicable.applyResponseRule(rrr);
		}
	}
	
	public void assembleForwardResponseRule(List<Parameters> forwardParamsList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters forwardParameters : forwardParamsList) {
			ForwardResponseRule frr = assembleForwardResponseRule(forwardParameters);
			responseRuleApplicable.applyResponseRule(frr);
		}
	}

	public TransformRule assembleTransformRule(Parameters transformParameters) {
		String transformType = transformParameters.getString(TransformParameters.transformType);
		String contentType = transformParameters.getString(TransformParameters.contentType);
		String characterEncoding = transformParameters.getString(TransformParameters.characterEncoding);
		Parameters templateParams = transformParameters.getParameters(TransformParameters.template);
		List<Parameters> actionParamsList = transformParameters.getParametersList(TransformParameters.actions);
		Boolean defaultResponse = transformParameters.getBoolean(TransformParameters.defaultResponse);
		Boolean pretty = transformParameters.getBoolean(TransformParameters.pretty);
		
		TransformRule tr = TransformRule.newInstance(transformType, contentType, characterEncoding, defaultResponse, pretty);
		
		if(actionParamsList != null && actionParamsList.size()> 0) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParamsList) {
				assembleActionRule(actionParameters, actionList);
			}
			tr.setActionList(actionList);
		}
		
		if(templateParams != null) {
			String file = templateParams.getString(TemplateParameters.file);
			String resource = templateParams.getString(TemplateParameters.resource);
			String url = templateParams.getString(TemplateParameters.url);
			String content = templateParams.getText(TemplateParameters.content);
			String encoding = templateParams.getString(TemplateParameters.encoding);
			Boolean noCache = templateParams.getBoolean(TemplateParameters.noCache);
			TemplateRule templateRule = TemplateRule.newInstance(file, resource, url, content, encoding, noCache);
			tr.setTemplateRule(templateRule);
		}
		
		return tr;
	}
	
	public DispatchResponseRule assembleDispatchResponseRule(Parameters dispatchParameters) {
		String contentType = dispatchParameters.getString(DispatchParameters.contentType);
		String characterEncoding = dispatchParameters.getString(DispatchParameters.characterEncoding);
		Parameters templateParams = dispatchParameters.getParameters(DispatchParameters.template);
		List<Parameters> actionParamsList = dispatchParameters.getParametersList(DispatchParameters.actions);
		Boolean defaultResponse = dispatchParameters.getBoolean(DispatchParameters.defaultResponse);
		
		DispatchResponseRule drr = DispatchResponseRule.newInstance(contentType, characterEncoding, defaultResponse);
		
		if(actionParamsList != null && actionParamsList.size()> 0) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParamsList) {
				assembleActionRule(actionParameters, actionList);
			}
			drr.setActionList(actionList);
		}

		if(templateParams != null) {
			String file = templateParams.getString(TemplateParameters.file);
			String encoding = templateParams.getString(TemplateParameters.encoding);
			Boolean noCache = templateParams.getBoolean(TemplateParameters.noCache);
			TemplateRule templateRule = TemplateRule.newInstance(file, null, null, null, encoding, noCache);
			drr.setTemplateRule(templateRule);
		}
		
		return drr;
	}
	
	public RedirectResponseRule assembleRedirectResponseRule(Parameters redirectParameters) {
		String contentType = redirectParameters.getString(RedirectParameters.contentType);
		String translet = redirectParameters.getString(RedirectParameters.translet);
		String url = redirectParameters.getString(RedirectParameters.url);
		List<Parameters> parameterParamsList = redirectParameters.getParametersList(RedirectParameters.parameters);
		Boolean excludeNullParameter = redirectParameters.getBoolean(RedirectParameters.excludeNullParameter);
		List<Parameters> actionParamsList = redirectParameters.getParametersList(RedirectParameters.actions);
		Boolean defaultResponse = redirectParameters.getBoolean(RedirectParameters.defaultResponse);
		
		RedirectResponseRule rrr = RedirectResponseRule.newInstance(contentType, translet, url, excludeNullParameter, defaultResponse);
		
		ItemRuleMap parameterItemRuleMap = assembleItemRuleMap(parameterParamsList);
		if(parameterItemRuleMap != null) {
			rrr.setParameterItemRuleMap(parameterItemRuleMap);
		}
		
		if(actionParamsList != null && actionParamsList.size()> 0) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParamsList) {
				assembleActionRule(actionParameters, actionList);
			}
			rrr.setActionList(actionList);
		}
		
		return rrr;
	}
	
	public ForwardResponseRule assembleForwardResponseRule(Parameters forwardParameters) {
		String contentType = forwardParameters.getString(ForwardParameters.contentType);
		String translet = forwardParameters.getString(ForwardParameters.translet);
		List<Parameters> attributeParamsList = forwardParameters.getParametersList(ForwardParameters.attributes);
		List<Parameters> actionParamsList = forwardParameters.getParametersList(ForwardParameters.actions);
		Boolean defaultResponse = forwardParameters.getBoolean(ForwardParameters.defaultResponse);
		
		translet = assistant.getFullTransletName(translet);
		
		ForwardResponseRule rrr = ForwardResponseRule.newInstance(contentType, translet, defaultResponse);
		
		ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(attributeParamsList);
		if(attributeItemRuleMap != null) {
			rrr.setAttributeItemRuleMap(attributeItemRuleMap);
		}
		
		if(actionParamsList != null && actionParamsList.size()> 0) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParamsList) {
				assembleActionRule(actionParameters, actionList);
			}
			rrr.setActionList(actionList);
		}
		
		return rrr;
	}
	
	public void assembleActionRule(Parameters actionParameters, ActionRuleApplicable actionRuleApplicable) {
		String id = actionParameters.getString(ActionParameters.id);
		String beanId = actionParameters.getString(ActionParameters.beanId);
		String methodName = actionParameters.getString(ActionParameters.methodName);
		List<Parameters> argumentParamsList = actionParameters.getParametersList(ActionParameters.arguments);
		List<Parameters> propertyParamsList = actionParameters.getParametersList(ActionParameters.properties);
		String include = actionParameters.getString(ActionParameters.include);
		List<Parameters> echoParamsList = actionParameters.getParametersList(ActionParameters.echo);
		Boolean hidden = actionParameters.getBoolean(ActionParameters.include);
		
		if(!assistant.isNullableActionId() && StringUtils.isEmpty(id))
			throw new IllegalArgumentException("The <echo>, <action>, <include> element requires a id attribute.");
		
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
			actionRuleApplicable.applyActionRule(beanActionRule);
			assistant.putBeanReference(beanId, beanActionRule);
		} else if(echoParamsList != null) {
			EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
			ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(echoParamsList);
			echoActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
			actionRuleApplicable.applyActionRule(echoActionRule);
		} else if(include != null) {
			IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, include, hidden);
			ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(echoParamsList);
			includeActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
			actionRuleApplicable.applyActionRule(includeActionRule);
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
