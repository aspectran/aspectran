package com.aspectran.web.service;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.service.CoreAspectranService;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.context.loader.WebAponActivityContextLoader;
import com.aspectran.web.context.loader.WebXmlActivityContextLoader;

public class WebAspectranService extends CoreAspectranService {
	
	private final Logger logger = LoggerFactory.getLogger(WebAspectranService.class);
	
	private final ServletContext servletContext;

	public WebAspectranService(ServletContext servletContext, String aspectranConfigParam) {
		super(new AspectranConfig(aspectranConfigParam));
		
		this.servletContext = servletContext;

		ApplicationAdapter aa = WebApplicationAdapter.determineWebApplicationAdapter(servletContext);
		ActivityContextLoader acl = null;
		
		if(getRootContext() != null && getRootContext().endsWith(".apon"))
			acl = new WebXmlActivityContextLoader(aa);
		else
			acl = new WebAponActivityContextLoader(aa);

		setActivityContextLoader(acl);
		
		initActivityContext();
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
