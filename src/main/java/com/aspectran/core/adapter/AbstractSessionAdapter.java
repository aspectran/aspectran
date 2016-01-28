/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.adapter;

import java.util.Enumeration;

/**
 * The Abstract Class for session object adapter.
 *
 * @since 2011. 3. 13.
 */
public abstract class AbstractSessionAdapter implements SessionAdapter {
	
	protected Object adaptee;
	
	/**
	 * Instantiates a new AbstractSessionAdapter.
	 *
	 * @param adaptee the adaptee
	 */
	public AbstractSessionAdapter(Object adaptee) {
		this.adaptee = adaptee;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.SessionAdapter#getAdaptee()
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAdaptee() {
		return (T)adaptee;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.SessionAdapter#getId()
	 */
	abstract public String getId();

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.SessionAdapter#getCreationTime()
	 */
	abstract public long getCreationTime();
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.SessionAdapter#getLastAccessedTime()
	 */
	abstract public long getLastAccessedTime();
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.SessionAdapter#getMaxInactiveInterval()
	 */
	abstract public int getMaxInactiveInterval();
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.SessionAdapter#release()
	 */
	public void release() {
		adaptee = null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(adaptee == null) {
			return super.toString();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("{id=").append(getId());
		sb.append(", creationTime=").append(getCreationTime());
		sb.append(", lastAccessedTime=").append(getLastAccessedTime());
		sb.append(", maxInactiveInterval=").append(getMaxInactiveInterval());
		sb.append(", attributeNames=[");
		for(Enumeration<String> en = getAttributeNames(); en.hasMoreElements(); ) {
			sb.append(en.nextElement());
			if(en.hasMoreElements())
				sb.append(",");
		}
		sb.append("]}");
		
		return sb.toString();
	}
	
}
