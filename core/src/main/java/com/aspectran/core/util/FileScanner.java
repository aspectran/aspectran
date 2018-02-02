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

import com.aspectran.core.util.wildcard.WildcardMatcher;
import com.aspectran.core.util.wildcard.WildcardPattern;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A utility class that finds files corresponding to a given pattern.
 * Note that the file separator always uses a slash (/), regardless of the OS.
 *
 * @author Juho Jeong
 * @since 1.3.0
 */
public class FileScanner {

    private static final char FILE_SEPARATOR = '/';

    private final String basePath;

    public FileScanner() {
        this(null);
    }

    public FileScanner(String basePath) {
        this.basePath = basePath;
    }

    public Map<String, File> scan(String filePathPattern) {
        final Map<String, File> scannedFiles = new LinkedHashMap<>();
        scan(filePathPattern, scannedFiles);
        return scannedFiles;
    }

    public void scan(String filePathPattern, final Map<String, File> scannedFiles) {
        scan(filePathPattern, (filePath, scannedFile) -> {
            scannedFiles.put(filePath, scannedFile);
        });
    }

    public void scan(String filePathPattern, SaveHandler saveHandler) {
        if (filePathPattern == null) {
            throw new IllegalArgumentException("Argument 'filePathPattern' must not be null");
        }

        WildcardPattern pattern = WildcardPattern.compile(filePathPattern, FILE_SEPARATOR);
        WildcardMatcher matcher = new WildcardMatcher(pattern);
        matcher.separate(filePathPattern);

        StringBuilder sb = new StringBuilder();

        while (matcher.hasNext()) {
            String term = matcher.next();
            if (term.length() > 0) {
                if (!WildcardPattern.hasWildcards(term)) {
                    if (sb.length() > 0)
                        sb.append(FILE_SEPARATOR);
                    sb.append(term);
                } else {
                    break;
                }
            } else {
                sb.append(FILE_SEPARATOR);
            }
        }

        String basePath = sb.toString();

        scan(basePath, matcher, saveHandler);
    }

    public Map<String, File> scan(String basePath, String filePathPattern) {
        final Map<String, File> scannedFiles = new LinkedHashMap<>();
        scan(basePath, filePathPattern, scannedFiles);
        return scannedFiles;
    }

    public void scan(String basePath, String filePathPattern, final Map<String, File> scannedFiles) {
        scan(basePath, filePathPattern, (filePath, scannedFile) -> {
            scannedFiles.put(filePath, scannedFile);
        });
    }

    public void scan(String basePath, String filePathPattern, SaveHandler saveHandler) {
        WildcardPattern pattern = WildcardPattern.compile(filePathPattern, FILE_SEPARATOR);
        WildcardMatcher matcher = new WildcardMatcher(pattern);
        if (basePath.charAt(basePath.length() - 1) == FILE_SEPARATOR) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        scan(basePath, matcher, saveHandler);
    }

    protected void scan(final String targetPath, final WildcardMatcher matcher, final SaveHandler saveHandler) {
        final File target;
        if (basePath != null) {
            target = new File(basePath, targetPath);
        } else {
            target = new File(targetPath);
        }
        if (!target.exists()) {
            return;
        }
        target.listFiles(file -> {
            String filePath = targetPath + FILE_SEPARATOR + file.getName();

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

    public interface SaveHandler {

        void save(String filePath, File scannedFile);

    }

}
