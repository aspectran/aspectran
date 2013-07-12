package com.aspectran.web.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.base.context.ActivityContext;
import com.aspectran.base.context.builder.ContextMerger;
import com.aspectran.base.type.ContextMergeMode;

public class ContextManager {

	private static final Log log = LogFactory.getLog(ContextManager.class);
	
	public static final String CONTEXT_MANAGER_ATTRIBUTE = ContextManager.class.getName() + ".MANAGER";
	
	private Map<String, ActivityContext> contextMap;
	
	private ContextMergeMode defaultMergeMode;
	
	protected ContextManager(ServletContext servletContext) {
		if(servletContext.getAttribute(CONTEXT_MANAGER_ATTRIBUTE) != null) {
			throw new IllegalStateException(
					"Cannot initialize context because there is already a root context present - " +
					"check whether you have multiple ContextLoader* definitions in your web.xml!");
		}
		
		servletContext.log("Initializing trasnlets context manager");
		
		if(log.isInfoEnabled()) {
			log.info("Trasnlets context manager: initialization started");
		}
		
		long startTime = System.currentTimeMillis();

		try {
			ContextLoader contextLoader = new DirectContextLoader(servletContext);
			
			defaultMergeMode = contextLoader.getMergeMode();
			
			List<ActivityContext> contextList = contextLoader.getContextList();
			
			if(contextList != null) {
				contextMap = new HashMap<String, ActivityContext>();
				
				for(ActivityContext context : contextList) {
					log.debug("context: " + context);
					contextMap.put(context.getId(), context);
				}
			}

			servletContext.setAttribute(CONTEXT_MANAGER_ATTRIBUTE, this);

			if(log.isDebugEnabled()) {
				log.debug("Published root TransletsContext as ServletContext attribute with name [" +
						CONTEXT_MANAGER_ATTRIBUTE + "]");
			}
			
			if(log.isInfoEnabled()) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				log.info("Root TransletsContext: initialization completed in " + elapsedTime + " ms");
			}
		} catch(RuntimeException ex) {
			log.error("Context initialization failed", ex);
			servletContext.setAttribute(CONTEXT_MANAGER_ATTRIBUTE, ex);
			throw ex;
		} catch(Error err) {
			log.error("Context initialization failed", err);
			servletContext.setAttribute(CONTEXT_MANAGER_ATTRIBUTE, err);
			throw err;
		}
	}
	
	public ActivityContext getContext(String contextName) {
		if(contextMap == null || contextMap.size() == 0)
			return null;
		
		return contextMap.get(contextName);
	}
	
	public ActivityContext getContext(List<String> contextNames, ContextMergeMode mergeMethod) {
		if(contextNames.size() == 1)
			return getContext(contextNames.get(0));
		
		if(contextMap == null || contextMap.size() == 0)
			return null;

		List<ActivityContext> contextList = new ArrayList<ActivityContext>();;
		
		for(String contextName : contextNames) {
			contextList.add(contextMap.get(contextName));
		}
		
		ContextMerger contextMerger = new ContextMerger(mergeMethod == null ? defaultMergeMode : mergeMethod);
		
		return contextMerger.merge(contextList);
	}
	
	protected void destroy(ServletContext servletContext) {
		servletContext.log("Closing Trasnlets root context");

		try {
			for(Map.Entry<String, ActivityContext> entry : contextMap.entrySet()) {
				entry.getValue().destroy();
			}
		} finally {
			servletContext.removeAttribute(CONTEXT_MANAGER_ATTRIBUTE);
		}
	}

/*
			
			// context-relative path to our configuration resource for the translets
			String configFilePath = getServletConfig().getInitParameter(TRANSLETS_CONTEXT_CONFIG_LOCATION_PARAM);
			
			if(configFilePath == null)
				configFilePath = DEFAULT_TRANSLETS_CONTEXT_CONFIG_LOCATION;
			
			configFilePath = getServletContext().getRealPath(configFilePath);
			String serviceRootPath = getServletContext().getRealPath(TransletsConfig.URI_SEPARATOR);

			TransletsContextBuilder builder = new TransletsContextBuilder(serviceRootPath);
			context = builder.buildTransletsContext(configFilePath);
			
			Object object = getServletContext().getAttribute(RootContextLoader.ROOT_TRANSLETS_CONTEXT_ATTRIBUTE);
			
			if(object != null) {
				if(object instanceof TransletsContext) {
					TransletsContextMerger.merge(context, (TransletsContext)object);
				} else {
					log.error("Failed to merge root TransletsContext " + object);
				}
			}
*/	
	
}
