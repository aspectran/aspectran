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
package com.aspectran.web.support.multipart.inmemory;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.support.http.MediaType;
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
 * An in-memory implementation of the Apache Commons FileUpload {@link FileItem} interface.
 * <p>This class is designed for environments where file system access is restricted,
 * such as Google App Engine, by storing all uploaded file data in memory.
 * All methods related to file system operations are either adapted for in-memory
 * handling or are no-ops.
 */
public class InMemoryFileItem implements FileItem, FileItemHeadersSupport {

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
     * Whether this item is a simple form field.
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
    private transient InMemoryOutputStream mos;

    /**
     * The threshold above which uploads will be stored on disk.
     */
    private final int sizeThreshold;

    /**
     * The file items headers.
     */
    private FileItemHeaders headers;

    /**
     * Constructs a new MemoryFileItem instance.
     * @param fieldName the name of the form field
     * @param contentType the content type passed by the browser or {@code null} if not specified
     * @param isFormField whether this item is a plain form field, as opposed to a file upload
     * @param fileName the original filename in the user's filesystem, or {@code null} if not specified
     * @param sizeThreshold the threshold, in bytes, below which items will be retained in memory.
     *                      (sizeThreshold will always be equal to file upload limit)
     */
    public InMemoryFileItem(String fieldName, String contentType,
                            boolean isFormField, String fileName, int sizeThreshold) {
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFormField = isFormField;
        this.fileName = fileName;
        this.sizeThreshold = sizeThreshold;
    }

    /**
     * Returns an {@link InputStream} that can be used to retrieve the contents of the file.
     * @return an {@link InputStream} for the file's contents
     * @throws IOException if an I/O error occurs
     */
    public InputStream getInputStream() throws IOException {
        if (cachedContent == null) {
            cachedContent = mos.getData();
        }
        return new ByteArrayInputStream(cachedContent);
    }

    /**
     * Returns the content type passed by the browser, or {@code null} if not defined.
     * @return the content type of the item
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the character encoding of the item, extracted from the content type.
     * @return the character encoding, or {@code null} if not specified
     */
    public String getCharset() {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(getContentType(), ';');
        return params.get(MediaType.PARAM_CHARSET);
    }

    /**
     * Returns the original filename in the client's filesystem.
     * @return the original filename
     */
    public String getName() {
        return fileName;
    }

    /**
     * Returns {@code true} as this implementation always stores data in memory.
     * @return always {@code true}
     */
    public boolean isInMemory() {
        return true;
    }

    /**
     * Returns the size of the file item in bytes.
     * @return the size of the file item, in bytes
     */
    public long getSize() {
        if (cachedContent != null) {
            return cachedContent.length;
        } else {
            return mos.getData().length;
        }
    }

    /**
     * Returns the contents of the file item as an array of bytes.
     * @return the contents of the file item
     */
    public byte[] get() {
        if (cachedContent == null) {
            cachedContent = mos.getData();
        }
        return cachedContent;
    }

    /**
     * Returns the contents of the file as a String, using the specified encoding.
     * @param charset the character encoding to use
     * @return the contents of the file as a string
     * @throws UnsupportedEncodingException if the requested character encoding is not available
     */
    public String getString(String charset) throws UnsupportedEncodingException {
        return new String(get(), charset);
    }

    /**
     * Returns the contents of the file as a String, using the default character encoding.
     * @return the contents of the file as a string
     */
    public String getString() {
        byte[] rawData = get();
        String charset = getCharset();
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        try {
            return new String(rawData, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(rawData);
        }
    }

    /**
     * A convenience method to write an uploaded item to a file.
     * <p>Since this implementation stores data in memory, this method simply
     * writes the in-memory data to the specified file.
     * @param file the file to which the uploaded item should be written
     * @throws Exception if an error occurs during the write operation
     */
    public void write(File file) throws Exception {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(get());
        }
    }

    /**
     * Does nothing, as the data is stored in memory and will be garbage collected.
     */
    public void delete() {
        // As all the data are in Heap, it will be garbage collected.
    }

    /**
     * Returns the name of the field in the multipart form corresponding to this file item.
     * @return the name of the form field
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name used to reference this file item.
     * @param fieldName the name of the form field
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Determines whether this instance represents a simple form field.
     * @return {@code true} if it is a simple form field; {@code false} if it is an uploaded file
     */
    public boolean isFormField() {
        return isFormField;
    }

    /**
     * Specifies whether this instance represents a simple form field.
     * @param state {@code true} if it is a simple form field; {@code false} if it is an uploaded file
     */
    public void setFormField(boolean state) {
        isFormField = state;
    }

    /**
     * Returns an {@link OutputStream} that can be used to store the contents of the file.
     * @return an {@link OutputStream} to write the file's contents
     * @throws IOException if an I/O error occurs
     */
    public OutputStream getOutputStream() throws IOException {
        if (mos == null) {
            mos = new InMemoryOutputStream(sizeThreshold);
        }
        return mos;
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "name=" + this.getName() + ", size=" + this.getSize() + "bytes, " +
                "isFormField=" + isFormField() + ", FieldName=" + this.getFieldName();
    }

    /**
     * Writes the state of this object during serialization.
     * @param out the stream to which the state should be written
     * @throws IOException if an error occurs
     */
    private void writeObject(@NonNull ObjectOutputStream out) throws IOException {
        // Read the data
        cachedContent = get();

        // write out values
        out.defaultWriteObject();
    }

    /**
     * Reads the state of this object during deserialization.
     * @param in the stream from which the state should be read
     * @throws IOException if an error occurs
     * @throws ClassNotFoundException if class cannot be found
     */
    private void readObject(@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException {
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
     * @return the file items headers
     */
    public FileItemHeaders getHeaders() {
        return headers;
    }

    /**
     * Sets the file item headers.
     * @param pHeaders the file items headers
     */
    public void setHeaders(FileItemHeaders pHeaders) {
        headers = pHeaders;
    }

}
