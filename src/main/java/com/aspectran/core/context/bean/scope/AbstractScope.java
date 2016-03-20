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
package com.aspectran.core.context.bean.scope;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class AbstractScope.
 *
 * @author Juho Jeong
 * @since 2011. 3. 12.
 */
public class AbstractScope implements Scope {

	private static final Log log = LogFactory.getLog(AbstractScope.class);

	private StampedLock scopeLock = new StampedLock();

	protected final Map<BeanRule, Object> scopedBeanMap = new HashMap<BeanRule, Object>();
	
	private final ScopeType scopeType;
	
	public AbstractScope(ScopeType scopeType) {
		this.scopeType = scopeType;
	}

	@Override
	public StampedLock getScopeStampedLock() {
		return scopeLock;
	}

	@Override
	public Object getBean(BeanRule beanRule) {
		return scopedBeanMap.get(beanRule);
	}

	@Override
	public void putBean(BeanRule beanRule, Object bean) {
		scopedBeanMap.put(beanRule, bean);
	}

	@Override
	public void destroy() {
		if(log.isDebugEnabled()) {
			if(scopedBeanMap.size() > 0)
				log.debug("Destroy scoped beans in the " + this);
		}

		for(Map.Entry<BeanRule, Object> entry : scopedBeanMap.entrySet()) {
			BeanRule beanRule = entry.getKey();
			Object bean = entry.getValue();
			doDestroy(beanRule, bean);
		}

		scopedBeanMap.clear();
	}

	private void doDestroy(BeanRule beanRule, Object bean) {
		if(bean != null) {
			try {
				if(beanRule.isDisposableBean()) {
					((DisposableBean)bean).destroy();
				} else if(beanRule.getDestroyMethodName() != null) {
					Method destroyMethod = beanRule.getDestroyMethod();
					destroyMethod.invoke(bean, MethodUtils.EMPTY_OBJECT_ARRAY);
				}
			} catch(Exception e) {
				log.error("Cannot destroy " + scopeType + " scoped bean " + beanRule, e);
			}
		}
	}
	
}
