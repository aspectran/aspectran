package com.aspectran.core.context.aspect.pointcut;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractPointcut {
	
	protected List<PointcutPattern> pointcutPatternList;

	public List<PointcutPattern> getPointcutPatternList() {
		return pointcutPatternList;
	}

	public void setPointcutPatternList(List<PointcutPattern> pointcutPatternList) {
		this.pointcutPatternList = pointcutPatternList;
	}
	
	public void addPointcutPattern(PointcutPattern pointcutPattern) {
		if(pointcutPatternList == null)
			pointcutPatternList = new ArrayList<PointcutPattern>();
		
		pointcutPatternList.add(pointcutPattern);
	}
	
	public void addPointcutPattern(List<PointcutPattern> pointcutPatternList) {
		if(pointcutPatternList == null)
			pointcutPatternList = new ArrayList<PointcutPattern>();

		pointcutPatternList.addAll(pointcutPatternList);
	}
	
}
