package com.aspectran.web.context;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.base.context.ActivityContext;
import com.aspectran.base.type.ContextMergeMode;
import com.aspectran.base.util.StringUtils;

public class ReferContextLoader implements ContextLoader {

	private final Log log = LogFactory.getLog(ReferContextLoader.class);

	private static final String CONTEXT_PARAM = "context";

	private static final String MERGE_MODE_PARAM = "mergeMode";

	private ContextMergeMode mergeMode;
	
	private List<ActivityContext> contextList;
	
	public ReferContextLoader(ServletConfig servletConfig, ContextManager contextManager) {
		mergeMode = ContextMergeMode.REPLACE;

		// context-relative path to our configuration resource for the translets
		String contextParamVal = servletConfig.getInitParameter(CONTEXT_PARAM);
		mergeMode = ContextMergeMode.valueOf(servletConfig.getInitParameter(MERGE_MODE_PARAM));
		
		log.debug("refer context names: " + contextParamVal);
		log.debug("contextMergeMode: " + mergeMode);
		
		String[] contextNames = StringUtils.tokenize(contextParamVal, "\n\t,;:| ");

		if(contextNames.length > 0) {
			contextList = new ArrayList<ActivityContext>();
			
			for(String contextName : contextNames) {
				log.debug("contextName: " + contextName);
				ActivityContext context = contextManager.getContext(contextName);
				log.debug("context: " + context);
				
				contextList.add(context);
			}
		}
	}

	public ContextMergeMode getMergeMode() {
		return mergeMode;
	}
	
	public List<ActivityContext> getContextList() {
		return contextList;
	}
}
