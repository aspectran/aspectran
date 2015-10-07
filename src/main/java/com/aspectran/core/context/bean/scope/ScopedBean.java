/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.bean.scope;

import com.aspectran.core.context.bean.BeanDestroyFailedException;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.util.MethodUtils;

/**
 * The Class ScopedBean.
 *
 * @author Juho Jeong
 * @since 2011. 1. 7.
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
				MethodUtils.invokeMethod(bean, destroyMethodName, null, null);
			} catch(Exception e) {
				throw new BeanDestroyFailedException(beanRule, e); 
			}
			
			bean = null;
		}
	}
	
}
