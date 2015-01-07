package com.aspectran.web.service;

import javax.servlet.ServletContext;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.service.CoreAspectranService;
import com.aspectran.web.adapter.WebApplicationAdapter;
import com.aspectran.web.context.loader.WebAponActivityContextLoader;
import com.aspectran.web.context.loader.WebXmlActivityContextLoader;

public class WebAspectranService extends CoreAspectranService {

	public static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";
	
	public static final String WEB_APPLICATION_ADAPTER_ATTRIBUTE = 
			WebApplicationAdapter.class.getName() + ".WEB_APPLICATION_ADAPTER";

	public WebAspectranService(ServletContext servletContext, String aspectranConfigParam) {
		this(servletContext, aspectranConfigParam, null);
	}
	
	public WebAspectranService(ServletContext servletContext, String aspectranConfigParam, AspectranClassLoader aspectranClassLoader) {
		AspectranConfig aspectranConfig = new AspectranConfig(aspectranConfigParam);

		ApplicationAdapter aa = new WebApplicationAdapter(servletContext);
		
		ActivityContextLoader acl = null;
		
		if(getRootContext() != null && getRootContext().endsWith(".apon"))
			acl = new WebXmlActivityContextLoader(aa);
		else
			acl = new WebAponActivityContextLoader(aa);
		
		acl.setAspectranClassLoader(aspectranClassLoader);

		setAspectranClassLoader(aspectranClassLoader);
		setActivityContextLoader(acl);
		setApplicationAdapter(aa);
		
		initActivityContext(aspectranConfig);
	}
	
	/*
	private WebApplicationAdapter determineWebApplicationAdapter() {
		WebApplicationAdapter webApplicationAdapter = (WebApplicationAdapter)servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE);
		
		if(webApplicationAdapter == null)
			webApplicationAdapter = createWebApplicationAdapter();

		return webApplicationAdapter;
	}
	
	private WebApplicationAdapter getWebApplicationAdapter(ServletContext servletContext) {
		return (WebApplicationAdapter)servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE);
	}
	
	private WebApplicationAdapter createWebApplicationAdapter() {
		WebApplicationAdapter webApplicationAdapter = new WebApplicationAdapter(servletContext);
		servletContext.setAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE, webApplicationAdapter);
		
		logger.debug("WebApplicationAdapter attribute was created. " + webApplicationAdapter);
		
		return webApplicationAdapter;
	}

	private void destoryWebApplicationAdapter() {
		WebApplicationAdapter webApplicationAdapter = getWebApplicationAdapter(servletContext);
		
		if(webApplicationAdapter != null) {
			Scope scope = webApplicationAdapter.getScope();
	
			if(scope != null)
				scope.destroy();

			if(servletContext.getAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE) != null) {
				servletContext.removeAttribute(WEB_APPLICATION_ADAPTER_ATTRIBUTE);
				logger.debug("WebApplicationAdapter attribute was removed.");
			} else {
				logger.debug("WebApplicationAdapter attribute was already removed.");
			}
		}
	}
	*/
}
