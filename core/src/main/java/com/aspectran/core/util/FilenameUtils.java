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
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Utility methods for General filename and filepath manipulation.
 * <p>
 * This class defines six components within a filename
 * (example C:\dev\project\file.txt):</p>
 * <ul>
 * <li>the prefix - C:\</li>
 * <li>the path - dev\project\</li>
 * <li>the full path - C:\dev\project\</li>
 * <li>the name - file.txt</li>
 * <li>the base name - file</li>
 * <li>the extension - txt</li>
 * </ul>
 */
public class FilenameUtils {

    private static final String NAME_SEPARATOR = "_";

    private static final String EXTENSION_SEPARATOR = ".";

    private static final char UNIX_SEPARATOR = '/';

    private static final char WINDOWS_SEPARATOR = '\\';

    private static final String EXTENSIONS_SEPARATORS = " ,;\t\n\r\f";

    /**
     * Gets the name minus the path from a full filename.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The text after the last forward or backslash is returned.</p>
     * <pre>
     * a/b/c.txt --&gt; c.txt
     * a.txt     --&gt; a.txt
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; ""
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.</p>
     * @param filename  the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getName(String filename) {
        Assert.notNull(filename, "'filename' must not be null");
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    /**
     * Gets the base name, minus the full path and extension, from a full filename.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The text after the last forward or backslash and before the last dot is returned.</p>
     * <pre>
     * a/b/c.txt --&gt; c
     * a.txt     --&gt; a
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; ""
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.</p>
     * @param filename  the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getBaseName(String filename) {
        return removeExtension(getName(filename));
    }

    /**
     * Extract the file extension from the given filename.
     * <p>
     * This method returns the textual part of the filename after the last dot.
     * There must be no directory separator after the dot.</p>
     * <pre>
     * foo.txt      --&gt; "txt"
     * a/b/c.jpg    --&gt; "jpg"
     * a/b.txt/c    --&gt; ""
     * a/b/c        --&gt; ""
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.</p>
     * @param filename the filename to retrieve the extension of
     * @return the extension of the file or an empty string if none exists
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
     * <p>
     * This method returns the textual part of the filename before the last dot.
     * There must be no directory separator after the dot.</p>
     * <pre>
     * foo.txt    --&gt; foo
     * a\b\c.jpg  --&gt; a\b\c
     * a\b\c      --&gt; a\b\c
     * a.b\c      --&gt; a.b\c
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.</p>
     * @param filename  the filename to query, null returns null
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
     * Returns the index of the last directory separator character.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The position of the last forward or backslash is returned.</p>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.</p>
     * @param filename  the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there
     * is no such character
     */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Returns the index of the last extension separator character, which is a dot.
     * <p>
     * This method also checks that there is no directory separator after the last dot.
     * To do this it uses {@link #indexOfLastSeparator(String)} which will
     * handle a file in either Unix or Windows format.</p>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.</p>
     * @param filename  the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there
     * is no such character
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
     * Gets the path from a full filename.
     * @param filename a full filename
     * @return the path
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
     * Gets the path with end separator from a full filename.
     * @param filename a full filename
     * @return the path
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
     * Checks whether the extension of the filename is valid.
     * The extension check is case-sensitive on all platforms.
     * @param filename the filename to query, null returns false
     * @param allowedFileExtensions the allowed file extensions
     * @param deniedFileExtensions the denied file extensions
     * @return true if is valid file extension; false otherwise
     */
    public static boolean isValidFileExtension(String filename, String allowedFileExtensions, String deniedFileExtensions) {
        if (filename == null) {
            return false;
        }
        String ext = getExtension(filename).toLowerCase();
        if (allowedFileExtensions != null && !allowedFileExtensions.isEmpty()) {
            if (ext.length() == 0) {
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
            if (ext.length() == 0) {
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
     * Returns a file name that does not overlap in the specified directory.
     * If a duplicate file name exists, it is returned by appending a number after the file name.
     * @param srcFile the file to seek
     * @return a unique file
     * @throws IOException if failed to obtain a unique file
     */
    public static File generateUniqueFile(File srcFile) throws IOException {
        return generateUniqueFile(srcFile, EXTENSION_SEPARATOR);
    }

    /**
     * Returns a file name that does not overlap in the specified directory.
     * If a duplicate file name exists, it is returned by appending a number after the file name.
     * @param srcFile the file to seek
     * @param extSeparator the file extension separator
     * @return a unique file
     * @throws IOException if failed to obtain a unique file
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
     * Creates and returns a system-safe file name without duplicates in the specified directory.
     * If there is a duplicate file name, a number is added after the file name and returned.
     * File extensions are separated by '.' character.
     * <pre>
     * ex) 1111111111_1.txt
     * </pre>
     * @param file the file to seek
     * @return a unique file
     * @throws IOException if failed to obtain a unique file
     */
    public static File generateSafetyUniqueFile(File file) throws IOException {
        return generateSafetyUniqueFile(file, EXTENSION_SEPARATOR);
    }

    /**
     * Creates and returns a system-safe file name without duplicates in the specified directory.
     * If there is a duplicate file name, a number is added after the file name and returned.
     * If the file extension separator is specified as '_', the file name can be obtained
     * in the following format.
     * <pre>
     * ex) 1111111111_txt
     * </pre>
     * @param file the file to seek
     * @return a unique file
     * @param extSeparator the file extension separator
     * @throws IOException if failed to obtain a unique file
     */
    public static File generateSafetyUniqueFile(File file, String extSeparator) throws IOException {
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
     * Recovers the extension from a unique file name.
     * @param uniqueFilename a unique file name
     * @return file name with recovered extension
     */
    public static String recoverExtension(String uniqueFilename) {
        return StringUtils.replaceLast(uniqueFilename, NAME_SEPARATOR, EXTENSION_SEPARATOR);
    }

}
