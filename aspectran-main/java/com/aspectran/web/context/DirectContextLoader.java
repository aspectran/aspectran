package com.aspectran.web.context;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.context.builder.BakContextBuilder;
import com.aspectran.core.context.builder.BakContextBuilderAssistant;
import com.aspectran.core.type.ContextMergeMode;
import com.aspectran.core.util.StringUtils;

public class DirectContextLoader implements ContextLoader {

	private final Log log = LogFactory.getLog(DirectContextLoader.class);

	private static final String CONFIG_PARAM_NAME_PREFIX = "translets:";

	private static final String CONTEXT_CONFIG_LOCATION_PARAM = "contextConfigLocation";
	
	private static final String MERGE_MODE_PARAM = "mergeMode";
	
	private static final String DEFAULT_CONTEXT_CONFIG_LOCATION = "WEB-INF/translets/context.xml";
	
	private ContextMergeMode mergeMode;
	
	private List<AspectranContext> contextList;
	
	private ServletContext servletContext;
	
	public DirectContextLoader(ServletContext servletContext) {
		this.servletContext = servletContext;
		
		// context-relative path to our configuration resource for the translets
		String contextConfigLocation = servletContext.getInitParameter(CONFIG_PARAM_NAME_PREFIX + CONTEXT_CONFIG_LOCATION_PARAM);
		mergeMode = ContextMergeMode.valueOf(servletContext.getInitParameter(MERGE_MODE_PARAM));
		
		if(contextConfigLocation == null)
			contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;

		if(mergeMode == null)
			mergeMode = ContextMergeMode.REPLACE;
		
		log.debug("contextConfigLocation: " + contextConfigLocation);
		log.debug("contextMergeMode: " + mergeMode);
		
		loadContext(contextConfigLocation);
	}

	public DirectContextLoader(ServletConfig servletConfig) {
		this.servletContext = servletConfig.getServletContext();
		
		// context-relative path to our configuration resource for the translets
		String contextConfigLocation = servletConfig.getInitParameter(CONTEXT_CONFIG_LOCATION_PARAM);
		mergeMode = ContextMergeMode.valueOf(servletContext.getInitParameter(MERGE_MODE_PARAM));
		
		if(contextConfigLocation == null)
			contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;

		if(mergeMode == null)
			mergeMode = ContextMergeMode.REPLACE;

		log.debug("contextConfigLocation: " + contextConfigLocation);
		log.debug("contextMergeMode: " + mergeMode);
		
		loadContext(contextConfigLocation);
	}

	private void loadContext(String contextConfigLocation) {
		String[] filePathes = StringUtils.tokenize(contextConfigLocation, "\n\t,;:| ");
		
		if(filePathes.length > 0) {
			String serviceRootPath = servletContext.getRealPath("/");
			BakContextBuilder builder = new BakContextBuilder(serviceRootPath);
			
			contextList = new ArrayList<AspectranContext>();
			
			for(String filePath : filePathes) {
				String configFilePath = servletContext.getRealPath(filePath);
				
				log.debug(filePath + " => " + configFilePath);
				
				AspectranContext context = builder.build(configFilePath);
				
				contextList.add(context);
			}
		}
	}
	
	public ContextMergeMode getMergeMode() {
		return mergeMode;
	}
	
	public List<AspectranContext> getContextList() {
		return contextList;
	}
}
