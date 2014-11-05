/**
 * 
 */
package com.aspectran.core.context.bean.scope;

import com.aspectran.core.context.bean.BeanDestroyFailedException;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.var.rule.BeanRule;


/**
 *
 * @author Gulendol
 * @since 2011. 1. 7.
 *
 */
public class ScopedBean implements DisposableBean {

	private BeanRule beanRule;
	
	private Object bean;
	
	public ScopedBean(BeanRule beanRule) {
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
	
	public void destroy() {
		if(bean != null && beanRule.getDestroyMethodName() != null) {
			String destroyMethodName = beanRule.getDestroyMethodName();
	
			try {
				MethodUtils.invokeMethod(bean, destroyMethodName, null);
			} catch(Exception e) {
				throw new BeanDestroyFailedException(beanRule, e); 
			}
			
			bean = null;
		}
	}
	
}
