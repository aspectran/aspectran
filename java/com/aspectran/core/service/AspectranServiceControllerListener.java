package com.aspectran.core.service;

public interface AspectranServiceControllerListener {
	
	public void started();
	
	public void restarted();

	public void paused(long timeout);
	
	public void resumed();

	public void stopped();

}