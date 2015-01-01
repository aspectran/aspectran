package com.aspectran.web.context.service;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.service.ActivityContextService;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.context.loader.WebActivityContextLoader;

public class WebActivityContextService extends ActivityContextService {
	
	private final Logger logger = LoggerFactory.getLogger(WebActivityContextService.class);
	
	private final ServletContext servletContext;

	public WebActivityContextService(ServletContext servletContext, String aspectranConfigParam) {
		super(new AspectranConfig(aspectranConfigParam), new WebActivityContextLoader());
		
		this.servletContext = servletContext;

		ApplicationAdapter aa = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);
		ActivityContextLoader acl = getActivityContextLoader();
		acl.setApplicationAdapter(aa);
	}
	
	public boolean dispose() {
		boolean cleanlyDestoryed = super.dispose();
		
		try {
			WebApplicationAdapter.destoryWebApplicationAdapter(servletContext);
		} catch(Exception e) {
			cleanlyDestoryed = false;
			logger.error("WebApplicationAdapter were not destroyed cleanly.", e);
		}
		
		return cleanlyDestoryed;
	}
	
}
