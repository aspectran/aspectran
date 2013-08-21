package com.aspectran.web.servlet;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.aspectran.core.context.bean.scope.SessionScope;

public class AspectranHttpSessionListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent se) {
		
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		SessionScope scope = (SessionScope)session.getAttribute(SessionScope.SESSION_SCOPE_ATTRIBUTE);
		
		if(scope != null)
			scope.destroy();
	}

}
