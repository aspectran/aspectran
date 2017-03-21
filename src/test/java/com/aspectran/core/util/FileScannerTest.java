/*
 * Copyright 2008-2017 Juho Jeong
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

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aspectran.core.util.wildcard.WildcardMatcher;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * The Class FileScannerTest.
 *
 * @since 1.3.0
 */
public class FileScannerTest {

	private static final char FILE_SEPARATOR = '/';
	
	private final String basePath;
	
	public FileScannerTest() {
		this(null);
	}
	
	public FileScannerTest(String basePath) {
		this.basePath = basePath;
	}

	public Map<String, File> scanFiles(String filePathPattern) {
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
		
		return scanFiles(basePath, matcher);
	}
	
	public Map<String, File> scanFiles(String basePath, String filePathPattern) {
		Map<String, File> scannedFiles = new LinkedHashMap<String, File>();
		
		scanFiles(basePath, filePathPattern, scannedFiles);
		
		return scannedFiles;
	}
	
	public void scanFiles(String basePath, String filePathPattern, Map<String, File> scannedFiles) {
		WildcardPattern pattern = WildcardPattern.compile(filePathPattern, FILE_SEPARATOR);
		WildcardMatcher matcher = new WildcardMatcher(pattern);
		
		if (basePath.charAt(basePath.length() - 1) == FILE_SEPARATOR) {
			basePath = basePath.substring(0, basePath.length() - 1);
		}
		
		scanFiles(basePath, matcher, scannedFiles);
	}
	
	private Map<String, File> scanFiles(String basePath, WildcardMatcher matcher) {
		Map<String, File> scannedFiles = new LinkedHashMap<String, File>();
		
		scanFiles(basePath, matcher, scannedFiles);
		
		return scannedFiles;
	}
	
	private void scanFiles(final String targetPath, final WildcardMatcher matcher, final Map<String, File> scannedFiles) {
		final File target;
		if (basePath != null)
			target = new File(basePath, targetPath);
		else
			target = new File(targetPath);
		
		if (!target.exists())
			return;

		target.listFiles(new FileFilter() {
			public boolean accept(File file) {
				String filePath = targetPath + FILE_SEPARATOR + file.getName();

				if (file.isDirectory()) {
					scanFiles(filePath, matcher, scannedFiles);
				} else {
					if (matcher.matches(filePath)) {
						putFile(scannedFiles, filePath, target);
					}
				}
				return false;
			}
		});
	}
	
	protected void putFile(Map<String, File> scannedFiles, String filePath, File scannedFile) {
		scannedFiles.put(filePath, scannedFile);
	}
	
	public static void main(String argv[]) {
		FileScannerTest scanner = new FileScannerTest(".");
		Map<String, File> files = scanner.scanFiles("/WEB-INF/jsp/pages/**/*.jsp");
		
		for (Map.Entry<String, File> entry : files.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}
	}
	
}
