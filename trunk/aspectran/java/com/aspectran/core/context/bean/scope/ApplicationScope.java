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
public class ApplicationScope extends AbstractScope implements Scope {
	
	private final Log log = LogFactory.getLog(ApplicationScope.class);

	public void destroy() {
		if(log.isDebugEnabled())
			log.debug("destroy application-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}
	
}
