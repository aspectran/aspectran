/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;

/**
 * @author aspectran
 * 
 */
public class JdkDynamicBeanProxy extends AbstractDynamicBeanProxy implements InvocationHandler {

	protected JdkDynamicBeanProxy(CoreActivity activity, List<AspectRule> aspectRuleList, BeanRule beanRule) {
		super(activity, aspectRuleList, beanRule);
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return dynamicInvoke(proxy, method, args, null);
	}
	
	public static Object newInstance(CoreActivity activity, List<AspectRule> aspectRuleList, BeanRule beanRule, Object obj) {
		JdkDynamicBeanProxy proxy = new JdkDynamicBeanProxy(activity, aspectRuleList, beanRule);
		
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), proxy);
	}
}