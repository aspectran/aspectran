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
public class SessionScope extends AbstractScope implements Scope {

	public static final String SESSION_SCOPE_ATTRIBUTE = SessionScope.class.getName();
	
	private final Logger logger = LoggerFactory.getLogger(SessionScope.class);
	
	public void destroy() {
		if(logger.isDebugEnabled())
			logger.debug("destroy session-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}
}
