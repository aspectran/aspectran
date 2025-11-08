/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.wildcard.WildcardMatcher;
import com.aspectran.utils.wildcard.WildcardPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static com.aspectran.utils.ClassUtils.PACKAGE_SEPARATOR_CHAR;
import static com.aspectran.utils.PathUtils.REGULAR_FILE_SEPARATOR;
import static com.aspectran.utils.PathUtils.REGULAR_FILE_SEPARATOR_CHAR;

/**
 * A utility class for scanning the classpath for classes that match certain criteria.
 * <p>This scanner can find classes in both standard file-system directories and in JAR files.</p>
 *
 * @author Juho Jeong
 */
public class ClassScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClassScanner.class);

    private final ClassLoader classLoader;

    /**
     * Creates a new ClassScanner with the given class loader.
     * @param classLoader the ClassLoader to use for scanning
     */
    public ClassScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Returns the ClassLoader used by this scanner.
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Finds all classes that match the given class name pattern.
     * @param classNamePattern the class name pattern to match (e.g., "com.example.**.service.*Service")
     * @return a map of scanned classes, with resource names as keys and class objects as values
     * @throws IOException if an I/O error occurs during scanning
     */
    public Map<String, Class<?>> scan(String classNamePattern) throws IOException {
        final Map<String, Class<?>> scannedClasses = new LinkedHashMap<>();
        scan(classNamePattern, scannedClasses);
        return scannedClasses;
    }

    /**
     * Finds all classes that match the given class name pattern and stores them in the provided map.
     * @param classNamePattern the class name pattern to match
     * @param scannedClasses the map to store the scanned classes in
     * @throws IOException if an I/O error occurs during scanning
     */
    public void scan(String classNamePattern, @NonNull final Map<String, Class<?>> scannedClasses) throws IOException {
        scan(classNamePattern, scannedClasses::put);
    }

    /**
     * Finds all classes that match the given class name pattern and processes them with the given handler.
     * @param classNamePattern the class name pattern to match
     * @param saveHandler the handler to process the found classes
     * @throws IOException if an I/O error occurs during scanning
     */
    public void scan(String classNamePattern, SaveHandler saveHandler) throws IOException {
        if (classNamePattern == null) {
            throw new IllegalArgumentException("classNamePattern must not be null");
        }

        classNamePattern = classNamePattern.replace(PACKAGE_SEPARATOR_CHAR, REGULAR_FILE_SEPARATOR_CHAR);
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

        WildcardPattern pattern = WildcardPattern.compile(subPattern, REGULAR_FILE_SEPARATOR_CHAR);
        WildcardMatcher matcher = new WildcardMatcher(pattern);

        Enumeration<URL> resources = classLoader.getResources(basePackageName);

        if (!StringUtils.endsWith(basePackageName, REGULAR_FILE_SEPARATOR_CHAR)) {
            basePackageName += REGULAR_FILE_SEPARATOR_CHAR;
        }

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if (logger.isDebugEnabled()) {
                logger.debug("Scanning components from {}", resource.getFile());
            }

            if (isJarResource(resource)) {
                scanFromJarResource(resource, matcher, saveHandler);
            } else {
                scan(resource.getFile(), basePackageName, null, matcher, saveHandler);
            }
        }
    }

    /**
     * Recursively scans a directory to find all class files, matching them against the given pattern.
     * @param targetPath the path of the directory to scan
     * @param basePackageName the base package name corresponding to the root of the scan
     * @param relativePackageName the current package name relative to the base package
     * @param matcher the wildcard matcher to test against class names
     * @param saveHandler the handler to process found classes
     */
    private void scan(String targetPath, String basePackageName, String relativePackageName,
                      WildcardMatcher matcher, SaveHandler saveHandler) {
        File target = new File(targetPath);
        if (!target.exists()) {
            return;
        }

        target.listFiles(file -> {
            String fileName = file.getName();
            if (file.isDirectory()) {
                String subPackageName;
                if (relativePackageName != null) {
                    subPackageName = relativePackageName + fileName + REGULAR_FILE_SEPARATOR;
                } else {
                    subPackageName = fileName + REGULAR_FILE_SEPARATOR;
                }

                String basePath2 = targetPath + fileName + REGULAR_FILE_SEPARATOR;
                scan(basePath2, basePackageName, subPackageName, matcher, saveHandler);
            } else if (fileName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
                String fn = fileName.substring(0, fileName.length() - ClassUtils.CLASS_FILE_SUFFIX.length());
                String className;
                if (relativePackageName != null) {
                    className = basePackageName + relativePackageName + fn;
                } else {
                    className = basePackageName + fn;
                }

                String relativePath = className.substring(basePackageName.length());
                if (matcher.matches(relativePath)) {
                    String resourceName = targetPath + fileName;
                    Class<?> targetClass = loadClass(className);
                    saveHandler.save(resourceName, targetClass);
                }
            }
            return false;
        });
    }

    /**
     * Scans a JAR file resource for classes matching the given pattern.
     * @param resource the JAR file URL to scan
     * @param matcher the wildcard matcher to test against class names
     * @param saveHandler the handler to process found classes
     * @throws IOException if an I/O error occurs while reading the JAR file
     */
    protected void scanFromJarResource(@NonNull URL resource, WildcardMatcher matcher, SaveHandler saveHandler)
            throws IOException {
        URLConnection conn = resource.openConnection();
        JarFile jarFile;
        String jarFileUrl;
        String entryNamePrefix;
        boolean newJarFile = false;

        if (conn instanceof JarURLConnection jarCon) {
            // Should usually be the case for traditional JAR files.
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
            if (!entryNamePrefix.endsWith(REGULAR_FILE_SEPARATOR)) {
                // Root entry path must end with slash to allow for proper matching.
                // The Sun JRE does not return a slash here, but BEA JRockit does.
                entryNamePrefix = entryNamePrefix + REGULAR_FILE_SEPARATOR;
            }

            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(entryNamePrefix) && entryName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
                    String entryNameSuffix = entryName.substring(entryNamePrefix.length(), entryName.length() -
                            ClassUtils.CLASS_FILE_SUFFIX.length());

                    if (matcher.matches(entryNameSuffix)) {
                        String resourceName = jarFileUrl + ResourceUtils.JAR_URL_SEPARATOR + entryName;
                        String className = entryNamePrefix + entryNameSuffix;
                        Class<?> targetClass = loadClass(className);
                        saveHandler.save(resourceName, targetClass);
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

    @NonNull
    private JarFile getJarFile(@NonNull String jarFileUrl) throws IOException {
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

    private boolean isJarResource(@NonNull URL url) {
        String protocol = url.getProtocol();
        return (ResourceUtils.URL_PROTOCOL_JAR.equals(protocol) || ResourceUtils.URL_PROTOCOL_ZIP.equals(protocol));
    }

    /**
     * Determines the base package name from a class name pattern that may contain wildcards.
     * The base package is the part of the pattern before the first wildcard character.
     * @param classNamePattern the class name pattern
     * @return the base package name, or {@code null} if the pattern is invalid
     */
    @Nullable
    private String determineBasePackageName(String classNamePattern) {
        WildcardPattern pattern = new WildcardPattern(classNamePattern, REGULAR_FILE_SEPARATOR_CHAR);
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
            sb.append(str).append(REGULAR_FILE_SEPARATOR_CHAR);
        }
        return sb.toString();
    }

    private Class<?> loadClass(String className) {
        className = className.replace(REGULAR_FILE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to load class: " + className, e);
        }
    }

    /**
     * A handler for processing classes found during a scan.
     */
    public interface SaveHandler {

        /**
         * Called when a matching class is found.
         * @param resourceName the name of the resource (e.g., file path or JAR entry path)
         * @param targetClass the loaded class object
         */
        void save(String resourceName, Class<?> targetClass);

    }

}
