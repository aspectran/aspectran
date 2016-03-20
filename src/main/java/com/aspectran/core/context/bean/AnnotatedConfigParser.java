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
package com.aspectran.core.context.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.annotation.Action;
import com.aspectran.core.context.bean.annotation.Autowired;
import com.aspectran.core.context.bean.annotation.Bean;
import com.aspectran.core.context.bean.annotation.Configuration;
import com.aspectran.core.context.bean.annotation.Destroy;
import com.aspectran.core.context.bean.annotation.Dispatch;
import com.aspectran.core.context.bean.annotation.Forward;
import com.aspectran.core.context.bean.annotation.Initialize;
import com.aspectran.core.context.bean.annotation.Qualifier;
import com.aspectran.core.context.bean.annotation.Redirect;
import com.aspectran.core.context.bean.annotation.Request;
import com.aspectran.core.context.bean.annotation.Required;
import com.aspectran.core.context.bean.annotation.Transform;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.MethodActionRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AnnotatedConfigParser.
 *
 * <p>Created: 2016. 2. 16.</p>
 *
 * @since 2.0.0
 * @author Juho Jeong
 */
public class AnnotatedConfigParser {

	private final Log log = LogFactory.getLog(AnnotatedConfigParser.class);

	private final BeanRuleRegistry beanRuleRegistry;
	
	private final AnnotatedConfigRelater relater;
	
	private final Map<String, BeanRule> idBasedBeanRuleMap;

	private final Map<Class<?>, Set<BeanRule>> typeBasedBeanRuleMap;

	private final Map<Class<?>, BeanRule> configBeanRuleMap;

	
	public AnnotatedConfigParser(BeanRuleRegistry beanRuleRegistry, AnnotatedConfigRelater relater) {
		this.beanRuleRegistry = beanRuleRegistry;
		this.relater = relater;
		
		this.idBasedBeanRuleMap = beanRuleRegistry.getIdBasedBeanRuleMap();
		this.typeBasedBeanRuleMap = beanRuleRegistry.getTypeBasedBeanRuleMap();
		this.configBeanRuleMap = beanRuleRegistry.getConfigBeanRuleMap();
	}
	
	public void parse() {
		if(log.isDebugEnabled())
			log.debug("Parse the config bean rules: " + configBeanRuleMap.size());

		for(BeanRule beanRule : configBeanRuleMap.values()) {
			if(!beanRule.isOffered()) {
				parseConfigBean(beanRule);
			}
		}

		if(log.isDebugEnabled())
			log.debug("Parse the id based bean rules: " + idBasedBeanRuleMap.size());

		for(BeanRule beanRule : idBasedBeanRuleMap.values()) {
			if(!beanRule.isOffered()) {
				parseFieldAutowire(beanRule);
				parseMethodAutowire(beanRule);
			}
		}

		if(log.isDebugEnabled())
			log.debug("Parse the type based bean rules: " + typeBasedBeanRuleMap.size());

		for(Set<BeanRule> set : typeBasedBeanRuleMap.values()) {
			for(BeanRule beanRule : set) {
				if(!beanRule.isOffered()) {
					parseFieldAutowire(beanRule);
					parseMethodAutowire(beanRule);
				}
			}
		}
	}
	
	private void parseConfigBean(BeanRule beanRule) {
		Class<?> beanClass = beanRule.getBeanClass();
		Configuration configAnno = beanClass.getAnnotation(Configuration.class);

		if(configAnno != null) {
			String[] nameArray = splitNamespace(configAnno.namespace());

			for(Method method : beanClass.getMethods()) {
				if(method.isAnnotationPresent(Bean.class)) {
					parseBeanRule(beanClass, method, nameArray);
				} else if(method.isAnnotationPresent(Request.class)) {
					parseTransletRule(beanClass, method, nameArray);
				}
			}
		}
	}

	private void parseFieldAutowire(BeanRule beanRule) {
		Class<?> beanClass = beanRule.getBeanClass();

		try {
			while(beanClass != null) {
				for(Field field : beanClass.getDeclaredFields()) {
					if(field.isAnnotationPresent(Autowired.class)) {
						Autowired autowiredAnno = field.getAnnotation(Autowired.class);
						boolean required = autowiredAnno.required();
						Qualifier qualifierAnno = field.getAnnotation(Qualifier.class);
						String qualifier = (qualifierAnno != null) ? StringUtils.emptyToNull(qualifierAnno.value()) : null;

						Class<?> type = field.getType();
						String name = (qualifier != null) ? qualifier : field.getName();
						checkExistence(type, name, required);

						AutowireRule autowireRule = new AutowireRule();
						autowireRule.setTarget(field);
						autowireRule.setTypes(type);
						autowireRule.setQualifiers(qualifier);
						autowireRule.setRequired(required);

						beanRule.addAutowireTarget(autowireRule);
					}
				}

				beanClass = beanClass.getSuperclass();
			}
		} catch(Throwable ex) {
			throw new IllegalStateException("Failed to introspect annotations on " + beanClass, ex);
		}
	}

	private void parseMethodAutowire(BeanRule beanRule) {
		Class<?> beanClass = beanRule.getBeanClass();

		try {
			while(beanClass != null) {
				for(Method method : beanClass.getDeclaredMethods()) {
					if(method.isAnnotationPresent(Autowired.class)) {
						Autowired autowiredAnno = method.getAnnotation(Autowired.class);
						boolean required = autowiredAnno.required();
						Qualifier qualifierAnno = method.getAnnotation(Qualifier.class);
						String qualifier = (qualifierAnno != null) ? StringUtils.emptyToNull(qualifierAnno.value()) : null;

						Parameter[] params = method.getParameters();
						Class<?>[] paramTypes = new Class<?>[params.length];
						String[] paramQualifiers = new String[params.length];
						for(int i = 0; i < params.length; i++) {
							Qualifier paramQualifierAnno = params[i].getAnnotation(Qualifier.class);
							String paramQualifier;
							if(paramQualifierAnno != null) {
								paramQualifier = StringUtils.emptyToNull(paramQualifierAnno.value());
							} else {
								paramQualifier = qualifier;
							}

							paramTypes[i] = params[i].getType();
							paramQualifiers[i] = (paramQualifier != null) ? paramQualifier : params[i].getName();
							checkExistence(paramTypes[i], paramQualifiers[i], required);
						}

						AutowireRule autowireRule = new AutowireRule();
						autowireRule.setTarget(method);
						autowireRule.setTypes(paramTypes);
						autowireRule.setQualifiers(paramQualifiers);
						autowireRule.setRequired(required);
						beanRule.addAutowireTarget(autowireRule);
					} else if(method.isAnnotationPresent(Required.class)) {
						BeanRuleAnalyzer.checkRequiredProperty(beanRule, method);
					} else if(method.isAnnotationPresent(Initialize.class)) {
						if(!beanRule.isInitializableBean() && !beanRule.isInitializableTransletBean() && beanRule.getInitMethod() != null) {
							beanRule.setInitMethod(method);
							beanRule.setInitMethodRequiresTranslet(MethodActionRule.isRequiresTranslet(method));
						}
					} else if(method.isAnnotationPresent(Destroy.class)) {
						if(!beanRule.isDisposableBean() && beanRule.getDestroyMethod() != null) {
							beanRule.setDestroyMethod(method);
						}
					}
				}

				beanClass = beanClass.getSuperclass();
			}
		} catch(Throwable ex) {
			throw new IllegalStateException("Failed to introspect annotations on " + beanClass, ex);
		}
	}

	private void parseBeanRule(Class<?> beanClass, Method method, String[] nameArray) {
		try {
			Bean beanAnno = method.getAnnotation(Bean.class);
			String beanId = applyNamespaceForBean(nameArray, StringUtils.emptyToNull(beanAnno.id()));
			String initMethodName = StringUtils.emptyToNull(beanAnno.initMethod());
			String destroyMethodName = StringUtils.emptyToNull(beanAnno.destroyMethod());
			String factoryMethodName = StringUtils.emptyToNull(beanAnno.factoryMethod());

			BeanRule beanRule = new BeanRule();
			beanRule.setId(beanId);
			beanRule.setOfferBeanId(BeanRule.CLASS_DIRECTIVE_PREFIX + beanClass.getName());
			beanRule.setOfferBeanClass(beanClass);
			beanRule.setOfferMethodName(method.getName());
			beanRule.setOfferMethod(method);
			beanRule.setOffered(true);
			beanRule.setInitMethodName(initMethodName);
			beanRule.setDestroyMethodName(destroyMethodName);
			beanRule.setFactoryMethodName(factoryMethodName);

			Class<?> targetBeanClass = BeanRuleAnalyzer.determineBeanClass(beanRule);
			relater.relay(targetBeanClass, beanRule);
		} catch(Throwable ex) {
			throw new IllegalStateException("Failed to introspect annotations on " + beanClass, ex);
		}
	}

	private void parseTransletRule(Class<?> beanClass, Method method, String[] nameArray) {
		try {
			Request requestAnno = method.getAnnotation(Request.class);
			String transletName = applyNamespaceForTranslet(nameArray, StringUtils.emptyToNull(requestAnno.translet()));
			RequestMethodType[] requestMethods = requestAnno.method();

			String actionId;
			Action actionAnno = method.getAnnotation(Action.class);
			if(actionAnno != null) {
				actionId = applyNamespaceForTranslet(nameArray, StringUtils.emptyToNull(actionAnno.id()));
			} else {
				actionId = null;
			}

			TransletRule transletRule = TransletRule.newInstance(transletName, requestMethods);

			if(method.isAnnotationPresent(Dispatch.class)) {
				Dispatch dispatchAnno = method.getAnnotation(Dispatch.class);
				String dispatchName = StringUtils.emptyToNull(dispatchAnno.name());
				String characterEncoding = StringUtils.emptyToNull(dispatchAnno.characterEncoding());
				DispatchResponseRule drr = DispatchResponseRule.newInstance(dispatchName, characterEncoding);
				transletRule.setResponseRule(ResponseRule.newInstance(drr));
			} else if(method.isAnnotationPresent(Transform.class)) {
				Transform transformAnno = method.getAnnotation(Transform.class);
				TransformType transformType = transformAnno.transformType();
				String contentType = StringUtils.emptyToNull(transformAnno.contentType());
				String templateId = StringUtils.emptyToNull(transformAnno.templateId());
				String characterEncoding = StringUtils.emptyToNull(transformAnno.characterEncoding());
				boolean pretty = transformAnno.pretty();
				TransformRule tr = TransformRule.newInstance(transformType, contentType, templateId, characterEncoding, null, pretty);
				transletRule.setResponseRule(ResponseRule.newInstance(tr));
			} else if(method.isAnnotationPresent(Forward.class)) {
				Forward forwardAnno = method.getAnnotation(Forward.class);
				String translet = StringUtils.emptyToNull(forwardAnno.translet());
				ForwardResponseRule frr = ForwardResponseRule.newInstance(translet);
				transletRule.setResponseRule(ResponseRule.newInstance(frr));
			} else if(method.isAnnotationPresent(Redirect.class)) {
				Redirect redirectAnno = method.getAnnotation(Redirect.class);
				String target = StringUtils.emptyToNull(redirectAnno.target());
				RedirectResponseRule rrr = RedirectResponseRule.newInstance(target);
				transletRule.setResponseRule(ResponseRule.newInstance(rrr));
			}

			MethodActionRule methodActionRule = new MethodActionRule();
			methodActionRule.setActionId(actionId);
			methodActionRule.setConfigBeanClass(beanClass);
			methodActionRule.setMethod(method);

			transletRule.applyActionRule(methodActionRule);
			relater.relay(transletRule);
		} catch(Throwable ex) {
			throw new IllegalStateException("Failed to introspect annotations on " + beanClass, ex);
		}
	}

	private String[] splitNamespace(String namespace) {
		if(StringUtils.isEmpty(namespace)) {
            return new String[1];
        }

        int cnt = StringUtils.search(namespace, ActivityContext.ID_SEPARATOR_CHAR);
        if(cnt == 0) {
            String[] arr = new String[2];
            arr[1] = namespace;
            return arr;
        }

        StringTokenizer st = new StringTokenizer(namespace, ActivityContext.ID_SEPARATOR);
        List<String> list = new ArrayList<String>();
        while(st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        list.add(null);
        Collections.reverse(list);

        return list.toArray(new String[list.size()]);
	}
	
	private String applyNamespaceForBean(String[] nameArray, String name) {
        nameArray[0] = name;
        StringBuilder sb = new StringBuilder();
        for(int i = nameArray.length - 1; i >= 0; i--) {
            sb.append(nameArray[i]);
            if(i > 0)
                sb.append(ActivityContext.ID_SEPARATOR_CHAR);
        }
        return sb.toString();
	}

	private String applyNamespaceForTranslet(String[] nameArray, String name) {
        nameArray[0] = name;
        StringBuilder sb = new StringBuilder();
        for(int i = nameArray.length - 1; i >= 0; i--) {
            sb.append(nameArray[i]);
            if(i > 0)
                sb.append(ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR);
        }
        return sb.toString();
	}
	
	private boolean checkExistence(Class<?> requiredType, String beanId, boolean required) {
		BeanRule[] beanRules = beanRuleRegistry.getBeanRule(requiredType);

		if(beanRules == null || beanRules.length == 0) {
			if(required)
				throw new RequiredTypeBeanNotFoundException(requiredType);
			else
				return false;
		}

		if(beanRules.length == 1)
			return true;
		
		if(beanId != null) {
			for(BeanRule beanRule : beanRules) {
				if(beanId.equals(beanRule.getId())) {
					return true;
				}
			}
		}
		if(required)
			throw new NoUniqueBeanException(requiredType, beanRules);
		else
			return false;
	}

}
