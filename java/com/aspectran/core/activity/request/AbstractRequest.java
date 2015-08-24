/**
 * 
 */
package com.aspectran.core.activity.request;

import java.util.Collections;
import java.util.Enumeration;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.activity.request.parameter.FileParameterMap;


/**
 * The Class AbstractRequest.
 *
 * @author Gulendol
 * @since 2011. 3. 12.
 */
public abstract class AbstractRequest {

	protected FileParameterMap fileParameterMap;
	
	/** The max length exceeded. */
	protected boolean maxLengthExceeded;
	
	public FileParameter getFileParameter(String name) {
		if(fileParameterMap == null)
			return null;
		
		return fileParameterMap.getFileParameter(name);
	}
	
	public FileParameter[] getFileParameterValues(String name) {
		if(fileParameterMap == null)
			return null;
		
		return fileParameterMap.getFileParameters(name);
	}

	public void setFileParameter(String name, FileParameter fileParameter) {
		touchFileParameterMap().putFileParameter(name, fileParameter);
	}
	
	public void setFileParameter(String name, FileParameter[] fileParameters) {
		touchFileParameterMap().putFileParameters(name, fileParameters);
	}
	
	public Enumeration<String> getFileParameterNames() {
		FileParameterMap fileParameterMap = touchFileParameterMap();

		if(fileParameterMap == null)
			return null;

		return Collections.enumeration(fileParameterMap.keySet());
	}
	
	public FileParameter[] removeFileParameter(String name) {
		if(fileParameterMap == null)
			return null;
		
		return fileParameterMap.remove(name);
	}
	
	protected FileParameterMap touchFileParameterMap() {
		if(fileParameterMap == null) {
			fileParameterMap = new FileParameterMap();
		}
		
		return fileParameterMap;
	}

	/**
	 * Checks if is max length exceeded.
	 *
	 * @return true, if is max length exceeded
	 */
	public boolean isMaxLengthExceeded() {
		return maxLengthExceeded;
	}

	/**
	 * Sets the max length exceeded.
	 *
	 * @param maxLengthExceeded the new max length exceeded
	 */
	public void setMaxLengthExceeded(boolean maxLengthExceeded) {
		this.maxLengthExceeded = maxLengthExceeded;
	}

}
