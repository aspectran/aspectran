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

import java.util.HashMap;
import java.util.Iterator;

/**
 * The Class ScopedBeanMap.
 *
 * @author Juho Jeong
 * @since 2011. 1. 7.
 */
public class ScopedBeanMap extends HashMap<String, ScopedBean> implements Iterable<ScopedBean> {
	
	/** @serial */
	static final long serialVersionUID = -3559362779320716165L;

	/**
	 * Adds a instantiated bean.
	 *
	 * @param scopedBean the instantiated bean
	 * @return the instantiated bean
	 */
	public ScopedBean putScopeBean(ScopedBean scopedBean) {
		return put(scopedBean.getBeanRule().getId(), scopedBean);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<ScopedBean> iterator() {
		return this.values().iterator();
	}

}
