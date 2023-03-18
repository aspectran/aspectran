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
package com.aspectran.core.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class is a clone of org.apache.commons.io.FileUtils</p>
 *
 * Simple utility methods for file and directory copying.
 * All copy methods use a block size of 4096 bytes,
 * and close all affected streams when done.
 */
public class FileCopyUtils {

    /**
     * The default buffer size ({@value}) to use in copy methods.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FileCopyUtils() {
        super();
    }

    /**
     * Copies a file to a directory preserving the file date.
     * <p>
     * This method copies the contents of the specified source file
     * to a file of the same name in the specified destination directory.
     * The destination directory is created if it does not exist.
     * If the destination file exists, then this method will overwrite it.</p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.</p>
     * @param srcFile an existing file to copy, must not be {@code null}
     * @param destDir the directory to place the copy in, must not be {@code null}
     * @throws NullPointerException if source or destination is null
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @see #copyFile(File, File, boolean)
     */
    public static void copyFileToDirectory(final File srcFile, final File destDir) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    /**
     * Copies a file to a directory optionally preserving the file date.
     * <p>
     * This method copies the contents of the specified source file
     * to a file of the same name in the specified destination directory.
     * The destination directory is created if it does not exist.
     * If the destination file exists, then this method will overwrite it.</p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the file's last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.</p>
     * @param srcFile          an existing file to copy, must not be {@code null}
     * @param destDir          the directory to place the copy in, must not be {@code null}
     * @param preserveFileDate true if the file date of the copy
     *                         should be the same as the original
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @see #copyFile(File, File, boolean)
     */
    public static void copyFileToDirectory(final File srcFile, final File destDir, final boolean preserveFileDate)
        throws IOException {
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && !destDir.isDirectory()) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        final File destFile = new File(destDir, srcFile.getName());
        copyFile(srcFile, destFile, preserveFileDate);
    }

    /**
     * Copies a file to a new location preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the
     * specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this
     * method will overwrite it.</p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.</p>
     * @param srcFile  an existing file to copy, must not be {@code null}
     * @param destFile the new file, must not be {@code null}
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File)
     * @see #copyFile(File, File, boolean)
     */
    public static void copyFile(final File srcFile, final File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    /**
     * Copies a file to a new location.
     * <p>
     * This method copies the contents of the specified source file
     * to the specified destination file.
     * The directory holding the destination file is created if it does not exist.
     * If the destination file exists, then this method will overwrite it.</p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the file's last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.</p>
     * @param srcFile          an existing file to copy, must not be {@code null}
     * @param destFile         the new file, must not be {@code null}
     * @param preserveFileDate true if the file date of the copy
     *                         should be the same as the original
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File, boolean)
     * @see #doCopyFile(File, File, boolean)
     */
    public static void copyFile(final File srcFile, final File destFile,
                                final boolean preserveFileDate) throws IOException {
        checkFileRequirements(srcFile, destFile);
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        }
        final File parentFile = destFile.getParentFile();
        if (parentFile != null) {
            if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
            }
        }
        if (destFile.exists() && !destFile.canWrite()) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile, preserveFileDate);
    }

    /**
     * Copy bytes from a <code>File</code> to an <code>OutputStream</code>.
     * @param input  the <code>File</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     */
    public static long copyFile(final File input, final OutputStream output) throws IOException {
        try (FileInputStream fis = new FileInputStream(input)) {
            return copyLarge(fis, output);
        }
    }

    /**
     * Copies bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.</p>
     * <p>
     * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.</p>
     * @param input the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     */
    private static long copyLarge(final InputStream input, final OutputStream output)
        throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Internal copy file method.
     * This uses the original file length, and throws an IOException
     * if the output file length is different from the current input file length.
     * So it may fail if the file changes size.
     * It may also fail with "IllegalArgumentException: Negative size" if the input file is truncated part way
     * through copying the data and the new file size is less than the current position.
     * @param srcFile          the validated source file, must not be {@code null}
     * @param destFile         the validated destination file, must not be {@code null}
     * @param preserveFileDate whether to preserve the file date
     * @throws IOException     if an error occurs
     */
    private static void doCopyFile(final File srcFile, final File destFile, final boolean preserveFileDate)
        throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }
        Path srcPath = srcFile.toPath();
        Path destPath = destFile.toPath();
        final long newLastModified = preserveFileDate ? srcFile.lastModified() : destFile.lastModified();
        Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
        destFile.setLastModified(newLastModified);
    }

    /**
     * Copies a whole directory to a new location preserving the file dates.
     * <p>
     * This method copies the specified directory and all its child
     * directories and files to the specified destination.
     * The destination is the new location and name of the directory.</p>
     * <p>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.</p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.</p>
     * @param srcDir  an existing directory to copy, must not be {@code null}
     * @param destDir the new directory, must not be {@code null}
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     */
    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    /**
     * Copies a whole directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.</p>
     * <p>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.</p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the files' last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.</p>
     * @param srcDir           an existing directory to copy, must not be {@code null}
     * @param destDir          the new directory, must not be {@code null}
     * @param preserveFileDate true if the file date of the copy
     *                         should be the same as the original
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     */
    public static void copyDirectory(final File srcDir, final File destDir,
                                     final boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }

    /**
     * Copies a filtered directory to a new location preserving the file dates.
     * <p>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.</p>
     * <p>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.</p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.</p>
     * <strong>Example: Copy directories only</strong>
     * <pre>
     *  // only copy the directory structure
     *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
     * </pre>
     *
     * <strong>Example: Copy directories and txt files</strong>
     * <pre>
     *  // Create a filter for ".txt" files
     *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *  // Create a filter for either directories or ".txt" files
     *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     *  // Copy using the filter
     *  FileCopyUtils.copyDirectory(srcDir, destDir, filter);
     * </pre>
     * @param srcDir  an existing directory to copy, must not be {@code null}
     * @param destDir the new directory, must not be {@code null}
     * @param filter  the filter to apply, null means copy all directories and files
     *                should be the same as the original
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     */
    public static void copyDirectory(final File srcDir, final File destDir,
                                     final FileFilter filter) throws IOException {
        copyDirectory(srcDir, destDir, filter, true);
    }

    /**
     * Copies a filtered directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.</p>
     * <p>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.</p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the files' last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.</p>
     * @param srcDir           an existing directory to copy, must not be {@code null}
     * @param destDir          the new directory, must not be {@code null}
     * @param filter           the filter to apply, null means copy all directories and files
     * @param preserveFileDate true if the file date of the copy
     *                         should be the same as the original
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     */
    public static void copyDirectory(final File srcDir, final File destDir,
                                     final FileFilter filter, final boolean preserveFileDate) throws IOException {
        checkFileRequirements(srcDir, destDir);
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }
        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            final File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
    }

    /**
     * Checks requirements for file copy.
     * @param src the source file
     * @param dest the destination
     * @throws FileNotFoundException if the destination does not exist
     */
    private static void checkFileRequirements(final File src, final File dest) throws FileNotFoundException {
        if (src == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (dest == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!src.exists()) {
            throw new FileNotFoundException("Source '" + src + "' does not exist");
        }
    }

    /**
     * Internal copy directory method.
     * @param srcDir           the validated source directory, must not be {@code null}
     * @param destDir          the validated destination directory, must not be {@code null}
     * @param filter           the filter to apply, null means copy all directories and files
     * @param preserveFileDate whether to preserve the file date
     * @param exclusionList    list of files and directories to exclude from the copy, may be null
     * @throws IOException     if an error occurs
     */
    private static void doCopyDirectory(final File srcDir, final File destDir, final FileFilter filter,
                                        final boolean preserveFileDate, final List<String> exclusionList)
        throws IOException {
        // recurse
        final File[] srcFiles = (filter == null ? srcDir.listFiles() : srcDir.listFiles(filter));
        if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
                } else {
                    doCopyFile(srcFile, dstFile, preserveFileDate);
                }
            }
        }

        // Do this last, as the above has probably affected directory metadata
        if (preserveFileDate) {
            destDir.setLastModified(srcDir.lastModified());
        }
    }

}
