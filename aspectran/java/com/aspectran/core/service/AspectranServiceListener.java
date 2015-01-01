package com.aspectran.core.service;


public interface AspectranServiceListener {
	
	public void started();
	
	public void restarted();

	public void paused(long timeout);
	
	public void resumed();

	public void stopped();

}