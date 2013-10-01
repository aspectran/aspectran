package com.aspectran.core.context.bean.loader;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.wildcard.WildcardMatcher;
import com.aspectran.core.util.wildcard.WildcardPattern;

public class BeanClassLoader {

	private final char RESOURCE_NAME_SPEPARATOR = '/';

	private final ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

	private String beanIdPattern;

	public BeanClassLoader(String beanIdPattern) {
		this.beanIdPattern = beanIdPattern;
	}

	public Map<String, Class<?>> loadClasses(String classNamePattern) throws IOException, ClassNotFoundException {
		classNamePattern = classNamePattern.replace(ClassUtils.PACKAGE_SEPARATOR, RESOURCE_NAME_SPEPARATOR);

		String basePackageName = determineBasePackageName(classNamePattern);
		System.out.println("basePackageName: " + basePackageName);

		String subPattern = classNamePattern.substring(basePackageName.length());
		System.out.println("subPattern: " + subPattern);

		
		WildcardPattern pattern = new WildcardPattern(subPattern, RESOURCE_NAME_SPEPARATOR);
		WildcardMatcher matcher = new WildcardMatcher(pattern);
		
		Enumeration<URL> resources = classLoader.getResources(basePackageName);
		Map<String, Class<?>> classMap = new LinkedHashMap<String, Class<?>>();
		
		while(resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			
			if(isJarResource(resource)) {
				getClassesFromJarResources(resource, matcher);
			} else {
				//getClasses(resource.toString(), matcher);
			}
		}
		
		return classMap;

	}

	protected Set<Class<?>> getClassesFromJarResources(URL resource, WildcardMatcher matcher) throws IOException, ClassNotFoundException {
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
			if(!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
				// Root entry path must end with slash to allow for proper matching.
				// The Sun JRE does not return a slash here, but BEA JRockit does.
				rootEntryPath = rootEntryPath + "/";
			}
			Set<Class<?>> result = new LinkedHashSet<Class<?>>();
			for(Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String entryPath = entry.getName();
				System.out.println("entryPath: " + entryPath);
				System.out.println("rootEntryPath: " + rootEntryPath);
				if(entryPath.startsWith(rootEntryPath) && entryPath.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
					String relativePath = entryPath.substring(rootEntryPath.length(), entryPath.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
					System.out.println("relativePath: " + relativePath);
					
					if(matcher.matches(relativePath)) {
						String className = (rootEntryPath + relativePath);
						className = className.replace(RESOURCE_NAME_SPEPARATOR, ClassUtils.PACKAGE_SEPARATOR);
						
						System.out.println("[clazz] " + className);
						Class<?> clazz = classLoader.loadClass(className);
						result.add(clazz);
					}
				}
			}
			return result;
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
	
	private String determineBasePackageName(String classNamePattern) {
		WildcardPattern pattern = new WildcardPattern(classNamePattern, RESOURCE_NAME_SPEPARATOR);
		WildcardMatcher matcher = new WildcardMatcher(pattern);

		boolean matched = matcher.matches(classNamePattern);

		if(!matched)
			return null;
		
		StringBuilder sb = new StringBuilder();

		while(matcher.hasNext()) {
			String str = matcher.next();

			if(matcher.hasWildcards(str))
				break;

			sb.append(str).append(RESOURCE_NAME_SPEPARATOR);
		}

		return sb.toString();
	}

	protected boolean isJarResource(URL url) throws IOException {
		String protocol = url.getProtocol();
		return (ResourceUtils.URL_PROTOCOL_JAR.equals(protocol) || ResourceUtils.URL_PROTOCOL_ZIP.equals(protocol));
	}

	/**
	 * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private Class[] getClasses(String packageName, WildcardMatcher matcher) throws ClassNotFoundException, IOException {
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while(resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for(File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if(!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for(File file : files) {
			if(file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + ClassUtils.PACKAGE_SEPARATOR + file.getName()));
			} else if(file.getName().endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
				classes.add(Class.forName(packageName + ClassUtils.PACKAGE_SEPARATOR + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	public static void main(String[] args) {
		try {
			BeanClassLoader loader = new BeanClassLoader("");
			loader.loadClasses("com.**.scope.**.*Xml*");
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
