package com.aspectran.web.adapter;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.bean.scope.SessionScope;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public class HttpSessionScope extends SessionScope implements HttpSessionBindingListener {

	private static final Log log = LogFactory.getLog(HttpSessionScope.class);
	
	private static final boolean debugEnabled = log.isDebugEnabled();
	
	public void valueBound(HttpSessionBindingEvent event) {
		HttpSession session = event.getSession();
		
		if(debugEnabled)
			log.debug("Session Bound: " + session.getId() + ", " + event.getValue());
	}

	public void valueUnbound(HttpSessionBindingEvent event) {
		HttpSession session = event.getSession();

		if(debugEnabled)
			log.debug("Session Unbound: " + session.getId() + ", " + event.getValue());
		
		Scope scope = (SessionScope)event.getValue();
		
		if(scope != null)
			scope.destroy();
	}
	
}
