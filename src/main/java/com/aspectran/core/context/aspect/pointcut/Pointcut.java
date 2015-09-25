/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.aspect.pointcut;

import java.util.List;

import com.aspectran.core.context.rule.PointcutPatternRule;

/**
 * The Interface Pointcut.
 */
public interface Pointcut {
	
	public List<PointcutPatternRule> getPointcutPatternRuleList();
	
	public boolean isExistsBeanMethodNamePattern();
	
	public boolean matches(String transletName);
	
	public boolean matches(String transletName, String beanId);
	
	public boolean matches(String transletName, String beanId, String beanMethodName);

	public boolean exists(String transletName);
	
	public boolean exists(String transletName, String beanId);
	
	public boolean exists(String transletName, String beanId, String beanMethodName);

	public boolean patternMatches(String pattern, String str);
	
	public boolean patternMatches(String pattern, String str, char separator);

	public void clear();
	
}
