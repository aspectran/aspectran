package com.aspectran.web.context;

import java.util.List;

import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.type.ContextMergeMode;

public interface ContextLoader {

	public ContextMergeMode getMergeMode();
	
	public List<AspectranContext> getContextList();
	
}
