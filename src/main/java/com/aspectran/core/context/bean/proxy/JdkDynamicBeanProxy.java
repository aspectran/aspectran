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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ExceptionHandlingRule;

/**
 * The Class JdkDynamicBeanProxy.
 *
 * @author Juho Jeong
 */
public class JdkDynamicBeanProxy extends AbstractDynamicBeanProxy implements InvocationHandler {

	private final ActivityContext context;

	private final BeanRule beanRule;

	private final Object bean;
	
	protected JdkDynamicBeanProxy(ActivityContext context, BeanRule beanRule, Object bean) {
		super(context.getAspectRuleRegistry());

		this.context = context;
		this.beanRule = beanRule;
		this.bean = bean;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Activity activity = context.getCurrentActivity();

		String transletName = activity.getTransletName();
		String beanId = beanRule.getId();
		String className = beanRule.getClassName();
		String methodName = method.getName();

		AspectAdviceRuleRegistry aarr = retrieveAspectAdviceRuleRegistry(activity, transletName, beanId, className, methodName);

		if(aarr == null) {
			return method.invoke(bean, args);
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
					result = method.invoke(bean, args);
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

			List<ExceptionHandlingRule> exceptionHandlingRuleList = aarr.getExceptionHandlingRuleList();
			if(exceptionHandlingRuleList != null) {
				activity.responseByContentType(exceptionHandlingRuleList);
				if(activity.isActivityEnded()) {
					return null;
				}
			}

			throw e;
		}
	}
	
	public static Object newInstance(ActivityContext context, BeanRule beanRule, Object bean) {
		JdkDynamicBeanProxy proxy = new JdkDynamicBeanProxy(context, beanRule, bean);
		return Proxy.newProxyInstance(beanRule.getBeanClass().getClassLoader(), beanRule.getBeanClass().getInterfaces(), proxy);
	}
	
}