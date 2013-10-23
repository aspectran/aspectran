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
public class ApplicationScope extends AbstractScope implements Scope {
	
	public static final String APPLICATION_SCOPE_ATTRIBUTE = ApplicationScope.class.getName();

	private final Log log = LogFactory.getLog(ApplicationScope.class);

	public void destroy() {
		if(log.isDebugEnabled())
			log.debug("destroy application-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}
	
}
