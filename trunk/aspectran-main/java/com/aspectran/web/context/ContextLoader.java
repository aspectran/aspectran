package com.aspectran.web.context;

import java.util.List;

import com.aspectran.base.context.ActivityContext;
import com.aspectran.base.type.ContextMergeMode;

public interface ContextLoader {

	public ContextMergeMode getMergeMode();
	
	public List<ActivityContext> getContextList();
	
}
