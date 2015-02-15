/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;

/**
 * @author aspectran
 * 
 */
public class CglibDynamicBeanProxy extends AbstractDynamicBeanProxy implements MethodInterceptor {

	protected CglibDynamicBeanProxy(Activity activity, List<AspectRule> aspectRuleList, BeanRule beanRule) {
		super(activity, aspectRuleList, beanRule);
	}

	public Object intercept(final Object object, final Method method, final Object[] args, final MethodProxy methodProxy) throws Throwable {
		ProxyMethodInvoker proxyMethodInvoker = new ProxyMethodInvoker() {

			public Object invoke() throws Throwable {
				return methodProxy.invokeSuper(object, args);
			}
			
		};
		
		return dynamicInvoke(object, method, args, proxyMethodInvoker);
	}
	
	public static Object newInstance(Activity activity, List<AspectRule> aspectRuleList, BeanRule beanRule, Class<?>[] constructorArgTypes, Object[] constructorArgs) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(beanRule.getBeanClass());
		enhancer.setCallback(new CglibDynamicBeanProxy(activity, aspectRuleList, beanRule));
		Object obj;
		
		if(constructorArgs == null)
			obj = enhancer.create();
		else
			obj = enhancer.create(constructorArgTypes, constructorArgs);
		
		return obj;
	}
}