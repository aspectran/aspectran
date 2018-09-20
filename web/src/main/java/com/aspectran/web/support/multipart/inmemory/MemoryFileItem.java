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
package com.aspectran.web.support.multipart.inmemory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemHeadersSupport;
import org.apache.commons.fileupload.ParameterParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * The class is an implementation of the {@link org.apache.commons.fileupload.FileItem FileItem}
 * that removed file-related codes to support environments such as GAE
 * where the file system is not available.
 */
public class MemoryFileItem implements FileItem, FileItemHeadersSupport {

    private static final long serialVersionUID = 2593099556437676842L;

    /**
     * Default content charset to be used when no explicit charset parameter is provided by the sender. Media subtypes
     * of the "text" type are defined to have a default charset value of "ISO-8859-1" when received via HTTP.
     */
    public static final String DEFAULT_CHARSET = "ISO-8859-1";

    /**
     * The name of the form field as provided by the browser.
     */
    private String fieldName;

    /**
     * The content type passed by the browser, or {@code null} if not defined.
     */
    private final String contentType;

    /**
     * Whether or not this item is a simple form field.
     */
    private boolean isFormField;

    /**
     * The original filename in the user's filesystem.
     */
    private final String fileName;

    /**
     * Cached contents of the file.
     */
    private byte[] cachedContent;

    /**
     * Output stream for this item.
     */
    private transient MemoryOutputStream mos;

    /**
     * The threshold above which uploads will be stored on disk.
     */
    private int sizeThreshold;

    /**
     * The file items headers.
     */
    private FileItemHeaders headers;

    /**
     * Constructs a new MemoryFileItem instance.
     *
     * @param fieldName the name of the form field
     * @param contentType the content type passed by the browser or {@code null} if not specified
     * @param isFormField whether or not this item is a plain form field, as opposed to a file upload
     * @param fileName the original filename in the user's filesystem, or {@code null} if not specified
     * @param sizeThreshold the threshold, in bytes, below which items will be retained in memory.
     *                      (sizeThreshold will always be equal to file upload limit)
     */
    public MemoryFileItem(String fieldName, String contentType, boolean isFormField, String fileName, int sizeThreshold) {
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFormField = isFormField;
        this.fileName = fileName;
        this.sizeThreshold = sizeThreshold;
    }

    /**
     * Returns an {@link java.io.InputStream InputStream} that can be used to retrieve the contents of the file.
     *
     * @return an {@link java.io.InputStream InputStream} that can be used to retrieve the contents of the file
     * @throws IOException if an error occurs
     */
    public InputStream getInputStream() throws IOException {
        if (cachedContent == null) {
            cachedContent = mos.getData();
        }
        return new ByteArrayInputStream(cachedContent);
    }

    /**
     * Returns the content type passed by the agent or {@code null} if not defined.
     *
     * @return the content type passed by the agent or {@code null} if not defined
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the content charset passed by the agent or {@code null} if not defined.
     *
     * @return the content charset passed by the agent or {@code null} if not defined
     */
    @SuppressWarnings("unchecked")
    public String getCharSet() {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(getContentType(), ';');
        return params.get("charset");
    }

    /**
     * Returns the original filename in the client's filesystem.
     *
     * @return the original filename in the client's filesystem
     */
    public String getName() {
        return fileName;
    }

    /**
     * Provides a hint as to whether or not the file contents will be read from memory.
     *
     * @return {@code true} if the file contents will be read from memory; {@code false} otherwise
     */
    public boolean isInMemory() {
        return true;
    }

    /**
     * Returns the size of the file.
     *
     * @return the size of the file, in bytes
     */
    public long getSize() {
        if (cachedContent != null) {
            return cachedContent.length;
        } else {
            return mos.getData().length;
        }
    }

    /**
     * Returns the contents of the file as an array of bytes.
     *
     * @return the contents of the file as an array of bytes
     */
    public byte[] get() {
        if (cachedContent == null) {
            cachedContent = mos.getData();
        }
        return cachedContent;
    }

    /**
     * Returns the contents of the file as a String, using the specified encoding.
     * This method uses {@link #get()} to retrieve the contents of the file.
     *
     * @param charset the charset to use
     * @return the contents of the file, as a string
     * @throws UnsupportedEncodingException if the requested character encoding is not available
     */
    public String getString(final String charset) throws UnsupportedEncodingException {
        return new String(get(), charset);
    }

    /**
     * Returns the contents of the file as a String, using the default
     * character encoding.  This method uses {@link #get()} to retrieve the
     * contents of the file.
     *
     * @return the contents of the file, as a string
     */
    public String getString() {
        byte[] rawdata = get();
        String charset = getCharSet();
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        try {
            return new String(rawdata, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(rawdata);
        }
    }

    /**
     * A convenience method to write an uploaded item to disk. The client code
     * is not concerned with whether or not the item is stored in memory, or on
     * disk in a temporary location. They just want to write the uploaded item
     * to a file.
     * <p>
     * This implementation first attempts to rename the uploaded item to the
     * specified destination file, if the item was originally written to disk.
     * Otherwise, the data will be copied to the specified file.
     * <p>
     * This method is only guaranteed to work <em>once</em>, the first time it
     * is invoked for a particular item. This is because, in the event that the
     * method renames a temporary file, that file will no longer be available
     * to copy or rename again at a later time.
     *
     * @param file the {@code File} into which the uploaded item should
     *             be stored
     * @throws Exception if an error occurs
     */
    public void write(File file) throws Exception {
        try (FileOutputStream fout = new FileOutputStream(file)) {
            fout.write(get());
        }
    }

    /**
     * Does nothing.
     */
    public void delete() {
        // As all the data are in Heap, it will be garbage collected.
    }

    /**
     * Returns the name of the field in the multipart form corresponding to this file item.
     *
     * @return the name of the form field
     * @see #setFieldName(java.lang.String)
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name used to reference this file item.
     *
     * @param fieldName the name of the form field
     * @see #getFieldName()
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Determines whether or not a <code>FileItem</code> instance represents a simple form field.
     *
     * @return {@code true} if the instance represents a simple form field;
     *      {@code false} if it represents an uploaded file.
     * @see #setFormField(boolean)
     */
    public boolean isFormField() {
        return isFormField;
    }

    /**
     * Specifies whether or not a <code>FileItem</code> instance represents a simple form field.
     *
     * @param state {@code true} if the instance represents a simple form field;
     *      {@code false} if it represents an uploaded file
     * @see #isFormField()
     */
    public void setFormField(boolean state) {
        isFormField = state;
    }

    /**
     * Returns an {@link java.io.OutputStream OutputStream} of the file.
     *
     * @return an {@link java.io.OutputStream OutputStream} of the file
     * @throws IOException if an error occurs
     */
    public OutputStream getOutputStream() throws IOException {
        if (mos == null) {
            mos = new MemoryOutputStream(sizeThreshold);
        }
        return mos;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "name=" + this.getName() + ", size=" + this.getSize() + "bytes, " +
                "isFormField=" + isFormField() + ", FieldName=" + this.getFieldName();
    }

    /**
     * Writes the state of this object during serialization.
     *
     * @param out the stream to which the state should be written
     * @throws IOException if an error occurs
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // Read the data
        cachedContent = get();

        // write out values
        out.defaultWriteObject();
    }

    /**
     * Reads the state of this object during deserialization.
     *
     * @param in the stream from which the state should be read
     * @throws IOException if an error occurs
     * @throws ClassNotFoundException if class cannot be found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read values
        in.defaultReadObject();

        OutputStream output = getOutputStream();
        if (cachedContent != null) {
            output.write(cachedContent);
        }
        output.close();

        cachedContent = null;
    }

    /**
     * Returns the file item headers.
     *
     * @return the file items headers
     */
    public FileItemHeaders getHeaders() {
        return headers;
    }

    /**
     * Sets the file item headers.
     *
     * @param pHeaders the file items headers
     */
    public void setHeaders(FileItemHeaders pHeaders) {
        headers = pHeaders;
    }

}