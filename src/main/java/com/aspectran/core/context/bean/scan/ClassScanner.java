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
package com.aspectran.core.context.bean.scan;

import java.io.IOException;
import java.util.Map;

import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.wildcard.WildcardPattern;

public interface ClassScanner {

	public Parameters getFilterParameters();

	public void setFilterParameters(Parameters filterParameters);

	public ClassScanFilter getClassScanFilter();

	public void setClassScanFilter(Class<?> classScanFilterClass);

	public void setClassScanFilter(String classScanFilterClassName);

	public WildcardPattern getBeanIdMaskPattern();

	public void setBeanIdMaskPattern(WildcardPattern beanIdMaskPattern);

	public void setBeanIdMaskPattern(String beanIdMask);
	
	public Map<String, Class<?>> scanClasses(String classNamePattern) throws IOException, ClassNotFoundException;
	
	public void scanClasses(String classNamePattern, Map<String, Class<?>> scannedClasses);

}
