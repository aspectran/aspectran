/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.bean.scan;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.AspectranConstants;
import com.aspectran.core.context.builder.apon.params.FilterParameters;
import com.aspectran.core.util.ClassScanner;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

public class BeanClassScanner extends ClassScanner {

	private final Log log = LogFactory.getLog(BeanClassScanner.class);
	
	private Parameters filterParameters;
	
	private BeanClassScanFilter beanClassScanFilter;
	
	private WildcardPattern beanIdMaskPattern;
	
	private Map<String, WildcardPattern> excludePatternCache = new HashMap<String, WildcardPattern>();

	public BeanClassScanner(ClassLoader classLoader) {
		super(classLoader);
	}
	
	public Parameters getFilterParameters() {
		return filterParameters;
	}

	public void setFilterParameters(Parameters filterParameters) {
		this.filterParameters = filterParameters;
		
		String classScanFilterClassName = filterParameters.getString(FilterParameters.filterClass);
		if(classScanFilterClassName != null)
			setBeanClassScanFilter(classScanFilterClassName);
	}

	public BeanClassScanFilter getBeanClassScanFilter() {
		return beanClassScanFilter;
	}

	public void setBeanClassScanFilter(BeanClassScanFilter beanClassScanFilter) {
		this.beanClassScanFilter = beanClassScanFilter;
	}
	
	public void setBeanClassScanFilter(Class<?> beanClassScanFilterClass) {
		try {
			beanClassScanFilter = (BeanClassScanFilter)beanClassScanFilterClass.newInstance();
		} catch(Exception e) {
			throw new BeanClassScanFailedException("Failed to instantiate BeanClassScanFilter [" + beanClassScanFilterClass + "]", e);
		}
	}

	public WildcardPattern getBeanIdMaskPattern() {
		return beanIdMaskPattern;
	}

	public void setBeanIdMaskPattern(WildcardPattern beanIdMaskPattern) {
		this.beanIdMaskPattern = beanIdMaskPattern;
	}

	public void setBeanIdMaskPattern(String beanIdMaskPattern) {
		this.beanIdMaskPattern = new WildcardPattern(beanIdMaskPattern, AspectranConstants.ID_SEPARATOR);
	}
	
	public void setBeanClassScanFilter(String classScanFilterClassName) {
		Class<?> filterClass;
		try {
			filterClass = getClassLoader().loadClass(classScanFilterClassName);
		} catch(ClassNotFoundException e) {
			throw new BeanClassScanFailedException("Failed to instantiate BeanClassScanFilter [" + classScanFilterClassName + "]", e);
		}
		setBeanClassScanFilter(filterClass);
	}

	@Override
	public Map<String, Class<?>> scanClasses(String classNamePattern) {
		try {
			return super.scanClasses(classNamePattern);
		} catch(IOException e) {
			throw new BeanClassScanFailedException("Failed to scan bean class. classNamePattern: " + classNamePattern, e);
		}
	}

	@Override
	public void scanClasses(String classNamePattern, Map<String, Class<?>> scannedClasses) throws IOException {
		try {
			super.scanClasses(classNamePattern, scannedClasses);
		} catch(IOException e) {
			throw new BeanClassScanFailedException("Failed to scan bean class. classNamePattern: " + classNamePattern, e);
		}
	}

	@Override
	protected void putClass(Map<String, Class<?>> scannedClasses, String resourceName, Class<?> scannedClass) {
		if(scannedClass.isInterface())
			return;

		String className = scannedClass.getName();
		String beanId = className;

		if(beanIdMaskPattern != null) {
			String maskedBeanId = beanIdMaskPattern.mask(beanId);
			if(maskedBeanId != null) {
				beanId = maskedBeanId;
			} else {
				log.warn("Unmatched pattern can not be masking. beanId: " + beanId + " (maskPattern: " + beanIdMaskPattern + ")");
			}
		}

		if(beanClassScanFilter != null) {
			beanId = beanClassScanFilter.filter(beanId, resourceName, scannedClass);
			if(beanId == null) {
				return;
			}
		}

		if(filterParameters != null) {
			String[] excludePatterns = filterParameters.getStringArray(FilterParameters.exclude);
			
			if(excludePatterns != null) {
				for(String excludePattern : excludePatterns) {
					WildcardPattern pattern = excludePatternCache.get(excludePattern);
					if(pattern == null) {
						pattern = new WildcardPattern(excludePattern, ClassUtils.PACKAGE_SEPARATOR_CHAR);
						excludePatternCache.put(excludePattern, pattern);
					}
					if(pattern.matches(className)) {
						return;
					}
				}
			}
		}
		
		super.putClass(scannedClasses, beanId, scannedClass);
		
		if(log.isTraceEnabled())
			log.trace("scanned bean class {beanId: " + beanId + ", className: " + className + "}");
	}

}
