/**
 * 
 */
package com.aspectran.core.context.bean.scope;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 *
 * @author Gulendol
 * @since 2011. 3. 12.
 *
 */
public class RequestScope extends AbstractScope implements Scope {
	
	private final Log log = LogFactory.getLog(RequestScope.class);
	
	public void destroy() {
		if(log.isDebugEnabled())
			log.debug("destroy request-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}
}
