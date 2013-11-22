package com.aspectran.web.adapter;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.bean.scope.SessionScope;

public class HttpSessionScope extends SessionScope implements HttpSessionBindingListener {

	private static final Logger logger = LoggerFactory.getLogger(HttpSessionScope.class);
	
	private static final boolean debugEnabled = logger.isDebugEnabled();
	
	public void valueBound(HttpSessionBindingEvent event) {
		HttpSession session = event.getSession();
		logger.debug("session-scope bound: {}, {}", session.getId(), event.getValue());
	}

	public void valueUnbound(HttpSessionBindingEvent event) {
		HttpSession session = event.getSession();

		if(debugEnabled)
			logger.debug("session-scope unbound: {}, {}", session.getId(), event.getValue());
		
		Scope scope = (SessionScope)event.getValue();
		
		try {
			Thread.sleep(20000L);
		} catch(InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(scope != null)
			scope.destroy();
	}
	
}
