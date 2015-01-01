package com.aspectran.core.context.service;

import com.aspectran.core.context.ActivityContext;


public interface ActivityContextServiceController {
	
	public ActivityContext start();

	public boolean restart();
	
	public void pause();
	
	/**
	 * @param timeout - the maximum time to wait in milliseconds. 
	 */
	public void pause(long timeout);
	
	public void resume();
	
	public boolean stop();
	
}
