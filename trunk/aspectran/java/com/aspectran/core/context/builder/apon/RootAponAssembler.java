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
import com.aspectran.core.context.builder.apon.params.ExceptionRaizedParameters;
import com.aspectran.core.context.builder.apon.params.ForwardParameters;
import com.aspectran.core.context.builder.apon.params.ImportParameters;
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
import com.aspectran.core.context.rule.ResponseByContentTypeRuleMap;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;
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
 * AspectranAponAssembler.
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
		rootParameters.setValue(RootParameters.aspectran, assembleAspectran());
		return rootParameters;
	}
	
	public Parameters assembleAspectran() throws Exception {
		Parameters aspectranParameters = new AspectranParameters();
		
		DefaultSettings defaultSettings = assistant.getDefaultSettings();
		if(defaultSettings != null) {
			DefaultSettingsParameters settingParameters = aspectranParameters.newParameters(AspectranParameters.setting);
			settingParameters.setValue(DefaultSettingsParameters.transletNamePattern, defaultSettings.getTransletNamePattern());
			settingParameters.setValue(DefaultSettingsParameters.transletNamePatternPrefix, defaultSettings.getTransletNamePatternPrefix());
			settingParameters.setValue(DefaultSettingsParameters.transletNamePatternSuffix, defaultSettings.getTransletNamePatternSuffix());
			settingParameters.setValue(DefaultSettingsParameters.transletInterfaceClassName, defaultSettings.getTransletInterfaceClassName());
			settingParameters.setValue(DefaultSettingsParameters.transletImplementClassName, defaultSettings.getTransletImplementClassName());
			settingParameters.setValue(DefaultSettingsParameters.nullableContentId, defaultSettings.getNullableContentId());
			settingParameters.setValue(DefaultSettingsParameters.nullableActionId, defaultSettings.getNullableActionId());
			settingParameters.setValue(DefaultSettingsParameters.activityDefaultHandler, defaultSettings.getActivityDefaultHandler());
			settingParameters.setValue(DefaultSettingsParameters.beanProxyMode, defaultSettings.getBeanProxyMode());
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
		aspectParameters.setValue(AspectParameters.id, aspectRule.getId());
		aspectParameters.setValue(AspectParameters.useFor, aspectRule.getAspectTargetType());
		
		Parameters joinpointParameters = aspectParameters.newParameters(AspectParameters.jointpoint);
		joinpointParameters.setValue(JoinpointParameters.scope, aspectRule.getJoinpointScope());
		
		Parameters pointcutParameters = joinpointParameters.newParameters(JoinpointParameters.pointcut);

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
				GenericParameters settingParameters = aspectParameters.newParameters(AspectParameters.setting);
				for(Map.Entry<String, String> entry : settings.entrySet()) {
					settingParameters.putValue(entry.getKey(), entry.getValue());
				}
			}
		}

		Parameters adviceParameters = aspectParameters.newParameters(AspectParameters.advice);
		adviceParameters.setValue(AdviceParameters.bean, aspectRule.getAdviceBeanId());
		
		List<AspectAdviceRule> aspectAdviceRuleList = aspectRule.getAspectAdviceRuleList();
		if(aspectAdviceRuleList != null) {
			for(AspectAdviceRule aspectAdviceRule : aspectAdviceRuleList) {
				if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.beforeAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.setValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.setValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.afterAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.setValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.setValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.aroundAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.setValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.setValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.finallyAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.setValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.setValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.EXCPETION_RAIZED) {
					Parameters exceptionRaizedParameters = adviceParameters.newParameters(AdviceParameters.exceptionRaized);
					if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						exceptionRaizedParameters.setValue(ExceptionRaizedParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						exceptionRaizedParameters.setValue(ExceptionRaizedParameters.action, assembleActionParameters(beanActionRule));
					}
					ResponseByContentTypeRuleMap responseByContentTypeRuleMap = aspectAdviceRule.getResponseByContentTypeRuleMap();
					for(ResponseByContentTypeRule rbctr : responseByContentTypeRuleMap) {
						exceptionRaizedParameters.putValue(ExceptionRaizedParameters.responseByContentTypes, assembleResponseByContentTypeParameters(rbctr));
					}
				}
			}
		}
		
		List<AspectJobAdviceRule> aspectJobAdviceRuleList = aspectRule.getAspectJobAdviceRuleList();
		if(aspectJobAdviceRuleList != null) {
			for(AspectJobAdviceRule aspectJobAdviceRule : aspectJobAdviceRuleList) {
				Parameters jobParameters = new JobParameters();
				jobParameters.setValue(JobParameters.translet, aspectJobAdviceRule.getJobTransletName());
				jobParameters.setValue(JobParameters.disabled, aspectJobAdviceRule.getDisabled());
				adviceParameters.putValue(AdviceParameters.jobs, jobParameters);
			}
		}
		
		return aspectParameters;
	}

	public Parameters assembleBeanParameters(BeanRule beanRule) {
		Parameters beanParameters = new BeanParameters();
		beanParameters.setValue(BeanParameters.id, beanRule.getId());
		beanParameters.setValue(BeanParameters.className, beanRule.getClassName());
		if(beanRule.getSingleton() == Boolean.TRUE && beanRule.getScopeType() == ScopeType.SINGLETON)
			beanParameters.setValue(BeanParameters.singleton, beanRule.getSingleton());
		else
			beanParameters.setValue(BeanParameters.scope, beanRule.getScopeType().toString());
		beanParameters.setValue(BeanParameters.factoryMethod, beanRule.getFactoryMethodName());
		beanParameters.setValue(BeanParameters.initMethod, beanRule.getInitMethodName());
		beanParameters.setValue(BeanParameters.destroyMethod, beanRule.getDestroyMethodName());
		beanParameters.setValue(BeanParameters.lazyInit, beanRule.getLazyInit());
		beanParameters.setValue(BeanParameters.important, beanRule.getImportant());
		beanParameters.setValue(BeanParameters.important, beanRule.getImportant());
		
		ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
		if(constructorArgumentItemRuleMap != null) {
			ConstructorParameters constructorParameters = beanParameters.newParameters(BeanParameters.constructor);
			for(ItemRule itemRule : constructorArgumentItemRuleMap) {
				constructorParameters.putValue(ConstructorParameters.arguments, assembleItemParameters(itemRule));
			}
		}
		
		ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
		if(propertyItemRuleMap != null) {
			for(ItemRule itemRule : propertyItemRuleMap) {
				beanParameters.putValue(BeanParameters.properties, assembleItemParameters(itemRule));
			}
		}
		
		return beanParameters;
	}
	
	public Parameters assembleTransletParameters(TransletRule transletRule) {
		Parameters transletParameters = new TransletParameters();
		transletParameters.setValue(TransletParameters.name, transletRule.getName());

		RequestRule requestRule = transletRule.getRequestRule();
		if(requestRule != null) {
			RequestParameters requestParameters = transletParameters.newParameters(TransletParameters.request);
			requestParameters.setValue(RequestParameters.method, requestRule.getMethod());
			requestParameters.setValue(RequestParameters.characterEncoding, requestRule.getCharacterEncoding());
			
			ItemRuleMap attributeItemRuleMap = requestRule.getAttributeItemRuleMap();
			if(attributeItemRuleMap != null) {
				for(ItemRule itemRule : attributeItemRuleMap) {
					requestParameters.putValue(RequestParameters.attributes, assembleItemParameters(itemRule));
				}
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
		
		ResponseByContentTypeRuleMap exceptionHandlingRuleMap = transletRule.getExceptionHandlingRuleMap();
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
			importParameters.setValue(ImportParameters.file, imp.getDistinguishedName());
		} else if(imp.getImportType() == ImportType.RESOURCE) {
			importParameters.setValue(ImportParameters.resource, imp.getDistinguishedName());
		} else if(imp.getImportType() == ImportType.URL) {
			importParameters.setValue(ImportParameters.url, imp.getDistinguishedName());
			importParameters.setValue(ImportParameters.fileType, imp.getImportFileType());
		}
		
		return importParameters;
		
	}
	
	public Parameters assembleResponseByContentTypeParameters(ResponseByContentTypeRule responseByContentTypeRule) {
		ResponseByContentTypeParameters rbctp = new ResponseByContentTypeParameters();
		rbctp.setValue(ResponseByContentTypeParameters.exceptionType, responseByContentTypeRule.getExceptionType());
		
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
			responseParameters.setValue(ResponseParameters.forward, assembleForwardParameters(forwardResponse.getForwardResponseRule()));
		} else if(responseRule.getResponseType() == ResponseType.REDIRECT) {
			RedirectResponse redirectResponse = responseRule.getRespondent();
			responseParameters.setValue(ResponseParameters.redirect, assembleRedirectParameters(redirectResponse.getRedirectResponseRule()));
		}
		
		return responseParameters;
	}
	
	public Parameters assembleTransformParameters(TransformRule transformRule) {
		TransformParameters transformParameters = new TransformParameters();

		if(transformRule.getTransformType() != null)
			transformParameters.setValue(TransformParameters.type, transformRule.getTransformType().toString());

		if(transformRule.getContentType() != null)
			transformParameters.setValue(TransformParameters.contentType, transformRule.getContentType().toString());
		
		transformParameters.setValue(TransformParameters.characterEncoding, transformRule.getCharacterEncoding());

		if(transformRule.getTemplateRule() != null)
			transformParameters.setValue(TransformParameters.template, assembleTemplateParameters(transformRule.getTemplateRule()));
		
		ActionList actionList = transformRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, transformParameters, TransformParameters.actions);
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
			assembleActionList(actionList, dispatchParameters, DispatchParameters.actions);
		}
		
		dispatchParameters.setValue(DispatchParameters.defaultResponse, dispatchResponseRule.getDefaultResponse());
		
		return dispatchParameters;
	}
	
	public Parameters assembleForwardParameters(ForwardResponseRule forwardResponseRule) {
		ForwardParameters forwardParameters = new ForwardParameters();
		
		if(forwardResponseRule.getContentType() != null)
			forwardParameters.setValue(ForwardParameters.contentType, forwardResponseRule.getContentType().toString());
		
		forwardParameters.setValue(ForwardParameters.translet, forwardResponseRule.getTransletName());
		
		ItemRuleMap attributeItemRuleMap = forwardResponseRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			for(ItemRule itemRule : attributeItemRuleMap) {
				forwardParameters.putValue(ForwardParameters.attributes, assembleItemParameters(itemRule));
			}
		}
		
		ActionList actionList = forwardResponseRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, forwardParameters, ForwardParameters.actions);
		}
		
		forwardParameters.setValue(ForwardParameters.defaultResponse, forwardResponseRule.getDefaultResponse());
		
		return forwardParameters;
	}
	
	public Parameters assembleRedirectParameters(RedirectResponseRule redirectResponseRule) {
		RedirectParameters redirectParameters = new RedirectParameters();

		if(redirectResponseRule.getContentType() != null)
			redirectParameters.setValue(RedirectParameters.contentType, redirectResponseRule.getContentType().toString());
		
		redirectParameters.setValue(RedirectParameters.translet, redirectResponseRule.getTransletName());
		redirectParameters.setValue(RedirectParameters.url, redirectResponseRule.getUrl());
		
		ItemRuleMap parameterItemRuleMap = redirectResponseRule.getParameterItemRuleMap();
		if(parameterItemRuleMap != null) {
			for(ItemRule itemRule : parameterItemRuleMap) {
				redirectParameters.putValue(RedirectParameters.parameters, assembleItemParameters(itemRule));
			}
		}

		redirectParameters.setValue(RedirectParameters.excludeNullParameter, redirectResponseRule.getExcludeNullParameter());

		ActionList actionList = redirectResponseRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, redirectParameters, RedirectParameters.actions);
		}
		
		redirectParameters.setValue(RedirectParameters.defaultResponse, redirectResponseRule.getDefaultResponse());
		
		return redirectParameters;
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
		if(!itemRule.isUnknownName())
			itemParameters.setValue(ItemParameters.name, itemRule.getName());
		if(itemRule.getValueType() != null)
			itemParameters.setValue(ItemParameters.valueType, itemRule.getValueType());
		if(itemRule.getDefaultValue() != null)
			itemParameters.setValue(ItemParameters.defaultValue, itemRule.getDefaultValue());
		if(itemRule.getTokenize() != null)
			itemParameters.setValue(ItemParameters.tokenize, itemRule.getTokenize());

		if(itemRule.getType() == ItemType.SINGLE) {
			String value = itemRule.getValue();
			if(value != null) {
				itemParameters.setValue(ItemParameters.value, value);
			}
		} else if(itemRule.getType() == ItemType.LIST) {
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
