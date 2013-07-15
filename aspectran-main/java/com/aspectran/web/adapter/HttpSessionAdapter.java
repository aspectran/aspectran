package com.aspectran.web.adapter;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.ScopeBean;
import com.aspectran.core.context.bean.ScopeBeanMap;
import com.aspectran.core.context.bean.scope.SessionScope;

/**
 * The Class HttpSessionAdapter.
 * 
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public class HttpSessionAdapter extends AbstractSessionAdapter implements SessionAdapter {
	
	public static final String SCOPE_BEAN_DESTROY_LISTENER = 
		HttpSessionAdapter.class.getName() + ".SCOPE_BEAN_DESTROY_LISTENER";
	
	/**
	 * Instantiates a new http session adapter.
	 *
	 * @param session the session
	 */
	public HttpSessionAdapter(HttpSession session) {
		super(session);
		
		if(session.getAttribute(SCOPE_BEAN_DESTROY_LISTENER) != null) {
			ScopeBeanDestroyListener listener = new ScopeBeanDestroyListener();
			session.setAttribute(SCOPE_BEAN_DESTROY_LISTENER, listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractSessionAdapter#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return ((HttpSession)adaptee).getAttribute(name);
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractSessionAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		if(SessionScope.SESSION_SCOPE_ATTRIBUTE.equals(name))
			throw new IllegalArgumentException("The specified attribute name is not allowed. Reserved attribute name '" + name + "'");
		
		((HttpSession)adaptee).setAttribute(name, value);
	}
	
	class ScopeBeanDestroyListener implements HttpSessionBindingListener {
		public void valueBound(HttpSessionBindingEvent se) {
			
		}
		
		public void valueUnbound(HttpSessionBindingEvent se) {
			HttpSession session = se.getSession();
			
			ScopeBeanMap ibm = (ScopeBeanMap)session.getAttribute(SessionScope.SESSION_SCOPE_ATTRIBUTE);
			
			if(ibm != null) {
				try {
					for(ScopeBean scopeBean : ibm) {
						scopeBean.destroy();
					}
				} catch(Exception e) {
					//로깅
				}
			}
		}
	}

}
