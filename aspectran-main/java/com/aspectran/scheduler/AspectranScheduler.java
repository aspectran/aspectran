package com.aspectran.scheduler;



public interface AspectranScheduler {

	public void startup() throws Exception;
	
	public void startup(int delaySeconds) throws Exception;
	
	public void shutdown() throws Exception;
	
	public void shutdown(boolean waitForJobsToComplete) throws Exception;
	
	public void pause(String schedulerId) throws Exception;
	
	public void resume(String schedulerId) throws Exception;
	
}
