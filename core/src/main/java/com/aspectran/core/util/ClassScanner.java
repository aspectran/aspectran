/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.util;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardMatcher;
import com.aspectran.core.util.wildcard.WildcardPattern;

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

/**
 * The Class ClassScanner.
 * 
 * @author Juho Jeong
 */
public class ClassScanner {

    private final Log log = LogFactory.getLog(ClassScanner.class);

    private final ClassLoader classLoader;

    public ClassScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Find all classes that match the class name pattern.
     *
     * @param classNamePattern the class name pattern
     * @return a Map for scanned classes
     * @throws IOException if an I/O error has occurred
     */
    public Map<String, Class<?>> scan(String classNamePattern) throws IOException {
        final Map<String, Class<?>> scannedClasses = new LinkedHashMap<>();
        scan(classNamePattern, scannedClasses);
        return scannedClasses;
    }

    /**
     * Find all classes that match the class name pattern.
     *
     * @param classNamePattern the class name pattern
     * @param scannedClasses the Map for scanned classes
     * @throws IOException if an I/O error has occurred
     */
    public void scan(String classNamePattern, final Map<String, Class<?>> scannedClasses) throws IOException {
        scan(classNamePattern, (resourceName, scannedClass) -> {
            scannedClasses.put(resourceName, scannedClass);
        });
    }

    /**
     * Find all classes that match the class name pattern.
     *
     * @param classNamePattern the class name pattern
     * @param saveHandler the save handler
     * @throws IOException if an I/O error has occurred
     */
    public void scan(String classNamePattern, SaveHandler saveHandler) throws IOException {
        if (classNamePattern == null) {
            throw new IllegalArgumentException("Argument 'classNamePattern' must not be null");
        }

        classNamePattern = classNamePattern.replace(ClassUtils.PACKAGE_SEPARATOR_CHAR, ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR);

        String basePackageName = determineBasePackageName(classNamePattern);
        if (basePackageName == null) {
            return;
        }

        String subPattern;
        if (classNamePattern.length() > basePackageName.length()) {
            subPattern = classNamePattern.substring(basePackageName.length());
        } else {
            subPattern = StringUtils.EMPTY;
        }

        WildcardPattern pattern = WildcardPattern.compile(subPattern, ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR);
        WildcardMatcher matcher = new WildcardMatcher(pattern);

        Enumeration<URL> resources = classLoader.getResources(basePackageName);

        if (!StringUtils.endsWith(basePackageName, ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR)) {
            basePackageName += ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR;
        }

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if (log.isDebugEnabled()) {
                log.debug("Scanning classes: " + classNamePattern + " at " + resource.getFile());
            }

            if (isJarResource(resource)) {
                scanFromJarResource(resource, matcher, saveHandler);
            } else {
                scan(resource.getFile(), basePackageName, null,  matcher, saveHandler);
            }
        }
    }

    /**
     * Recursive method used to find all classes in a given directory and sub dirs.
     *
     * @param targetPath the target path
     * @param basePackageName the base package name
     * @param relativePackageName the relative package name
     * @param matcher the matcher
     * @param saveHandler the save handler
     */
    private void scan(final String targetPath, final String basePackageName, final String relativePackageName,
                      final WildcardMatcher matcher, final SaveHandler saveHandler) {
        final File target = new File(targetPath);
        if (!target.exists()) {
            return;
        }

        target.listFiles(file -> {
            String fileName = file.getName();
            if (file.isDirectory()) {
                String relativePackageName2;
                if (relativePackageName == null) {
                    relativePackageName2 = fileName + ResourceUtils.REGULAR_FILE_SEPARATOR;
                } else {
                    relativePackageName2 = relativePackageName + fileName + ResourceUtils.REGULAR_FILE_SEPARATOR;
                }

                String basePath2 = targetPath + fileName + ResourceUtils.REGULAR_FILE_SEPARATOR;
                scan(basePath2, basePackageName, relativePackageName2, matcher, saveHandler);
            } else if (fileName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
                String className;
                if (relativePackageName != null) {
                    className = basePackageName + relativePackageName + fileName.substring(0, fileName.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
                } else {
                    className = basePackageName + fileName.substring(0, fileName.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
                }

                String relativePath = className.substring(basePackageName.length());
                if (matcher.matches(relativePath)) {
                    String resourceName = targetPath + fileName;
                    Class<?> classType = loadClass(className);
                    saveHandler.save(resourceName, classType);
                }
            }
            return false;
        });
    }

    protected void scanFromJarResource(URL resource, WildcardMatcher matcher, SaveHandler saveHandler) throws IOException {
        URLConnection conn = resource.openConnection();
        JarFile jarFile;
        String jarFileUrl;
        String entryNamePrefix;
        boolean newJarFile = false;

        if (conn instanceof JarURLConnection) {
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
            if (separatorIndex != -1) {
                jarFileUrl = urlFile.substring(0, separatorIndex);
                entryNamePrefix = urlFile.substring(separatorIndex + ResourceUtils.JAR_URL_SEPARATOR.length());
                jarFile = getJarFile(jarFileUrl);
            } else {
                jarFile = new JarFile(urlFile);
                jarFileUrl = urlFile;
                entryNamePrefix = "";
            }
            newJarFile = true;
        }

        try {
            //Looking for matching resources in jar file [" + jarFileUrl + "]"
            if (!entryNamePrefix.endsWith(ResourceUtils.REGULAR_FILE_SEPARATOR)) {
                // Root entry path must end with slash to allow for proper matching.
                // The Sun JRE does not return a slash here, but BEA JRockit does.
                entryNamePrefix = entryNamePrefix + ResourceUtils.REGULAR_FILE_SEPARATOR;
            }

            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(entryNamePrefix) && entryName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
                    String entryNameSuffix = entryName.substring(entryNamePrefix.length(), entryName.length() - ClassUtils.CLASS_FILE_SUFFIX.length());

                    if (matcher.matches(entryNameSuffix)) {
                        String resourceName = jarFileUrl + ResourceUtils.JAR_URL_SEPARATOR + entryName;
                        String className = entryNamePrefix + entryNameSuffix;
                        Class<?> classType = loadClass(className);
                        saveHandler.save(resourceName, classType);
                    }
                }
            }
        } finally {
            // Close jar file, but only if freshly obtained -
            // not from JarURLConnection, which might cache the file reference.
            if (newJarFile) {
                jarFile.close();
            }
        }
    }

    private JarFile getJarFile(String jarFileUrl) throws IOException {
        if (jarFileUrl.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            try {
                return new JarFile(ResourceUtils.toURI(jarFileUrl).getSchemeSpecificPart());
            } catch (URISyntaxException ex) {
                // Fallback for URLs that are not valid URIs (should hardly ever happen).
                return new JarFile(jarFileUrl.substring(ResourceUtils.FILE_URL_PREFIX.length()));
            }
        } else {
            return new JarFile(jarFileUrl);
        }
    }

    private boolean isJarResource(URL url) throws IOException {
        String protocol = url.getProtocol();
        return (ResourceUtils.URL_PROTOCOL_JAR.equals(protocol) || ResourceUtils.URL_PROTOCOL_ZIP.equals(protocol));
    }

    private String determineBasePackageName(String classNamePattern) {
        WildcardPattern pattern = new WildcardPattern(classNamePattern, ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR);
        WildcardMatcher matcher = new WildcardMatcher(pattern);

        boolean matched = matcher.matches(classNamePattern);
        if (!matched) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        while (matcher.hasNext()) {
            String str = matcher.next();
            if (WildcardPattern.hasWildcards(str)) {
                break;
            }
            sb.append(str).append(ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR);
        }
        return sb.toString();
    }

    private Class<?> loadClass(String className) {
        className = className.replace(ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR, ClassUtils.PACKAGE_SEPARATOR_CHAR);

        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to load class: " + className, e);
        }
    }

    public interface SaveHandler {
        void save(String resourceName, Class<?> scannedClass);
    }

}
