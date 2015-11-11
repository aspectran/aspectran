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
package com.aspectran.core.context.bean.scope;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class SessionScope.
 *
 * @author Juho Jeong
 * @since 2011. 3. 12.
 */
public class SessionScope extends AbstractScope implements Scope {

	private final Log log = LogFactory.getLog(SessionScope.class);
	
	public SessionScope() {
	}
	
	public void destroy() {
		if(log.isDebugEnabled())
			log.debug("destroy session-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}
	
}
