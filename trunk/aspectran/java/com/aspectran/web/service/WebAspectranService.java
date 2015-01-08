package com.aspectran.web.service;

import javax.servlet.ServletContext;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.config.AspectranContextConfig;
import com.aspectran.core.service.CoreAspectranService;
import com.aspectran.core.var.apon.Parameter;
import com.aspectran.core.var.apon.Parameters;
import com.aspectran.web.adapter.WebApplicationAdapter;

public class WebAspectranService extends CoreAspectranService {

	public static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";
	
	public static final String WEB_APPLICATION_ADAPTER_ATTRIBUTE = 
			WebApplicationAdapter.class.getName() + ".WEB_APPLICATION_ADAPTER";

	private static final String DEFAULT_ROOT_CONTEXT = "/WEB-INF/aspectran/root.xml";
	
	public WebAspectranService(ServletContext servletContext, String aspectranConfigParam) {
		this(servletContext, aspectranConfigParam, null);
	}
	
	public WebAspectranService(ServletContext servletContext, String aspectranConfigParam, AspectranClassLoader aspectranClassLoader) {
		AspectranConfig aspectranConfig = new AspectranConfig(aspectranConfigParam);

		Parameters contextParams = aspectranConfig.getParameters(AspectranConfig.context);
		Parameter rootContextParam = contextParams.getParameter(AspectranContextConfig.root);
		String rootContext = rootContextParam.getValueAsString();

		if(rootContext == null || rootContext.length() == 0) {
			rootContextParam.setValue(DEFAULT_ROOT_CONTEXT);
		}
		
		ApplicationAdapter aa = new WebApplicationAdapter(servletContext);
		
		setAspectranClassLoader(aspectranClassLoader);
		setApplicationAdapter(aa);
		
		initialize(aspectranConfig);
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
