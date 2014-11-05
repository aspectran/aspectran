/**
 * 
 */
package com.aspectran.core.context.bean.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gulendol
 * @since 2011. 3. 12.
 *
 */
public class RequestScope extends AbstractScope implements Scope {
	
	private final Logger logger = LoggerFactory.getLogger(RequestScope.class);
	
	public void destroy() {
		if(logger.isDebugEnabled())
			logger.debug("destroy request-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}
}
