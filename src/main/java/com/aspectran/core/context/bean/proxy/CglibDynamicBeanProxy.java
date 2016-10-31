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
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.Method;
import java.util.List;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionRule;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * The Class CglibDynamicBeanProxy.
 */
public class CglibDynamicBeanProxy extends AbstractDynamicBeanProxy implements MethodInterceptor {

	private final ActivityContext context;

	private final BeanRule beanRule;

	public CglibDynamicBeanProxy(ActivityContext context, BeanRule beanRule) {
		super(context.getAspectRuleRegistry());

		this.context = context;
		this.beanRule = beanRule;
	}

	@Override
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		Activity activity = context.getCurrentActivity();

		String transletName = activity.getTransletName();
		String beanId = beanRule.getId();
		String className = beanRule.getClassName();
		String methodName = method.getName();

		AspectAdviceRuleRegistry aarr = retrieveAspectAdviceRuleRegistry(activity, transletName, beanId, className, methodName);

		if (aarr == null) {
			return methodProxy.invokeSuper(proxy, args);
		}

		try {
			try {
				if (aarr.getBeforeAdviceRuleList() != null) {
					activity.executeAdvice(aarr.getBeforeAdviceRuleList());
				}

				if (log.isDebugEnabled()) {
					log.debug("invoke a proxied method [" + method + "] within the bean " + beanRule);
				}

				Object result = methodProxy.invokeSuper(proxy, args);

				if (aarr.getAfterAdviceRuleList() != null) {
					activity.executeAdvice(aarr.getAfterAdviceRuleList());
				}

				return result;
			} finally {
				if (aarr.getFinallyAdviceRuleList() != null) {
					activity.executeAdviceWithoutThrow(aarr.getFinallyAdviceRuleList());
				}
			}
		} catch (Exception e) {
			activity.setRaisedException(e);

			List<ExceptionRule> exceptionRuleList = aarr.getExceptionRuleList();
			if (exceptionRuleList != null) {
				activity.handleException(exceptionRuleList);
				if (activity.isResponseReserved()) {
					return null;
				}
			}

			throw e;
		}
	}

	/**
	 * Creates a proxy class of bean and returns an instance of that class.
	 *
	 * @param context the activity context
	 * @param beanRule the bean rule
	 * @param constructorArgs the arguments passed to a constructor
	 * @param constructorArgTypes the parameter types for a constructor
	 * @return a new proxy bean object
	 */
	public static Object newInstance(ActivityContext context, BeanRule beanRule, Object[] constructorArgs, Class<?>[] constructorArgTypes) {
		Enhancer enhancer = new Enhancer();
		enhancer.setClassLoader(context.getClassLoader());
		enhancer.setSuperclass(beanRule.getBeanClass());
		enhancer.setCallback(new CglibDynamicBeanProxy(context, beanRule));
		return enhancer.create(constructorArgTypes, constructorArgs);
	}

}