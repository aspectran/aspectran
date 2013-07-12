/**
 * 
 */
package com.aspectran.core.bean;

import java.lang.reflect.InvocationTargetException;

import com.aspectran.base.rule.BeanRule;
import com.aspectran.base.util.MethodUtils;
import com.aspectran.core.bean.ablility.Disposable;


/**
 *
 * @author Gulendol
 * @since 2011. 1. 7.
 *
 */
public class ScopeBean implements Disposable {

	private BeanRule beanRule;
	
	private Object bean;
	
	public ScopeBean(BeanRule beanRule) {
		this.beanRule = beanRule;
	}
	
	public BeanRule getBeanRule() {
		return beanRule;
	}

	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
	}
	
	public void destroy() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		if(bean == null)
			return;
		
		String destroyMethodName = beanRule.getDestroyMethod();
		
		if(destroyMethodName != null)
			MethodUtils.invokeMethod(bean, destroyMethodName, null);

	}
	
}
