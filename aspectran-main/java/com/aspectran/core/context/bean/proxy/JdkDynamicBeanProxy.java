/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.aspectran.core.context.bean.LocalBeanRegistry;
import com.aspectran.core.var.rule.BeanRule;

/**
 * @author aspectran
 * 
 */
public class JdkDynamicBeanProxy implements InvocationHandler {

	private LocalBeanRegistry beanRegistry;

	private BeanRule beanRule;
	
	protected JdkDynamicBeanProxy(LocalBeanRegistry beanRegistry, BeanRule beanRule) {
		this.beanRegistry = beanRegistry;
		this.beanRule = beanRule;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			System.out.print("begin method " + method.getName() + "(");

			for(int i = 0; i < args.length; i++) {
				if(i > 0)
					System.out.print(",");
				System.out.print(" " + args[i].toString());
			}
			
			System.out.println(" )");
			
			return method.invoke(proxy, args);
		} catch(InvocationTargetException e) {
			throw e.getTargetException();
		} catch(Exception e) {
			throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
		} finally {
			System.out.println("end method " + method.getName());
		}
	}
	
	public static Object newInstance(LocalBeanRegistry beanRegistry, BeanRule beanRule, Object obj) {
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new JdkDynamicBeanProxy(beanRegistry, beanRule));
	}
}