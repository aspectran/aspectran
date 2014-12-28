package com.aspectran.core.adapter;

import java.util.Map;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.service.ActivityContextServiceHandler;

/**
 * The Interface ApplicationAdapter.
 *
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public interface ApplicationAdapter {

	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	public Scope getScope();
	
	/**
	 * Gets the adaptee.
	 *
	 * @return the adaptee
	 */
	public Object getAdaptee();
	
	/**
	 * Gets the attribute.
	 *
	 * @param name the name
	 * @return the attribute
	 */
	public Object getAttribute(String name);

	/**
	 * Sets the attribute.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void setAttribute(String name, Object value);
	
	/**
	 * Gets the application base directory path.
	 *
	 * @return the application basedirectory path
	 */
	public String getApplicationBasePath();
	
	public Map<ActivityContext, ActivityContextServiceHandler> getActivityContextServiceHandlers();
	
	public void putActivityContextServiceHandler(ActivityContext activityContext, ActivityContextServiceHandler activityContextServiceHandler);
	
	public ActivityContextServiceHandler getActivityContextServiceHandler(ActivityContext activityContext);

}
