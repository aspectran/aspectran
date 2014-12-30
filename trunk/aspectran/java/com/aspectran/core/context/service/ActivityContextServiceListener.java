package com.aspectran.core.context.service;


public interface ActivityContextServiceListener {
	
	public void started();
	
	public void restarted();

	public void paused(long timeout);
	
	public void resumed();

	public void stopped();

}