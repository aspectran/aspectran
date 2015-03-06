package com.aspectran.core.adapter;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.service.AspectranServiceController;

/**
 * The Interface ApplicationAdapter.
 *
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public interface ApplicationAdapter {
	
	/**
	 * Gets the adaptee.
	 *
	 * @return the adaptee
	 */
	public <T> T getAdaptee();

	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	public Scope getScope();
	
	/**
	 * Gets the attribute.
	 *
	 * @param name the name
	 * @return the attribute
	 */
	public <T> T getAttribute(String name);

	/**
	 * Sets the attribute.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void setAttribute(String name, Object value);
	
	public Enumeration<String> getAttributeNames();
	
	public void removeAttribute(String name);
	
	public AspectranServiceController getAspectranServiceController();

	public ClassLoader getClassLoader();
	
	public String getApplicationBasePath();
	
	public String toRealPath(String filePath) throws IOException;
	
	public File toRealPathAsFile(String filePath);

}
