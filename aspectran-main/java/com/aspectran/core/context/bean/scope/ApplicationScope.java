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
public class ApplicationScope extends AbstractScope implements Scope {
	
	public static final String APPLICATION_SCOPE_ATTRIBUTE = ApplicationScope.class.getName();

	private final Logger logger = LoggerFactory.getLogger(ApplicationScope.class);

	public void destroy() {
		if(logger.isDebugEnabled())
			logger.debug("destroy application-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}
	
}
