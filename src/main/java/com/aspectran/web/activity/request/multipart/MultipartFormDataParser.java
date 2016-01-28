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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.util.FileUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.activity.request.multipart.MultipartFileParameter;
import com.aspectran.web.activity.request.multipart.MultipartFormDataParser;
import com.aspectran.web.activity.request.multipart.MultipartRequestException;

/**
 * Multi-part form data request handler.
 */
public class MultipartFormDataParser {
	
	private static final Log log = LogFactory.getLog(MultipartFormDataParser.class);

	/**
	 * The constant DEFAULT_MAX_REQUEST_SIZE.
	 */
	public static final long DEFAULT_MAX_REQUEST_SIZE = 250 * 1024 * 1024;

	/**
	 * The constant DEFAULT_SIZE_THRESHOLD.
	 */
	public static final int DEFAULT_SIZE_THRESHOLD = 256 * 1024;

	private String characterEncoding;

	private String temporaryFilePath;
	
	private long maxRequestSize = DEFAULT_MAX_REQUEST_SIZE;

	private final HttpServletRequest request;

	private final Map<String, List<String>> parsedParameterListMap;

	private final Map<String, List<FileParameter>> parsedFileParameterListMap;

	private boolean maxLengthExceeded;
	
	private boolean parsed;
	
	private String allowedFileExtensions;

	private String deniedFileExtensions;

	/**
	 * Instantiates a new MultipartFormDataParser.
	 *
	 * @param request the HTTP request
	 */
	public MultipartFormDataParser(HttpServletRequest request) {
		this.request = request;
		this.characterEncoding = request.getCharacterEncoding();
		
		this.parsedParameterListMap = new HashMap<String, List<String>>();
		this.parsedFileParameterListMap = new HashMap<String, List<FileParameter>>();
	}

	/**
	 * Gets the HTTP request.
	 *
	 * @return the HTTP request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * Gets character encoding.
	 *
	 * @return the character encoding
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	/**
	 * Sets character encoding.
	 *
	 * @param characterEncoding the character encoding
	 */
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	/**
	 * Returns directory path used for temporary files.
	 *
	 * @return the directory path used for temporary files
	 */
	public String getTemporaryFilePath() {
		if(temporaryFilePath == null)
			return System.getProperty("java.io.tmpdir");

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
	 * Gets allowed file extensions.
	 *
	 * @return the allowed file extensions
	 */
	public String getAllowedFileExtensions() {
		return allowedFileExtensions;
	}

	/**
	 * Sets allowed file extensions.
	 *
	 * @param allowedFileExtensions the allowed file extensions
	 */
	public void setAllowedFileExtensions(String allowedFileExtensions) {
		this.allowedFileExtensions = allowedFileExtensions;
	}

	/**
	 * Gets denied file extensions.
	 *
	 * @return the denied file extensions
	 */
	public String getDeniedFileExtensions() {
		return deniedFileExtensions;
	}

	/**
	 * Sets denied file extensions.
	 *
	 * @param deniedFileExtensions the denied file extensions
	 */
	public void setDeniedFileExtensions(String deniedFileExtensions) {
		this.deniedFileExtensions = deniedFileExtensions;
	}

	/**
	 * Parse the given servlet request, resolving its multipart elements.
	 *
	 * @throws MultipartRequestException if multipart resolution failed
	 */
	public void parse() {
		if(parsed)
			return;
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		
		factory.setSizeThreshold(DEFAULT_SIZE_THRESHOLD);

		if(getTemporaryFilePath() != null) {
			File repository = new File(temporaryFilePath);
			
			if(!repository.exists() && !repository.mkdirs()) {
				throw new IllegalArgumentException("Given temporaryFilePath [" + temporaryFilePath + "] could not be created.");
			}
			
			factory.setRepository(repository);
		}

		try {
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setSizeMax(maxRequestSize);
			upload.setHeaderEncoding(request.getCharacterEncoding());

			Map<String, List<FileItem>> fileItemListMap;

			try {
				RequestContext requestContext = createRequestContext(request);
				fileItemListMap = upload.parseParameterMap(requestContext);
			} catch(SizeLimitExceededException e) {
				log.warn("Max length exceeded. multipart.maxRequestSize: " + maxRequestSize);
				maxLengthExceeded = true;
				return;
			}

			parseMultipart(fileItemListMap);
			
			parsed = true;
		} catch(Exception e) {
			throw new MultipartRequestException("Could not parse multipart servlet request.", e);
		}
	}

	/**
	 * Parse form fields and file item.
	 *
	 * @param fileItemListMap the file item list map
	 */
	public void parseMultipart(Map<String, List<FileItem>> fileItemListMap) {
		for(Map.Entry<String, List<FileItem>> entry : fileItemListMap.entrySet()) {
			String fieldName = entry.getKey();
			List<FileItem> fileItemList = entry.getValue();

			if(fileItemList != null && fileItemList.size() > 0) {
				for(FileItem fileItem : fileItemList) {
					if(fileItem.isFormField()) {
						List<String> parameterList = parsedParameterListMap.get(fieldName);
						
						if(parameterList == null) {
							parameterList = new ArrayList<String>(fileItemList.size());
							parsedParameterListMap.put(fieldName, parameterList);
						}
						
						parameterList.add(getString(fileItem));
					} else {
						// Skip file uploads that don't have a file name - meaning that
						// no file was selected.
						if(fileItem.getName() == null || fileItem.getName().trim().length() == 0)
							continue;
						
						boolean valid = FileUtils.isValidFileExtension(fileItem.getName(), allowedFileExtensions, deniedFileExtensions);
						
						if(!valid)
							continue;
						
						List<FileParameter> fileParameterList = parsedFileParameterListMap.get(fieldName);
						
						if(fileParameterList == null) {
							fileParameterList = new ArrayList<FileParameter>(fileItemListMap.size());
							parsedFileParameterListMap.put(fieldName, fileParameterList);
						}
						
						FileParameter fileParameter = new MultipartFileParameter(fileItem);
						fileParameterList.add(fileParameter);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the string.
	 * 
	 * @param fileItem the file item
	 * 
	 * @return the string
	 */
	private String getString(FileItem fileItem) {
		String value = null;

		if(characterEncoding != null) {
			try {
				value = fileItem.getString(characterEncoding);
			} catch(Exception e) {
				value = null;
			}
		}

		if(value == null)
			value = fileItem.getString();

		return value;
	}

	/**
	 * Returns whether request header has exceed the maximum length
	 *
	 * @return true, if is max length exceeded
	 */
	public boolean isMaxLengthExceeded() {
		return maxLengthExceeded;
	}

	/**
	 * Gets the parameter names.
	 *
	 * @return the parameter names
	 */
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parsedParameterListMap.keySet());
	}

	/**
	 * Returns the parameter value.
	 *
	 * @param name the parameter name
	 * @return String the parameter value
	 */
	public String getParameter(String name) {
		List<String> items = parsedParameterListMap.get(name);

		if(items == null || items.size() == 0)
			return null;

		return items.get(0);
	}

	/**
	 * Returns an array of the parameter values.
	 *
	 * @param name the parameter name
	 * @return String[] an array of the parameter values
	 */
	public String[] getParameterValues(String name) {
		List<String> items = parsedParameterListMap.get(name);

		if(items == null || items.size() == 0)
			return null;

		return items.toArray(new String[items.size()]);
	}

	/**
	 * Gets parameter list.
	 *
	 * @param name the name
	 * @return the parameter list
	 */
	public List<String> getParameterList(String name) {
		return parsedParameterListMap.get(name);
	}

	/**
	 * Gets the multipart file item names.
	 *
	 * @return the multipart file item names
	 */
	public Enumeration<String> getFileParameterNames() {
		return Collections.enumeration(parsedFileParameterListMap.keySet());
	}

	/**
	 * Gets the multipart file item.
	 *
	 * @param name the name of the multipart file item
	 * @return the multipart item
	 */
	public FileParameter getFileParameter(String name) {
		List<FileParameter> list = parsedFileParameterListMap.get(name);

		if(list == null || list.size() == 0)
			return null;

		return list.get(0);
	}

	/**
	 * Gets the multipart items.
	 *
	 * @param name the name
	 * @return the multipart file items
	 */
	public FileParameter[] getFileParameters(String name) {
		List<FileParameter> list = parsedFileParameterListMap.get(name);
		
		if(list == null)
			return null;
		
		return list.toArray(new MultipartFileParameter[list.size()]);
	}

	/**
	 * Gets file parameter list.
	 *
	 * @param name the name
	 * @return the file parameter list
	 */
	public List<FileParameter> getFileParameterList(String name) {
		return parsedFileParameterListMap.get(name);
	}
	
	/**
	 * Creates a RequestContext needed by Jakarta Commons Upload.
	 * 
	 * @param req the request.
	 * 
	 * @return a new request context.
	 */
	private RequestContext createRequestContext(final HttpServletRequest req) {
		return new RequestContext() {
			public String getCharacterEncoding() {
				return req.getCharacterEncoding();
			}

			public String getContentType() {
				return req.getContentType();
			}

			@Deprecated
			public int getContentLength() {
				return req.getContentLength();
			}

			public InputStream getInputStream() throws IOException {
				return req.getInputStream();
			}
		};
	}

}
