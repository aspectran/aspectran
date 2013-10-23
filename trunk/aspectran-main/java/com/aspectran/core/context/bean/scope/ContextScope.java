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
public class ContextScope extends AbstractScope implements Scope {

	private final Log log = LogFactory.getLog(ContextScope.class);
	
	public void destroy() {
		if(log.isDebugEnabled())
			log.debug("destroy context-scoped beans " + scopedBeanMap);
		
		super.destroy();
	}

}
