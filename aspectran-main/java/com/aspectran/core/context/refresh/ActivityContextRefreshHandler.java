package com.aspectran.core.context.refresh;

import com.aspectran.core.context.ActivityContext;

public interface ActivityContextRefreshHandler {

	public void handle(ActivityContext newActivityContext);
	
}
