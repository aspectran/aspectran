package com.aspectran.web.adapter;

import javax.servlet.http.HttpSession;

import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.bean.scope.SessionScope;

/**
 * The Class HttpSessionAdapter.
 * 
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public class HttpSessionAdapter extends AbstractSessionAdapter implements SessionAdapter {
	
	public static final String SESSION_SCOPE_ATTRIBUTE = HttpSessionScope.class.getName();
	
	/**
	 * Instantiates a new http session adapter.
	 *
	 * @param session the session
	 */
	public HttpSessionAdapter(HttpSession session) {
		super(session);
	}
	
	public Object getAttribute(String name) {
		return ((HttpSession)adaptee).getAttribute(name);
	}

	public void setAttribute(String name, Object value) {
		((HttpSession)adaptee).setAttribute(name, value);
	}

	public synchronized Scope getScope() {
		Scope scope = (SessionScope)getAttribute(SESSION_SCOPE_ATTRIBUTE);

		if(scope == null) {
			scope = new HttpSessionScope();
			setAttribute(SESSION_SCOPE_ATTRIBUTE, scope);
		}
		
		return scope;
	}

}
