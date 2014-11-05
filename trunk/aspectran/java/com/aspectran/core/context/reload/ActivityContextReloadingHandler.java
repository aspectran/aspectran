package com.aspectran.core.context.reload;

import com.aspectran.core.context.ActivityContext;

public interface ActivityContextReloadingHandler {

	public void handle(ActivityContext newActivityContext);
	
}
