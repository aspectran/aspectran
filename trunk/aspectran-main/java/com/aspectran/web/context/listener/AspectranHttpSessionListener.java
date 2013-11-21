package com.aspectran.web.context.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.bean.scope.SessionScope;

public class AspectranHttpSessionListener implements HttpSessionListener {

	private final Logger logger = LoggerFactory.getLogger(AspectranHttpSessionListener.class);
	
	private final boolean debugEnabled = logger.isDebugEnabled();
	
	private int sessionCount = 0;
	
	public void sessionCreated(HttpSessionEvent se) {
		synchronized (this) {
            sessionCount++;
        }
		
		HttpSession session = se.getSession();
		
		if(debugEnabled)
			logger.debug("session created: {}, total sessions: {}", session.getId(), sessionCount);
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		synchronized (this) {
            sessionCount--;
        }
		
		if(debugEnabled)
			logger.debug("session destroyed: {}, total sessions: {}", se.getSession().getId(), sessionCount);
		
		HttpSession session = se.getSession();
		
		SessionScope scope = (SessionScope)session.getAttribute(SessionScope.SESSION_SCOPE_ATTRIBUTE);
		
		if(scope != null)
			scope.destroy();
	}
	
}
