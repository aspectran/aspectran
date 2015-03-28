/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;

/**
 * @author aspectran
 * 
 */
public class JdkDynamicBeanProxy extends AbstractDynamicBeanProxy implements InvocationHandler {

	private Object bean;
	
	protected JdkDynamicBeanProxy(ActivityContext context, BeanRule beanRule, Object bean) {
		super(context, beanRule);
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return dynamicInvoke(bean, method, args, null);
	}
	
	public static Object newInstance(ActivityContext context, BeanRule beanRule, Object bean) {
		JdkDynamicBeanProxy proxy = new JdkDynamicBeanProxy(context, beanRule, bean);
		
		return Proxy.newProxyInstance(beanRule.getBeanClass().getClassLoader(), beanRule.getBeanClass().getInterfaces(), proxy);
	}
}