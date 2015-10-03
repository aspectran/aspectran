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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.builder.apon.params.FilterParameters;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardMatcher;
import com.aspectran.core.util.wildcard.WildcardPattern;

public class BeanClassScanner implements ClassScanner {

	private final Log log = LogFactory.getLog(BeanClassScanner.class);
	
	private final ClassLoader classLoader;
	
	private Parameters filterParameters;
	
	private ClassScanFilter classScanFilter;
	
	private WildcardPattern beanIdMaskPattern;
	
	private Map<String, WildcardPattern> wildcardPatternCache = new HashMap<String, WildcardPattern>();

	public BeanClassScanner(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	public Parameters getFilterParameters() {
		return filterParameters;
	}

	public void setFilterParameters(Parameters filterParameters) {
		this.filterParameters = filterParameters;
		
		String classScanFilterClassName = filterParameters.getString(FilterParameters.filterClass);
		if(classScanFilterClassName != null)
			setClassScanFilter(classScanFilterClassName);
	}

	public ClassScanFilter getClassScanFilter() {
		return classScanFilter;
	}

	public void setClassScanFilter(ClassScanFilter classScanFilter) {
		this.classScanFilter = classScanFilter;
	}
	
	public void setClassScanFilter(Class<?> classScanFilterClass) {
		try {
			classScanFilter = (ClassScanFilter)classScanFilterClass.newInstance();
		} catch(Exception e) {
			throw new ClassScanFailedException("Failed to instantiate [" + classScanFilterClass + "]", e);
		}
	}

	public WildcardPattern getBeanIdMaskPattern() {
		return beanIdMaskPattern;
	}

	public void setBeanIdMaskPattern(WildcardPattern beanIdMaskPattern) {
		this.beanIdMaskPattern = beanIdMaskPattern;
	}

	public void setBeanIdMaskPattern(String beanIdMask) {
		beanIdMaskPattern = new WildcardPattern(beanIdMask, AspectranConstant.ID_SEPARATOR);
	}
	
	public void setClassScanFilter(String classScanFilterClassName) {
		Class<?> filterClass;
		try {
			filterClass = classLoader.loadClass(classScanFilterClassName);
		} catch(ClassNotFoundException e) {
			throw new ClassScanFailedException("Failed to instantiate [" + classScanFilterClassName + "]", e);
		}
		setClassScanFilter(filterClass);
	}
	
	public Map<String, Class<?>> scanClasses(String classNamePattern) throws IOException, ClassNotFoundException {
		Map<String, Class<?>> scannedClasses = new LinkedHashMap<String, Class<?>>();
		
		scanClasses(classNamePattern, scannedClasses);
		
		return scannedClasses;
	}
	
	public void scanClasses(String classNamePattern, Map<String, Class<?>> scannedClasses) {
		try {
			classNamePattern = classNamePattern.replace(ClassUtils.PACKAGE_SEPARATOR_CHAR, ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR);
	
			String basePackageName = determineBasePackageName(classNamePattern);
	
			String subPattern;
			if(classNamePattern.length() > basePackageName.length())
				subPattern = classNamePattern.substring(basePackageName.length());
			else
				subPattern = StringUtils.EMPTY;
			
			WildcardPattern pattern = WildcardPattern.compile(subPattern, ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR);
			WildcardMatcher matcher = new WildcardMatcher(pattern);
			
			Enumeration<URL> resources = classLoader.getResources(basePackageName);
			
			if(basePackageName != null && !basePackageName.endsWith(ResourceUtils.RESOURCE_NAME_SPEPARATOR))
				basePackageName += ResourceUtils.RESOURCE_NAME_SPEPARATOR;
			
			while(resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				
				if(log.isDebugEnabled())
					log.debug("bean scanning: " + classNamePattern + " at " + resource.getFile());
				
				if(isJarResource(resource)) {
					scanClassesFromJarResource(resource, matcher, scannedClasses);
				} else {
					scanClasses(resource.getFile(), basePackageName, null,  matcher, scannedClasses);
				}
			}
		} catch(IOException e) {
			throw new ClassScanFailedException("bean-class scanning failed.", e);
		}
	}
	
	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private void scanClasses(final String targetPath, final String basePackageName, final String relativePackageName, final WildcardMatcher matcher, final Map<String, Class<?>> scannedClasses) {
		final File target = new File(targetPath);
		if(!target.exists())
			return;

		target.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if(file.isDirectory()) {
					String relativePackageName2;
					if(relativePackageName == null)
						relativePackageName2 = file.getName() + ResourceUtils.RESOURCE_NAME_SPEPARATOR;
					else
						relativePackageName2 = relativePackageName + file.getName() + ResourceUtils.RESOURCE_NAME_SPEPARATOR;
							
					String basePath2 = targetPath + file.getName() + ResourceUtils.RESOURCE_NAME_SPEPARATOR;
					scanClasses(basePath2, basePackageName, relativePackageName2, matcher, scannedClasses);
				} else if(file.getName().endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
					String className;
					if(relativePackageName != null)
						className = basePackageName + relativePackageName + file.getName().substring(0, file.getName().length() - ClassUtils.CLASS_FILE_SUFFIX.length());
					else
						className = basePackageName + file.getName().substring(0, file.getName().length() - ClassUtils.CLASS_FILE_SUFFIX.length());
					String relativePath = className.substring(basePackageName.length(), className.length());
					if(matcher.matches(relativePath)) {
						Class<?> classType = loadClass(className);
						String beanId = composeBeanId(relativePath);
						putClass(scannedClasses, beanId, className, classType);
					}
				}
				return false;
			}
		});
	}

	protected void scanClassesFromJarResource(URL resource, WildcardMatcher matcher, Map<String, Class<?>> scannedClasses) throws IOException {
		URLConnection conn = resource.openConnection();
		JarFile jarFile = null;
		String jarFileUrl = null;
		String entryNamePrefix = null;
		boolean newJarFile = false;

		if(conn instanceof JarURLConnection) {
			// Should usually be the case for traditional JAR files.
			JarURLConnection jarCon = (JarURLConnection)conn;
			jarCon.setUseCaches(false);
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			JarEntry jarEntry = jarCon.getJarEntry();
			entryNamePrefix = (jarEntry != null ? jarEntry.getName() : "");
		} else {
			// No JarURLConnection -> need to resort to URL file parsing.
			// We'll assume URLs of the format "jar:path!/entry", with the protocol
			// being arbitrary as long as following the entry format.
			// We'll also handle paths with and without leading "file:" prefix.
			String urlFile = resource.getFile();
			int separatorIndex = urlFile.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
			if(separatorIndex != -1) {
				jarFileUrl = urlFile.substring(0, separatorIndex);
				entryNamePrefix = urlFile.substring(separatorIndex + ResourceUtils.JAR_URL_SEPARATOR.length());
				jarFile = getJarFile(jarFileUrl);
			} else {
				jarFile = new JarFile(urlFile);
				//jarFileUrl = urlFile;
				entryNamePrefix = "";
			}
			newJarFile = true;
		}
		
		try {
			//Looking for matching resources in jar file [" + jarFileUrl + "]"
			if(!entryNamePrefix.endsWith(ResourceUtils.RESOURCE_NAME_SPEPARATOR)) {
				// Root entry path must end with slash to allow for proper matching.
				// The Sun JRE does not return a slash here, but BEA JRockit does.
				entryNamePrefix = entryNamePrefix + ResourceUtils.RESOURCE_NAME_SPEPARATOR;
			}
			
			for(Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String entryName = entry.getName();
				if(entryName.startsWith(entryNamePrefix) && entryName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
					String entryNameSuffix = entryName.substring(entryNamePrefix.length(), entryName.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
					
					if(matcher.matches(entryNameSuffix)) {
						String className = entryNamePrefix + entryNameSuffix;
						Class<?> classType = loadClass(className);
						String beanId = composeBeanId(entryNameSuffix);
						putClass(scannedClasses, beanId, className, classType);
					}
				}
			}
		} finally {
			// Close jar file, but only if freshly obtained -
			// not from JarURLConnection, which might cache the file reference.
			if(newJarFile) {
				if(jarFile != null)
					jarFile.close();
			}
		}
	}

	protected JarFile getJarFile(String jarFileUrl) throws IOException {
		if(jarFileUrl.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
			try {
				return new JarFile(ResourceUtils.toURI(jarFileUrl).getSchemeSpecificPart());
			} catch(URISyntaxException ex) {
				// Fallback for URLs that are not valid URIs (should hardly ever happen).
				return new JarFile(jarFileUrl.substring(ResourceUtils.FILE_URL_PREFIX.length()));
			}
		} else {
			return new JarFile(jarFileUrl);
		}
	}
	
	protected boolean isJarResource(URL url) throws IOException {
		String protocol = url.getProtocol();
		return (ResourceUtils.URL_PROTOCOL_JAR.equals(protocol) || ResourceUtils.URL_PROTOCOL_ZIP.equals(protocol));
	}

	private String determineBasePackageName(String classNamePattern) {
		WildcardPattern pattern = new WildcardPattern(classNamePattern, ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR);
		WildcardMatcher matcher = new WildcardMatcher(pattern);

		boolean matched = matcher.matches(classNamePattern);
		if(!matched)
			return null;
		
		StringBuilder sb = new StringBuilder();

		while(matcher.hasNext()) {
			String str = matcher.next();

			if(WildcardPattern.hasWildcards(str))
				break;

			sb.append(str).append(ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR);
		}
		
		return sb.toString();
	}

	private String composeBeanId(String relativePath) {
		return relativePath.replace(ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR, ClassUtils.PACKAGE_SEPARATOR_CHAR);
	}
	
	private Class<?> loadClass(String className) {
		className = className.replace(ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR, ClassUtils.PACKAGE_SEPARATOR_CHAR);
		
		try {
			return classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new ClassScanFailedException("bean-class loading failed. class name: " + className, e);
		}
	}
	
	private boolean putClass(Map<String, Class<?>> scannedClasses, String beanId, String className, Class<?> scannedClass) {
		className = className.replace(ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR, ClassUtils.PACKAGE_SEPARATOR_CHAR);

		if(beanIdMaskPattern != null) {
			String maskedBeanId = beanIdMaskPattern.mask(beanId);
			if(maskedBeanId != null) {
				beanId = maskedBeanId;
			}  else {
				log.warn("Unmatched the pattern can not be masking. beanId: " + beanId + " (maskPattern: " + beanIdMaskPattern + ")");
			}
		}

		if(classScanFilter != null) {
			beanId = classScanFilter.filter(beanId, className, scannedClass);
			if(beanId == null) {
				return false;
			}
		}

		boolean valid = true;
		
		if(filterParameters != null) {
			String[] excludePatterns = filterParameters.getStringArray(FilterParameters.exclude);
			
			if(excludePatterns != null) {
				for(String excludePattern : excludePatterns) {
					WildcardPattern wildcardPattern = wildcardPatternCache.get(excludePattern);
					if(wildcardPattern == null) {
						wildcardPattern = new WildcardPattern(excludePattern, ClassUtils.PACKAGE_SEPARATOR_CHAR);
						wildcardPatternCache.put(excludePattern, wildcardPattern);
					}
					if(wildcardPattern.matches(className)) {
						valid = false;
						break;
					}
				}
			}
		}
		
		if(valid) {
			scannedClasses.put(beanId, scannedClass);
			
			if(log.isTraceEnabled())
				log.trace("scanned beanClass {beanId: " + beanId + ", className: " + className + "}");
		}
		
		return valid;
	}

}
