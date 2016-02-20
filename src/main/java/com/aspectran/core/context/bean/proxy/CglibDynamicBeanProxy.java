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

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * The Class CglibDynamicBeanProxy.
 *
 * @author Juho Jeong
 */
public class CglibDynamicBeanProxy extends AbstractDynamicBeanProxy implements MethodInterceptor {

	protected CglibDynamicBeanProxy(ActivityContext context, BeanRule beanRule) {
		super(context, beanRule);
	}

	@Override
	public Object intercept(final Object proxy, final Method method, final Object[] args, final MethodProxy methodProxy) throws Throwable {
		ProxyMethodInvoker proxyMethodInvoker = new ProxyMethodInvoker() {
			@Override
			public Object invoke() throws Throwable {
				// execute the original method.
				return methodProxy.invokeSuper(proxy, args);
			}
		};

		return dynamicInvoke(proxy, method, args, proxyMethodInvoker);
	}

	public static Object newInstance(ActivityContext context, BeanRule beanRule, Object[] constructorArgs, Class<?>[] constructorArgTypes) {
		Enhancer enhancer = new Enhancer();
		enhancer.setClassLoader(context.getClassLoader());
		enhancer.setSuperclass(beanRule.getBeanClass());
		enhancer.setCallback(new CglibDynamicBeanProxy(context, beanRule));
		Object proxy;
		if(constructorArgs == null) {
			proxy = enhancer.create();
		} else {
			proxy = enhancer.create(constructorArgTypes, constructorArgs);
		}
		return proxy;
	}

	public static Object newInstance(ActivityContext context, BeanRule beanRule, Object bean) {
		Enhancer enhancer = new Enhancer();
		enhancer.setClassLoader(context.getClassLoader());
		enhancer.setCallback(new CglibDynamicBeanProxy(context, beanRule));
		Object proxy;
			proxy = enhancer.create();
		return proxy;

	}

}