package com.aspectran.core.context.aspect.pointcut;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.var.type.PointcutPatternOperationType;

public abstract class AbstractPointcut {
	
	private List<PointcutPattern> includePatternList;
	
	private List<PointcutPattern> excludePatternList;
	
	public List<PointcutPattern> getIncludePatternList() {
		return includePatternList;
	}

	public void setIncludePatternList(List<PointcutPattern> includePatternList) {
		this.includePatternList = includePatternList;
	}

	public List<PointcutPattern> getExcludePatternList() {
		return excludePatternList;
	}

	public void setExcludePatternList(List<PointcutPattern> excludePatternList) {
		this.excludePatternList = excludePatternList;
	}
	
	public void addPointcutPattern(List<PointcutPattern> pointcutPatternList) {
		for(PointcutPattern pointcutPattern : pointcutPatternList) {
			addPointcutPattern(pointcutPattern);
		};
	}
	
	public void addPointcutPattern(PointcutPattern pointcutPattern) {
		if(pointcutPattern.getPointcutPatternOperationType() == PointcutPatternOperationType.WITHOUT) {
			if(excludePatternList == null)
				excludePatternList = new ArrayList<PointcutPattern>();
			
			excludePatternList.add(pointcutPattern);
		} else {
			if(includePatternList == null)
				includePatternList = new ArrayList<PointcutPattern>();
			
			includePatternList.add(pointcutPattern);
		}
	}

}
