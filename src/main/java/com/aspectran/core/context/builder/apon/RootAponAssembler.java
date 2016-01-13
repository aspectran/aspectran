/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.builder.apon;

import java.util.List;
import java.util.Map;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponse;
import com.aspectran.core.context.builder.AssistantLocal;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.builder.DefaultSettings;
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
import com.aspectran.core.context.builder.apon.params.DefaultSettingsParameters;
import com.aspectran.core.context.builder.apon.params.DispatchParameters;
import com.aspectran.core.context.builder.apon.params.ExceptionRaisedParameters;
import com.aspectran.core.context.builder.apon.params.ForwardParameters;
import com.aspectran.core.context.builder.apon.params.ImportParameters;
import com.aspectran.core.context.builder.apon.params.ItemHolderParameters;
import com.aspectran.core.context.builder.apon.params.ItemParameters;
import com.aspectran.core.context.builder.apon.params.JobParameters;
import com.aspectran.core.context.builder.apon.params.JoinpointParameters;
import com.aspectran.core.context.builder.apon.params.PointcutParameters;
import com.aspectran.core.context.builder.apon.params.RedirectParameters;
import com.aspectran.core.context.builder.apon.params.RequestParameters;
import com.aspectran.core.context.builder.apon.params.ResponseByContentTypeParameters;
import com.aspectran.core.context.builder.apon.params.ResponseParameters;
import com.aspectran.core.context.builder.apon.params.RootParameters;
import com.aspectran.core.context.builder.apon.params.TemplateParameters;
import com.aspectran.core.context.builder.apon.params.TransformParameters;
import com.aspectran.core.context.builder.apon.params.TransletParameters;
import com.aspectran.core.context.rule.*;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.ImportType;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class RootAponAssembler.
 * 
 * <p>Created: 2015. 01. 27 오후 10:36:29</p>
 */
public class RootAponAssembler {
	
	private final ContextBuilderAssistant assistant;
	
	public RootAponAssembler(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}
	
	public Parameters assembleRoot() throws Exception {
		Parameters rootParameters = new RootParameters();
		rootParameters.putValue(RootParameters.aspectran, assembleAspectran());
		return rootParameters;
	}
	
	public Parameters assembleAspectran() throws Exception {
		Parameters aspectranParameters = new AspectranParameters();
		
		AssistantLocal assistantLocal = assistant.getAssistantLocal();
		if(assistantLocal.getDescription() != null)
			aspectranParameters.putValue(AspectranParameters.description, assistantLocal.getDescription());
		
		DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
		if(defaultSettings != null) {
			DefaultSettingsParameters settingParameters = aspectranParameters.newParameters(AspectranParameters.settings);
			if(defaultSettings.getTransletNamePattern() != null) {
				settingParameters.putValue(DefaultSettingsParameters.transletNamePattern, defaultSettings.getTransletNamePattern());
			} else {
				settingParameters.putValue(DefaultSettingsParameters.transletNamePrefix, defaultSettings.getTransletNamePrefix());
				settingParameters.putValue(DefaultSettingsParameters.transletNameSuffix, defaultSettings.getTransletNameSuffix());
			}
			settingParameters.putValue(DefaultSettingsParameters.transletInterfaceClass, defaultSettings.getTransletInterfaceClassName());
			settingParameters.putValue(DefaultSettingsParameters.transletImplementClass, defaultSettings.getTransletImplementClassName());
			settingParameters.putValue(DefaultSettingsParameters.nullableContentId, defaultSettings.getNullableContentId());
			settingParameters.putValue(DefaultSettingsParameters.nullableActionId, defaultSettings.getNullableActionId());
			settingParameters.putValue(DefaultSettingsParameters.beanProxifier, defaultSettings.getBeanProxifier());
			settingParameters.putValue(DefaultSettingsParameters.pointcutPatternVerifiable, defaultSettings.getPointcutPatternVerifiable());
		}
		
		Map<String, String> typeAliases = assistant.getTypeAliases();
		if(!typeAliases.isEmpty()) {
			GenericParameters typeAliasParameters = aspectranParameters.newParameters(AspectranParameters.typeAlias);
			for(Map.Entry<String, String> entry : typeAliases.entrySet()) {
				typeAliasParameters.putValue(entry.getKey(), entry.getValue());
			}
		}

		AspectRuleMap aspectRuleMap = assistant.getAspectRuleMap();
		for(AspectRule aspectRule : aspectRuleMap) {
			Parameters p = assembleAspectParameters(aspectRule);
			aspectranParameters.putValue(AspectranParameters.aspects, p);
		}

		BeanRuleMap beanRuleMap = assistant.getBeanRuleMap();
		for(BeanRule beanRule : beanRuleMap) {
			Parameters p = assembleBeanParameters(beanRule);
			aspectranParameters.putValue(AspectranParameters.beans, p);
		}
		
		TransletRuleMap transletRuleMap = assistant.getTransletRuleMap();
		for(TransletRule transletRule : transletRuleMap) {
			Parameters p = assembleTransletParameters(transletRule);
			aspectranParameters.putValue(AspectranParameters.translets, p);
		}
		
		TemplateRuleMap templateRuleMap = assistant.getTemplateRuleMap();
		for(TemplateRule templateRule : templateRuleMap) {
			Parameters p = assembleTemplateParameters(templateRule);
			aspectranParameters.putValue(AspectranParameters.templates, p);
		}

		List<Importable> pendingList = assistant.getImportHandler().getPendingList();
		if(pendingList != null) {
			for(Importable imp : pendingList) {
				aspectranParameters.putValue(AspectranParameters.imports, assembleImportParameters(imp));
			}
		}

		return aspectranParameters;
	}
	
	public Parameters assembleAspectParameters(AspectRule aspectRule) {
		Parameters aspectParameters = new AspectParameters();
		if(aspectRule.getDescription() != null) {
			aspectParameters.putValue(AspectParameters.description, aspectRule.getDescription());
		}
		aspectParameters.putValue(AspectParameters.id, aspectRule.getId());
		aspectParameters.putValue(AspectParameters.useFor, aspectRule.getAspectTargetType());
		
		Parameters joinpointParameters = aspectParameters.newParameters(AspectParameters.jointpoint);
		joinpointParameters.putValue(JoinpointParameters.scope, aspectRule.getJoinpointScope());
		
		PointcutRule pointcutRule = aspectRule.getPointcutRule();
		if(pointcutRule != null) {
			Parameters pointcutParameters = joinpointParameters.newParameters(JoinpointParameters.pointcut);
			List<Parameters> targetParametersList = pointcutRule.getTargetParametersList();
			if(targetParametersList != null) {
				for(Parameters targetParameters : targetParametersList) {
					pointcutParameters.putValue(PointcutParameters.targets, targetParameters);
				}
			}
			pointcutParameters.putValue(PointcutParameters.simpleTrigger, pointcutRule.getSimpleTriggerParameters());
			pointcutParameters.putValue(PointcutParameters.cronTrigger, pointcutRule.getCronTriggerParameters());
		}
		
		SettingsAdviceRule settingsAdviceRule = aspectRule.getSettingsAdviceRule();
		if(settingsAdviceRule != null) {
			Map<String, String> settings = settingsAdviceRule.getSettings();
			if(settings != null) {
				GenericParameters settingsParameters = aspectParameters.newParameters(AspectParameters.settings);
				for(Map.Entry<String, String> entry : settings.entrySet()) {
					settingsParameters.putValue(entry.getKey(), entry.getValue());
				}
			}
		}

		List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
		if(aspectAdviceRuleList != null) {
			Parameters adviceParameters = aspectParameters.newParameters(AspectParameters.advice);
			adviceParameters.putValue(AdviceParameters.bean, aspectRule.getAdviceBeanId());
			for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
				if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.beforeAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.afterAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.aroundAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.finallyAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					}
				}
			}
		}
		
		ExceptionHandlingRule exceptionHandlingRule = aspectRule.getExceptionHandlingRule();
		if(exceptionHandlingRule != null) {
			Parameters exceptionRaisedParameters = aspectParameters.touchParameters(AspectParameters.exceptionRaised);
			if(exceptionHandlingRule.getDescription() != null) {
				exceptionRaisedParameters.putValue(ExceptionRaisedParameters.description, exceptionHandlingRule.getDescription());
			}
			if(exceptionHandlingRule.getActionType() == ActionType.ECHO) {
				EchoActionRule echoActionRule = exceptionHandlingRule.getExecutableAction().getActionRule();
				exceptionRaisedParameters.putValue(ExceptionRaisedParameters.action, assembleActionParameters(echoActionRule));
			} else if(exceptionHandlingRule.getActionType() == ActionType.BEAN) {
				BeanActionRule beanActionRule = exceptionHandlingRule.getExecutableAction().getActionRule();
				exceptionRaisedParameters.putValue(ExceptionRaisedParameters.action, assembleActionParameters(beanActionRule));
			}
			for(ResponseByContentTypeRule rbctr : exceptionHandlingRule) {
				exceptionRaisedParameters.putValue(ExceptionRaisedParameters.responseByContentTypes, assembleResponseByContentTypeParameters(rbctr));
			}
		}
		
		List<AspectJobAdviceRule> aspectJobAdviceRuleList = aspectRule.getAspectJobAdviceRuleList();
		if(aspectJobAdviceRuleList != null) {
			Parameters adviceParameters = aspectParameters.touchParameters(AspectParameters.advice);
			for(AspectJobAdviceRule aspectJobAdviceRule : aspectJobAdviceRuleList) {
				Parameters jobParameters = new JobParameters();
				jobParameters.putValue(JobParameters.translet, aspectJobAdviceRule.getJobTransletName());
				jobParameters.putValue(JobParameters.disabled, aspectJobAdviceRule.getDisabled());
				adviceParameters.putValue(AdviceParameters.jobs, jobParameters);
			}
		}
		
		return aspectParameters;
	}

	public Parameters assembleBeanParameters(BeanRule beanRule) {
		Parameters beanParameters = new BeanParameters();
		if(beanRule.getDescription() != null) {
			beanParameters.putValue(BeanParameters.description, beanRule.getDescription());
		}
		beanParameters.putValue(BeanParameters.id, beanRule.getId());
		beanParameters.putValue(BeanParameters.mask, beanRule.getMaskPattern());
		beanParameters.putValue(BeanParameters.className, beanRule.getClassName());
		if(beanRule.getSingleton() == Boolean.TRUE && beanRule.getScopeType() == ScopeType.SINGLETON)
			beanParameters.putValue(BeanParameters.singleton, beanRule.getSingleton());
		else
			beanParameters.putValue(BeanParameters.scope, beanRule.getScopeType().toString());
		beanParameters.putValue(BeanParameters.factoryBean, beanRule.getFactoryBeanId());
		beanParameters.putValue(BeanParameters.factoryMethod, beanRule.getFactoryMethodName());
		beanParameters.putValue(BeanParameters.initMethod, beanRule.getInitMethodName());
		beanParameters.putValue(BeanParameters.destroyMethod, beanRule.getDestroyMethodName());
		beanParameters.putValue(BeanParameters.lazyInit, beanRule.getLazyInit());
		beanParameters.putValue(BeanParameters.important, beanRule.getImportant());
		beanParameters.putValue(BeanParameters.filter, beanRule.getFilterParameters());
		
		ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
		if(constructorArgumentItemRuleMap != null) {
			ConstructorParameters constructorParameters = beanParameters.newParameters(BeanParameters.constructor);
			constructorParameters.putValue(ConstructorParameters.arguments, assembleItemHolderParameters(constructorArgumentItemRuleMap));
		}
		
		ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
		if(propertyItemRuleMap != null) {
			beanParameters.putValue(BeanParameters.properties, assembleItemHolderParameters(propertyItemRuleMap));
		}
		
		return beanParameters;
	}
	
	public Parameters assembleTransletParameters(TransletRule transletRule) {
		Parameters transletParameters = new TransletParameters();
		if(transletRule.getDescription() != null) {
			transletParameters.putValue(TransletParameters.description, transletRule.getDescription());
		}
		transletParameters.putValue(TransletParameters.name, transletRule.getName());
		transletParameters.putValue(TransletParameters.mask, transletRule.getMaskPattern());
		transletParameters.putValue(TransletParameters.path, transletRule.getPath());
		
		if(transletRule.getRestVerb() != null)
			transletParameters.putValue(TransletParameters.restVerb, transletRule.getRestVerb().toString());

		RequestRule requestRule = transletRule.getRequestRule();
		if(requestRule != null) {
			RequestParameters requestParameters = transletParameters.newParameters(TransletParameters.request);
			requestParameters.putValue(RequestParameters.requestMethod, requestRule.getRequestMethod());
			requestParameters.putValue(RequestParameters.characterEncoding, requestRule.getCharacterEncoding());
			
			ItemRuleMap attributeItemRuleMap = requestRule.getAttributeItemRuleMap();
			if(attributeItemRuleMap != null) {
				requestParameters.putValue(RequestParameters.attributes, assembleItemHolderParameters(attributeItemRuleMap));
			}
		}
		
		if(transletRule.isExplicitContent()) {
			ContentList contentList = transletRule.getContentList();
			if(contentList != null) {
				if(!contentList.isOmittable()) {
					Parameters contentsParameters = transletParameters.newParameters(TransletParameters.contents1);
					for(ActionList actionList : contentList) {
						Parameters contentParameters = contentsParameters.newParameters(ContentsParameters.contents);
						assembleActionList(actionList, contentParameters, ContentParameters.actions);
					}
				} else {
					for(ActionList actionList : contentList) {
						Parameters contentParameters = transletParameters.newParameters(TransletParameters.contents2);
						assembleActionList(actionList, contentParameters, ContentParameters.actions);
					}
				}
			}
		} else {
			ContentList contentList = transletRule.getContentList();
			if(contentList != null) {
				for(ActionList actionList : contentList) {
					assembleActionList(actionList, transletParameters, TransletParameters.actions);
				}
			}
		}
		
		List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
		if(responseRuleList != null) {
			for(ResponseRule responseRule : responseRuleList) {
				transletParameters.putValue(TransletParameters.responses, assembleResponseParameters(responseRule));
			}
		} else {
			ResponseRule responseRule = transletRule.getResponseRule();
			if(responseRule != null && !transletRule.isImplicitResponse()) {
				transletParameters.putValue(TransletParameters.responses, assembleResponseParameters(responseRule));
			} else {
				Response response = responseRule.getResponse();
				if(response.getResponseType() == ResponseType.TRANSFORM) {
					TransformResponse transformResponse = (TransformResponse)response;
					transletParameters.putValue(TransletParameters.transform, assembleTransformParameters(transformResponse.getTransformRule()));
				} else if(response.getResponseType() == ResponseType.DISPATCH) {
					DispatchResponse dispatchResponse = (DispatchResponse)response;
					transletParameters.putValue(TransletParameters.dispatch, assembleDispatchParameters(dispatchResponse.getDispatchResponseRule()));
				} else if(response.getResponseType() == ResponseType.FORWARD) {
					ForwardResponse forwardResponse = (ForwardResponse)response;
					transletParameters.putValue(TransletParameters.forward, assembleForwardParameters(forwardResponse.getForwardResponseRule()));
				} else if(response.getResponseType() == ResponseType.REDIRECT) {
					RedirectResponse redirectResponse = (RedirectResponse)response;
					transletParameters.putValue(TransletParameters.redirect, assembleRedirectParameters(redirectResponse.getRedirectResponseRule()));
				}
			}
		}
		
		ExceptionHandlingRule exceptionHandlingRuleMap = transletRule.getExceptionHandlingRule();
		if(exceptionHandlingRuleMap != null) {
			for(ResponseByContentTypeRule rbctr : exceptionHandlingRuleMap) {
				transletParameters.putValue(TransletParameters.exception, assembleResponseByContentTypeParameters(rbctr));
			}

		}
		
		return transletParameters;
	}
	
	public Parameters assembleImportParameters(Importable imp) {
		Parameters importParameters = new ImportParameters();
		
		if(imp.getImportType() == ImportType.FILE) {
			importParameters.putValue(ImportParameters.file, imp.getDistinguishedName());
		} else if(imp.getImportType() == ImportType.RESOURCE) {
			importParameters.putValue(ImportParameters.resource, imp.getDistinguishedName());
		} else if(imp.getImportType() == ImportType.URL) {
			importParameters.putValue(ImportParameters.url, imp.getDistinguishedName());
			importParameters.putValue(ImportParameters.fileType, imp.getImportFileType());
		}
		
		return importParameters;
		
	}
	
	public Parameters assembleResponseByContentTypeParameters(ResponseByContentTypeRule responseByContentTypeRule) {
		ResponseByContentTypeParameters rbctp = new ResponseByContentTypeParameters();
		rbctp.putValue(ResponseByContentTypeParameters.exceptionType, responseByContentTypeRule.getExceptionType());
		
		ResponseMap responseMap = responseByContentTypeRule.getResponseMap();
		for(Response response : responseMap) {
			if(response.getResponseType() == ResponseType.TRANSFORM) {
				TransformResponse transformResponse = (TransformResponse)response;
				rbctp.putValue(ResponseByContentTypeParameters.transforms, assembleTransformParameters(transformResponse.getTransformRule()));
			} else if(response.getResponseType() == ResponseType.DISPATCH) {
				DispatchResponse dispatchResponse = (DispatchResponse)response;
				rbctp.putValue(ResponseByContentTypeParameters.dispatchs, assembleDispatchParameters(dispatchResponse.getDispatchResponseRule()));
			} else if(response.getResponseType() == ResponseType.FORWARD) {
				ForwardResponse forwardResponse = (ForwardResponse)response;
				rbctp.putValue(ResponseByContentTypeParameters.forwards, assembleForwardParameters(forwardResponse.getForwardResponseRule()));
			} else if(response.getResponseType() == ResponseType.REDIRECT) {
				RedirectResponse redirectResponse = (RedirectResponse)response;
				rbctp.putValue(ResponseByContentTypeParameters.redirects, assembleRedirectParameters(redirectResponse.getRedirectResponseRule()));
			}
		}
		
		return rbctp;
	}
	
	public Parameters assembleResponseParameters(ResponseRule responseRule) {
		ResponseParameters responseParameters = new ResponseParameters();
		
		responseParameters.putValue(ResponseParameters.name, responseRule.getName());
		responseParameters.putValue(ResponseParameters.characterEncoding, responseRule.getCharacterEncoding());
		
		if(responseRule.getResponseType() == ResponseType.TRANSFORM) {
			TransformResponse transformResponse = responseRule.getRespondent();
			responseParameters.putValue(ResponseParameters.transform, assembleTransformParameters(transformResponse.getTransformRule()));
		} else if(responseRule.getResponseType() == ResponseType.DISPATCH) {
			DispatchResponse dispatchResponse = responseRule.getRespondent();
			responseParameters.putValue(ResponseParameters.dispatch, assembleDispatchParameters(dispatchResponse.getDispatchResponseRule()));
		} else if(responseRule.getResponseType() == ResponseType.FORWARD) {
			ForwardResponse forwardResponse = responseRule.getRespondent();
			responseParameters.putValue(ResponseParameters.forward, assembleForwardParameters(forwardResponse.getForwardResponseRule()));
		} else if(responseRule.getResponseType() == ResponseType.REDIRECT) {
			RedirectResponse redirectResponse = responseRule.getRespondent();
			responseParameters.putValue(ResponseParameters.redirect, assembleRedirectParameters(redirectResponse.getRedirectResponseRule()));
		}
		
		return responseParameters;
	}
	
	public Parameters assembleTransformParameters(TransformRule transformRule) {
		TransformParameters transformParameters = new TransformParameters();

		if(transformRule.getTransformType() != null)
			transformParameters.putValue(TransformParameters.type, transformRule.getTransformType().toString());

		if(transformRule.getContentType() != null)
			transformParameters.putValue(TransformParameters.contentType, transformRule.getContentType().toString());
		
		transformParameters.putValue(TransformParameters.characterEncoding, transformRule.getCharacterEncoding());

		if(transformRule.getTemplateRule() != null)
			transformParameters.putValue(TransformParameters.template, assembleTemplateParameters(transformRule.getTemplateRule()));
		
		ActionList actionList = transformRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, transformParameters, TransformParameters.actions);
		}

		transformParameters.putValue(TransformParameters.defaultResponse, transformRule.getDefaultResponse());
		transformParameters.putValue(TransformParameters.pretty, transformRule.getPretty());
		
		return transformParameters;
	}
	
	public Parameters assembleDispatchParameters(DispatchResponseRule dispatchResponseRule) {
		DispatchParameters dispatchParameters = new DispatchParameters();

		if(dispatchResponseRule.getContentType() != null)
			dispatchParameters.putValue(DispatchParameters.contentType, dispatchResponseRule.getContentType().toString());
		
		dispatchParameters.putValue(DispatchParameters.characterEncoding, dispatchResponseRule.getCharacterEncoding());
		
		if(dispatchResponseRule.getTemplateRule() != null)
			dispatchParameters.putValue(DispatchParameters.template, assembleTemplateParameters(dispatchResponseRule.getTemplateRule()));
		
		ActionList actionList = dispatchResponseRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, dispatchParameters, DispatchParameters.actions);
		}
		
		dispatchParameters.putValue(DispatchParameters.defaultResponse, dispatchResponseRule.getDefaultResponse());
		
		return dispatchParameters;
	}
	
	public Parameters assembleForwardParameters(ForwardResponseRule forwardResponseRule) {
		ForwardParameters forwardParameters = new ForwardParameters();
		
		if(forwardResponseRule.getContentType() != null)
			forwardParameters.putValue(ForwardParameters.contentType, forwardResponseRule.getContentType().toString());
		
		forwardParameters.putValue(ForwardParameters.translet, forwardResponseRule.getTransletName());
		
		ItemRuleMap attributeItemRuleMap = forwardResponseRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			forwardParameters.putValue(ForwardParameters.attributes, assembleItemHolderParameters(attributeItemRuleMap));
		}
		
		ActionList actionList = forwardResponseRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, forwardParameters, ForwardParameters.actions);
		}
		
		forwardParameters.putValue(ForwardParameters.defaultResponse, forwardResponseRule.getDefaultResponse());
		
		return forwardParameters;
	}
	
	public Parameters assembleRedirectParameters(RedirectResponseRule redirectResponseRule) {
		RedirectParameters redirectParameters = new RedirectParameters();

		if(redirectResponseRule.getContentType() != null)
			redirectParameters.putValue(RedirectParameters.contentType, redirectResponseRule.getContentType().toString());
		
		redirectParameters.putValue(RedirectParameters.translet, redirectResponseRule.getTransletName());
		redirectParameters.putValue(RedirectParameters.url, redirectResponseRule.getUrl());
		
		ItemRuleMap parameterItemRuleMap = redirectResponseRule.getParameterItemRuleMap();
		if(parameterItemRuleMap != null) {
			redirectParameters.putValue(RedirectParameters.parameters, assembleItemHolderParameters(parameterItemRuleMap));
		}

		redirectParameters.putValue(RedirectParameters.excludeNullParameter, redirectResponseRule.getExcludeNullParameter());

		ActionList actionList = redirectResponseRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, redirectParameters, RedirectParameters.actions);
		}
		
		redirectParameters.putValue(RedirectParameters.defaultResponse, redirectResponseRule.getDefaultResponse());
		
		return redirectParameters;
	}
	
	public Parameters assembleTemplateParameters(TemplateRule templateRule) {
		TemplateParameters templateParameters = new TemplateParameters();
		templateParameters.putValue(TemplateParameters.id, templateRule.getId());
		templateParameters.putValue(TemplateParameters.engine, templateRule.getEngine());
		templateParameters.putValue(TemplateParameters.file, templateRule.getFile());
		templateParameters.putValue(TemplateParameters.resource, templateRule.getResource());
		templateParameters.putValue(TemplateParameters.url, templateRule.getUrl());
		templateParameters.putValue(TemplateParameters.content, templateRule.getContent());
		templateParameters.putValue(TemplateParameters.encoding, templateRule.getEncoding());
		templateParameters.putValue(TemplateParameters.noCache, templateRule.getNoCache());
		
		return templateParameters;
	}
	
	public void assembleActionList(ActionList actionList, Parameters parameters, ParameterDefine parameterDefine) {
		for(Executable action : actionList) {
			if(action.getActionType() == ActionType.ECHO) {
				EchoActionRule echoActionRule = action.getActionRule();
				parameters.putValue(parameterDefine, assembleActionParameters(echoActionRule));
			} else if(action.getActionType() == ActionType.BEAN) {
				BeanActionRule beanActionRule = action.getActionRule();
				parameters.putValue(parameterDefine, assembleActionParameters(beanActionRule));
			} else if(action.getActionType() == ActionType.INCLUDE) {
				IncludeActionRule includeActionRule = action.getActionRule();
				parameters.putValue(parameterDefine, assembleActionParameters(includeActionRule));
			}
		}
	}
	
	public Parameters assembleActionParameters(BeanActionRule beanActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.putValue(ActionParameters.id, beanActionRule.getActionId());
		actionParameters.putValue(ActionParameters.beanId, beanActionRule.getBeanId());
		actionParameters.putValue(ActionParameters.methodName, beanActionRule.getMethodName());
		
		ItemRuleMap propertyItemRuleMap = beanActionRule.getPropertyItemRuleMap();
		if(propertyItemRuleMap != null) {
			actionParameters.putValue(ActionParameters.properties, assembleItemHolderParameters(propertyItemRuleMap));
		}
		
		ItemRuleMap argumentItemRuleMap = beanActionRule.getArgumentItemRuleMap();
		if(argumentItemRuleMap != null) {
			actionParameters.putValue(ActionParameters.arguments, assembleItemHolderParameters(argumentItemRuleMap));
		}
		
		actionParameters.putValue(ActionParameters.hidden, beanActionRule.getHidden());
		
		return actionParameters;
	}
	
	public Parameters assembleActionParameters(EchoActionRule echoActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.putValue(ActionParameters.id, echoActionRule.getActionId());
		
		ItemRuleMap attributeItemRuleMap = echoActionRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			actionParameters.putValue(ActionParameters.echo, assembleItemHolderParameters(attributeItemRuleMap));
		}
		
		actionParameters.putValue(ActionParameters.hidden, echoActionRule.getHidden());
		
		return actionParameters;
	}
	
	public Parameters assembleActionParameters(IncludeActionRule includeActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.putValue(ActionParameters.id, includeActionRule.getActionId());
		actionParameters.putValue(ActionParameters.include, includeActionRule.getTransletName());
		
		ItemRuleMap attributeItemRuleMap = includeActionRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			actionParameters.putValue(ActionParameters.attributes, assembleItemHolderParameters(attributeItemRuleMap));
		}
		
		actionParameters.putValue(ActionParameters.hidden, includeActionRule.getHidden());
		
		return actionParameters;
	}

	public Parameters assembleItemHolderParameters(ItemRuleMap itemRuleMap) {
		ItemHolderParameters itemHolderParameters = new ItemHolderParameters();
		for(ItemRule itemRule : itemRuleMap) {
			itemHolderParameters.putValue(ItemHolderParameters.item, assembleItemParameters(itemRule));
		}
		return itemHolderParameters;
	}
	
	public Parameters assembleItemParameters(ItemRule itemRule) {
		ItemParameters itemParameters = new ItemParameters();
		if(itemRule.getType() != null && itemRule.getType() != ItemType.SINGULAR)
			itemParameters.putValue(ItemParameters.type, itemRule.getType().toString());
		if(!itemRule.isAutoGeneratedName())
			itemParameters.putValue(ItemParameters.name, itemRule.getName());
		if(itemRule.getValueType() != null)
			itemParameters.putValue(ItemParameters.valueType, itemRule.getValueType().toString());
		if(itemRule.getDefaultValue() != null)
			itemParameters.putValue(ItemParameters.defaultValue, itemRule.getDefaultValue().toString());
		if(itemRule.getTokenize() != null)
			itemParameters.putValue(ItemParameters.tokenize, itemRule.getTokenize());

		/*
		if(itemRule.getValueType() == ItemValueType.PARAMETERS) {
			Parameter p = itemParameters.getParameter(ItemParameters.value);
			p.setParameterValueType(ParameterValueType.TEXT);
		}
		*/
		
		if(itemRule.getType() == ItemType.SINGULAR) {
			String value = itemRule.getValue();
			if(value != null) {
				itemParameters.putValue(ItemParameters.value, value);
			}
		} else if(itemRule.getType() == ItemType.ARRAY || itemRule.getType() == ItemType.LIST || itemRule.getType() == ItemType.SET) {
			List<String> valueList = itemRule.getValueList();
			if(valueList != null) {
				for(String value : valueList) {
					itemParameters.putValue(ItemParameters.value, value);
				}
			}
		} else if(itemRule.getType() == ItemType.MAP || itemRule.getType() == ItemType.PROPERTIES) {
			Map<String, String> valueMap = itemRule.getValueMap();
			if(valueMap != null) {
				Parameters para = itemParameters.newParameters(ItemParameters.value);
				for(Map.Entry<String, String> entry : valueMap.entrySet()) {
					para.putValue(entry.getKey(), entry.getValue());
				}
			}
		}
		
		return itemParameters;
	}
	
}
