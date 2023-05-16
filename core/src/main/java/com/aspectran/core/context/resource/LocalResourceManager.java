/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.context.resource;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.util.FileCopyUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.SystemUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The Class LocalResourceManager.
 *
 * <p>Created: 2014. 12. 18 PM 5:51:13</p>
 */
public class LocalResourceManager extends ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(LocalResourceManager.class);

    private static final String TEMP_RESOURCE_DIRNAME_PREFIX = "_resource_";

    private final String resourceLocation;

    private final int resourceNameStart;

    private final SiblingsClassLoader owner;

    public LocalResourceManager(SiblingsClassLoader owner) throws InvalidResourceException {
        this(owner, null);
    }

    public LocalResourceManager(SiblingsClassLoader owner, String resourceLocation) throws InvalidResourceException {
        super();

        this.owner = owner;

        if (resourceLocation != null && !resourceLocation.isEmpty()) {
            File file = new File(resourceLocation);
            if (!file.exists() || !file.canRead()) {
                logger.warn("Non-existent or inaccessible resource location: " + resourceLocation);
                this.resourceLocation = null;
                this.resourceNameStart = 0;
                return;
            }
            if (!file.isDirectory() && !resourceLocation.toLowerCase().endsWith(ResourceUtils.JAR_FILE_EXTENSION)) {
                throw new InvalidResourceException("Invalid resource directory or jar file: " + file.getAbsolutePath());
            }

            this.resourceLocation = file.getAbsolutePath();
            this.resourceNameStart = this.resourceLocation.length() + 1;

            findResource(file);
        } else {
            this.resourceLocation = null;
            this.resourceNameStart = 0;
        }

        if (owner.isRoot()) {
            sweepTempResourceFiles();
        }
    }

    @Override
    public void reset() throws InvalidResourceException {
        super.reset();

        if (resourceLocation != null) {
            findResource(new File(resourceLocation));
        }
    }

    private void findResource(File file) throws InvalidResourceException {
        try {
            if (file.isDirectory()) {
                List<File> jarFileList = new ArrayList<>();
                findResourceInDir(file, jarFileList);
                if (!jarFileList.isEmpty()) {
                    for (File jarFile : jarFileList) {
                        owner.joinSibling(jarFile.getAbsolutePath());
                    }
                }
            } else {
                findResourceFromJAR(file);
            }
        } catch (Exception e) {
            throw new InvalidResourceException("Failed to find resource from [" + resourceLocation + "]", e);
        }
    }

    private void findResourceInDir(File dir, List<File> jarFileList) {
        dir.listFiles(file -> {
            String filePath = file.getAbsolutePath();
            String resourceName = filePath.substring(resourceNameStart);
            try {
                putResource(resourceName, file);
            } catch (InvalidResourceException e) {
                throw new RuntimeException(e);
            }
            if (file.isDirectory()) {
                findResourceInDir(file, jarFileList);
            } else if (file.isFile()) {
                if (filePath.toLowerCase().endsWith(ResourceUtils.JAR_FILE_EXTENSION)) {
                    jarFileList.add(file);
                }
            }
            return false;
        });
    }

    private void findResourceFromJAR(File target) throws InvalidResourceException, IOException {
        String tempPath = SystemUtils.getProperty(AspectranConfig.TEMP_PATH_PROPERTY_NAME);
        File altResourceDir = null;
        if (tempPath != null) {
            Path tempDir = Path.of(tempPath);
            if (Files.isDirectory(tempDir) && Files.isWritable(tempDir)) {
                altResourceDir = Files.createTempDirectory(tempDir, TEMP_RESOURCE_DIRNAME_PREFIX).toFile();
            }
        }
        if (altResourceDir == null) {
            altResourceDir = Files.createTempDirectory(TEMP_RESOURCE_DIRNAME_PREFIX).toFile();
        }
        FileCopyUtils.copyFileToDirectory(target, altResourceDir);
        File altTarget = new File(altResourceDir, target.getName());
        try (JarFile jarFile = new JarFile(altTarget)) {
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                putResource(altTarget, entry);
            }
        }
        altResourceDir.deleteOnExit();
        altTarget.deleteOnExit();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.appendForce("resourceLocation", resourceLocation);
        tsb.append("numberOfResources", getNumberOfResources());
        tsb.append("owner", owner);
        return tsb.toString();
    }

    private void sweepTempResourceFiles() {
        String tempPath = SystemUtils.getProperty(AspectranConfig.TEMP_PATH_PROPERTY_NAME);
        if (tempPath != null) {
            Path tempDir = Path.of(tempPath);
            if (logger.isDebugEnabled()) {
                logger.debug("Sweeping " + tempDir.toAbsolutePath() + TEMP_RESOURCE_DIRNAME_PREFIX + "*" +
                        " for old resource files");
            }
            try {
                Files.walk(tempDir, 1, FileVisitOption.FOLLOW_LINKS)
                        .filter(Files::isDirectory)
                        .filter(p -> p.getFileName().toString().startsWith(TEMP_RESOURCE_DIRNAME_PREFIX))
                        .forEach(p -> {
                            try {
                                Files.walk(p)
                                        .sorted(Comparator.reverseOrder())
                                        .map(Path::toFile)
                                        .forEach(file -> {
                                            if (logger.isTraceEnabled()) {
                                                logger.trace("Delete temp resource: " + file);
                                            }
                                            file.delete();
                                        });
                            } catch (IOException e) {
                                logger.warn("Failed to delete temp resource: " + e.getMessage());
                            }
                        });
            } catch (IOException e) {
                logger.warn(e);
            }
        }
    }

}
