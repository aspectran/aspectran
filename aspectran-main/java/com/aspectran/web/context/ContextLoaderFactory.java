package com.aspectran.web.context;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Gulendol
 * @since 2011. 1. 30.
 *
 */
public class ContextLoaderFactory {

	private static final Log log = LogFactory.getLog(ContextLoaderFactory.class);
	
	public static ContextLoader getContextLoader(ServletContext servletContext) {
		log.info("DirectContextLoader");
		ContextLoader contextLoader = new DirectContextLoader(servletContext);
		
		return contextLoader;
	}
	
	public static ContextLoader getContextLoader(ServletConfig servletConfig) {
		ServletContext servletContext = servletConfig.getServletContext();
		
		ContextManager contextManager = (ContextManager)servletContext.getAttribute(ContextManager.CONTEXT_MANAGER_ATTRIBUTE);
		log.info("ContextManager: " + contextManager);
		
		ContextLoader contextLoader = null;
		
		if(contextManager == null) {
			log.info("DirectContextLoader");
			contextLoader = new DirectContextLoader(servletConfig);
		} else {
			log.info("ReferContextLoader: " + contextManager);
			contextLoader = new ReferContextLoader(servletConfig, contextManager);
		}
		
		return contextLoader;
	}
	
}
