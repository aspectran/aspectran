/**
 * 
 */
package com.aspectran.core.context.bean.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author aspectran
 * 
 */
public class DynamicBeanProxy implements InvocationHandler {

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
}