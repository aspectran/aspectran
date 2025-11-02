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

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

import static com.aspectran.utils.PathUtils.REGULAR_FILE_SEPARATOR;
import static com.aspectran.utils.PathUtils.WINDOWS_FILE_SEPARATOR;

/**
 * General utility methods for manipulating filenames and file paths.
 * <p>This class provides a set of platform-independent methods for working with file paths.
 * It defines six components within a filename (e.g., {@code C:\dev\project\file.txt}):</p>
 * <ul>
 *   <li>Prefix: {@code C:\}</li>
 *   <li>Path: {@code dev\project\}</li>
 *   <li>Full Path: {@code C:\dev\project\}</li>
 *   <li>Name: {@code file.txt}</li>
 *   <li>Base Name: {@code file}</li>
 *   <li>Extension: {@code txt}</li>
 * </ul>
 */
public abstract class FilenameUtils {

    private static final String NAME_SEPARATOR = "_";

    private static final String EXTENSION_SEPARATOR = ".";

    private static final String EXTENSIONS_SEPARATORS = " ,;\t\n\r\f";

    /**
     * Gets the name of the file from a full path, excluding the path.
     * <p>This method handles both Unix and Windows formatted paths.
     * The text after the last forward or backslash is returned.</p>
     * <pre>
     * a/b/c.txt --&gt; c.txt
     * a.txt     --&gt; a.txt
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; ""
     * </pre>
     * @param filename the filename to query, must not be null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getName(String filename) {
        Assert.notNull(filename, "'filename' must not be null");
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    /**
     * Gets the base name of the file, excluding the path and extension.
     * <p>This method handles both Unix and Windows formatted paths.
     * The text after the last separator and before the last dot is returned.</p>
     * <pre>
     * a/b/c.txt --&gt; c
     * a.txt     --&gt; a
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; ""
     * </pre>
     * @param filename the filename to query, must not be null
     * @return the base name of the file
     */
    public static String getBaseName(String filename) {
        return removeExtension(getName(filename));
    }

    /**
     * Extracts the file extension from the given filename.
     * <p>This method returns the textual part of the filename after the last dot.
     * There must be no directory separator after the dot for an extension to be found.</p>
     * <pre>
     * foo.txt      --&gt; "txt"
     * a/b/c.jpg    --&gt; "jpg"
     * a/b.txt/c    --&gt; ""
     * a/b/c        --&gt; ""
     * </pre>
     * @param filename the filename to retrieve the extension of, must not be null
     * @return the extension of the file, or an empty string if none exists
     */
    public static String getExtension(String filename) {
        Assert.notNull(filename, "'filename' must not be null");
        int index = indexOfExtension(filename);
        if (index == -1) {
            return StringUtils.EMPTY;
        } else {
            return filename.substring(index + 1);
        }
    }

    /**
     * Removes the extension from a filename.
     * <p>This method returns the textual part of the filename before the last dot.
     * There must be no directory separator after the dot.</p>
     * <pre>
     * foo.txt    --&gt; foo
     * a\b\c.jpg  --&gt; a\b\c
     * a\b\c      --&gt; a\b\c
     * a.b\c      --&gt; a.b\c
     * </pre>
     * @param filename the filename to query, must not be null
     * @return the filename minus the extension
     */
    public static String removeExtension(String filename) {
        Assert.notNull(filename, "'filename' must not be null");
        int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    /**
     * Returns the index of the last directory separator character ('/' or '\').
     * <p>This method handles both Unix and Windows formatted paths.</p>
     * @param filename the filename to find the last path separator in (may be {@code null})
     * @return the index of the last separator character, or -1 if there is no such character
     */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(REGULAR_FILE_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_FILE_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Returns the index of the last extension separator character ('.').
     * <p>This method also checks that there is no directory separator after the last dot.
     * To do this it uses {@link #indexOfLastSeparator(String)}.</p>
     * @param filename the filename to find the last extension separator in (may be {@code null})
     * @return the index of the last extension separator, or -1 if there is no such character
     */
    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return (lastSeparator > extensionPos ? -1 : extensionPos);
    }

    /**
     * Gets the path from a full filename, excluding the file name.
     * @param filename a full filename (may be {@code null})
     * @return the path, or an empty string if none exists
     */
    public static String getFullPath(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        if (index < 0) {
            return StringUtils.EMPTY;
        }
        return filename.substring(0, index);
    }

    /**
     * Gets the path from a full filename, including the trailing separator.
     * @param filename a full filename (may be {@code null})
     * @return the path with a trailing separator, or an empty string if none exists
     */
    public static String getFullPathWithEndSeparator(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        if (index < 0) {
            return StringUtils.EMPTY;
        }
        return filename.substring(0, index + 1);
    }

    /**
     * Checks whether the extension of the filename is valid based on allowed and denied lists.
     * The extension check is case-insensitive.
     * @param filename the filename to check (may be {@code null})
     * @param allowedFileExtensions a comma-separated list of allowed extensions
     * @param deniedFileExtensions a comma-separated list of denied extensions
     * @return {@code true} if the file extension is valid, {@code false} otherwise
     */
    public static boolean isValidFileExtension(String filename, String allowedFileExtensions, String deniedFileExtensions) {
        if (filename == null) {
            return false;
        }
        String ext = getExtension(filename).toLowerCase();
        if (allowedFileExtensions != null && !allowedFileExtensions.isEmpty()) {
            if (ext.isEmpty()) {
                return false;
            }
            StringTokenizer st = new StringTokenizer(allowedFileExtensions.toLowerCase(), EXTENSIONS_SEPARATORS);
            while (st.hasMoreTokens()) {
                String ext2 = st.nextToken();
                if (ext.equals(ext2)) {
                    return true;
                }
            }
            return false;
        }
        if (deniedFileExtensions != null && !deniedFileExtensions.isEmpty()) {
            if (ext.isEmpty()) {
                return true;
            }
            StringTokenizer st = new StringTokenizer(deniedFileExtensions.toLowerCase(), EXTENSIONS_SEPARATORS);
            while (st.hasMoreTokens()) {
                String ext2 = st.nextToken();
                if (ext.equals(ext2)) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    /**
     * Generates a unique file handle by appending a counter to the filename if it already exists.
     * @param srcFile the file to make unique
     * @return a unique {@link File} handle (which may not yet exist on the filesystem)
     * @throws IOException if an I/O error occurs
     */
    public static File generateUniqueFile(File srcFile) throws IOException {
        return generateUniqueFile(srcFile, EXTENSION_SEPARATOR);
    }

    /**
     * Generates a unique file handle by appending a counter to the filename if it already exists.
     * @param srcFile the file to make unique
     * @param extSeparator the file extension separator
     * @return a unique {@link File} handle (which may not yet exist on the filesystem)
     * @throws IOException if an I/O error occurs
     */
    public static File generateUniqueFile(File srcFile, String extSeparator) throws IOException {
        Assert.notNull(srcFile, "'srcFile' must not be null");

        String path = getFullPath(srcFile.getCanonicalPath());
        String name = removeExtension(srcFile.getName());
        String ext = getExtension(srcFile.getName());

        String newName;
        if (StringUtils.hasLength(ext)) {
            newName = name + extSeparator + ext;
        } else {
            newName = name;
        }

        File destFile = new File(path, newName);
        int count = 0;
        while (destFile.exists()) {
            count++;
            if (ext != null && !ext.isEmpty()) {
                newName = name + NAME_SEPARATOR + count + extSeparator + ext;
            } else {
                newName = name + NAME_SEPARATOR + count;
            }
            destFile = new File(path, newName);
        }
        return (count == 0 ? srcFile : destFile);
    }

    /**
     * Creates a system-safe, unique filename based on the current time and a random number.
     * This is useful for creating temporary or uploaded file names that are unlikely to collide.
     * <pre>
     * e.g., 1616493813337_1234.txt
     * </pre>
     * @param file the original file to derive the path and extension from
     * @return a unique {@link File} handle
     * @throws IOException if an I/O error occurs
     */
    public static File generateSafetyUniqueFile(File file) throws IOException {
        return generateSafetyUniqueFile(file, EXTENSION_SEPARATOR);
    }

    /**
     * Creates a system-safe, unique filename based on the current time and a random number.
     * @param file the original file to derive the path and extension from
     * @param extSeparator the file extension separator
     * @return a unique {@link File} handle
     * @throws IOException if an I/O error occurs
     */
    public static File generateSafetyUniqueFile(@NonNull File file, String extSeparator) throws IOException {
        String path = file.getCanonicalPath();
        String ext = getExtension(path);

        String prefix = Long.toString(System.currentTimeMillis());
        Random rnd = new Random();
        String suffix = Integer.toString(rnd.nextInt(9999));

        String fullName;
        if (ext != null && !ext.isEmpty()) {
            fullName = prefix + NAME_SEPARATOR + suffix + extSeparator + ext;
        } else {
            fullName = prefix + NAME_SEPARATOR + suffix;
        }

        File file2 = new File(getFullPath(path), fullName);
        return generateUniqueFile(file2, NAME_SEPARATOR);
    }

    /**
     * Recovers a filename with a standard extension separator ('.') from a name
     * that used a different separator.
     * @param uniqueFilename a unique filename, potentially with a custom separator
     * @return file name with the standard extension separator
     */
    @NonNull
    public static String recoverExtension(String uniqueFilename) {
        return StringUtils.replaceLast(uniqueFilename, NAME_SEPARATOR, EXTENSION_SEPARATOR);
    }

}
