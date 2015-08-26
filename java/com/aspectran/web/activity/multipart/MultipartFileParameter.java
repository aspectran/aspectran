/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.web.activity.multipart;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.util.FileUtils;

/**
 * MultipartItem implementation for Jakarta Commons FileUpload.
 * 
 * <p>Created: 2008. 04. 11 오후 8:55:25</p>
 */
public class MultipartFileParameter extends FileParameter {

	private FileItem fileItem;

	private long fileSize;

	/**
	 * Create an instance wrapping the given FileItem.
	 * 
	 * @param fileItem the FileItem to wrap
	 */
	public MultipartFileParameter(FileItem fileItem) {
		this.fileItem = fileItem;
		this.fileSize = fileItem.getSize();
	}

	/**
	 * Gets the content type of the data being uploaded. This is never null, and
	 * defaults to "content/unknown" when the mime type of the data couldn't be
	 * determined and was not set manually.
	 *  
	 * @return the content type
	 */
	public String getContentType() {
		return fileItem.getContentType();
	}

	/**
	 * Gets the file name.
	 * 
	 * @return the file name
	 */
	public String getFileName() {
		return getCanonicalName(fileItem.getName());
	}

	/**
	 * Gets the file size.
	 * 
	 * @return the file size
	 */
	public long getFileSize() {
		return this.fileSize;
	}

	/**
	 * Return an InputStream to read the contents of the file from.
	 * 
	 * @return the contents of the file as stream, or an empty stream if empty
	 * 
	 * @throws IOException in case of access errors (if the temporary store fails)
	 */
	public InputStream getInputStream() throws IOException {
		if(!isAvailable())
			throw new IllegalStateException("File has been moved - cannot be read again.");

		InputStream inputStream = fileItem.getInputStream();
		
		return (inputStream != null ? inputStream : new ByteArrayInputStream(new byte[0]));
	}

	/**
	 * Return an byte array to read the contents of the file from.
	 * 
	 * @return the bytes
	 */
	public byte[] getBytes() {
		if(!isAvailable())
			throw new IllegalStateException("File has been moved - cannot be read again.");
		
		byte[] bytes = fileItem.get();
		
		return (bytes != null ? bytes : new byte[0]);
	}

	/**
	 * Save the uploaded file to the given destination file.
	 * 
	 * @param dest the destination file
	 * @param overwrite 이미 파일이 존재할 경우 덮어 쓸지 여부
	 * 
	 * @return 저장된 파일
	 * 
	 * @throws Exception the exception
	 */
	public File saveAs(File dest, boolean overwrite) throws IOException {
		if(!isAvailable())
			throw new IllegalStateException("File has been moved - cannot be read again.");

		if(!overwrite) {
			String path = FileUtils.getPathWithoutFileName(dest.getAbsolutePath());
			String fileName = dest.getName();
			String newFileName = FileUtils.obtainUniqueFileName(path, fileName);
			
			if(fileName != newFileName)
				dest = new File(path, newFileName);
		} else {
			if(dest.exists() && !dest.delete()) {
				throw new IOException("Destination file [" + dest.getAbsolutePath() + "] already exists and could not be deleted.");
			}
		}
		
		try {
			fileItem.write(dest);
		} catch(FileUploadException e) {
			throw new IllegalStateException(e.getMessage());
		} catch(IOException e) {
			throw e;
		} catch(Exception e) {
			throw new IOException("Could not save file. Cause: " + e);
		}

		savedFile = dest;

		return dest;
	}

	/**
	 * Delete.
	 */
	public void delete() {
		fileItem.delete();
	}

	public void release() {
		if(fileItem != null) {
			fileItem = null;
		}
		if(savedFile != null) {
			savedFile.setWritable(true);
			savedFile = null;
		}
	}
	
	/**
	 * Returns the canonical name of the given file.
	 * 
	 * @param fileName the given file
	 * 
	 * @return the canonical name of the given file
	 */
	private String getCanonicalName(String fileName) {
		int forwardSlash = fileName.lastIndexOf("/");   // check for Unix-style path
		int backwardSlash = fileName.lastIndexOf("\\"); // check for Windows-style path

		if(forwardSlash != -1 && forwardSlash > backwardSlash)
			fileName = fileName.substring(forwardSlash + 1, fileName.length());
		else if(backwardSlash != -1 && backwardSlash >= forwardSlash)
			fileName = fileName.substring(backwardSlash + 1, fileName.length());

		return fileName;
	}

	/**
	 * Determine whether the multipart content is still available.
	 * If a temporary file has been moved, the content is no longer available.
	 * 
	 * @return true, if checks if is available
	 */
	private boolean isAvailable() {
		// If in memory, it's available.
		if(this.fileItem.isInMemory()) {
			return true;
		}

		// Check actual existence of temporary file.
		if(this.fileItem instanceof DiskFileItem) {
			return ((DiskFileItem)this.fileItem).getStoreLocation().exists();
		}

		// Check whether current file size is different than original one.
		return (this.fileItem.getSize() == this.fileSize);
	}
}
