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
package com.aspectran.web.support.multipart.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.util.FilenameUtils;
import com.aspectran.core.util.LinkedMultiValueMap;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.activity.request.multipart.MultipartFormDataParser;
import com.aspectran.web.activity.request.multipart.MultipartRequestException;

/**
 * Multi-part form data parser for Apache Commons FileUpload.
 */
public class CommonsMultipartFormDataParser implements MultipartFormDataParser {

	private static final Log log = LogFactory.getLog(CommonsMultipartFormDataParser.class);

	/** The constant DEFAULT_MAX_REQUEST_SIZE. */
	private static final long DEFAULT_MAX_REQUEST_SIZE = 250 * 1024 * 1024;

	/** The constant DEFAULT_SIZE_THRESHOLD. */
	private static final int DEFAULT_SIZE_THRESHOLD = 256 * 1024;

	private String tempDirectoryPath;

	private long maxRequestSize = DEFAULT_MAX_REQUEST_SIZE;

	private String allowedFileExtensions;

	private String deniedFileExtensions;

	/**
	 * Instantiates a new MultipartFormDataParser.
	 */
	public CommonsMultipartFormDataParser() {
	}

	@Override
	public String getTempDirectoryPath() {
		return tempDirectoryPath;
	}

	@Override
	public void setTempDirectoryPath(String tempDirectoryPath) {
		this.tempDirectoryPath = tempDirectoryPath;
	}

	@Override
	public long getMaxRequestSize() {
		return maxRequestSize;
	}

	@Override
	public void setMaxRequestSize(long maxSize) {
		this.maxRequestSize = maxSize;
	}

	@Override
	public String getAllowedFileExtensions() {
		return allowedFileExtensions;
	}

	@Override
	public void setAllowedFileExtensions(String allowedFileExtensions) {
		this.allowedFileExtensions = allowedFileExtensions;
	}

	@Override
	public String getDeniedFileExtensions() {
		return deniedFileExtensions;
	}

	@Override
	public void setDeniedFileExtensions(String deniedFileExtensions) {
		this.deniedFileExtensions = deniedFileExtensions;
	}

	@Override
	public void parse(RequestAdapter requestAdapter) {
		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(DEFAULT_SIZE_THRESHOLD);

			if(tempDirectoryPath != null) {
				File repository = new File(tempDirectoryPath);
				if(!repository.exists() && !repository.mkdirs()) {
					throw new IllegalArgumentException("Given tempDirectoryPath [" + tempDirectoryPath + "] could not be created.");
				}
				factory.setRepository(repository);
			}

			FileUpload upload = new ServletFileUpload(factory);
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
		MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
		MultiValueMap<String, FileParameter> fileParameterMap = new LinkedMultiValueMap<>();

		for(Map.Entry<String, List<FileItem>> entry : fileItemListMap.entrySet()) {
			String fieldName = entry.getKey();
			List<FileItem> fileItemList = entry.getValue();

			if(fileItemList != null && !fileItemList.isEmpty()) {
				for(FileItem fileItem : fileItemList) {
					if(fileItem.isFormField()) {
						String value = getString(fileItem, characterEncoding);
						parameterMap.add(fieldName, value);
					} else {
						String fileName = fileItem.getName();

						// Skip file uploads that don't have a file name - meaning that
						// no file was selected.
						if(fileName == null || StringUtils.isEmpty(fileName))
							continue;
						
						boolean valid = FilenameUtils.isValidFileExtension(fileName, allowedFileExtensions, deniedFileExtensions);
						if(!valid)
							continue;

						FileParameter fileParameter = new CommonsMultipartFileParameter(fileItem);
						fileParameterMap.add(fieldName, fileParameter);
					}
				}
			}
		}

		if(!parameterMap.isEmpty()) {
			for(Map.Entry<String, List<String>> entry : parameterMap.entrySet()) {
				String name = entry.getKey();
				List<String> list = entry.getValue();
				String[] values = list.toArray(new String[list.size()]);
				requestAdapter.setParameter(name, values);
			}
		}

		if(!fileParameterMap.isEmpty()) {
			for(Map.Entry<String, List<FileParameter>> entry : fileParameterMap.entrySet()) {
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
