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
import com.aspectran.core.context.builder.apon.params.EnvironmentParameters;
import com.aspectran.core.context.builder.apon.params.ExceptionParameters;
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
import com.aspectran.core.context.builder.assistant.AssistantLocal;
import com.aspectran.core.context.builder.assistant.ContextBuilderAssistant;
import com.aspectran.core.context.builder.assistant.DefaultSettings;
import com.aspectran.core.context.builder.importer.Importer;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.JobRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.HeadingActionRule;
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
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.ImporterType;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;

/**
 * The Class RootAponAssembler.
 * 
 * <p>Created: 2015. 01. 27 PM 10:36:29</p>
 */
public class RootAponAssembler {
	
	private final ContextBuilderAssistant assistant;
	
	public RootAponAssembler(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}
	
	public Parameters assembleRootParameters() throws Exception {
		Parameters rootParameters = new RootParameters();
		rootParameters.putValue(RootParameters.aspectran, assembleAspectranParameters());
		return rootParameters;
	}
	
	private Parameters assembleAspectranParameters() throws Exception {
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
				settingParameters.putValueNonNull(DefaultSettingsParameters.transletNamePrefix, defaultSettings.getTransletNamePrefix());
				settingParameters.putValueNonNull(DefaultSettingsParameters.transletNameSuffix, defaultSettings.getTransletNameSuffix());
			}
			settingParameters.putValueNonNull(DefaultSettingsParameters.transletInterfaceClass, defaultSettings.getTransletInterfaceClassName());
			settingParameters.putValueNonNull(DefaultSettingsParameters.transletImplementClass, defaultSettings.getTransletImplementationClassName());
			settingParameters.putValueNonNull(DefaultSettingsParameters.nullableActionId, defaultSettings.getNullableActionId());
			settingParameters.putValueNonNull(DefaultSettingsParameters.beanProxifier, defaultSettings.getBeanProxifier());
			settingParameters.putValueNonNull(DefaultSettingsParameters.pointcutPatternVerifiable, defaultSettings.getPointcutPatternVerifiable());
			settingParameters.putValueNonNull(DefaultSettingsParameters.defaultTemplateEngine, defaultSettings.getDefaultTemplateEngine());
		}
		
		List<EnvironmentRule> environmentRules = assistant.getEnvironmentRules();
		if(!environmentRules.isEmpty()) {
			for(EnvironmentRule environmentRule : environmentRules) {
				Parameters p = assembleEnvironmentParameters(environmentRule);
				aspectranParameters.putValue(AspectranParameters.environments, p);
			}
		}
		
		Map<String, String> typeAliases = assistant.getTypeAliases();
		if(!typeAliases.isEmpty()) {
			VariableParameters typeAliasParameters = aspectranParameters.newParameters(AspectranParameters.typeAlias);
			for(Map.Entry<String, String> entry : typeAliases.entrySet()) {
				typeAliasParameters.putValue(entry.getKey(), entry.getValue());
			}
		}

		for(AspectRule aspectRule : assistant.getAspectRules()) {
			Parameters p = assembleAspectParameters(aspectRule);
			aspectranParameters.putValue(AspectranParameters.aspects, p);
		}

		for(BeanRule beanRule : assistant.getBeanRules()) {
			Parameters p = assembleBeanParameters(beanRule);
			aspectranParameters.putValue(AspectranParameters.beans, p);
		}
		
		for(TransletRule transletRule : assistant.getTransletRules()) {
			Parameters p = assembleTransletParameters(transletRule);
			aspectranParameters.putValue(AspectranParameters.translets, p);
		}
		
		for(TemplateRule templateRule : assistant.getTemplateRules()) {
			Parameters p = assembleTemplateParameters(templateRule);
			aspectranParameters.putValue(AspectranParameters.templates, p);
		}

		List<Importer> pendingList = assistant.getImportHandler().getPendingList();
		if(pendingList != null) {
			for(Importer importer : pendingList) {
				aspectranParameters.putValue(AspectranParameters.imports, assembleImportParameters(importer));
			}
		}

		return aspectranParameters;
	}
	
	private Parameters assembleEnvironmentParameters(EnvironmentRule environmentRule) {
		Parameters environmentParameters = new EnvironmentParameters();
		environmentParameters.putValueNonNull(EnvironmentParameters.profile, environmentRule.getProfile());
		if(environmentRule.getPropertyItemRuleMap() != null) {
			Parameters itemHoderParameters = assembleItemHolderParameters(environmentRule.getPropertyItemRuleMap());
			environmentParameters.putValue(EnvironmentParameters.properties, itemHoderParameters);
		}
		return environmentParameters;
	}
	
	private Parameters assembleAspectParameters(AspectRule aspectRule) {
		Parameters aspectParameters = new AspectParameters();
		aspectParameters.putValueNonNull(AspectParameters.description, aspectRule.getDescription());
		aspectParameters.putValueNonNull(AspectParameters.id, aspectRule.getId());
		aspectParameters.putValueNonNull(AspectParameters.usedFor, aspectRule.getAspectTargetType());
		
		Parameters joinpointParameters = aspectParameters.newParameters(AspectParameters.jointpoint);
		joinpointParameters.putValueNonNull(JoinpointParameters.scope, aspectRule.getJoinpointType());
		
		MethodType[] targetMethods = aspectRule.getTargetMethods();
		if(targetMethods != null) {
			for(MethodType targetMethod : targetMethods) {
				joinpointParameters.putValue(JoinpointParameters.methods, targetMethod);
			}
		}
		
		MethodType[] targetHeaders = aspectRule.getTargetMethods();
		if(targetHeaders != null) {
			for(MethodType targetHeader : targetHeaders) {
				joinpointParameters.putValue(JoinpointParameters.methods, targetHeader);
			}
		}
		
		joinpointParameters.putValueNonNull(JoinpointParameters.simpleTrigger, aspectRule.getSimpleTriggerParameters());
		joinpointParameters.putValueNonNull(JoinpointParameters.cronTrigger, aspectRule.getCronTriggerParameters());

		
		PointcutRule pointcutRule = aspectRule.getPointcutRule();
		if(pointcutRule != null) {
			Parameters pointcutParameters = joinpointParameters.newParameters(JoinpointParameters.pointcut);
			List<Parameters> targetParametersList = pointcutRule.getTargetParametersList();
			if(targetParametersList != null) {
				for(Parameters targetParameters : targetParametersList) {
					pointcutParameters.putValue(PointcutParameters.targets, targetParameters);
				}
			}
			pointcutParameters.putValueNonNull(PointcutParameters.simpleTrigger, pointcutRule.getSimpleTriggerParameters());
			pointcutParameters.putValueNonNull(PointcutParameters.cronTrigger, pointcutRule.getCronTriggerParameters());
		}
		
		SettingsAdviceRule settingsAdviceRule = aspectRule.getSettingsAdviceRule();
		if(settingsAdviceRule != null) {
			Map<String, Object> settings = settingsAdviceRule.getSettings();
			if(settings != null) {
				VariableParameters settingsParameters = aspectParameters.newParameters(AspectParameters.settings);
				for(Map.Entry<String, Object> entry : settings.entrySet()) {
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
					if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.HEADERS) {
						HeadingActionRule headingActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(headingActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.afterAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.HEADERS) {
						HeadingActionRule headingActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(headingActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.aroundAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.HEADERS) {
						HeadingActionRule headingActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(headingActionRule));
					}
				} else if(aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
					Parameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.finallyAdvice);
					if(aspectAdviceRule.getActionType() == ActionType.BEAN) {
						BeanActionRule beanActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(beanActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.ECHO) {
						EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(echoActionRule));
					} else if(aspectAdviceRule.getActionType() == ActionType.HEADERS) {
						HeadingActionRule headingActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
						adviceActionParameters.putValue(AdviceActionParameters.action, assembleActionParameters(headingActionRule));
					}
				}
			}
		}
		
		ExceptionRule exceptionRule = aspectRule.getExceptionRule();
		if(exceptionRule != null) {
			Parameters exceptionParameters = aspectParameters.touchParameters(AspectParameters.exception);
			if(exceptionRule.getDescription() != null) {
				exceptionParameters.putValue(ExceptionParameters.description, exceptionRule.getDescription());
			}
			if(exceptionRule.getActionType() == ActionType.BEAN) {
				BeanActionRule beanActionRule = exceptionRule.getExecutableAction().getActionRule();
				exceptionParameters.putValue(ExceptionParameters.action, assembleActionParameters(beanActionRule));
			} else if(exceptionRule.getActionType() == ActionType.ECHO) {
				EchoActionRule echoActionRule = exceptionRule.getExecutableAction().getActionRule();
				exceptionParameters.putValue(ExceptionParameters.action, assembleActionParameters(echoActionRule));
			} else if(exceptionRule.getActionType() == ActionType.HEADERS) {
				HeadingActionRule headingActionRule = exceptionRule.getExecutableAction().getActionRule();
				exceptionParameters.putValue(ExceptionParameters.action, assembleActionParameters(headingActionRule));
			}
			for(ResponseByContentTypeRule rbctr : exceptionRule) {
				exceptionParameters.putValue(ExceptionParameters.responseByContentTypes, assembleResponseByContentTypeParameters(rbctr));
			}
		}
		
		List<JobRule> aspectJobAdviceRuleList = aspectRule.getAspectJobAdviceRuleList();
		if(aspectJobAdviceRuleList != null) {
			Parameters adviceParameters = aspectParameters.touchParameters(AspectParameters.advice);
			for(JobRule aspectJobAdviceRule : aspectJobAdviceRuleList) {
				Parameters jobParameters = new JobParameters();
				jobParameters.putValue(JobParameters.translet, aspectJobAdviceRule.getJobTransletName());
				jobParameters.putValueNonNull(JobParameters.disabled, aspectJobAdviceRule.getDisabled());
				adviceParameters.putValue(AdviceParameters.jobs, jobParameters);
			}
		}
		
		return aspectParameters;
	}

	private Parameters assembleBeanParameters(BeanRule beanRule) {
		Parameters beanParameters = new BeanParameters();
		beanParameters.putValueNonNull(BeanParameters.description, beanRule.getDescription());
		beanParameters.putValueNonNull(BeanParameters.id, beanRule.getId());
		beanParameters.putValueNonNull(BeanParameters.className, beanRule.getClassName());
		beanParameters.putValueNonNull(BeanParameters.scan, beanRule.getScanPath());
		beanParameters.putValueNonNull(BeanParameters.mask, beanRule.getMaskPattern());
		if(beanRule.getSingleton() == Boolean.TRUE && beanRule.getScopeType() == ScopeType.SINGLETON)
			beanParameters.putValue(BeanParameters.singleton, beanRule.getSingleton());
		else if(beanRule.getScopeType() != null)
			beanParameters.putValue(BeanParameters.scope, beanRule.getScopeType().toString());
		beanParameters.putValueNonNull(BeanParameters.offerBean, beanRule.getOfferBeanId());
		beanParameters.putValueNonNull(BeanParameters.offerMethod, beanRule.getOfferMethodName());
		beanParameters.putValueNonNull(BeanParameters.initMethod, beanRule.getInitMethodName());
		beanParameters.putValueNonNull(BeanParameters.factoryMethod, beanRule.getFactoryMethodName());
		beanParameters.putValueNonNull(BeanParameters.destroyMethod, beanRule.getDestroyMethodName());
		beanParameters.putValueNonNull(BeanParameters.lazyInit, beanRule.getLazyInit());
		beanParameters.putValueNonNull(BeanParameters.important, beanRule.getImportant());
		beanParameters.putValueNonNull(BeanParameters.filter, beanRule.getFilterParameters());
		
		ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
		if(constructorArgumentItemRuleMap != null) {
			ConstructorParameters constructorParameters = beanParameters.newParameters(BeanParameters.constructor);
			Parameters itemHoderParameters = assembleItemHolderParameters(constructorArgumentItemRuleMap);
			constructorParameters.putValue(ConstructorParameters.arguments, itemHoderParameters);
		}
		
		ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
		if(propertyItemRuleMap != null) {
			Parameters itemHoderParameters = assembleItemHolderParameters(propertyItemRuleMap);
			beanParameters.putValue(BeanParameters.properties, itemHoderParameters);
		}
		
		return beanParameters;
	}
	
	private Parameters assembleTransletParameters(TransletRule transletRule) {
		Parameters transletParameters = new TransletParameters();
		transletParameters.putValueNonNull(TransletParameters.description, transletRule.getDescription());
		transletParameters.putValueNonNull(TransletParameters.name, transletRule.getName());
		transletParameters.putValueNonNull(TransletParameters.scan, transletRule.getScanPath());
		transletParameters.putValueNonNull(TransletParameters.mask, transletRule.getMaskPattern());
		
		if(transletRule.getAllowedMethods() != null)
			transletParameters.putValue(TransletParameters.method, MethodType.stringify(transletRule.getAllowedMethods()));

		RequestRule requestRule = transletRule.getRequestRule();
		if(requestRule != null) {
			RequestParameters requestParameters = transletParameters.newParameters(TransletParameters.request);
			requestParameters.putValueNonNull(RequestParameters.allowedMethod, requestRule.getAllowedMethod());
			requestParameters.putValueNonNull(RequestParameters.characterEncoding, requestRule.getCharacterEncoding());
			
			ItemRuleMap parameterItemRuleMap = requestRule.getParameterItemRuleMap();
			if(parameterItemRuleMap != null) {
				requestParameters.putValue(RequestParameters.parameters, assembleItemHolderParameters(parameterItemRuleMap));
			}

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
					contentsParameters.putValueNonNull(ContentsParameters.name, contentList.getName());
					contentsParameters.putValueNonNull(ContentsParameters.omittable, contentList.getOmittable());
					for(ActionList actionList : contentList) {
						Parameters contentParameters = contentsParameters.newParameters(ContentsParameters.contents);
						contentParameters.putValueNonNull(ContentParameters.name, actionList.getName());
						contentParameters.putValueNonNull(ContentParameters.omittable, actionList.getOmittable());
						contentParameters.putValueNonNull(ContentParameters.hidden, actionList.getHidden());
						assembleActionList(actionList, contentParameters, ContentParameters.actions);
					}
				} else {
					for(ActionList actionList : contentList) {
						Parameters contentParameters = transletParameters.newParameters(TransletParameters.contents2);
						contentParameters.putValueNonNull(ContentParameters.name, actionList.getName());
						contentParameters.putValueNonNull(ContentParameters.omittable, actionList.getOmittable());
						contentParameters.putValueNonNull(ContentParameters.hidden, actionList.getHidden());
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
			if(!transletRule.isImplicitResponse()) {
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
		
		ExceptionRule exceptionRule = transletRule.getExceptionRule();
		if(exceptionRule != null) {
			Parameters exceptionParameters = transletParameters.touchParameters(TransletParameters.exception);
			if(exceptionRule.getDescription() != null) {
				exceptionParameters.putValue(ExceptionParameters.description, exceptionRule.getDescription());
			}
			if(exceptionRule.getActionType() == ActionType.BEAN) {
				BeanActionRule beanActionRule = exceptionRule.getExecutableAction().getActionRule();
				exceptionParameters.putValue(ExceptionParameters.action, assembleActionParameters(beanActionRule));
			} else if(exceptionRule.getActionType() == ActionType.ECHO) {
				EchoActionRule echoActionRule = exceptionRule.getExecutableAction().getActionRule();
				exceptionParameters.putValue(ExceptionParameters.action, assembleActionParameters(echoActionRule));
			} else if(exceptionRule.getActionType() == ActionType.HEADERS) {
				HeadingActionRule headingActionRule = exceptionRule.getExecutableAction().getActionRule();
				exceptionParameters.putValue(ExceptionParameters.action, assembleActionParameters(headingActionRule));
			}
			for(ResponseByContentTypeRule rbctr : exceptionRule) {
				exceptionParameters.putValue(ExceptionParameters.responseByContentTypes, assembleResponseByContentTypeParameters(rbctr));
			}
		}

		return transletParameters;
	}
	
	private Parameters assembleImportParameters(Importer importer) {
		Parameters importParameters = new ImportParameters();

		if(importer.getImporterType() == ImporterType.FILE) {
			importParameters.putValue(ImportParameters.file, importer.getDistinguishedName());
		} else if(importer.getImporterType() == ImporterType.RESOURCE) {
			importParameters.putValue(ImportParameters.resource, importer.getDistinguishedName());
		} else if(importer.getImporterType() == ImporterType.URL) {
			importParameters.putValue(ImportParameters.url, importer.getDistinguishedName());
			importParameters.putValueNonNull(ImportParameters.fileType, importer.getImportFileType());
		}

		if(importer.getProfiles() != null) {
			String profiles = StringUtils.joinCommaDelimitedList(importer.getProfiles());
			importParameters.putValue(ImportParameters.profile, profiles);
		}
		
		return importParameters;
	}
	
	private Parameters assembleResponseByContentTypeParameters(ResponseByContentTypeRule responseByContentTypeRule) {
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
	
	private Parameters assembleResponseParameters(ResponseRule responseRule) {
		ResponseParameters responseParameters = new ResponseParameters();
		responseParameters.putValueNonNull(ResponseParameters.name, responseRule.getName());
		responseParameters.putValueNonNull(ResponseParameters.characterEncoding, responseRule.getCharacterEncoding());
		
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
	
	private Parameters assembleTransformParameters(TransformRule transformRule) {
		TransformParameters transformParameters = new TransformParameters();

		if(transformRule.getTransformType() != null)
			transformParameters.putValue(TransformParameters.type, transformRule.getTransformType().toString());

		if(transformRule.getContentType() != null)
			transformParameters.putValue(TransformParameters.contentType, transformRule.getContentType());
		
		transformParameters.putValueNonNull(TransformParameters.template, transformRule.getTemplateId());
		transformParameters.putValueNonNull(TransformParameters.characterEncoding, transformRule.getCharacterEncoding());
		transformParameters.putValueNonNull(TransformParameters.defaultResponse, transformRule.getDefaultResponse());
		transformParameters.putValueNonNull(TransformParameters.pretty, transformRule.getPretty());

		if(transformRule.getTemplateRule() != null)
			transformParameters.putValue(TransformParameters.builtin, assembleTemplateParameters(transformRule.getTemplateRule()));

		ActionList actionList = transformRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, transformParameters, TransformParameters.actions);
		}

		return transformParameters;
	}
	
	private Parameters assembleDispatchParameters(DispatchResponseRule dispatchResponseRule) {
		DispatchParameters dispatchParameters = new DispatchParameters();
		dispatchParameters.putValueNonNull(DispatchParameters.name, dispatchResponseRule.getName());
		dispatchParameters.putValueNonNull(DispatchParameters.dispatcher, dispatchResponseRule.getDispatcher());
		dispatchParameters.putValueNonNull(DispatchParameters.contentType, dispatchResponseRule.getContentType());
		dispatchParameters.putValueNonNull(DispatchParameters.characterEncoding, dispatchResponseRule.getCharacterEncoding());
		dispatchParameters.putValueNonNull(DispatchParameters.defaultResponse, dispatchResponseRule.getDefaultResponse());
		
		ActionList actionList = dispatchResponseRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, dispatchParameters, DispatchParameters.actions);
		}
		
		return dispatchParameters;
	}
	
	private Parameters assembleForwardParameters(ForwardResponseRule forwardResponseRule) {
		ForwardParameters forwardParameters = new ForwardParameters();
		forwardParameters.putValueNonNull(ForwardParameters.contentType, forwardResponseRule.getContentType());
		forwardParameters.putValueNonNull(ForwardParameters.translet, forwardResponseRule.getTransletName());
		forwardParameters.putValueNonNull(ForwardParameters.defaultResponse, forwardResponseRule.getDefaultResponse());
		
		ItemRuleMap attributeItemRuleMap = forwardResponseRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			forwardParameters.putValue(ForwardParameters.attributes, assembleItemHolderParameters(attributeItemRuleMap));
		}
		
		ActionList actionList = forwardResponseRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, forwardParameters, ForwardParameters.actions);
		}
		
		return forwardParameters;
	}
	
	private Parameters assembleRedirectParameters(RedirectResponseRule redirectResponseRule) {
		RedirectParameters redirectParameters = new RedirectParameters();
		redirectParameters.putValueNonNull(RedirectParameters.contentType, redirectResponseRule.getContentType());
		redirectParameters.putValueNonNull(RedirectParameters.target, redirectResponseRule.getTarget());
		redirectParameters.putValueNonNull(RedirectParameters.excludeNullParameter, redirectResponseRule.getExcludeNullParameter());
		redirectParameters.putValueNonNull(RedirectParameters.defaultResponse, redirectResponseRule.getDefaultResponse());
		
		ItemRuleMap parameterItemRuleMap = redirectResponseRule.getParameterItemRuleMap();
		if(parameterItemRuleMap != null) {
			redirectParameters.putValue(RedirectParameters.parameters, assembleItemHolderParameters(parameterItemRuleMap));
		}

		ActionList actionList = redirectResponseRule.getActionList();
		if(actionList != null) {
			assembleActionList(actionList, redirectParameters, RedirectParameters.actions);
		}
		
		return redirectParameters;
	}

	private Parameters assembleTemplateParameters(TemplateRule templateRule) {
		TemplateParameters templateParameters = new TemplateParameters();
		templateParameters.putValueNonNull(TemplateParameters.id, templateRule.getId());
		templateParameters.putValueNonNull(TemplateParameters.engine, templateRule.getEngine());
		templateParameters.putValueNonNull(TemplateParameters.name, templateRule.getName());
		templateParameters.putValueNonNull(TemplateParameters.file, templateRule.getFile());
		templateParameters.putValueNonNull(TemplateParameters.resource, templateRule.getResource());
		templateParameters.putValueNonNull(TemplateParameters.url, templateRule.getUrl());
		templateParameters.putValueNonNull(TemplateParameters.content, templateRule.getContent());
		templateParameters.putValueNonNull(TemplateParameters.encoding, templateRule.getEncoding());
		templateParameters.putValueNonNull(TemplateParameters.noCache, templateRule.getNoCache());
		
		return templateParameters;
	}
	
	private void assembleActionList(ActionList actionList, Parameters parameters, ParameterDefinition parameterDefinition) {
		for(Executable action : actionList) {
			if(action.getActionType() == ActionType.BEAN) {
				BeanActionRule beanActionRule = action.getActionRule();
				parameters.putValue(parameterDefinition, assembleActionParameters(beanActionRule));
			} else if(action.getActionType() == ActionType.INCLUDE) {
				IncludeActionRule includeActionRule = action.getActionRule();
				parameters.putValue(parameterDefinition, assembleActionParameters(includeActionRule));
			} else if(action.getActionType() == ActionType.ECHO) {
				EchoActionRule echoActionRule = action.getActionRule();
				parameters.putValue(parameterDefinition, assembleActionParameters(echoActionRule));
			} else if(action.getActionType() == ActionType.HEADERS) {
				HeadingActionRule headingActionRule = action.getActionRule();
				parameters.putValue(parameterDefinition, assembleActionParameters(headingActionRule));
			}
		}
	}
	
	private Parameters assembleActionParameters(BeanActionRule beanActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.putValueNonNull(ActionParameters.id, beanActionRule.getActionId());
		actionParameters.putValueNonNull(ActionParameters.bean, beanActionRule.getBeanId());
		actionParameters.putValueNonNull(ActionParameters.methodName, beanActionRule.getMethodName());
		actionParameters.putValueNonNull(ActionParameters.hidden, beanActionRule.getHidden());
		
		ItemRuleMap propertyItemRuleMap = beanActionRule.getPropertyItemRuleMap();
		if(propertyItemRuleMap != null) {
			Parameters itemHoderParameters = assembleItemHolderParameters(propertyItemRuleMap);
			actionParameters.putValue(ActionParameters.properties, itemHoderParameters);
		}
		
		ItemRuleMap argumentItemRuleMap = beanActionRule.getArgumentItemRuleMap();
		if(argumentItemRuleMap != null) {
			Parameters itemHoderParameters = assembleItemHolderParameters(argumentItemRuleMap);
			actionParameters.putValue(ActionParameters.arguments, itemHoderParameters);
		}
		
		return actionParameters;
	}
	
	private Parameters assembleActionParameters(IncludeActionRule includeActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.putValueNonNull(ActionParameters.id, includeActionRule.getActionId());
		actionParameters.putValueNonNull(ActionParameters.include, includeActionRule.getTransletName());
		actionParameters.putValueNonNull(ActionParameters.hidden, includeActionRule.getHidden());
		
		ItemRuleMap attributeItemRuleMap = includeActionRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			actionParameters.putValue(ActionParameters.attributes, assembleItemHolderParameters(attributeItemRuleMap));
		}
		
		return actionParameters;
	}

	private Parameters assembleActionParameters(EchoActionRule echoActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.putValueNonNull(ActionParameters.id, echoActionRule.getActionId());
		actionParameters.putValueNonNull(ActionParameters.hidden, echoActionRule.getHidden());
		
		ItemRuleMap attributeItemRuleMap = echoActionRule.getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			actionParameters.putValue(ActionParameters.echo, assembleItemHolderParameters(attributeItemRuleMap));
		}
		
		return actionParameters;
	}

	private Parameters assembleActionParameters(HeadingActionRule headingActionRule) {
		ActionParameters actionParameters = new ActionParameters();
		actionParameters.putValueNonNull(ActionParameters.id, headingActionRule.getActionId());
		actionParameters.putValueNonNull(ActionParameters.hidden, headingActionRule.getHidden());
		
		ItemRuleMap headerItemRuleMap = headingActionRule.getHeaderItemRuleMap();
		if(headerItemRuleMap != null) {
			actionParameters.putValue(ActionParameters.headers, assembleItemHolderParameters(headerItemRuleMap));
		}
		
		return actionParameters;
	}
	
	private Parameters assembleItemHolderParameters(ItemRuleMap itemRuleMap) {
		ItemHolderParameters itemHolderParameters = new ItemHolderParameters();
		for(ItemRule itemRule : itemRuleMap.values()) {
			itemHolderParameters.putValue(ItemHolderParameters.item, assembleItemParameters(itemRule));
		}
		return itemHolderParameters;
	}
	
	private Parameters assembleItemParameters(ItemRule itemRule) {
		ItemParameters itemParameters = new ItemParameters();
		if(itemRule.getType() != null && itemRule.getType() != ItemType.SINGLE)
			itemParameters.putValue(ItemParameters.type, itemRule.getType().toString());
		if(!itemRule.isAutoGeneratedName())
			itemParameters.putValue(ItemParameters.name, itemRule.getName());
		if(itemRule.getValueType() != null)
			itemParameters.putValue(ItemParameters.valueType, itemRule.getValueType().toString());
		
		itemParameters.putValueNonNull(ItemParameters.defaultValue, itemRule.getDefaultValue());
		itemParameters.putValueNonNull(ItemParameters.tokenize, itemRule.getTokenize());
		itemParameters.putValueNonNull(ItemParameters.mandatory, itemRule.getMandatory());

		if(itemRule.getType() == ItemType.SINGLE) {
			itemParameters.putValueNonNull(ItemParameters.value, itemRule.getValue());
		} else if(itemRule.isListableType()) {
			List<String> valueList = itemRule.getValueList();
			if(valueList != null) {
				for(String value : valueList) {
					itemParameters.putValue(ItemParameters.value, value);
				}
			}
		} else if(itemRule.isMappableType()) {
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
