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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponse;
import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.DefaultSettings;
import com.aspectran.core.context.builder.apon.params.ActionParameters;
import com.aspectran.core.context.builder.apon.params.AdviceParameters;
import com.aspectran.core.context.builder.apon.params.AspectParameters;
import com.aspectran.core.context.builder.apon.params.AspectranParameters;
import com.aspectran.core.context.builder.apon.params.BeanParameters;
import com.aspectran.core.context.builder.apon.params.ContentParameters;
import com.aspectran.core.context.builder.apon.params.ContentsParameters;
import com.aspectran.core.context.builder.apon.params.DefaultSettingsParameters;
import com.aspectran.core.context.builder.apon.params.DispatchParameters;
import com.aspectran.core.context.builder.apon.params.ExceptionParameters;
import com.aspectran.core.context.builder.apon.params.ForwardParameters;
import com.aspectran.core.context.builder.apon.params.ItemParameters;
import com.aspectran.core.context.builder.apon.params.JoinpointParameters;
import com.aspectran.core.context.builder.apon.params.PointcutParameters;
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
import com.aspectran.core.context.rule.AspectRuleMap;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
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
import com.aspectran.core.context.rule.TransletRuleMap;
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.Parameters;

/**
 * AspectranAponAssembler.
 * 
 * <p>Created: 2015. 01. 27 오후 10:36:29</p>
 */
public class AspectranAponAssembler {
	
	private final ContextBuilderAssistant assistant;
	
	public AspectranAponAssembler(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}
	
	public Parameters assembleAspectran() throws Exception {
		Parameters aspectranParameters = new AspectranParameters();
		
		DefaultSettings defaultSettings = assistant.getDefaultSettings();
		if(defaultSettings != null) {
			DefaultSettingsParameters settingParameters = aspectranParameters.getParameters(AspectranParameters.setting);
			settingParameters.setValue(DefaultSettingsParameters.transletNamePattern, defaultSettings.getTransletNamePattern());
			settingParameters.setValue(DefaultSettingsParameters.transletNamePatternPrefix, defaultSettings.getTransletNamePatternPrefix());
			settingParameters.setValue(DefaultSettingsParameters.transletNamePatternSuffix, defaultSettings.getTransletNamePatternSuffix());
			settingParameters.setValue(DefaultSettingsParameters.transletInterfaceClass, defaultSettings.getTransletInterfaceClassName());
			settingParameters.setValue(DefaultSettingsParameters.transletImplementClass, defaultSettings.getTransletImplementClassName());
			settingParameters.setValue(DefaultSettingsParameters.nullableContentId, defaultSettings.getNullableContentId());
			settingParameters.setValue(DefaultSettingsParameters.nullableActionId, defaultSettings.getNullableActionId());
			settingParameters.setValue(DefaultSettingsParameters.activityDefaultHandler, defaultSettings.getActivityDefaultHandler());
			settingParameters.setValue(DefaultSettingsParameters.beanProxyMode, defaultSettings);
		}
		
		Map<String, String> typeAliases = assistant.getTypeAliases();
		if(!typeAliases.isEmpty()) {
			GenericParameters typeAliasParameters = aspectranParameters.getParameters(AspectranParameters.typeAlias);
			for(Map.Entry<String, String> entry : typeAliases.entrySet()) {
				typeAliasParameters.putValue(entry.getKey(), entry.getValue());
			}
		}

		AspectRuleMap aspectRuleMap = assistant.getAspectRuleMap();
		for(AspectRule aspectRule : aspectRuleMap) {
			Parameters p = new AspectParameters();
			aspectranParameters.putValue(AspectranParameters.aspects, p);
		}
		
		BeanRuleMap beanRuleMap = assistant.getBeanRuleMap();
		for(BeanRule beanRule : beanRuleMap) {
			Parameters p = new AspectParameters();
			aspectranParameters.putValue(AspectranParameters.beans, p);
		}
		
		TransletRuleMap transletRuleMap = assistant.getTransletRuleMap();
		for(TransletRule transletRule : transletRuleMap) {
			Parameters p = new AspectParameters();
			aspectranParameters.putValue(AspectranParameters.translets, p);
		}
		
		if(!aspectRuleMap.isEmpty()) {
			List<Parameters> aspectParametersList = aspectranParameters.getParametersList(AspectranParameters.aspects);
			for(Parameters aspectParameters : aspectParametersList) {
				//assembleAspectRule(aspectParameters);
			}
			
		}

		return aspectranParameters;
	}
	
	public Parameters assembleAspectRule(AspectRule aspectRule) {
//		id = new ParameterDefine("id", ParameterValueType.STRING);
//		useFor = new ParameterDefine("useFor", ParameterValueType.STRING);
//		jointpoint = new ParameterDefine("joinpoint", JoinpointParameters.class);
//		setting = new ParameterDefine("setting", GenericParameters.class);
//		advice = new ParameterDefine("advice", AdviceParameters.class);
		
		Parameters aspectParameters = new AspectParameters();
		aspectParameters.setValue(AspectParameters.id, aspectRule.getId());
		aspectParameters.setValue(AspectParameters.useFor, aspectRule.getAspectTargetType());
		
//		scope = new ParameterDefine("scope", ParameterValueType.STRING);
//		pointcut = new ParameterDefine("pointcut", PointcutParameters.class);
		
		Parameters joinpointParameters = aspectParameters.getParameters(AspectParameters.jointpoint);
		joinpointParameters.setValue(JoinpointParameters.scope, aspectRule.getJoinpointScope());
		
//		targets = new ParameterDefine("target", TargetParameters.class, true);
//		simpleTrigger = new ParameterDefine("simpleTrigger", SimpleTriggerParameters.class);
//		cronTrigger = new ParameterDefine("cronTrigger", CronTriggerParameters.class);
		
		Parameters pointcutParameters = joinpointParameters.getParameters(JoinpointParameters.pointcut);

		PointcutRule pointcutRule = aspectRule.getPointcutRule();
		if(pointcutRule != null) {
			List<Parameters> targetParametersList = pointcutRule.getTargetParametersList();
			if(targetParametersList != null) {
				for(Parameters targetParameters : targetParametersList) {
					pointcutParameters.putValue(PointcutParameters.targets, targetParameters);
				}
			}
			pointcutParameters.setValue(PointcutParameters.simpleTrigger, pointcutRule.getSimpleTriggerParameters());
			pointcutParameters.setValue(PointcutParameters.cronTrigger, pointcutRule.getCronTriggerParameters());
		}
		
		SettingsAdviceRule settingsAdviceRule = aspectRule.getSettingsAdviceRule();
		if(settingsAdviceRule != null) {
			Map<String, String> settings = settingsAdviceRule.getSettings();
			if(settings != null) {
				GenericParameters settingParameters = aspectParameters.getParameters(AspectParameters.setting);
				for(Map.Entry<String, String> entry : settings.entrySet()) {
					settingParameters.putValue(entry.getKey(), entry.getValue());
				}
			}
		}

		Parameters adviceParameters = aspectParameters.getParameters(AspectParameters.advice);
		adviceParameters.setValue(AdviceParameters.bean, aspectRule.getAdviceBeanId());
		
		List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
		if(aspectAdviceRuleList != null) {
			for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
				if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceParameters.setValue(AdviceParameters.beforeAction, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceParameters.setValue(AdviceParameters.beforeAction, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceParameters.setValue(AdviceParameters.afterAction, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceParameters.setValue(AdviceParameters.afterAction, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceParameters.setValue(AdviceParameters.aroundAction, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceParameters.setValue(AdviceParameters.aroundAction, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceParameters.setValue(AdviceParameters.finallyAction, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceParameters.setValue(AdviceParameters.finallyAction, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.EXCPETION_RAIZED) {
					//adviceParameters.setValue(AdviceParameters.exceptionRaized, assembleActionParameters(echoActionRule));
				}
			}
		}
		
		List<AspectJobAdviceRule> aspectJobAdviceRuleList = aspectRule.getAspectJobAdviceRuleList();
		


	
		
		assistant.addAspectRule(aspectRule);
		
		return aspectParameters;
	}

	
	public Parameters assembleResponseParameters(ResponseRule responseRule) {
		ResponseParameters responseParameters = new ResponseParameters();
		
//		private String name;
//		
//		private String characterEncoding;
//		
//		private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
//		
//		private Responsible response;
		
//		name = new ParameterDefine("name", ParameterValueType.STRING);
//		characterEncoding = new ParameterDefine("characterEncoding", ParameterValueType.STRING);
//		dispatchs = new ParameterDefine("dispatch", DispatchParameters.class);
//		transforms = new ParameterDefine("transform", TransformParameters.class);
//		redirects = new ParameterDefine("redirect", RedirectParameters.class);
//		forwards = new ParameterDefine("forward", ForwardParameters.class);

		responseParameters.setValue(ResponseParameters.name, responseRule.getName());
		responseParameters.setValue(ResponseParameters.characterEncoding, responseRule.getCharacterEncoding());
		
		if(responseRule.getResponseType() == ResponseType.TRANSFORM) {
			TransformResponse transformResponse = responseRule.getRespondent();
			responseParameters.setValue(ResponseParameters.transform, assembleTransformParameters(transformResponse.getTransformRule()));
		} else if(responseRule.getResponseType() == ResponseType.DISPATCH) {
			DispatchResponse dispatchResponse = responseRule.getRespondent();
			responseParameters.setValue(ResponseParameters.dispatch, assembleDispatchParameters(dispatchResponse.getDispatchResponseRule()));
		} else if(responseRule.getResponseType() == ResponseType.FORWARD) {
			ForwardResponse forwardResponse = responseRule.getRespondent();
			forwardResponse.getForwardResponseRule();
		} else if(responseRule.getResponseType() == ResponseType.REDIRECT) {
			RedirectResponse redirectResponse = responseRule.getRespondent();
			redirectResponse.getRedirectResponseRule();
		}
		
		
		return responseParameters;
	}
	
	public Parameters assembleTransformParameters(TransformRule transformRule) {
		TransformParameters transformParameters = new TransformParameters();

		if(transformRule.getTransformType() != null)
			transformParameters.setValue(TransformParameters.transformType, transformRule.getTransformType().toString());

		if(transformRule.getContentType() != null)
			transformParameters.setValue(TransformParameters.contentType, transformRule.getContentType().toString());
		
		transformParameters.setValue(TransformParameters.characterEncoding, transformRule.getCharacterEncoding());

		if(transformRule.getTemplateRule() != null)
			transformParameters.setValue(TransformParameters.template, assembleTemplateParameters(transformRule.getTemplateRule()));
		
		ActionList actionList = transformRule.getActionList();
		if(actionList != null) {
			for(Executable action : actionList) {
				if(action.getActionType() == ActionType.ECHO) {
					EchoActionRule echoActionRule = action.getActionRule();
					transformParameters.putValue(TransformParameters.actions, assembleActionParameters(echoActionRule));
				} else if(action.getActionType() == ActionType.BEAN) {
					BeanActionRule beanActionRule = action.getActionRule();
					transformParameters.setValue(TransformParameters.actions, assembleActionParameters(beanActionRule));
				} else if(action.getActionType() == ActionType.INCLUDE) {
					IncludeActionRule includeActionRule = action.getActionRule();
					transformParameters.setValue(TransformParameters.actions, assembleActionParameters(includeActionRule));
				}
			}
		}

		transformParameters.setValue(TransformParameters.defaultResponse, transformRule.getDefaultResponse());
		transformParameters.setValue(TransformParameters.pretty, transformRule.getPretty());
		
		return transformParameters;
	}
	
	public Parameters assembleDispatchParameters(DispatchResponseRule dispatchResponseRule) {
		DispatchParameters dispatchParameters = new DispatchParameters();

		if(dispatchResponseRule.getContentType() != null)
			dispatchParameters.setValue(DispatchParameters.contentType, dispatchResponseRule.getContentType().toString());
		
		dispatchParameters.setValue(DispatchParameters.characterEncoding, dispatchResponseRule.getCharacterEncoding());
		
		if(dispatchResponseRule.getTemplateRule() != null)
			dispatchParameters.setValue(DispatchParameters.template, assembleTemplateParameters(dispatchResponseRule.getTemplateRule()));
		
		ActionList actionList = dispatchResponseRule.getActionList();
		if(actionList != null) {
			for(Executable action : actionList) {
				if(action.getActionType() == ActionType.ECHO) {
					EchoActionRule echoActionRule = action.getActionRule();
					dispatchParameters.putValue(DispatchParameters.actions, assembleActionParameters(echoActionRule));
				} else if(action.getActionType() == ActionType.BEAN) {
					BeanActionRule beanActionRule = action.getActionRule();
					dispatchParameters.setValue(DispatchParameters.actions, assembleActionParameters(beanActionRule));
				} else if(action.getActionType() == ActionType.INCLUDE) {
					IncludeActionRule includeActionRule = action.getActionRule();
					dispatchParameters.setValue(DispatchParameters.actions, assembleActionParameters(includeActionRule));
				}
			}
		}
		
		dispatchParameters.setValue(DispatchParameters.defaultResponse, dispatchResponseRule.getDefaultResponse());
		
		return dispatchParameters;
	}
	
	public Parameters assembleTemplateParameters(TemplateRule templateRule) {
		TemplateParameters templateParameters = new TemplateParameters();
		templateParameters.setValue(TemplateParameters.file, templateRule.getFile());
		templateParameters.setValue(TemplateParameters.resource, templateRule.getResource());
		templateParameters.setValue(TemplateParameters.url, templateRule.getUrl());
		templateParameters.setValue(TemplateParameters.content, templateRule.getContent());
		templateParameters.setValue(TemplateParameters.encoding, templateRule.getEncoding());
		templateParameters.setValue(TemplateParameters.noCache, templateRule.getNoCache());
		
		return templateParameters;
	}
	
	public Parameters assembleActionParameters(BeanActionRule beanActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.setValue(ActionParameters.id, beanActionRule.getActionId());
		actionParameters.setValue(ActionParameters.beanId, beanActionRule.getBeanId());
		actionParameters.setValue(ActionParameters.methodName, beanActionRule.getMethodName());
		
		ItemRuleMap propertyItemRuleMap = beanActionRule.getPropertyItemRuleMap();
		if(propertyItemRuleMap != null) {
			for(ItemRule itemRule : propertyItemRuleMap) {
				actionParameters.putValue(ActionParameters.properties, assembleItemParameters(itemRule));
			}
		}
		
		ItemRuleMap argumentItemRuleMap = beanActionRule.getArgumentItemRuleMap();
		if(argumentItemRuleMap != null) {
			for(ItemRule itemRule : argumentItemRuleMap) {
				actionParameters.putValue(ActionParameters.arguments, assembleItemParameters(itemRule));
			}
		}
		
		actionParameters.setValue(ActionParameters.hidden, beanActionRule.getHidden());
		
		return actionParameters;
	}
	
	public Parameters assembleActionParameters(EchoActionRule echoActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.setValue(ActionParameters.id, echoActionRule.getActionId());
		
		ItemRuleMap attributeItemRuleMap = echoActionRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			for(ItemRule itemRule : attributeItemRuleMap) {
				actionParameters.putValue(ActionParameters.echo, assembleItemParameters(itemRule));
			}
		}
		
		actionParameters.setValue(ActionParameters.hidden, echoActionRule.getHidden());
		
		return actionParameters;
	}
	
	public Parameters assembleActionParameters(IncludeActionRule includeActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.setValue(ActionParameters.id, includeActionRule.getActionId());
		actionParameters.setValue(ActionParameters.include, includeActionRule.getTransletName());
		
		ItemRuleMap attributeItemRuleMap = includeActionRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			for(ItemRule itemRule : attributeItemRuleMap) {
				actionParameters.putValue(ActionParameters.attributes, assembleItemParameters(itemRule));
			}
		}
		
		actionParameters.setValue(ActionParameters.hidden, includeActionRule.getHidden());
		
		return actionParameters;
	}

	public Parameters assembleItemParameters(ItemRule itemRule) {
		ItemParameters itemParameters = new ItemParameters();
		if(itemRule.getType() != null)
			itemParameters.setValue(ItemParameters.type, itemRule.getType().toString());
		itemParameters.setValue(ItemParameters.name, itemRule.getName());
		if(itemRule.getTokens() != null)
			itemParameters.setValue(ItemParameters.value, itemRule.getValue());
		if(itemRule.getValueType() != null)
			itemParameters.setValue(ItemParameters.valueType, itemRule.getValueType());
		if(itemRule.getDefaultValue() != null)
			itemParameters.setValue(ItemParameters.defaultValue, itemRule.getDefaultValue());
		if(itemRule.getTokenize() != null)
			itemParameters.setValue(ItemParameters.tokenize, itemRule.getTokenize());
		
		return itemParameters;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void assembleBeanRule(Parameters beanParameters) throws ClassNotFoundException, IOException, CloneNotSupportedException {
		String id = beanParameters.getString(BeanParameters.id);
		String className = assistant.resolveAliasType(beanParameters.getString(BeanParameters.className));
		String scope = beanParameters.getString(BeanParameters.scope);
		Boolean singleton = beanParameters.getBoolean(BeanParameters.singleton);
		String factoryMethod = beanParameters.getString(BeanParameters.factoryMethod);
		String initMethod = beanParameters.getString(BeanParameters.initMethod);
		String destroyMethod = beanParameters.getString(BeanParameters.destroyMethod);
		Boolean lazyInit = beanParameters.getBoolean(BeanParameters.lazyInit);
		Boolean important = beanParameters.getBoolean(BeanParameters.important);
		List<Parameters> constructorArgumentParametersList = beanParameters.getParametersList(BeanParameters.constructor);
		List<Parameters> propertyParametersList = beanParameters.getParametersList(BeanParameters.properties);
		
		BeanRule beanRule = BeanRule.newInstance(id, className, scope, singleton, factoryMethod, initMethod, destroyMethod, lazyInit, important);

		ItemRuleMap constructorArgumentItemRuleMap = assembleItemRuleMap(constructorArgumentParametersList);
		ItemRuleMap propertyItemRuleMap = assembleItemRuleMap(propertyParametersList);

		if(constructorArgumentItemRuleMap != null)
			beanRule.setConstructorArgumentItemRuleMap(constructorArgumentItemRuleMap);
		
		if(propertyItemRuleMap != null)
			beanRule.setPropertyItemRuleMap(propertyItemRuleMap);
		
		assistant.addBeanRule(beanRule);
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
		
		List<Parameters> transformParametersList = transletParameters.getParametersList(TransletParameters.transforms);
		if(transformParametersList != null && !transformParametersList.isEmpty()) {
			assembleTransformRule(transformParametersList, transletRule);
		}
		
		List<Parameters> dispatchParametersList = transletParameters.getParametersList(TransletParameters.dispatchs);
		if(dispatchParametersList != null && !dispatchParametersList.isEmpty()) {
			assembleDispatchResponseRule(dispatchParametersList, transletRule);
		}

		List<Parameters> redirectParametersList = transletParameters.getParametersList(TransletParameters.redirects);
		if(redirectParametersList != null && !redirectParametersList.isEmpty()) {
			assembleRedirectResponseRule(redirectParametersList, transletRule);
		}
		
		List<Parameters> forwardParametersList = transletParameters.getParametersList(TransletParameters.forwards);
		if(forwardParametersList != null && !forwardParametersList.isEmpty()) {
			assembleForwardResponseRule(forwardParametersList, transletRule);
		}

		assistant.addTransletRule(transletRule);
	}
	
	public RequestRule assembleRequestRule(Parameters requestParameters) {
		String method = requestParameters.getString(RequestParameters.method);
		String characterEncoding = requestParameters.getString(RequestParameters.characterEncoding);
		List<Parameters> attributeParametersList = requestParameters.getParametersList(RequestParameters.attributes);
		
		RequestRule requestRule = RequestRule.newInstance(method, characterEncoding);
	
		ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(attributeParametersList);
		if(attributeItemRuleMap != null) {
			requestRule.setAttributeItemRuleMap(attributeItemRuleMap);
		}
	
		return requestRule;
	}

	public ResponseRule assembleResponseRule(Parameters responseParameters) {
		String name = responseParameters.getString(ResponseParameters.name);
		String characterEncoding = responseParameters.getString(ResponseParameters.characterEncoding);

		ResponseRule responseRule = ResponseRule.newInstance(name, characterEncoding);
		
		List<Parameters> transformParametersList = responseParameters.getParametersList(ResponseParameters.transform);
		if(transformParametersList != null && !transformParametersList.isEmpty()) {
			assembleTransformRule(transformParametersList, responseRule);
		}
		
		List<Parameters> dispatchParametersList = responseParameters.getParametersList(ResponseParameters.dispatch);
		if(dispatchParametersList != null && !dispatchParametersList.isEmpty()) {
			assembleDispatchResponseRule(dispatchParametersList, responseRule);
		}

		List<Parameters> redirectParametersList = responseParameters.getParametersList(ResponseParameters.redirect);
		if(redirectParametersList != null && !redirectParametersList.isEmpty()) {
			assembleRedirectResponseRule(redirectParametersList, responseRule);
		}
		
		List<Parameters> forwardParametersList = responseParameters.getParametersList(ResponseParameters.forward);
		if(forwardParametersList != null && !forwardParametersList.isEmpty()) {
			assembleForwardResponseRule(forwardParametersList, responseRule);
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
		List<Parameters> actionParametersList = contentParameters.getParametersList(ContentParameters.actions);
		
		if(!assistant.isNullableContentId() && StringUtils.isEmpty(id))
			throw new IllegalArgumentException("The <content> element requires a id attribute.");
		
		ActionList actionList = ActionList.newInstance(id, name, omittable, hidden, contentList);

		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			for(Parameters actionParameters : actionParametersList) {
				assembleActionRule(actionParameters, actionList);
			}
		}
		
		return actionList;
	}
	
	public void assembleActionRule(Parameters actionParameters, ActionRuleApplicable actionRuleApplicable) {
		String id = actionParameters.getString(ActionParameters.id);
		String beanId = actionParameters.getString(ActionParameters.beanId);
		String methodName = actionParameters.getString(ActionParameters.methodName);
		List<Parameters> argumentParametersList = actionParameters.getParametersList(ActionParameters.arguments);
		List<Parameters> propertyParametersList = actionParameters.getParametersList(ActionParameters.properties);
		String include = actionParameters.getString(ActionParameters.include);
		List<Parameters> echoParametersList = actionParameters.getParametersList(ActionParameters.echo);
		Boolean hidden = actionParameters.getBoolean(ActionParameters.include);
		
		if(!assistant.isNullableActionId() && StringUtils.isEmpty(id))
			throw new IllegalArgumentException("The <echo>, <action>, <include> element requires a id attribute.");
		
		if(beanId != null && methodName != null) {
			BeanActionRule beanActionRule = BeanActionRule.newInstance(id, beanId, methodName, hidden);
			ItemRuleMap argumentItemRuleMap = assembleItemRuleMap(argumentParametersList);
			if(argumentItemRuleMap != null) {
				beanActionRule.setArgumentItemRuleMap(argumentItemRuleMap);
			}
			ItemRuleMap propertyItemRuleMap = assembleItemRuleMap(propertyParametersList);
			if(propertyItemRuleMap != null) {
				beanActionRule.setPropertyItemRuleMap(propertyItemRuleMap);
			}
			actionRuleApplicable.applyActionRule(beanActionRule);
			assistant.putBeanReference(beanId, beanActionRule);
		} else if(echoParametersList != null) {
			EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
			ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(echoParametersList);
			echoActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
			actionRuleApplicable.applyActionRule(echoActionRule);
		} else if(include != null) {
			IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, include, hidden);
			ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(echoParametersList);
			includeActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
			actionRuleApplicable.applyActionRule(includeActionRule);
		}
	}

	public ResponseByContentTypeRule assembleResponseByContentTypeRule(Parameters responseByContentTypeParameters) {
		ResponseByContentTypeRule rbctr = new ResponseByContentTypeRule();
		
		String exceptionType = responseByContentTypeParameters.getString(ResponseByContentTypeParameters.exceptionType);
		rbctr.setExceptionType(exceptionType);
		
		List<Parameters> transformParametersList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.transforms);
		if(transformParametersList != null && !transformParametersList.isEmpty()) {
			assembleTransformRule(transformParametersList, rbctr);
		}
		
		List<Parameters> dispatchParametersList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.dispatchs);
		if(dispatchParametersList != null && !dispatchParametersList.isEmpty()) {
			assembleDispatchResponseRule(dispatchParametersList, rbctr);
		}

		List<Parameters> redirectParametersList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.redirects);
		if(redirectParametersList != null && !redirectParametersList.isEmpty()) {
			assembleRedirectResponseRule(redirectParametersList, rbctr);
		}
		
		List<Parameters> forwardParametersList = responseByContentTypeParameters.getParametersList(ResponseByContentTypeParameters.forwards);
		if(forwardParametersList != null && !forwardParametersList.isEmpty()) {
			assembleForwardResponseRule(forwardParametersList, rbctr);
		}
		
		return rbctr;
	}
	
	public void assembleTransformRule(List<Parameters> transformParametersList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters transformParameters : transformParametersList) {
			TransformRule tr = assembleTransformRule(transformParameters);
			responseRuleApplicable.applyResponseRule(tr);
		}
	}
	
	public TransformRule assembleTransformRule(Parameters transformParameters) {
		String transformType = transformParameters.getString(TransformParameters.transformType);
		String contentType = transformParameters.getString(TransformParameters.contentType);
		String characterEncoding = transformParameters.getString(TransformParameters.characterEncoding);
		Parameters templateParameters = transformParameters.getParameters(TransformParameters.template);
		List<Parameters> actionParametersList = transformParameters.getParametersList(TransformParameters.actions);
		Boolean defaultResponse = transformParameters.getBoolean(TransformParameters.defaultResponse);
		Boolean pretty = transformParameters.getBoolean(TransformParameters.pretty);
		
		TransformRule tr = TransformRule.newInstance(transformType, contentType, characterEncoding, defaultResponse, pretty);
		
		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParametersList) {
				assembleActionRule(actionParameters, actionList);
			}
			tr.setActionList(actionList);
		}
		
		if(templateParameters != null) {
			String file = templateParameters.getString(TemplateParameters.file);
			String resource = templateParameters.getString(TemplateParameters.resource);
			String url = templateParameters.getString(TemplateParameters.url);
			String content = templateParameters.getText(TemplateParameters.content);
			String encoding = templateParameters.getString(TemplateParameters.encoding);
			Boolean noCache = templateParameters.getBoolean(TemplateParameters.noCache);
			TemplateRule templateRule = TemplateRule.newInstance(file, resource, url, content, encoding, noCache);
			tr.setTemplateRule(templateRule);
		}
		
		return tr;
	}

	public void assembleDispatchResponseRule(List<Parameters> dispatchParametersList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters dispatchParameters : dispatchParametersList) {
			DispatchResponseRule drr = assembleDispatchResponseRule(dispatchParameters);
			responseRuleApplicable.applyResponseRule(drr);
		}
	}
	
	public DispatchResponseRule assembleDispatchResponseRule(Parameters dispatchParameters) {
		String contentType = dispatchParameters.getString(DispatchParameters.contentType);
		String characterEncoding = dispatchParameters.getString(DispatchParameters.characterEncoding);
		Parameters templateParameters = dispatchParameters.getParameters(DispatchParameters.template);
		List<Parameters> actionParametersList = dispatchParameters.getParametersList(DispatchParameters.actions);
		Boolean defaultResponse = dispatchParameters.getBoolean(DispatchParameters.defaultResponse);
		
		DispatchResponseRule drr = DispatchResponseRule.newInstance(contentType, characterEncoding, defaultResponse);
		
		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParametersList) {
				assembleActionRule(actionParameters, actionList);
			}
			drr.setActionList(actionList);
		}
	
		if(templateParameters != null) {
			String file = templateParameters.getString(TemplateParameters.file);
			String encoding = templateParameters.getString(TemplateParameters.encoding);
			Boolean noCache = templateParameters.getBoolean(TemplateParameters.noCache);
			TemplateRule templateRule = TemplateRule.newInstance(file, null, null, null, encoding, noCache);
			drr.setTemplateRule(templateRule);
		}
		
		return drr;
	}

	public void assembleRedirectResponseRule(List<Parameters> redirectParametersList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters redirectParameters : redirectParametersList) {
			RedirectResponseRule rrr = assembleRedirectResponseRule(redirectParameters);
			responseRuleApplicable.applyResponseRule(rrr);
		}
	}
	
	public RedirectResponseRule assembleRedirectResponseRule(Parameters redirectParameters) {
		String contentType = redirectParameters.getString(RedirectParameters.contentType);
		String translet = redirectParameters.getString(RedirectParameters.translet);
		String url = redirectParameters.getString(RedirectParameters.url);
		List<Parameters> parameterParametersList = redirectParameters.getParametersList(RedirectParameters.parameters);
		Boolean excludeNullParameter = redirectParameters.getBoolean(RedirectParameters.excludeNullParameter);
		List<Parameters> actionParametersList = redirectParameters.getParametersList(RedirectParameters.actions);
		Boolean defaultResponse = redirectParameters.getBoolean(RedirectParameters.defaultResponse);
		
		RedirectResponseRule rrr = RedirectResponseRule.newInstance(contentType, translet, url, excludeNullParameter, defaultResponse);
		
		ItemRuleMap parameterItemRuleMap = assembleItemRuleMap(parameterParametersList);
		if(parameterItemRuleMap != null) {
			rrr.setParameterItemRuleMap(parameterItemRuleMap);
		}
		
		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParametersList) {
				assembleActionRule(actionParameters, actionList);
			}
			rrr.setActionList(actionList);
		}
		
		return rrr;
	}

	public void assembleForwardResponseRule(List<Parameters> forwardParametersList, ResponseRuleApplicable responseRuleApplicable) {
		for(Parameters forwardParameters : forwardParametersList) {
			ForwardResponseRule frr = assembleForwardResponseRule(forwardParameters);
			responseRuleApplicable.applyResponseRule(frr);
		}
	}

	public ForwardResponseRule assembleForwardResponseRule(Parameters forwardParameters) {
		String contentType = forwardParameters.getString(ForwardParameters.contentType);
		String translet = forwardParameters.getString(ForwardParameters.translet);
		List<Parameters> attributeParametersList = forwardParameters.getParametersList(ForwardParameters.attributes);
		List<Parameters> actionParametersList = forwardParameters.getParametersList(ForwardParameters.actions);
		Boolean defaultResponse = forwardParameters.getBoolean(ForwardParameters.defaultResponse);
		
		translet = assistant.applyTransletNamePattern(translet);
		
		ForwardResponseRule rrr = ForwardResponseRule.newInstance(contentType, translet, defaultResponse);
		
		ItemRuleMap attributeItemRuleMap = assembleItemRuleMap(attributeParametersList);
		if(attributeItemRuleMap != null) {
			rrr.setAttributeItemRuleMap(attributeItemRuleMap);
		}
		
		if(actionParametersList != null && !actionParametersList.isEmpty()) {
			ActionList actionList = new ActionList();
			for(Parameters actionParameters : actionParametersList) {
				assembleActionRule(actionParameters, actionList);
			}
			rrr.setActionList(actionList);
		}
		
		return rrr;
	}
	
	public ItemRuleMap assembleItemRuleMap(List<Parameters> itemParametersList) {
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
