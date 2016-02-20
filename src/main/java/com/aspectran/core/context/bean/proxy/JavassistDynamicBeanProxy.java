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

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * The Class JavassistDynamicBeanProxy.
 *
 * @author Juho Jeong
 * @since 1.1.0
 */
public class JavassistDynamicBeanProxy extends AbstractDynamicBeanProxy implements MethodHandler  {

	protected JavassistDynamicBeanProxy(ActivityContext context, BeanRule beanRule) {
		super(context, beanRule);
	}

	@Override
	public Object invoke(final Object self, Method overridden, final Method proceed, final Object[] args) throws Throwable {
		ProxyMethodInvoker proxyMethodInvoker = new ProxyMethodInvoker() {
			@Override
			public Object invoke() throws Throwable {
				// execute the original method.
				return proceed.invoke(self, args);
			}
		};
		
		return dynamicInvoke(self, overridden, args, proxyMethodInvoker);
	}
	
	/**
	 * Creates a proxy class of bean and returns an instance of that class.
	 *
	 * @param context the activity context
	 * @param beanRule the bean rule
	 * @param constructorArgTypes the parameter types for a constructor
	 * @param constructorArgs the arguments passed to a constructor
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

//	/**
//	 * Creates a proxy class of bean and returns an instance of that class.
//	 *
//	 * @param context the activity context
//	 * @param beanRule the bean rule
//	 * @param constructorArgTypes the parameter types for a constructor
//	 * @param constructorArgs the arguments passed to a constructor
//	 * @return a new proxy bean object
//	 */
//	public static Object newInstance(ActivityContext context, BeanRule beanRule, Object bean) {
//		try {
//			ProxyFactory proxyFactory = new ProxyFactory();
//			MethodHandler methodHandler = new JavassistDynamicBeanProxy(context, beanRule);
//			return proxyFactory.create(constructorArgTypes, constructorArgs, methodHandler);
//		} catch(Exception e) {
//			throw new ProxyBeanInstantiationException(beanRule.getBeanClass(), e);
//		}
//	}

}