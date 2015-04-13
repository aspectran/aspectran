/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.activity.request.parameter;

import com.aspectran.core.util.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Created: 2008. 04. 11 오후 4:19:40</p>
 */
public class FileParameter {
	
	private File file;
	
	private String contentType;
	
	private boolean refused;
	
	protected File savedFile;
	
	/**
	 * Instantiates a new file item.
	 */
	protected FileParameter() {
	}
	
	/**
	 * Instantiates a new file item.
	 * 
	 * @param file the file
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public FileParameter(File file) {
		this.file = file;
	}
	
	/**
	 * Gets the actual name of the file uploaded.
	 * 
	 * @return the actual name of the file uploaded
	 */
	public String getFileName() {
		return file.getName();
	}

	/**
	 * Gets the size of the file uploaded.
	 * 
	 * @return the size of the file uploaded
	 */
	public long getFileSize() {
		return file.length();
	}

	/**
	 * Gets the the content type of the file.
	 * 
	 * @return the content type of the file
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Gets the input stream of the file.
	 * 
	 * @return the input stream of the file
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	/**
	 * Gets the bytes.
	 * JVM이 다루는 Heap 메모리보다 큰 배열을 사용할 수는 없습니다.
	 * 
	 * @return the bytes
	 */
	public byte[] getBytes() throws IOException {
		InputStream input = getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		final byte[] buffer = new byte[256 * 1024];
		int len;

		while((len = input.read(buffer)) != -1) {
			output.write(buffer, 0, len);
		}

		input.close();
		output.close();
		
		return output.toByteArray();
	}
	
	/**
	 * Checks if is refused.
	 * 
	 * @return true, if is refused
	 */
	public boolean isRefused() {
		return refused;
	}

	/**
	 * Sets the refused.
	 * 
	 * @param refused the new refused
	 */
	public void setRefused(boolean refused) {
		this.refused = refused;
	}
	
	/**
	 * Save the uploaded file to the given destination file.
	 * 이미 동일한 파일명을 가진 파일이 존재할 경우 다른 유일한 파일명으로 저장한다.
	 * 
	 * @param destFile the destination file
	 * 
	 * @return saved file
	 * 
	 * @throws Exception the exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public File saveAs(File destFile) throws IOException {
		return saveAs(destFile, false);
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
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public File saveAs(File dest, boolean overwrite) throws IOException {
		if(!overwrite) {
			String path = FileUtils.getPathWithoutFileName(dest.getAbsolutePath());
			String fileName = FileUtils.obtainUniqueFileName(path, dest.getName());
			dest = new File(path, fileName);
		}
		
		InputStream input = getInputStream();
		OutputStream output = new FileOutputStream(dest);

		final byte[] buffer = new byte[256 * 1024];
		int len;

		while((len = input.read(buffer)) != -1) {
			output.write(buffer, 0, len);
		}

		output.flush();
		output.close();
		input.close();
		
		savedFile = dest;
		
		return dest;
	}
	
	public File getSavedFile() {
		return savedFile;
	}

	/**
	 * Delete.
	 */
	public void delete() {
		file.delete();
	}
	
	/**
	 * Rollback.
	 */
	public void rollback() {
		if(savedFile != null)
			savedFile.delete();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{file=").append(file);
		sb.append(", contentType=").append(contentType);
		sb.append(", refused=").append(refused);
		sb.append(", savedFile=").append(savedFile);
		sb.append("}");
		
		return sb.toString();
	}
}
