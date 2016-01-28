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

import com.aspectran.core.context.AspectranConstants;
import com.aspectran.core.context.builder.apon.params.FilterParameters;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.FileScanner;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TransletFileScanner extends FileScanner {

	private final Log log = LogFactory.getLog(TransletFileScanner.class);
	
	private final ClassLoader classLoader;
	
	private Parameters filterParameters;
	
	private TransletFileScanFilter templateFileScanFilter;
	
	private WildcardPattern transletNameMaskPattern;
	
	private Map<String, WildcardPattern> excludePatternCache = new HashMap<String, WildcardPattern>();

	public TransletFileScanner(String applicationBasePath, ClassLoader classLoader) {
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

	public TransletFileScanFilter getTemplateFileScanFilter() {
		return templateFileScanFilter;
	}

	public void setTemplateFileScanFilter(TransletFileScanFilter templateFileScanFilter) {
		this.templateFileScanFilter = templateFileScanFilter;
	}
	
	public void setTemplateFileScanFilter(Class<?> templateFileScanFilterClass) {
		try {
			templateFileScanFilter = (TransletFileScanFilter)templateFileScanFilterClass.newInstance();
		} catch(Exception e) {
			throw new TransletScanFailedException("Failed to instantiate TemplateFileScanFilter [" + templateFileScanFilterClass + "]", e);
		}
	}

	public WildcardPattern getTransletNameMaskPattern() {
		return transletNameMaskPattern;
	}

	public void setTransletNameMaskPattern(WildcardPattern transletNameMaskPattern) {
		this.transletNameMaskPattern = transletNameMaskPattern;
	}

	public void setTransletNameMaskPattern(String transletNameMaskPattern) {
		this.transletNameMaskPattern = new WildcardPattern(transletNameMaskPattern, AspectranConstants.TRANSLET_NAME_SEPARATOR);
	}
	
	public void setTemplateFileScanFilter(String templateFileScanFilterClassName) {
		Class<?> filterClass;
		try {
			filterClass = classLoader.loadClass(templateFileScanFilterClassName);
		} catch(ClassNotFoundException e) {
			throw new TransletScanFailedException("Failed to instantiate TemplateFileScanFilter [" + templateFileScanFilterClassName + "]", e);
		}
		setTemplateFileScanFilter(filterClass);
	}
	
	protected void putFile(Map<String, File> scannedFiles, String filePath, File scannedFile) {
		String transletName = filePath;
		
		if(transletNameMaskPattern != null) {
			String maskedTransletName = transletNameMaskPattern.mask(transletName);
			if(maskedTransletName != null) {
				transletName = maskedTransletName;
			}  else {
				log.warn("Unmatched the pattern can not be masking. filePath: " + transletName + " (maskPattern: " + transletNameMaskPattern + ")");
			}
		}

		if(templateFileScanFilter != null) {
			boolean pass = templateFileScanFilter.filter(transletName, scannedFile);
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
					if(pattern.matches(filePath)) {
						return;
					}
				}
			}
		}
		
		super.putFile(scannedFiles, transletName, scannedFile);
		
		if(log.isTraceEnabled())
			log.trace("scanned template file: " + filePath);
	}
	
}
