package com.aspectran.core.context.aspect.pointcut;

import com.aspectran.core.rule.PointcutRule;

public interface PointcutFactory {
	
	public Pointcut createPointcut(PointcutRule pointcutRule);
	
}
