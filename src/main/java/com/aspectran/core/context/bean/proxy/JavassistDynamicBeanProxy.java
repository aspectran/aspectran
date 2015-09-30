/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import javassist.util.proxy.ProxyObject;

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
	
	/* (non-Javadoc)
	 * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(final Object self, Method overridden, final Method proceed, final Object[] args) throws Throwable {
		ProxyMethodInvoker proxyMethodInvoker = new ProxyMethodInvoker() {
			public Object invoke() throws Throwable {
				// execute the original method.
				return proceed.invoke(self, args);
			}
		};
		
		return dynamicInvoke(self, overridden, args, proxyMethodInvoker);
	}
	
	public static Object newInstance(ActivityContext context, BeanRule beanRule, Class<?>[] constructorArgTypes, Object[] constructorArgs) {
		try {
			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setSuperclass(beanRule.getBeanClass());
			Class<?> proxyClass = proxyFactory.createClass();
			Object proxy;
			if(constructorArgs == null)
				proxy = proxyClass.newInstance();
			else
				proxy = proxyFactory.create(constructorArgTypes, constructorArgs);
			((ProxyObject)proxy).setHandler(new JavassistDynamicBeanProxy(context, beanRule));
			
			return proxy;
		} catch(Exception e) {
			throw new ProxyBeanInstantiationException(beanRule.getBeanClass(), e);
		}
	}
}