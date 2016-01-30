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
package com.aspectran.core.context.builder.apon;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.ImportHandler;
import com.aspectran.core.context.builder.Importable;
import com.aspectran.core.context.builder.apon.params.ActionParameters;
import com.aspectran.core.context.builder.apon.params.AdviceActionParameters;
import com.aspectran.core.context.builder.apon.params.AdviceParameters;
import com.aspectran.core.context.builder.apon.params.AspectParameters;
import com.aspectran.core.context.builder.apon.params.AspectranParameters;
import com.aspectran.core.context.builder.apon.params.BeanParameters;
import com.aspectran.core.context.builder.apon.params.ConstructorParameters;
import com.aspectran.core.context.builder.apon.params.ContentParameters;
import com.aspectran.core.context.builder.apon.params.ContentsParameters;
import com.aspectran.core.context.builder.apon.params.DispatchParameters;
import com.aspectran.core.context.builder.apon.params.ExceptionParameters;
import com.aspectran.core.context.builder.apon.params.ExceptionRaisedParameters;
import com.aspectran.core.context.builder.apon.params.ForwardParameters;
import com.aspectran.core.context.builder.apon.params.ImportParameters;
import com.aspectran.core.context.builder.apon.params.ItemHolderParameters;
import com.aspectran.core.context.builder.apon.params.JobParameters;
import com.aspectran.core.context.builder.apon.params.JoinpointParameters;
import com.aspectran.core.context.builder.apon.params.RedirectParameters;
import com.aspectran.core.context.builder.apon.params.RequestParameters;
import com.aspectran.core.context.builder.apon.params.ResponseByContentTypeParameters;
import com.aspectran.core.context.builder.apon.params.ResponseParameters;
import com.aspectran.core.context.builder.apon.params.RootParameters;
import com.aspectran.core.context.builder.apon.params.TemplateParameters;
import com.aspectran.core.context.builder.apon.params.TransformParameters;
import com.aspectran.core.context.builder.apon.params.TransletParameters;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectJobAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.ExceptionHandlingRule;
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
import com.aspectran.core.util.apon.Parameters;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * The Class RootAponDisassembler.
 * 
 * <p>Created: 2015. 01. 27 PM 10:36:29</p>
 */
public class RootAponDisassembler {
	
	private final ContextBuilderAssistant assistant;
	
	public RootAponDisassembler(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}
	
	public void disassembleRoot(Parameters rootParameters) throws Exception {
		Parameters aspectranParameters = rootParameters.getParameters(RootParameters.aspectran);
		disassembleAspectran(aspectranParameters);
	}
	
	public void disassembleAspectran(Parameters aspectranParameters) throws Exception {
		String description = aspectranParameters.getString(AspectranParameters.description);
		if(description != null)
			assistant.getAssistantLocal().setDescription(description);
		
		Parameters defaultSettingsParameters = aspectranParameters.getParameters(AspectranParameters.settings);
		if(defaultSettingsParameters != null)
			disassembleDefaultSettings(defaultSettingsParameters);

		Parameters typeAliasParameters = aspectranParameters.getParameters(AspectranParameters.typeAlias);
		if(typeAliasParameters != null)
			disassembleTypeAlias(typeAliasParameters);
		
		List<Parameters> aspectParametersList = aspectranParameters.getParametersList(AspectranParameters.aspects);
		if(aspectParametersList != null) {
			for(Parameters aspectParameters : aspectParametersList) {
				disassembleAspectRule(aspectParameters);
			}
		}

		List<Parameters> beanParametersList = aspectranParameters.getParametersList(AspectranParameters.beans);
		if(beanParametersList != null) {
			for(Parameters beanParameters : beanParametersList) {
				disassembleBeanRule(beanParameters);
			}
		}

		List<Parameters> transletParametersList = aspectranParameters.getParametersList(AspectranParameters.translets);
		if(transletParametersList != null) {
			for(Parameters transletParameters : transletParametersList) {
				disassembleTransletRule(transletParameters);
			}
		}
		
		List<Parameters> importParametersList = aspectranParameters.getParametersList(AspectranParameters.imports);
		if(importParametersList != null) {
			for(Parameters importParameters : importParametersList) {
				disassembleImport(importParameters);
			}
		}
	}
	
	public void disassembleImport(Parameters importParameters) throws Exception {
		String resource = importParameters.getString(ImportParameters.resource);
		String file = importParameters.getString(ImportParameters.file);
		String url = importParameters.getString(ImportParameters.url);
		String fileType = importParameters.getString(ImportParameters.fileType);
		
		Importable importable = Importable.newInstance(assistant, resource, file, url, fileType);

		ImportHandler importHandler = assistant.getImportHandler();
		if(importHandler != null)
			importHandler.pending(importable);
	}
	
	public void disassembleDefaultSettings(Parameters defaultSettingsParameters) throws ClassNotFoundException {
		if(defaultSettingsParameters == null)
			return;
		
		Iterator<String> iter = defaultSettingsParameters.getParameterNameSet().iterator();
		
		while(iter.hasNext()) {
			String name = iter.next();
			
			DefaultSettingType settingType = null;
			
			if(name != null) {
				settingType = DefaultSettingType.valueOf(name);
				
				if(settingType == null)
					throw new IllegalArgumentException("Unknown default setting name '" + name + "'");
			}
			
			assistant.putSetting(settingType, defaultSettingsParameters.getString(name));
		}
		
		assistant.applySettings();
	}
	
	public void disassembleTypeAlias(Parameters parameters) {
		if(parameters == null)
			return;
		
		Iterator<String> iter = parameters.getParameterNameSet().iterator();
		
		while(iter.hasNext()) {
			String alias = iter.next();
			assistant.addTypeAlias(alias, parameters.getString(alias));
		}
	}

	public void disassembleAspectRule(Parameters aspectParameters) {
		String description = aspectParameters.getString(AspectParameters.description);
		String id = aspectParameters.getString(AspectParameters.id);
		String useFor = aspectParameters.getString(AspectParameters.useFor);
		AspectRule aspectRule = AspectRule.newInstance(id, useFor);

		if(description != null)
			aspectRule.setDescription(description);
	
		Parameters joinpointParameters = aspectParameters.getParameters(AspectParameters.jointpoint);
		String scope = joinpointParameters.getString(JoinpointParameters.scope);
		AspectRule.updateJoinpointScope(aspectRule, scope);
	
		Parameters pointcutParameters = joinpointParameters.getParameters(JoinpointParameters.pointcut);
		if(pointcutParameters != null) {
			PointcutRule pointcutRule = PointcutRule.newInstance(aspectRule, null, pointcutParameters);
			aspectRule.setPointcutRule(pointcutRule);
		}
	
		Parameters settingsParameters = aspectParameters.getParameters(AspectParameters.settings);
		if(settingsParameters != null) {
			SettingsAdviceRule settingsAdviceRule = SettingsAdviceRule.newInstance(aspectRule, settingsParameters);
			aspectRule.setSettingsAdviceRule(settingsAdviceRule);
		}
		
		Parameters adviceParameters = aspectParameters.getParameters(AspectParameters.advice);
		if(adviceParameters != null) {
			String adviceBeanId = adviceParameters.getString(AdviceParameters.bean);
			if(!StringUtils.isEmpty(adviceBeanId)) {
				aspectRule.setAdviceBeanId(adviceBeanId);
				assistant.putBeanReference(adviceBeanId, aspectRule);
			}
			
			Parameters beforeAdviceParameters = adviceParameters.getParameters(AdviceParameters.beforeAdvice);
			if(beforeAdviceParameters != null) {
				Parameters actionParameters = beforeAdviceParameters.getParameters(AdviceActionParameters.action);
				AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.BEFORE);
				disassembleActionRule(actionParameters, aspectAdviceRule);
				aspectRule.addAspectAdviceRule(aspectAdviceRule);
			}
			
			Parameters afterAdviceParameters = adviceParameters.getParameters(AdviceParameters.afterAdvice);
			if(afterAdviceParameters != null) {
				Parameters actionParameters = afterAdviceParameters.getParameters(AdviceActionParameters.action);
				AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AFTER);
				disassembleActionRule(actionParameters, aspectAdviceRule);
				aspectRule.addAspectAdviceRule(aspectAdviceRule);
			}
		
			Parameters aroundAdviceParameters = adviceParameters.getParameters(AdviceParameters.aroundAdvice);
			if(aroundAdviceParameters != null) {
				Parameters actionParameters = aroundAdviceParameters.getParameters(AdviceActionParameters.action);
				AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AROUND);
				disassembleActionRule(actionParameters, aspectAdviceRule);
				aspectRule.addAspectAdviceRule(aspectAdviceRule);
			}
		
			Parameters finallyAdviceParameters = adviceParameters.getParameters(AdviceParameters.finallyAdvice);
			if(finallyAdviceParameters != null) {
				Parameters actionParameters = finallyAdviceParameters.getParameters(AdviceActionParameters.action);
				AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AROUND);
				disassembleActionRule(actionParameters, aspectAdviceRule);
				aspectRule.addAspectAdviceRule(aspectAdviceRule);
			}
		
			List<Parameters> jobParametersList = adviceParameters.getParametersList(AdviceParameters.jobs);
			if(jobParametersList != null && !jobParametersList.isEmpty()) {
				for(Parameters jobParameters : jobParametersList) {
					String translet = jobParameters.getString(JobParameters.translet);
					Boolean disabled = jobParameters.getBoolean(JobParameters.disabled);
					
					translet = assistant.applyTransletNamePattern(translet);
					
					AspectJobAdviceRule ajar = AspectJobAdviceRule.newInstance(aspectRule, translet, disabled);
					aspectRule.addAspectJobAdviceRule(ajar);
				}
			}
		}
		
		Parameters exceptionRaisedParameters = aspectParameters.getParameters(AspectParameters.exceptionRaised);
		if(exceptionRaisedParameters != null) {
			ExceptionHandlingRule exceptionHandlingRule = new ExceptionHandlingRule();
	
			exceptionHandlingRule.setDescription(exceptionRaisedParameters.getString(ExceptionRaisedParameters.description));

			Parameters actionParameters = exceptionRaisedParameters.getParameters(ExceptionRaisedParameters.action);
			if(actionParameters != null) {
				disassembleActionRule(actionParameters, exceptionHandlingRule);
			}
	
			List<Parameters> rrtrParametersList = exceptionRaisedParameters.getParametersList(ExceptionRaisedParameters.responseByContentTypes);
			if(rrtrParametersList != null && !rrtrParametersList.isEmpty()) {
				for(Parameters rrtrParameters : rrtrParametersList) {
					ResponseByContentTypeRule rrtr = disassembleResponseByContentTypeRule(rrtrParameters);
					exceptionHandlingRule.putResponseByContentTypeRule(rrtr);
				}
			}
			
			aspectRule.setExceptionHandlingRule(exceptionHandlingRule);
		}

		
		assistant.addAspectRule(aspectRule);
	}

	public void disassembleBeanRule(Parameters beanParameters) throws ClassNotFoundException, IOException, CloneNotSupportedException {
		String description = beanParameters.getString(BeanParameters.description);
		String id = beanParameters.getString(BeanParameters.id);
		String className = assistant.resolveAliasType(beanParameters.getString(BeanParameters.className));
		String scan = beanParameters.getString(BeanParameters.scan);
		String mask = beanParameters.getString(BeanParameters.mask);
		String scope = beanParameters.getString(BeanParameters.scope);
		Boolean singleton = beanParameters.getBoolean(BeanParameters.singleton);
		String factoryBean = beanParameters.getString(BeanParameters.factoryBean);
		String factoryMethod = beanParameters.getString(BeanParameters.factoryMethod);
		String initMethod = beanParameters.getString(BeanParameters.initMethod);
		String destroyMethod = beanParameters.getString(BeanParameters.destroyMethod);
		Boolean lazyInit = beanParameters.getBoolean(BeanParameters.lazyInit);
		Boolean important = beanParameters.getBoolean(BeanParameters.important);
		ConstructorParameters constructorParameters = beanParameters.getParameters(BeanParameters.constructor);
		ItemHolderParameters propertyItemHolderParameters = beanParameters.getParameters(BeanParameters.properties);
		Parameters filterParameters = beanParameters.getParameters(BeanParameters.filter);
		
		BeanRule beanRule = BeanRule.newInstance(id, className, scan, mask, scope, singleton, factoryBean, factoryMethod, initMethod, destroyMethod, lazyInit, important);

		if(description != null)
			beanRule.setDescription(description);
		
		if(filterParameters != null)
			beanRule.setFilterParameters(filterParameters);
		
		if(constructorParameters != null) {
			Parameters constructorArgumentItemHolderParameters = constructorParameters.getParameters(ConstructorParameters.arguments);
			if(constructorArgumentItemHolderParameters != null) {
				ItemRuleMap constructorArgumentItemRuleMap = disassembleItemRuleMap(constructorArgumentItemHolderParameters);
				beanRule.setConstructorArgumentItemRuleMap(constructorArgumentItemRuleMap);
			}
		}
		
		if(propertyItemHolderParameters != null) {
			ItemRuleMap propertyItemRuleMap = disassembleItemRuleMap(propertyItemHolderParameters);
			beanRule.setPropertyItemRuleMap(propertyItemRuleMap);
		}
		
		assistant.addBeanRule(beanRule);
	}

	public void disassembleTransletRule(Parameters transletParameters) throws CloneNotSupportedException {
		String description = transletParameters.getString(TransletParameters.description);
		String name = transletParameters.getString(TransletParameters.name);
		String mask = transletParameters.getString(TransletParameters.mask);
		String path = transletParameters.getString(TransletParameters.path);
		String restVerb = transletParameters.getString(TransletParameters.restVerb);
		TransletRule transletRule = TransletRule.newInstance(name, mask, path, restVerb);
		
		if(description != null)
			transletRule.setDescription(description);
		
		Parameters requestParamters = transletParameters.getParameters(TransletParameters.request);
		if(requestParamters != null) {
			RequestRule requestRule = disassembleRequestRule(requestParamters);
			transletRule.setRequestRule(requestRule);
		}
		
		Parameters contentsParameters = transletParameters.getParameters(TransletParameters.contents1);
		if(contentsParameters != null) {
			ContentList contentList = disassembleContentList(contentsParameters);
			transletRule.setContentList(contentList);
		}
		
		List<Parameters> contentParametersList = transletParameters.getParametersList(TransletParameters.contents2);
		if(contentParametersList != null && !contentParametersList.isEmpty()) {
			ContentList contentList = transletRule.touchContentList();
			for(Parameters contentParamters : contentParametersList) {
				ActionList actionList = disassembleActionList(contentParamters, contentList);
				contentList.addActionList(actionList);
			}
		}
		
		List<Parameters> responseParametersList = transletParameters.getParametersList(TransletParameters.responses);
		if(responseParametersList != null) {
			for(Parameters responseParamters : responseParametersList) {
				ResponseRule responseRule = disassembleResponseRule(responseParamters);
				transletRule.addResponseRule(responseRule);
			}
		}
		
		Parameters exceptionParameters = transletParameters.getParameters(TransletParameters.exception);
		if(exceptionParameters != null) {
			List<Parameters> rbctParametersList = exceptionParameters.getParametersList(ExceptionParameters.responseByContentTypes);
			if(rbctParametersList != null && !rbctParametersList.isEmpty()) {
				for(Parameters rbctParameters : rbctParametersList) {
					ResponseByContentTypeRule rbctr = disassembleResponseByContentTypeRule(rbctParameters);
					ExceptionHandlingRule exceptionHandlingRule = transletRule.touchExceptionHandlingRule();
					exceptionHandlingRule.putResponseByContentTypeRule(rbctr);
				}
			}
		}
		
		List<Parameters> actionParametersList = transletParameters.getParametersList(TransletParameters.actions);
		if(actionParametersList != null) {
			for(Parameters actionParameters : actionParametersList) {
				disassembleActionRule(actionParameters, transletRule);
			}
		}
		
		Parameters transformParameters = transletParameters.getParameters(TransletParameters.transform);
		if(transformParameters != null) {
			TransformRule tr = disassembleTransformRule(transformParameters);
			transletRule.applyResponseRule(tr);
		}
		
		Parameters dispatchParameters = transletParameters.getParameters(TransletParameters.dispatch);
		if(dispatchParameters != null) {
			DispatchResponseRule drr = disassembleDispatchResponseRule(dispatchParameters);
			transletRule.applyResponseRule(drr);
		}

		Parameters redirectParameters = transletParameters.getParameters(TransletParameters.redirect);
		if(redirectParameters != null) {
			RedirectResponseRule rrr = disassembleRedirectResponseRule(redirectParameters);
			transletRule.applyResponseRule(rrr);
		}
		
		Parameters forwardParameters = transletParameters.getParameters(TransletParameters.forward);
		if(forwardParameters != null) {
			ForwardResponseRule frr = disassembleForwardResponseRule(forwardParameters);
			transletRule.applyResponseRule(frr);
		}

		assistant.addTransletRule(transletRule);
	}
	
	public RequestRule disassembleRequestRule(Parameters requestParameters) {
		String method = requestParameters.getString(RequestParameters.requestMethod);
		String characterEncoding = requestParameters.getString(RequestParameters.characterEncoding);
		ItemHolderParameters attributeItemHolderParameters = requestParameters.getParameters(RequestParameters.attributes);
		
		RequestRule requestRule = RequestRule.newInstance(method, characterEncoding);
	
		if(attributeItemHolderParameters != null) {
			ItemRuleMap attributeItemRuleMap = disassembleItemRuleMap(attributeItemHolderParameters);
			requestRule.setAttributeItemRuleMap(attributeItemRuleMap);
		}
	
		return requestRule;
	}

	public ResponseRule disassembleResponseRule(Parameters responseParameters) {
		String name = responseParameters.getString(ResponseParameters.name);
		String characterEncoding = responseParameters.getString(ResponseParameters.characterEncoding);

		ResponseRule responseRule = ResponseRule.newInstance(name, characterEncoding);
		
		Parameters transformParameters = responseParameters.getParameters(ResponseParameters.transform);
		if(transformParameters != null) {
			responseRule.applyResponseRule(disassembleTransformRule(transformParameters));
		}
		
		Parameters dispatchParameters = responseParameters.getParameters(ResponseParameters.dispatch);
		if(dispatchParameters != null) {
			responseRule.applyResponseRule(disassembleDispatchResponseRule(dispatchParameters));
		}

		Parameters redirectParameters = responseParameters.getParameters(ResponseParameters.redirect);
		if(redirectParameters != null) {
			responseRule.applyResponseRule(disassembleRedirectResponseRule(redirectParameters));
		}
		
		Parameters forwardParameters = responseParameters.getParameters(ResponseParameters.forward);
		if(forwardParameters != null) {
			responseRule.applyResponseRule(disassembleForwardResponseRule(forwardParameters));
		}
		
		return responseRule;
	}
	
	public ContentList disassembleContentList(Parameters contentsParameters) {
		String name = contentsParameters.getString(ContentsParameters.name);
		Boolean omittable = contentsParameters.getBoolean(ContentsParameters.omittable);
		List<Parameters> contentParametersList = contentsParameters.getParametersList(ContentsParameters.contents);
		
		ContentList contentList = ContentList.newInstance(name, omittable);
		
		if(contentParametersList != null) {
			for(Parameters contentParamters : contentParametersList) {
				ActionList actionList = disassembleActionList(contentParamters, contentList);
				contentList.addActionList(actionList);
			}
		}
		
		return contentList;
	}
	
	public ActionList disassembleActionList(Parameters contentParameters, ContentList contentList) {
		String name = contentParameters.getString(ContentParameters.name);
		Boolean omittable = contentParameters.getBoolean(ContentParameters.omittable);
		Boolean hidden = contentParameters.getBoolean(ContentParameters.hidden);
		List<Parameters> actionParametersList = contentParameters.getParametersList(ContentParameters.actions);
		
		ActionList actionList = ActionList.newInstance(name, omittable, hidden);

		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			for(Parameters actionParameters : actionParametersList) {
				disassembleActionRule(actionParameters, actionList);
			}
		}
		
		return actionList;
	}
	
	public void disassembleActionRule(Parameters actionParameters, ActionRuleApplicable actionRuleApplicable) {
		String id = actionParameters.getString(ActionParameters.id);
		String beanId = actionParameters.getString(ActionParameters.beanId);
		String methodName = actionParameters.getString(ActionParameters.methodName);
		ItemHolderParameters argumentItemHolderParameters = actionParameters.getParameters(ActionParameters.arguments);
		ItemHolderParameters propertyItemHolderParameters = actionParameters.getParameters(ActionParameters.properties);
		String include = actionParameters.getString(ActionParameters.include);
		ItemHolderParameters attributeItemHolderParameters = actionParameters.getParameters(ActionParameters.attributes);
		ItemHolderParameters echoItemHolderParameters = actionParameters.getParameters(ActionParameters.echo);
		Boolean hidden = actionParameters.getBoolean(ActionParameters.hidden);
		
		if(!assistant.isNullableActionId() && StringUtils.isEmpty(id))
			throw new IllegalArgumentException("The <echo>, <action>, <include> element requires a id attribute.");
		
		if(!StringUtils.isEmpty(methodName)) {
			BeanActionRule beanActionRule = BeanActionRule.newInstance(id, beanId, methodName, hidden);
			if(argumentItemHolderParameters != null) {
				ItemRuleMap argumentItemRuleMap = disassembleItemRuleMap(argumentItemHolderParameters);
				beanActionRule.setArgumentItemRuleMap(argumentItemRuleMap);
			}
			if(propertyItemHolderParameters != null) {
				ItemRuleMap propertyItemRuleMap = disassembleItemRuleMap(propertyItemHolderParameters);
				beanActionRule.setPropertyItemRuleMap(propertyItemRuleMap);
			}
			actionRuleApplicable.applyActionRule(beanActionRule);
			if(!StringUtils.isEmpty(beanId)) {
				assistant.putBeanReference(beanId, beanActionRule);
			}
		} else if(echoItemHolderParameters != null) {
			EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
			ItemRuleMap attributeItemRuleMap = disassembleItemRuleMap(echoItemHolderParameters);
			echoActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
			actionRuleApplicable.applyActionRule(echoActionRule);
		} else if(include != null) {
			IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, include, hidden);
			if(attributeItemHolderParameters != null) {
				ItemRuleMap attributeItemRuleMap = disassembleItemRuleMap(attributeItemHolderParameters);
				includeActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
			}
			actionRuleApplicable.applyActionRule(includeActionRule);
		}
	}

	public ResponseByContentTypeRule disassembleResponseByContentTypeRule(Parameters responseByContentTypeParameters) {
		ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
		
		String exceptionType = responseByContentTypeParameters.getString(ResponseByContentTypeParameters.exceptionType);
		rbctr.setExceptionType(exceptionType);
		
		List<Parameters> transformParametersList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.transforms);
		if(transformParametersList != null && !transformParametersList.isEmpty()) {
			disassembleTransformRule(transformParametersList, rbctr);
		}
		
		List<Parameters> dispatchParametersList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.dispatchs);
		if(dispatchParametersList != null && !dispatchParametersList.isEmpty()) {
			disassembleDispatchResponseRule(dispatchParametersList, rbctr);
		}

		List<Parameters> redirectParametersList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.redirects);
		if(redirectParametersList != null && !redirectParametersList.isEmpty()) {
			disassembleRedirectResponseRule(redirectParametersList, rbctr);
		}
		
		List<Parameters> forwardParametersList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.forwards);
		if(forwardParametersList != null && !forwardParametersList.isEmpty()) {
			disassembleForwardResponseRule(forwardParametersList, rbctr);
		}
		
		return rbctr;
	}
	
	public void disassembleTransformRule(List<Parameters> transformParametersList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters transformParameters : transformParametersList) {
			TransformRule tr = disassembleTransformRule(transformParameters);
			responseRuleApplicable.applyResponseRule(tr);
		}
	}
	
	public TransformRule disassembleTransformRule(Parameters transformParameters) {
		String transformType = transformParameters.getString(TransformParameters.type);
		String contentType = transformParameters.getString(TransformParameters.contentType);
		String templateId = transformParameters.getString(TransformParameters.template);
		String characterEncoding = transformParameters.getString(TransformParameters.characterEncoding);
		List<Parameters> actionParametersList = transformParameters.getParametersList(TransformParameters.actions);
		Boolean defaultResponse = transformParameters.getBoolean(TransformParameters.defaultResponse);
		Boolean pretty = transformParameters.getBoolean(TransformParameters.pretty);
		Parameters templateParameters = transformParameters.getParameters(TransformParameters.builtinTemplate);

		TransformRule tr = TransformRule.newInstance(transformType, contentType, templateId, characterEncoding, defaultResponse, pretty);
		
		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParametersList) {
				disassembleActionRule(actionParameters, actionList);
			}
			tr.setActionList(actionList);
		}
		
		if(templateParameters != null) {
			String engine = templateParameters.getString(TemplateParameters.engine);
			String name = templateParameters.getString(TemplateParameters.name);
			String file = templateParameters.getString(TemplateParameters.file);
			String resource = templateParameters.getString(TemplateParameters.resource);
			String url = templateParameters.getString(TemplateParameters.url);
			String content = templateParameters.getString(TemplateParameters.content);
			String encoding = templateParameters.getString(TemplateParameters.encoding);
			Boolean noCache = templateParameters.getBoolean(TemplateParameters.noCache);
			TemplateRule templateRule = TemplateRule.newInstanceForBuiltin(engine, name, file, resource, url, content, encoding, noCache);
			tr.setTemplateRule(templateRule);
		}
		
		return tr;
	}

	public void disassembleDispatchResponseRule(List<Parameters> dispatchParametersList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters dispatchParameters : dispatchParametersList) {
			DispatchResponseRule drr = disassembleDispatchResponseRule(dispatchParameters);
			responseRuleApplicable.applyResponseRule(drr);
		}
	}
	
	public DispatchResponseRule disassembleDispatchResponseRule(Parameters dispatchParameters) {
		String name = dispatchParameters.getString(DispatchParameters.name);
		String contentType = dispatchParameters.getString(DispatchParameters.contentType);
		String characterEncoding = dispatchParameters.getString(DispatchParameters.characterEncoding);
		List<Parameters> actionParametersList = dispatchParameters.getParametersList(DispatchParameters.actions);
		Boolean defaultResponse = dispatchParameters.getBoolean(DispatchParameters.defaultResponse);
		
		DispatchResponseRule drr = DispatchResponseRule.newInstance(name, contentType, characterEncoding, defaultResponse);
		
		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParametersList) {
				disassembleActionRule(actionParameters, actionList);
			}
			drr.setActionList(actionList);
		}
		
		return drr;
	}

	public void disassembleRedirectResponseRule(List<Parameters> redirectParametersList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters redirectParameters : redirectParametersList) {
			RedirectResponseRule rrr = disassembleRedirectResponseRule(redirectParameters);
			responseRuleApplicable.applyResponseRule(rrr);
		}
	}
	
	public RedirectResponseRule disassembleRedirectResponseRule(Parameters redirectParameters) {
		String contentType = redirectParameters.getString(RedirectParameters.contentType);
		String translet = redirectParameters.getString(RedirectParameters.translet);
		String url = redirectParameters.getString(RedirectParameters.url);
		ItemHolderParameters parameterItemHolderParametersList = redirectParameters.getParameters(RedirectParameters.parameters);
		Boolean excludeNullParameter = redirectParameters.getBoolean(RedirectParameters.excludeNullParameter);
		List<Parameters> actionParametersList = redirectParameters.getParametersList(RedirectParameters.actions);
		Boolean defaultResponse = redirectParameters.getBoolean(RedirectParameters.defaultResponse);
		
		RedirectResponseRule rrr = RedirectResponseRule.newInstance(contentType, translet, url, excludeNullParameter, defaultResponse);
		
		if(parameterItemHolderParametersList != null) {
			ItemRuleMap parameterItemRuleMap = disassembleItemRuleMap(parameterItemHolderParametersList);
			rrr.setParameterItemRuleMap(parameterItemRuleMap);
		}
		
		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParametersList) {
				disassembleActionRule(actionParameters, actionList);
			}
			rrr.setActionList(actionList);
		}
		
		return rrr;
	}

	public void disassembleForwardResponseRule(List<Parameters> forwardParametersList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters forwardParameters : forwardParametersList) {
			ForwardResponseRule frr = disassembleForwardResponseRule(forwardParameters);
			responseRuleApplicable.applyResponseRule(frr);
		}
	}

	public ForwardResponseRule disassembleForwardResponseRule(Parameters forwardParameters) {
		String contentType = forwardParameters.getString(ForwardParameters.contentType);
		String translet = forwardParameters.getString(ForwardParameters.translet);
		ItemHolderParameters attributeItemHolderParametersList = forwardParameters.getParameters(ForwardParameters.attributes);
		List<Parameters> actionParametersList = forwardParameters.getParametersList(ForwardParameters.actions);
		Boolean defaultResponse = forwardParameters.getBoolean(ForwardParameters.defaultResponse);
		
		translet = assistant.applyTransletNamePattern(translet);
		
		ForwardResponseRule rrr = ForwardResponseRule.newInstance(contentType, translet, defaultResponse);
		
		if(attributeItemHolderParametersList != null) {
			ItemRuleMap attributeItemRuleMap = disassembleItemRuleMap(attributeItemHolderParametersList);
			rrr.setAttributeItemRuleMap(attributeItemRuleMap);
		}
		
		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParametersList) {
				disassembleActionRule(actionParameters, actionList);
			}
			rrr.setActionList(actionList);
		}
		
		return rrr;
	}
	
	public ItemRuleMap disassembleItemRuleMap(Parameters itemHolderParameters) {
		List<Parameters> itemParametersList = itemHolderParameters.getParametersList(ItemHolderParameters.item);
		ItemRuleMap itemRuleMap = ItemRule.toItemRuleMap(itemParametersList);
			
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
