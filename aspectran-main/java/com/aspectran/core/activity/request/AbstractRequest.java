/**
 * 
 */
package com.aspectran.core.activity.request;

import com.aspectran.core.variable.FileItemMap;

/**
 * The Class AbstractRequest.
 *
 * @author Gulendol
 * @since 2011. 3. 12.
 */
public abstract class AbstractRequest {

	/** The file item map. */
	protected FileItemMap fileItemMap;
	
	/** The max length exceeded. */
	protected boolean maxLengthExceeded;
	
	/**
	 * Returns the file item map.
	 * 
	 * @return the file item map
	 */
	public FileItemMap getFileItemMap() {
		return fileItemMap;
	}

	/**
	 * Sets the file item map.
	 *
	 * @param fileItemMap the new file item map
	 */
	public void setFileItemMap(FileItemMap fileItemMap) {
//		if(this.fileItemMap != null && this.fileItemMap != fileItemMap)
//			this.fileItemMap.clear();

		this.fileItemMap = fileItemMap;
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
