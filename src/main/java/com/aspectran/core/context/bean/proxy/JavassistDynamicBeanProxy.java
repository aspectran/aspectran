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

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * The Class JavassistDynamicBeanProxy.
 *
 * @author Juho Jeong
 * @since 1.1.0
 */
public class JavassistDynamicBeanProxy extends AbstractDynamicBeanProxy implements MethodHandler  {

	private final ActivityContext context;

	private final BeanRule beanRule;

	public JavassistDynamicBeanProxy(ActivityContext context, BeanRule beanRule) {
		super(context.getAspectRuleRegistry());

		this.context = context;
		this.beanRule = beanRule;
	}

	@Override
	public Object invoke(Object self, Method overridden, Method proceed, Object[] args) throws Throwable {
		Activity activity = context.getCurrentActivity();

		String transletName = activity.getTransletName();
		String beanId = beanRule.getId();
		String className = beanRule.getClassName();
		String methodName = overridden.getName();

		AspectAdviceRuleRegistry aarr = retrieveAspectAdviceRuleRegistry(activity, transletName, beanId, className, methodName);

		if(aarr == null) {
			return proceed.invoke(self, args);
		}

		try {
			try {
				if(log.isTraceEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append("begin method ").append(methodName).append("(");
					for(int i = 0; i < args.length; i++) {
						if(i > 0)
							sb.append(", ");
						sb.append(args[i].toString());
					}
					sb.append(")");
					log.trace(sb.toString());
				}

				if(aarr.getBeforeAdviceRuleList() != null)
					activity.execute(aarr.getBeforeAdviceRuleList());

				Object result;

				if(!activity.isActivityEnded()) {
					result = proceed.invoke(self, args);
				} else {
					result = null;
				}

				if(aarr.getAfterAdviceRuleList() != null)
					activity.execute(aarr.getAfterAdviceRuleList());

				return result;
			} finally {
				if(aarr.getFinallyAdviceRuleList() != null)
					activity.forceExecute(aarr.getFinallyAdviceRuleList());

				if(log.isTraceEnabled()) {
					log.trace("end method " + methodName);
				}
			}
		} catch(Exception e) {
			activity.setRaisedException(e);

			List<ExceptionRule> exceptionRuleList = aarr.getExceptionRuleList();
			if(exceptionRuleList != null) {
				activity.responseByContentType(exceptionRuleList);
				if(activity.isActivityEnded()) {
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
		try {
			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setSuperclass(beanRule.getBeanClass());
			MethodHandler methodHandler = new JavassistDynamicBeanProxy(context, beanRule);
			return proxyFactory.create(constructorArgTypes, constructorArgs, methodHandler);
		} catch(Exception e) {
			throw new ProxyBeanInstantiationException(beanRule.getBeanClass(), e);
		}
	}

}