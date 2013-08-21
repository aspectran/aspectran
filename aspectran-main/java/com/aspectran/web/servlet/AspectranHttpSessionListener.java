package com.aspectran.web.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.aspectran.core.context.bean.scope.SessionScope;
import com.aspectran.web.context.AspectranContextLoader;

public class AspectranHttpSessionListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent se) {
		ServletContext servletContext = se.getSession().getServletContext();
		
		AspectranContextLoader aspectranContextLoader = (AspectranContextLoader)servletContext.getAttribute(AspectranContextLoader.ASPECTRAN_CONTEXT_LOADER_ATTRIBUTE);
		
		//aspectranContextLoader.getAspectranContext().getBeanRegistry().getBean(id, activity);
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		SessionScope scope = (SessionScope)session.getAttribute(SessionScope.SESSION_SCOPE_ATTRIBUTE);
		
		if(scope != null)
			scope.destroy();
	}

}
