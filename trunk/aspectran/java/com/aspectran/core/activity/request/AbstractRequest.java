/**
 * 
 */
package com.aspectran.core.activity.request;


/**
 * The Class AbstractRequest.
 *
 * @author Gulendol
 * @since 2011. 3. 12.
 */
public abstract class AbstractRequest {

//	/** The file item map. */
//	protected FileParameterMap fileParameterMap;
	
	/** The max length exceeded. */
	protected boolean maxLengthExceeded;
	
//	public FileParameter getFileParameter(String name) {
//		if(fileParameterMap == null)
//			return null;
//		
//		return fileParameterMap.getFileItem(name);
//	}
//	
//	/**
//	 * Returns the file item map.
//	 * 
//	 * @return the file item map
//	 */
//	public FileParameterMap getFileParameterMap() {
//		return fileParameterMap;
//	}
//
//	/**
//	 * Sets the file item map.
//	 *
//	 * @param fileParameterMap the new file item map
//	 */
//	public void setFileParameterMap(FileParameterMap fileParameterMap) {
//		this.fileParameterMap = fileParameterMap;
//	}
//	
//	public FileParameterMap touchFileParameterMap() {
//		if(fileParameterMap == null) {
//			fileParameterMap = new FileParameterMap();
//		}
//		
//		return fileParameterMap;
//	}

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
