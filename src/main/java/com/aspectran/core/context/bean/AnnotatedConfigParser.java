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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.aspectran.core.context.bean.annotation.Autowired;
import com.aspectran.core.context.bean.annotation.Bean;
import com.aspectran.core.context.bean.annotation.Configuration;
import com.aspectran.core.context.bean.annotation.Dispatch;
import com.aspectran.core.context.bean.annotation.Forward;
import com.aspectran.core.context.bean.annotation.Redirect;
import com.aspectran.core.context.bean.annotation.Request;
import com.aspectran.core.context.bean.annotation.Transform;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.MethodActionRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TransletRuleMap;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.StringUtils;

/**
 * The Class AnnotatedConfigParser.
 *
 * <p>Created: 2016. 2. 16.</p>
 *
 * @since 2.0.0
 * @author Juho Jeong
 */
public abstract class AnnotatedConfigParser {

	public static void parse(BeanRule beanRule, TransletRuleMap transletRuleMap) {
		Class<?> beanClass = beanRule.getBeanClass();

		Configuration configAnno = beanClass.getAnnotation(Configuration.class);
		String namespace = (configAnno != null) ? StringUtils.emptyToNull(configAnno.namespace()) : null;

		for(Method method : beanClass.getMethods()) {
			if(configAnno != null) {
				if(method.isAnnotationPresent(Bean.class)) {
					parseBean(beanClass, method, namespace);
				} else if(method.isAnnotationPresent(Request.class)) {
					parseTranslet(beanClass, method, namespace);
				} else if(method.isAnnotationPresent(Autowired.class)) {
					parseAutowire(beanClass, method, namespace);
				}
			} else {
				if(method.isAnnotationPresent(Autowired.class)) {
					parseAutowire(beanClass, method, namespace);
				}
			}
		}

		for(Field field : beanClass.getFields()) {
			if(field.isAnnotationPresent(Autowired.class)) {
				parseAutowire(beanClass, field, namespace);
			}
		}
	}

	private static void parseAutowire(Class<?> beanClass, Field field, String namespace) {
		Autowired autowiredAnno = beanClass.getAnnotation(Autowired.class);
		boolean required = autowiredAnno.required();

	}

	private static void parseAutowire(Class<?> beanClass, Method method, String namespace) {

	}

	private static void parseBean(Class<?> beanClass, Method method, String namespace) {
		Bean beanAnno = method.getAnnotation(Bean.class);
		String beanId = StringUtils.emptyToNull(beanAnno.id());
		String initMethodName = StringUtils.emptyToNull(beanAnno.initMethod());
		String destroyMethodName = StringUtils.emptyToNull(beanAnno.destroyMethod());
		String factoryMethodName = StringUtils.emptyToNull(beanAnno.factoryMethod());

		BeanRule beanRule = new BeanRule();
		beanRule.setId(beanId);
		beanRule.setOfferBeanId(BeanRule.CLASS_DIRECTIVE_PREFIX + beanClass.getName());
		beanRule.setOfferBeanClass(beanClass);
		beanRule.setOfferMethodName(method.getName());
		beanRule.setOfferMethod(method);;
		beanRule.setOffered(true);
		beanRule.setInitMethodName(initMethodName);
		beanRule.setDestroyMethodName(destroyMethodName);
		beanRule.setFactoryMethodName(factoryMethodName);

		BeanRuleRegistry.determineBeanClass(beanRule);

	}

	private static void parseTranslet(Class<?> beanClass, Method method, String namespace) {
		Request requestAnno = method.getAnnotation(Request.class);
		String transletName = StringUtils.emptyToNull(requestAnno.translet());
		RequestMethodType[] requestMethods = requestAnno.method();

		if(namespace != null && transletName != null)
			transletName = namespace + transletName;

		TransletRule transletRule = TransletRule.newInstance(transletName, requestMethods);

		if(method.isAnnotationPresent(Dispatch.class)) {
			Dispatch dispatchAnno = method.getAnnotation(Dispatch.class);
			String dispatchName = StringUtils.emptyToNull(dispatchAnno.name());
			String characterEncoding = StringUtils.emptyToNull(dispatchAnno.characterEncoding());
			DispatchResponseRule drr = DispatchResponseRule.newInstance(dispatchName, characterEncoding);
			transletRule.setResponseRule(ResponseRule.newInstance(drr));
		} else if(method.isAnnotationPresent(Transform.class)) {
			Transform transformAnno = method.getAnnotation(Transform.class);
			String transformType = StringUtils.emptyToNull(transformAnno.transformType());
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
		methodActionRule.setConfigBeanClass(beanClass);
		methodActionRule.setMethod(method);
		
		//transletRule.applyActionRule(beanActionRule);

		//transletRuleMap.putTransletRule(transletRule);
	}

	private static Constructor<?> getMatchConstructor(Class<?> clazz, Object[] args) {
		Constructor<?>[] candidates = clazz.getDeclaredConstructors();
		Constructor<?> constructorToUse = null;
		float bestMatchWeight = Float.MAX_VALUE;
		float matchWeight = Float.MAX_VALUE;

		for(Constructor<?> candidate : candidates) {
			if(candidate.isAnnotationPresent(Autowired.class)) {
				matchWeight = ClassUtils.getTypeDifferenceWeight(candidate.getParameterTypes(), args);

				if(matchWeight < bestMatchWeight) {
					constructorToUse = candidate;
					bestMatchWeight = matchWeight;
				}
			}
		}

		return constructorToUse;
	}
	
}
