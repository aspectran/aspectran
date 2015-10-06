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
package com.aspectran.core.context.translet.scan;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.builder.apon.params.FilterParameters;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.FileScanner;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

public class TemplateFileScanner extends FileScanner {

	private final Log log = LogFactory.getLog(TemplateFileScanner.class);
	
	private final ClassLoader classLoader;
	
	private Parameters filterParameters;
	
	private TemplateFileScanFilter templateFileScanFilter;
	
	private WildcardPattern transletNameMaskPattern;
	
	private Map<String, WildcardPattern> excludePatternCache = new HashMap<String, WildcardPattern>();

	public TemplateFileScanner(String applicationBasePath, ClassLoader classLoader) {
		super(applicationBasePath);
		this.classLoader = classLoader;
	}
	
	public Parameters getFilterParameters() {
		return filterParameters;
	}

	public void setFilterParameters(Parameters filterParameters) {
		this.filterParameters = filterParameters;
		
		String templateFileScanFilterClassName = filterParameters.getString(FilterParameters.filterClass);
		if(templateFileScanFilterClassName != null)
			setTemplateFileScanFilter(templateFileScanFilterClassName);
	}

	public TemplateFileScanFilter getTemplateFileScanFilter() {
		return templateFileScanFilter;
	}

	public void setTemplateFileScanFilter(TemplateFileScanFilter templateFileScanFilter) {
		this.templateFileScanFilter = templateFileScanFilter;
	}
	
	public void setTemplateFileScanFilter(Class<?> templateFileScanFilterClass) {
		try {
			templateFileScanFilter = (TemplateFileScanFilter)templateFileScanFilterClass.newInstance();
		} catch(Exception e) {
			throw new TemplateFileScanFailedException("Failed to instantiate [" + templateFileScanFilterClass + "]", e);
		}
	}

	public WildcardPattern getTransletNameMaskPattern() {
		return transletNameMaskPattern;
	}

	public void setTransletNameMaskPattern(WildcardPattern transletNameMaskPattern) {
		this.transletNameMaskPattern = transletNameMaskPattern;
	}

	public void setTransletNameMaskPattern(String transletNameMaskPattern) {
		this.transletNameMaskPattern = new WildcardPattern(transletNameMaskPattern, AspectranConstant.TRANSLET_NAME_SEPARATOR);
	}
	
	public void setTemplateFileScanFilter(String templateFileScanFilterClassName) {
		Class<?> filterClass;
		try {
			filterClass = classLoader.loadClass(templateFileScanFilterClassName);
		} catch(ClassNotFoundException e) {
			throw new TemplateFileScanFailedException("Failed to instantiate [" + templateFileScanFilterClassName + "]", e);
		}
		setTemplateFileScanFilter(filterClass);
	}
	
	protected void putFile(Map<String, File> scannedFiles, String filePath, File scannedFile) {
		String filePath2 = filePath;
		
		if(transletNameMaskPattern != null) {
			String maskedfilePath = transletNameMaskPattern.mask(filePath2);
			if(maskedfilePath != null) {
				filePath2 = maskedfilePath;
			}  else {
				log.warn("Unmatched the pattern can not be masking. filePath: " + filePath2 + " (maskPattern: " + transletNameMaskPattern + ")");
			}
		}

		if(templateFileScanFilter != null) {
			filePath2 = templateFileScanFilter.filter(filePath2, scannedFile);
			if(filePath2 == null) {
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
					if(pattern.matches(filePath)) {
						return;
					}
				}
			}
		}
		
		super.putFile(scannedFiles, filePath2, scannedFile);
		
		if(log.isTraceEnabled())
			log.trace("scanned template file: " + filePath);
	}
	
}
