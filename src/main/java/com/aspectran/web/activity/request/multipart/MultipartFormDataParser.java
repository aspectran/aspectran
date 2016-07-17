/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.web.activity.request.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.util.FileUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * Multi-part form data parser.
 */
public class MultipartFormDataParser {

	private static final Log log = LogFactory.getLog(MultipartFormDataParser.class);

	/** The constant DEFAULT_MAX_REQUEST_SIZE. */
	private static final long DEFAULT_MAX_REQUEST_SIZE = 250 * 1024 * 1024;

	/** The constant DEFAULT_SIZE_THRESHOLD. */
	private static final int DEFAULT_SIZE_THRESHOLD = 256 * 1024;

	private String temporaryFilePath;
	
	private long maxRequestSize = DEFAULT_MAX_REQUEST_SIZE;

	private String allowedFileExtensions;

	private String deniedFileExtensions;

	/**
	 * Instantiates a new MultipartFormDataParser.
	 */
	public MultipartFormDataParser() {
	}

	/**
	 * Returns the directory path used for temporary files.
	 *
	 * @return the directory path used for temporary files
	 */
	public String getTemporaryFilePath() {
		return temporaryFilePath;
	}

	/**
	 * Sets the directory path used to temporarily files.
	 *
	 * @param temporaryFilePath the directory path used for temporary files
	 */
	public void setTemporaryFilePath(String temporaryFilePath) {
		this.temporaryFilePath = temporaryFilePath;
	}

	/**
	 * Returns the maximum length of HTTP GET Request
	 *
	 * @return the max request size
	 */
	public long getMaxRequestSize() {
		return maxRequestSize;
	}

	/**
	 * Sets the maximum length of HTTP GET Request
	 *
	 * @param maxSize the maximum length of HTTP GET Request
	 */
	public void setMaxRequestSize(long maxSize) {
		this.maxRequestSize = maxSize;
	}

	/**
	 * Gets the allowed file extensions.
	 *
	 * @return the allowed file extensions
	 */
	public String getAllowedFileExtensions() {
		return allowedFileExtensions;
	}

	/**
	 * Sets the allowed file extensions.
	 *
	 * @param allowedFileExtensions the allowed file extensions
	 */
	public void setAllowedFileExtensions(String allowedFileExtensions) {
		this.allowedFileExtensions = allowedFileExtensions;
	}

	/**
	 * Gets the denied file extensions.
	 *
	 * @return the denied file extensions
	 */
	public String getDeniedFileExtensions() {
		return deniedFileExtensions;
	}

	/**
	 * Sets the denied file extensions.
	 *
	 * @param deniedFileExtensions the denied file extensions
	 */
	public void setDeniedFileExtensions(String deniedFileExtensions) {
		this.deniedFileExtensions = deniedFileExtensions;
	}

	/**
	 * Parse the given servlet request, resolving its multipart elements.
	 *
	 * @param requestAdapter the request adapter
	 * @throws MultipartRequestException if multipart resolution failed
	 */
	public void parse(RequestAdapter requestAdapter) {
		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(DEFAULT_SIZE_THRESHOLD);

			if(temporaryFilePath != null) {
				File repository = new File(temporaryFilePath);
				if(!repository.exists() && !repository.mkdirs()) {
					throw new IllegalArgumentException("Given temporaryFilePath [" + temporaryFilePath + "] could not be created.");
				}
				factory.setRepository(repository);
			}

			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setSizeMax(maxRequestSize);
			upload.setHeaderEncoding(requestAdapter.getCharacterEncoding());

			Map<String, List<FileItem>> fileItemListMap;

			try {
				RequestContext requestContext = createRequestContext(requestAdapter.getAdaptee());
				fileItemListMap = upload.parseParameterMap(requestContext);
			} catch(SizeLimitExceededException e) {
				log.warn("Max length exceeded. multipart.maxRequestSize: " + maxRequestSize);
				requestAdapter.setMaxLengthExceeded(true);
				return;
			}

			parseMultipart(fileItemListMap, requestAdapter);
		} catch(Exception e) {
			throw new MultipartRequestException("Could not parse multipart servlet request.", e);
		}
	}

	/**
	 * Parse form fields and file item.
	 *
	 * @param fileItemListMap the file item list map
	 * @param requestAdapter the request adapter
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	private void parseMultipart(Map<String, List<FileItem>> fileItemListMap, RequestAdapter requestAdapter) throws UnsupportedEncodingException {
		String characterEncoding = requestAdapter.getCharacterEncoding();
		Map<String, List<String>> parameterListMap = new HashMap<String, List<String>>();
		Map<String, List<FileParameter>> fileParameterListMap = new HashMap<String, List<FileParameter>>();

		for(Map.Entry<String, List<FileItem>> entry : fileItemListMap.entrySet()) {
			String fieldName = entry.getKey();
			List<FileItem> fileItemList = entry.getValue();

			if(fileItemList != null && !fileItemList.isEmpty()) {
				for(FileItem fileItem : fileItemList) {
					if(fileItem.isFormField()) {
						String value = getString(fileItem, characterEncoding);
						putParameter(fieldName, value, parameterListMap);
					} else {
						String fileName = fileItem.getName();

						// Skip file uploads that don't have a file name - meaning that
						// no file was selected.
						if(fileName == null || StringUtils.isEmpty(fileName))
							continue;
						
						boolean valid = FileUtils.isValidFileExtension(fileName, allowedFileExtensions, deniedFileExtensions);
						if(!valid)
							continue;

						FileParameter fileParameter = new MultipartFileParameter(fileItem);
						putFileParameter(fieldName, fileParameter, fileParameterListMap);


						requestAdapter.setFileParameter(fieldName, fileParameter);
					}
				}
			}
		}

		if(!parameterListMap.isEmpty()) {
			for(Map.Entry<String, List<String>> entry : parameterListMap.entrySet()) {
				String name = entry.getKey();
				List<String> list = entry.getValue();
				String[] values = list.toArray(new String[list.size()]);
				requestAdapter.setParameter(name, values);
			}
		}

		if(!fileParameterListMap.isEmpty()) {
			for(Map.Entry<String, List<FileParameter>> entry : fileParameterListMap.entrySet()) {
				String name = entry.getKey();
				List<FileParameter> list = entry.getValue();
				FileParameter[] values = list.toArray(new FileParameter[list.size()]);
				requestAdapter.setFileParameter(name, values);
			}
		}
	}
	
	private String getString(FileItem fileItem, String characterEncoding) throws UnsupportedEncodingException {
		if(characterEncoding != null) {
			return fileItem.getString(characterEncoding);
		} else {
			return fileItem.getString();
		}
	}

	private void putParameter(String fieldName, String value, Map<String, List<String>> parameterListMap) {
		List<String> list = parameterListMap.get(fieldName);
		if(list == null) {
			list = new LinkedList<String>();
			parameterListMap.put(fieldName, list);
		}
		list.add(value);
	}

	private void putFileParameter(String fieldName, FileParameter fileParameter, Map<String, List<FileParameter>> parameterListMap) {
		List<FileParameter> list = parameterListMap.get(fieldName);
		if(list == null) {
			list = new LinkedList<FileParameter>();
			parameterListMap.put(fieldName, list);
		}
		list.add(fileParameter);
	}

	/**
	 * Creates a RequestContext needed by Jakarta Commons Upload.
	 * 
	 * @param req the HTTP request.
	 * @return a new request context.
	 */
	private RequestContext createRequestContext(final HttpServletRequest req) {
		return new RequestContext() {
			@Override
			public String getCharacterEncoding() {
				return req.getCharacterEncoding();
			}

			@Override
			public String getContentType() {
				return req.getContentType();
			}

			@Override
			@Deprecated
			public int getContentLength() {
				return req.getContentLength();
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return req.getInputStream();
			}
		};
	}

}
