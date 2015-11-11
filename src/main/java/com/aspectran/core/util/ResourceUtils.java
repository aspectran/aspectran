/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
package com.aspectran.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * A class to simplify access to ResourceUtils through the classloader.
 */
public class ResourceUtils {

	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	public static final String FILE_URL_PREFIX = "file:";

	public static final String JAR_URL_PREFIX = "jar:";

	public static final String JAR_FILE_SUFFIX = ".jar";

	public static final String ZIP_FILE_SUFFIX = ".zip";

	public static final String URL_PROTOCOL_FILE = "file";

	public static final String URL_PROTOCOL_JAR = "jar";

	public static final String URL_PROTOCOL_ZIP = "zip";

	public static final String URL_PROTOCOL_VFSZIP = "vfszip";

	public static final String URL_PROTOCOL_WSJAR = "wsjar";

	public static final String URL_PROTOCOL_CODE_SOURCE = "code-source";

	public static final String JAR_URL_SEPARATOR = "!/";
	
	public static final String RESOURCE_NAME_SPEPARATOR = "/";

	public static final char RESOURCE_NAME_SPEPARATOR_CHAR = '/';
	
	public static boolean isUrl(String resourceLocation) {
		if(resourceLocation == null) {
			return false;
		}
		if(resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			return true;
		}
		try {
			new URL(resourceLocation);
			return true;
		} catch(MalformedURLException ex) {
			return false;
		}
	}

	public static URL getURL(String resourceLocation, ClassLoader classLoader) throws FileNotFoundException {
		Assert.notNull(resourceLocation, "Resource location must not be null");
		if(resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
			URL url = classLoader.getResource(path);
			if(url == null) {
				String description = "class path resource [" + path + "]";
				throw new FileNotFoundException(description + " cannot be resolved to URL because it does not exist");
			}
			return url;
		}
		try {
			// try URL
			return new URL(resourceLocation);
		} catch(MalformedURLException ex) {
			// no URL -> treat as file path
			try {
				return new File(resourceLocation).toURI().toURL();
			} catch(MalformedURLException ex2) {
				throw new FileNotFoundException("Resource location [" + resourceLocation
						+ "] is neither a URL not a well-formed file path");
			}
		}
	}

	public static File getFile(String resourceLocation, ClassLoader classLoader) throws FileNotFoundException {
		Assert.notNull(resourceLocation, "Resource location must not be null");
		if(resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
			String description = "class path resource [" + path + "]";
			URL url = classLoader.getResource(path);
			if(url == null) {
				throw new FileNotFoundException(description + " cannot be resolved to absolute file path "
						+ "because it does not reside in the file system");
			}
			return getFile(url, description);
		}
		try {
			// try URL
			return getFile(new URL(resourceLocation));
		} catch(MalformedURLException ex) {
			// no URL -> treat as file path
			return new File(resourceLocation);
		}
	}

	public static File getFile(URL resourceUrl) throws FileNotFoundException {
		return getFile(resourceUrl, "URL");
	}

	public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
		Assert.notNull(resourceUrl, "Resource URL must not be null");
		if(!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
			throw new FileNotFoundException(description + " cannot be resolved to absolute file path "
					+ "because it does not reside in the file system: " + resourceUrl);
		}
		try {
			return new File(toURI(resourceUrl).getSchemeSpecificPart());
		} catch(URISyntaxException ex) {
			// Fallback for URLs that are not valid URIs (should hardly ever happen).
			return new File(resourceUrl.getFile());
		}
	}

	public static File getFile(URI resourceUri) throws FileNotFoundException {
		return getFile(resourceUri, "URI");
	}

	public static File getFile(URI resourceUri, String description) throws FileNotFoundException {
		Assert.notNull(resourceUri, "Resource URI must not be null");
		if(!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
			throw new FileNotFoundException(description + " cannot be resolved to absolute file path "
					+ "because it does not reside in the file system: " + resourceUri);
		}
		return new File(resourceUri.getSchemeSpecificPart());
	}

	public static boolean isJarURL(URL url) {
		return URL_PROTOCOL_JAR.equals(url.getProtocol());
	}

	public static boolean isJarSimilarURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol)
				|| URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol)
				|| (URL_PROTOCOL_CODE_SOURCE.equals(protocol) && url.getPath().indexOf(JAR_URL_SEPARATOR) != -1));
	}
	
	public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
		String urlFile = jarUrl.getFile();
		int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
		if(separatorIndex != -1) {
			String jarFile = urlFile.substring(0, separatorIndex);
			try {
				return new URL(jarFile);
			} catch(MalformedURLException ex) {
				// Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
				// This usually indicates that the jar file resides in the file system.
				if(!jarFile.startsWith(RESOURCE_NAME_SPEPARATOR)) {
					jarFile = RESOURCE_NAME_SPEPARATOR + jarFile;
				}
				return new URL(FILE_URL_PREFIX + jarFile);
			}
		} else {
			return jarUrl;
		}
	}

	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}

	public static URI toURI(String location) throws URISyntaxException {
		return new URI(StringUtils.replace(location, " ", "%20"));
	}

	/**
	 * Returns the URL of the resource on the classpath
	 * 
	 * @param loader The classloader used to load the resource
	 * @param resource The resource to find
	 * @return The resource
	 * @throws IOException If the resource cannot be found or read
	 */
	public static URL getResourceURL(String resource, ClassLoader loader) throws IOException {
		URL url = null;
		if(loader != null)
			url = loader.getResource(resource);
		if(url == null)
			url = ClassLoader.getSystemResource(resource);
		if(url == null)
			throw new IOException("Could not find resource " + resource);
		return url;
	}

	/**
	 * Returns a resource on the classpath as a Stream object.
	 *
	 * @param resource The resource to find
	 * @param classLoader the class loader
	 * @return The resource
	 * @throws IOException If the resource cannot be found or read
	 */
	public static InputStream getResourceAsStream(String resource, ClassLoader classLoader) throws IOException {
		InputStream in = null;
		if(classLoader != null)
			in = classLoader.getResourceAsStream(resource);
		if(in == null)
			in = ClassLoader.getSystemResourceAsStream(resource);
		if(in == null)
			throw new IOException("Could not find resource " + resource);
		return in;
	}

	/**
	 * Returns a resource on the classpath as a Properties object.
	 *
	 * @param resource The resource to find
	 * @param classLoader the class loader
	 * @return The resource
	 * @throws IOException If the resource cannot be found or read
	 */
	public static Properties getResourceAsProperties(String resource, ClassLoader classLoader) throws IOException {
		Properties props = new Properties();
		InputStream in = null;
		String propfile = resource;
		in = getResourceAsStream(propfile, classLoader);
		props.load(in);
		in.close();
		return props;
	}

	/**
	 * Returns a resource on the classpath as a Reader object.
	 *
	 * @param resource The resource to find
	 * @param classLoader the class loader
	 * @return The resource
	 * @throws IOException If the resource cannot be found or read
	 */
	public static Reader getResourceAsReader(String resource, ClassLoader classLoader) throws IOException {
		return new InputStreamReader(getResourceAsStream(resource, classLoader));
	}

	/**
	 * Returns a resource on the classpath as a File object.
	 *
	 * @param resource  the resource to find
	 * @param classLoader the class loader
	 * @return The resource
	 * @throws IOException If the resource cannot be found or read
	 */
	public static File getResourceAsFile(String resource, ClassLoader classLoader) throws IOException {
		return new File(getResourceURL(resource, classLoader).getFile());
	}

	/**
	 * Gets a URL as an input stream.
	 *
	 * @param urlString  the URL to get
	 * @return An input stream with the data from the URL
	 * @throws IOException If the resource cannot be found or read
	 */
	public static InputStream getUrlAsStream(String urlString) throws IOException {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		return conn.getInputStream();
	}

	/**
	 * Gets a URL as a Reader
	 * 
	 * @param urlString  the URL to get
	 * @return A Reader with the data from the URL
	 * @throws IOException If the resource cannot be found or read
	 */
	public static Reader getUrlAsReader(String urlString) throws IOException {
		return new InputStreamReader(getUrlAsStream(urlString));
	}

	/**
	 * Gets a URL as a Properties object
	 * 
	 * @param urlString  the URL to get
	 * @return A Properties object with the data from the URL
	 * @throws IOException If the resource cannot be found or read
	 */
	public static Properties getUrlAsProperties(String urlString) throws IOException {
		Properties props = new Properties();
		InputStream in = null;
		String propfile = urlString;
		in = getUrlAsStream(propfile);
		props.load(in);
		in.close();
		return props;
	}
	
	/**
	 * Gets the class loader.
	 *
	 * @param clazz the clazz
	 * @return the class loader
	 */
	public static ClassLoader getClassLoader(Class<?> clazz) {
		ClassLoader cl = clazz.getClassLoader();
		
		if(cl == null)
			cl = ClassLoader.getSystemClassLoader();
		
		return cl;
	}
	
}
