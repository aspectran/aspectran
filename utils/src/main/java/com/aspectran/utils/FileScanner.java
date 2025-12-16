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

import com.aspectran.utils.wildcard.WildcardMatcher;
import com.aspectran.utils.wildcard.WildcardPattern;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.aspectran.utils.PathUtils.REGULAR_FILE_SEPARATOR_CHAR;

/**
 * A utility class that finds files matching a given wildcard pattern.
 * <p>The path separator is always a forward slash ('/') regardless of the operating system.
 * This scanner can be initialized with a base path to resolve relative patterns.</p>
 *
 * @author Juho Jeong
 * @since 1.3.0
 */
public class FileScanner {

    private final String basePath;

    /**
     * Creates a new FileScanner without a base path.
     */
    public FileScanner() {
        this(null);
    }

    /**
     * Creates a new FileScanner with the specified base path.
     * @param basePath the base path to scan from
     */
    public FileScanner(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Scans for files matching the given path pattern.
     * @param filePathPattern the wildcard pattern for file paths to match
     * @return a map of found files, with their relative paths as keys
     */
    public Map<String, File> scan(String filePathPattern) {
        Map<String, File> scannedFiles = new LinkedHashMap<>();
        scan(filePathPattern, scannedFiles);
        return scannedFiles;
    }

    /**
     * Scans for files matching the given path pattern and stores them in the provided map.
     * @param filePathPattern the wildcard pattern for file paths to match
     * @param scannedFiles the map to store the found files in
     */
    public void scan(String filePathPattern, @NonNull Map<String, File> scannedFiles) {
        scan(filePathPattern, scannedFiles::put);
    }

    /**
     * Scans for files matching the given path pattern and processes them with the given handler.
     * @param filePathPattern the wildcard pattern for file paths to match
     * @param saveHandler the handler to process each found file
     */
    public void scan(String filePathPattern, SaveHandler saveHandler) {
        if (filePathPattern == null) {
            throw new IllegalArgumentException("filePathPattern must not be null");
        }

        WildcardPattern pattern = WildcardPattern.compile(filePathPattern, REGULAR_FILE_SEPARATOR_CHAR);
        WildcardMatcher matcher = new WildcardMatcher(pattern);
        matcher.separate(filePathPattern);

        StringBuilder sb = new StringBuilder();
        while (matcher.hasNext()) {
            String term = matcher.next();
            if (!term.isEmpty()) {
                if (!WildcardPattern.hasWildcards(term)) {
                    if (!sb.isEmpty()) {
                        sb.append(REGULAR_FILE_SEPARATOR_CHAR);
                    }
                    sb.append(term);
                } else {
                    break;
                }
            } else {
                sb.append(REGULAR_FILE_SEPARATOR_CHAR);
            }
        }

        String basePath = sb.toString();
        scan(basePath, matcher, saveHandler);
    }

    /**
     * Scans for files under a given base path that match the provided pattern.
     * @param basePath the path of the directory to start scanning from
     * @param filePathPattern the wildcard pattern to match against files
     * @return a map of found files, with their relative paths as keys
     */
    public Map<String, File> scan(String basePath, String filePathPattern) {
        Map<String, File> scannedFiles = new LinkedHashMap<>();
        scan(basePath, filePathPattern, scannedFiles);
        return scannedFiles;
    }

    /**
     * Scans for files under a given base path that match the provided pattern
     * and stores them in the provided map.
     * @param basePath the path of the directory to start scanning from
     * @param filePathPattern the wildcard pattern to match against files
     * @param scannedFiles the map to store the found files in
     */
    public void scan(String basePath, String filePathPattern, @NonNull Map<String, File> scannedFiles) {
        scan(basePath, filePathPattern, scannedFiles::put);
    }

    /**
     * Scans for files under a given base path that match the provided pattern
     * and processes them with the given handler.
     * @param basePath the path of the directory to start scanning from
     * @param filePathPattern the wildcard pattern to match against files
     * @param saveHandler the handler to process each found file
     */
    public void scan(@NonNull String basePath, String filePathPattern, SaveHandler saveHandler) {
        WildcardPattern pattern = WildcardPattern.compile(filePathPattern, REGULAR_FILE_SEPARATOR_CHAR);
        WildcardMatcher matcher = new WildcardMatcher(pattern);
        if (basePath.charAt(basePath.length() - 1) == REGULAR_FILE_SEPARATOR_CHAR) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        scan(basePath, matcher, saveHandler);
    }

    /**
     * Recursively scans a directory to find all files, matching them against the given matcher.
     * @param targetPath the path of the directory to scan
     * @param matcher the wildcard matcher to test against file paths
     * @param saveHandler the handler to process found files
     */
    protected void scan(String targetPath, WildcardMatcher matcher, SaveHandler saveHandler) {
        File target;
        if (StringUtils.hasText(basePath)) {
            target = new File(basePath, targetPath);
        } else {
            target = new File(targetPath);
        }
        if (!target.exists()) {
            return;
        }
        target.listFiles(file -> {
            String filePath = targetPath + REGULAR_FILE_SEPARATOR_CHAR + file.getName();
            if (file.isDirectory()) {
                scan(filePath, matcher, saveHandler);
            } else {
                if (matcher.matches(filePath)) {
                    saveHandler.save(filePath, file);
                }
            }
            return false;
        });
    }

    /**
     * A handler for processing files found during a scan.
     */
    public interface SaveHandler {

        /**
         * Called when a matching file is found.
         * @param filePath the relative path of the file from the scan root
         * @param scannedFile the found {@link File} object
         */
        void save(String filePath, File scannedFile);

    }

}
