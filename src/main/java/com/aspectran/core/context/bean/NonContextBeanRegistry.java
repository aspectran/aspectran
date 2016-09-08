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
package com.aspectran.core.context.bean;

import java.util.concurrent.locks.StampedLock;

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;

/**
 * The Class NonContextBeanRegistry.
 * 
 * <p>Created: 2016. 9. 8.</p>
 */
public class NonContextBeanRegistry extends AbstractBeanRegistry {

	private final StampedLock singletonScopeLock = new StampedLock();

	public NonContextBeanRegistry(BeanRuleRegistry beanRuleRegistry) {
		super(beanRuleRegistry, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getBean(BeanRule beanRule) {
		if(beanRule.getScopeType() == ScopeType.PROTOTYPE) {
			return (T)createBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.SINGLETON) {
			return (T)getSingletonScopeBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.REQUEST) {
			throw new UnsupportedOperationException();
		} else if(beanRule.getScopeType() == ScopeType.SESSION) {
			throw new UnsupportedOperationException();
		} else if(beanRule.getScopeType() == ScopeType.APPLICATION) {
			throw new UnsupportedOperationException();
		}
		
		throw new BeanException();
	}
	
	private Object getSingletonScopeBean(BeanRule beanRule) {
		long stamp = singletonScopeLock.readLock();

		try {
			if(beanRule.isRegistered())
				return beanRule.getBean();

			long tryStamp = singletonScopeLock.tryConvertToWriteLock(stamp);

			if(tryStamp != 0L) {
				stamp = tryStamp;
			} else {
				singletonScopeLock.unlockRead(stamp);
				stamp = singletonScopeLock.writeLock();
			}

			Object bean = createBean(beanRule);

			beanRule.setBean(bean);
			beanRule.setRegistered(true);

			return bean;
		} finally {
			singletonScopeLock.unlock(stamp);
		}
	}
	
}
