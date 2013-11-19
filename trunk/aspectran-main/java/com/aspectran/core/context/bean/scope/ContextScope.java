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
public class ContextScope extends AbstractScope implements Scope {

	private final Logger logger = LoggerFactory.getLogger(ContextScope.class);
	
	public void destroy() {
		if(logger.isDebugEnabled())
			logger.debug("destroy context-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}

}
