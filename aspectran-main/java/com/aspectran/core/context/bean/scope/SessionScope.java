/**
 * 
 */
package com.aspectran.core.context.bean.scope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author Gulendol
 * @since 2011. 3. 12.
 *
 */
public class SessionScope extends AbstractScope implements Scope {

	public static final String SESSION_SCOPE_ATTRIBUTE = SessionScope.class.getName();
	
	private final Log log = LogFactory.getLog(SessionScope.class);
	
	public void destroy() {
		if(log.isDebugEnabled())
			log.debug("destroy session-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}
}
