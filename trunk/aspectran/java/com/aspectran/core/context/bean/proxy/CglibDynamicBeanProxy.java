/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;

/**
 * @author aspectran
 * 
 */
public class CglibDynamicBeanProxy extends AbstractDynamicBeanProxy implements MethodInterceptor {

	protected CglibDynamicBeanProxy(ActivityContext context, BeanRule beanRule) {
		super(context, beanRule);
	}

	public Object intercept(final Object proxy, final Method method, final Object[] args, final MethodProxy methodProxy) throws Throwable {
		ProxyMethodInvoker proxyMethodInvoker = new ProxyMethodInvoker() {
			public Object invoke() throws Throwable {
				return methodProxy.invokeSuper(proxy, args);
			}
		};
		
		return dynamicInvoke(proxy, method, args, proxyMethodInvoker);
	}
	
	public static Object newInstance(ActivityContext context, BeanRule beanRule, Class<?>[] constructorArgTypes, Object[] constructorArgs) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(beanRule.getBeanClass());
		enhancer.setCallback(new CglibDynamicBeanProxy(context, beanRule));
		Object obj;
		
		if(constructorArgs == null)
			obj = enhancer.create();
		else
			obj = enhancer.create(constructorArgTypes, constructorArgs);
		
		return obj;
	}
}