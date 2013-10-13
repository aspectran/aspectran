package com.aspectran.core.context.bean.loader;

import java.io.File;
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

import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.wildcard.WildcardMatcher;
import com.aspectran.core.util.wildcard.WildcardPattern;

public class BeanClassLoader {

	private final char RESOURCE_PATH_SPEPARATOR = '/';

	private final char BEAN_ID_WILDCARD_DELIMITER = '*';
	
	private final ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

	private final String beanIdPrefix;

	private final String beanIdSuffix;
	
	public BeanClassLoader(String beanIdPattern) {
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
		
		//System.out.println("beanIdPrefix: " + beanIdPrefix);
		//System.out.println("beanIdSuffix: " + beanIdSuffix);
	}

	public Map<String, Class<?>> loadBeanClassMap(String classNamePattern) throws IOException, ClassNotFoundException {
		classNamePattern = classNamePattern.replace(ClassUtils.PACKAGE_SEPARATOR, RESOURCE_PATH_SPEPARATOR);

		String basePackageName = determineBasePackageName(classNamePattern);
		//System.out.println("basePackageName: " + basePackageName);

		String subPattern = classNamePattern.substring(basePackageName.length());
		//System.out.println("subPattern: " + subPattern);

		
		WildcardPattern pattern = WildcardPattern.compile(subPattern, RESOURCE_PATH_SPEPARATOR);
		WildcardMatcher matcher = new WildcardMatcher(pattern);
		
		Enumeration<URL> resources = classLoader.getResources(basePackageName);
		Map<String, Class<?>> classMap = new LinkedHashMap<String, Class<?>>();
		
		while(resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			
			if(isJarResource(resource)) {
				Map<String, Class<?>> map = findClassesFromJarResources(resource, matcher);
				classMap.putAll(map);
			} else {
				//System.out.println("========" + resource.getFile());
				findClasses(basePackageName, null, resource.getFile(), matcher);
			}
		}
		
		return classMap;

	}

	protected Map<String, Class<?>> findClassesFromJarResources(URL resource, WildcardMatcher matcher) throws IOException, ClassNotFoundException {
		URLConnection con = resource.openConnection();
		JarFile jarFile = null;
		String jarFileUrl = null;
		String rootEntryPath = null;
		boolean newJarFile = false;

		if(con instanceof JarURLConnection) {
			// Should usually be the case for traditional JAR files.
			JarURLConnection jarCon = (JarURLConnection)con;
			jarCon.setUseCaches(false);
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			JarEntry jarEntry = jarCon.getJarEntry();
			rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
		} else {
			// No JarURLConnection -> need to resort to URL file parsing.
			// We'll assume URLs of the format "jar:path!/entry", with the protocol
			// being arbitrary as long as following the entry format.
			// We'll also handle paths with and without leading "file:" prefix.
			String urlFile = resource.getFile();
			int separatorIndex = urlFile.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
			if(separatorIndex != -1) {
				jarFileUrl = urlFile.substring(0, separatorIndex);
				rootEntryPath = urlFile.substring(separatorIndex + ResourceUtils.JAR_URL_SEPARATOR.length());
				jarFile = getJarFile(jarFileUrl);
			} else {
				jarFile = new JarFile(urlFile);
				jarFileUrl = urlFile;
				rootEntryPath = "";
			}
			newJarFile = true;
		}

		try {
			//Looking for matching resources in jar file [" + jarFileUrl + "]"
			if(rootEntryPath.length() > 0 && rootEntryPath.charAt(rootEntryPath.length() - 1) != RESOURCE_PATH_SPEPARATOR) {
				// Root entry path must end with slash to allow for proper matching.
				// The Sun JRE does not return a slash here, but BEA JRockit does.
				rootEntryPath = rootEntryPath + RESOURCE_PATH_SPEPARATOR;
			}
			Map<String, Class<?>> classMap = new LinkedHashMap<String, Class<?>>();
			for(Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String entryPath = entry.getName();
				//System.out.println("entryPath: " + entryPath);
				//System.out.println("  rootEntryPath: " + rootEntryPath);
				if(entryPath.startsWith(rootEntryPath) && entryPath.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
					String relativePath = entryPath.substring(rootEntryPath.length(), entryPath.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
					//System.out.println("  relativePath: " + relativePath);
					
					if(matcher.matches(relativePath)) {
						//System.out.println("entryPath: " + entryPath);
						//System.out.println("  rootEntryPath: " + rootEntryPath);
						//System.out.println("  relativePath: " + relativePath);
						String className = rootEntryPath + relativePath;
						className = className.replace(RESOURCE_PATH_SPEPARATOR, ClassUtils.PACKAGE_SEPARATOR);
						
						//System.out.println("  [clazz] " + className);
						//System.out.println("  [beanId] " + combineBeanId(relativePath));
						Class<?> clazz = classLoader.loadClass(className);
						classMap.put(combineBeanId(relativePath), clazz);
					}
				}
			}
			return classMap;
		} finally {
			// Close jar file, but only if freshly obtained -
			// not from JarURLConnection, which might cache the file reference.
			if(newJarFile) {
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

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private Map<String, Class<?>> findClasses(String basePackageName, String relativePackageName, String basePath, WildcardMatcher matcher) throws ClassNotFoundException {
		//System.out.println("@basePackageName: " + basePackageName);
		//System.out.println("@relativePackageName: " + relativePackageName);
		//System.out.println("@basePath: " + basePath);
		File path = new File(basePath);
		if(!path.exists())
			return null;

		if(basePackageName != null && basePackageName.length() > 0 && basePackageName.charAt(basePackageName.length() - 1) != RESOURCE_PATH_SPEPARATOR)
			basePackageName += RESOURCE_PATH_SPEPARATOR;
		
		if(relativePackageName != null && relativePackageName.length() > 0 && relativePackageName.charAt(relativePackageName.length() - 1) != RESOURCE_PATH_SPEPARATOR)
			relativePackageName += RESOURCE_PATH_SPEPARATOR;
		
		
		Map<String, Class<?>> classMap = new LinkedHashMap<String, Class<?>>();
		File[] files = path.listFiles();
		
		for(File file : files) {
			if(file.isDirectory()) {
				assert !file.getName().contains(".");
				String relativePackageName2 = relativePackageName == null ? file.getName() : relativePackageName + file.getName();
				String basePath2 = basePath + file.getName() + RESOURCE_PATH_SPEPARATOR;
				//System.out.println("-relativePackageName2: " + relativePackageName2);
				Map<String, Class<?>> map = findClasses(basePackageName, relativePackageName2, basePath2, matcher);
				if(map != null)
					classMap.putAll(map);
			} else if(file.getName().endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
				String className = basePackageName + relativePackageName + file.getName().substring(0, file.getName().length() - ClassUtils.CLASS_FILE_SUFFIX.length());
				String relativePath = className.substring(basePackageName.length(), className.length());
				//System.out.println("  -file.getName(): " + file.getName());
				//System.out.println("  -relativePath: " + relativePath);
				
				if(matcher.matches(relativePath)) {
					className = className.replace(RESOURCE_PATH_SPEPARATOR, ClassUtils.PACKAGE_SEPARATOR);
					//System.out.println("  className: " + className);
					
					//System.out.println("  [clazz] " + className);
					//System.out.println("  [beanId] " + combineBeanId(relativePath));
					Class<?> clazz = classLoader.loadClass(className);
					classMap.put(combineBeanId(relativePath), clazz);
				}
			}
		}
		
		return classMap;
	}

	private String determineBasePackageName(String classNamePattern) {
		WildcardPattern pattern = new WildcardPattern(classNamePattern, RESOURCE_PATH_SPEPARATOR);
		WildcardMatcher matcher = new WildcardMatcher(pattern);

		boolean matched = matcher.matches(classNamePattern);

		if(!matched)
			return null;
		
		StringBuilder sb = new StringBuilder();

		while(matcher.hasNext()) {
			String str = matcher.next();

			if(WildcardPattern.hasWildcards(str))
				break;

			sb.append(str).append(RESOURCE_PATH_SPEPARATOR);
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
		
		return beanId.replace(RESOURCE_PATH_SPEPARATOR, ClassUtils.PACKAGE_SEPARATOR);
	}
	
	public static void main(String[] args) {
		try {
			BeanClassLoader loader = new BeanClassLoader("component.*ZZZ");
			loader.loadBeanClassMap("com.**.*Sql*");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
	}
}
