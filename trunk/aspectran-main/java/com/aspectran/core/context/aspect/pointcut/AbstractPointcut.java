package com.aspectran.core.context.aspect.pointcut;

import java.util.List;

public abstract class AbstractPointcut {
	
	private List<String> includePatternList;
	
	private List<String> excludePatternList;

	public List<String> getIncludePatternList() {
		return includePatternList;
	}

	public void setIncludePatternList(List<String> includePatternList) {
		this.includePatternList = includePatternList;
	}

	public List<String> getExcludePatternList() {
		return excludePatternList;
	}

	public void setExcludePatternList(List<String> excludePatternList) {
		this.excludePatternList = excludePatternList;
	}

}
