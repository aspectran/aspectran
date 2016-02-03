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
package com.aspectran.core.context.translet.scan;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.bean.annotation.Translets;
import com.aspectran.core.context.builder.apon.params.FilterParameters;
import com.aspectran.core.util.ClassScanner;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * The Class TransletClassScanner.
 * 
 * @since 2.0.0
 */
public class TransletClassScanner extends ClassScanner {

	private final Log log = LogFactory.getLog(TransletClassScanner.class);

	private Parameters filterParameters;

	private TransletClassScanFilter transletClassScanFilter;

	private Map<String, WildcardPattern> excludePatternCache = new HashMap<String, WildcardPattern>();

	public TransletClassScanner(ClassLoader classLoader) {
		super(classLoader);
	}
	
	public Parameters getFilterParameters() {
		return filterParameters;
	}

	public void setFilterParameters(Parameters filterParameters) {
		this.filterParameters = filterParameters;
		
		String classScanFilterClassName = filterParameters.getString(FilterParameters.filterClass);
		if(classScanFilterClassName != null)
			setTransletClassScanFilter(classScanFilterClassName);
	}

	public TransletClassScanFilter getTransletClassScanFilter() {
		return transletClassScanFilter;
	}

	public void setTransletClassScanFilter(TransletClassScanFilter transletClassScanFilter) {
		this.transletClassScanFilter = transletClassScanFilter;
	}
	
	public void setTransletClassScanFilter(Class<?> transletClassScanFilterClass) {
		try {
			transletClassScanFilter = (TransletClassScanFilter)transletClassScanFilterClass.newInstance();
		} catch(Exception e) {
			throw new TransletScanFailedException("Failed to instantiate TransletClassScanFilter [" + transletClassScanFilterClass + "]", e);
		}
	}

	public void setTransletClassScanFilter(String classScanFilterClassName) {
		Class<?> filterClass;
		try {
			filterClass = getClassLoader().loadClass(classScanFilterClassName);
		} catch(ClassNotFoundException e) {
			throw new TransletScanFailedException("Failed to instantiate TransletClassScanFilter [" + classScanFilterClassName + "]", e);
		}
		setTransletClassScanFilter(filterClass);
	}

	@Override
	public Map<String, Class<?>> scanClasses(String classNamePattern) {
		try {
			return super.scanClasses(classNamePattern);
		} catch(IOException e) {
			throw new TransletScanFailedException("Failed to scan translet class. classNamePattern: " + classNamePattern, e);
		}
	}

	@Override
	public void scanClasses(String classNamePattern, Map<String, Class<?>> scannedClasses) throws IOException {
		try {
			super.scanClasses(classNamePattern, scannedClasses);
		} catch(IOException e) {
			throw new TransletScanFailedException("Failed to scan translet class. classNamePattern: " + classNamePattern, e);
		}
	}

	@Override
	protected void putClass(String resourceName, Class<?> scannedClass, Map<String, Class<?>> scannedClasses) {
		if(scannedClass.isInterface() ||
				Modifier.isAbstract(scannedClass.getModifiers()) ||
				!Modifier.isPublic(scannedClass.getModifiers()) ||
				!scannedClass.isAnnotationPresent(Translets.class))
			return;
		
		String className = scannedClass.getName();

		if(transletClassScanFilter != null) {
			boolean pass = transletClassScanFilter.filter(className, resourceName, scannedClass);
			if(!pass) {
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
		
		super.putClass(className, scannedClass, scannedClasses);
		
		if(log.isTraceEnabled())
			log.trace("scanned translet class [" + className + "]");
	}

}
