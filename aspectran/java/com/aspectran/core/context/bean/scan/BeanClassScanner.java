package com.aspectran.core.context.bean.scan;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.context.loader.AspectranClassLoader;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.wildcard.WildcardMatcher;
import com.aspectran.core.util.wildcard.WildcardPattern;

public class BeanClassScanner {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(BeanClassScanner.class);
	
	private static final char BEAN_ID_WILDCARD_DELIMITER = '*';
	
	private final ClassLoader classLoader;

	private final String beanIdPrefix;

	private final String beanIdSuffix;
	
	public BeanClassScanner(String beanIdPattern, ClassLoader classLoader) {
		int wildcardStartIndex = beanIdPattern.indexOf(BEAN_ID_WILDCARD_DELIMITER);
		
		if(wildcardStartIndex == -1) {
			this.beanIdPrefix = beanIdPattern;
			this.beanIdSuffix = null;
		} else {
			if(wildcardStartIndex == 0)
				this.beanIdPrefix = null;
			else
				this.beanIdPrefix = beanIdPattern.substring(0, wildcardStartIndex);
			
			if(wildcardStartIndex + 1 == beanIdPattern.length())
				this.beanIdSuffix = null;
			else
				this.beanIdSuffix = beanIdPattern.substring(wildcardStartIndex + 1);
		}
		
		this.classLoader = classLoader;
		
		//System.out.println("beanIdPrefix: " + beanIdPrefix);
		//System.out.println("beanIdSuffix: " + beanIdSuffix);
	}

	public Map<String, Class<?>> scanClass(String classNamePattern) throws IOException, ClassNotFoundException {
		Map<String, Class<?>> scanClasses = new LinkedHashMap<String, Class<?>>();
		
		scanClass(classNamePattern, scanClasses);
		
		return scanClasses;
	}
	
	public void scanClass(String classNamePattern, Map<String, Class<?>> scanClasses) {
		try {
			classNamePattern = classNamePattern.replace(ClassUtils.PACKAGE_SEPARATOR_CHAR, ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR);
			//System.out.println("classNamePattern: " + classNamePattern);
	
			String basePackageName = determineBasePackageName(classNamePattern);
			//System.out.println("basePackageName: " + basePackageName);
	
			String subPattern;
			
			if(classNamePattern.length() > basePackageName.length())
				subPattern = classNamePattern.substring(basePackageName.length());
			else
				subPattern = StringUtils.EMPTY;
			
			//System.out.println("subPattern: " + subPattern);
	
			WildcardPattern pattern = WildcardPattern.compile(subPattern, ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR);
			WildcardMatcher matcher = new WildcardMatcher(pattern);
			
			Enumeration<URL> resources = classLoader.getResources(basePackageName);
			
			if(basePackageName != null && !basePackageName.endsWith(ResourceUtils.RESOURCE_NAME_SPEPARATOR))
				basePackageName += ResourceUtils.RESOURCE_NAME_SPEPARATOR;
			
			while(resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				//System.out.println("classNamePattern: " + classNamePattern + "==scanClass=====" + resource.getFile());
				
				logger.debug("Bean Scanning: {} at {}", classNamePattern, resource.getFile());
				
				if(isJarResource(resource)) {
					scanClassFromJarResource(resource, matcher, scanClasses);
				} else {
					scanClass(resource.getFile(), basePackageName, null,  matcher, scanClasses);
				}
			}
		} catch(IOException e) {
			throw new BeanClassScanningFailedException("bean-class scanning failed.", e);
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
	private void scanClass(final String targetPath, final String basePackageName, final String relativePackageName, final WildcardMatcher matcher, final Map<String, Class<?>> scanClasses) {
		//System.out.println("@basePackageName: " + basePackageName);
		//System.out.println("@relativePackageName: " + relativePackageName);
		//System.out.println("@basePath: " + basePath);
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
					//System.out.println("-relativePackageName2: " + relativePackageName2);
					scanClass(basePath2, basePackageName, relativePackageName2, matcher, scanClasses);
				} else if(file.getName().endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
					String className;
					if(relativePackageName != null)
						className = basePackageName + relativePackageName + file.getName().substring(0, file.getName().length() - ClassUtils.CLASS_FILE_SUFFIX.length());
					else
						className = basePackageName + file.getName().substring(0, file.getName().length() - ClassUtils.CLASS_FILE_SUFFIX.length());
					String relativePath = className.substring(basePackageName.length(), className.length());
					//System.out.println("  -file.getName(): " + file.getName());
					//System.out.println("  -relativePath: " + relativePath);
					if(matcher.matches(relativePath)) {
						Class<?> classType = loadClass(className);
						String beanId = combineBeanId(relativePath);
						//System.out.println("scaned  [clazz] " + className);
						//System.out.println("scaned  [beanId] " + combineBeanId(relativePath));
						logger.trace("beanClass {beanId: " + beanId + ", className: " + className + "}");
						scanClasses.put(beanId, classType);
					}
				}
				return false;
			}
		});
	}

	protected void scanClassFromJarResource(URL resource, WildcardMatcher matcher, Map<String, Class<?>> scanClasses) throws IOException {
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
			//System.out.println("**JarURLConnection entryName: " + entryNamePrefix);
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
				//System.out.println("entryName: " + entryName);
				//System.out.println("  entryNamePrefix: " + entryNamePrefix);
				if(entryName.startsWith(entryNamePrefix) && entryName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
					String entryNameSuffix = entryName.substring(entryNamePrefix.length(), entryName.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
					//System.out.println("  entryNameSuffix: " + entryNameSuffix);
					
					if(matcher.matches(entryNameSuffix)) {
						//System.out.println("entryName: " + entryName);
						//System.out.println("  entryNamePrefix: " + entryNamePrefix);
						//System.out.println("  entryNameSuffix: " + entryNameSuffix);
						String className = entryNamePrefix + entryNameSuffix;
						Class<?> classType = loadClass(className);
						String beanId = combineBeanId(entryNameSuffix);
						logger.trace("beanClass {beanId: " + beanId + ", className: " + className + "} from jar: " + jarFile.getName());
						//System.out.println("  [clazz] " + className);
						//System.out.println("  [beanId] " + combineBeanId(relativePath));
						scanClasses.put(beanId, classType);
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

	private String combineBeanId(String relativePath) {
		String beanId;
		
		if(beanIdPrefix != null && beanIdSuffix != null) {
			beanId = beanIdPrefix + relativePath + beanIdSuffix;
		} else if(beanIdPrefix != null) {
			beanId = beanIdPrefix + relativePath;
		} else if(beanIdSuffix != null) {
			beanId = relativePath + beanIdSuffix;
		} else {
			beanId = relativePath;
		}
		
		return beanId.replace(ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR, ClassUtils.PACKAGE_SEPARATOR_CHAR);
	}
	
	private Class<?> loadClass(String className) {
		className = className.replace(ResourceUtils.RESOURCE_NAME_SPEPARATOR_CHAR, ClassUtils.PACKAGE_SEPARATOR_CHAR);
		
		try {
			return classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new BeanClassScanningFailedException("bean-class loading failed. class name: " + className, e);
		}
	}
	
	public static void main(String[] args) {
		try {
			BeanClassScanner loader = new BeanClassScanner("component.*ZZZ", AspectranClassLoader.getDefaultClassLoader());
			//loader.scanClass("com.**.*Sql*");
			//loader.scanClass("com.i*");
			loader.scanClass("com.*");
			System.out.println(loader.getClass().getName());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		/*
		Class<BeanClassLoader> clazz = BeanClassLoader.class;
		URL resource = clazz.getResource(".");
		System.out.println("resource: " + resource);

		ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
		try {
			Enumeration<URL> resources = classLoader.getResources("com");
			System.out.println("resources:");
			while(resources.hasMoreElements()) {
				URL nextElement = resources.nextElement();
				System.out.println(nextElement);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
*/
	}
}
