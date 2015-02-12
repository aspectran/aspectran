/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.web.activity;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.CoreActivityException;
import com.aspectran.core.activity.CoreActivityImpl;
import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.activity.variable.FileItem;
import com.aspectran.core.activity.variable.FileItemMap;
import com.aspectran.core.activity.variable.ValueObjectMap;
import com.aspectran.core.activity.variable.token.ItemTokenExpression;
import com.aspectran.core.activity.variable.token.ItemTokenExpressor;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.FileItemRule;
import com.aspectran.core.context.rule.FileItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.type.FileItemUnityType;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.web.activity.multipart.MultipartFileItem;
import com.aspectran.web.activity.multipart.MultipartRequestException;
import com.aspectran.web.activity.multipart.MultipartRequestDataParser;
import com.aspectran.web.activity.multipart.MultipartRequestWrapper;
import com.aspectran.web.adapter.HttpServletRequestAdapter;
import com.aspectran.web.adapter.HttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpSessionAdapter;

/**
 * <p>Created: 2008. 04. 28 오전 12:48:48</p>
 */
public class WebActivityImplBak extends CoreActivityImpl implements WebActivity {

	private static final String MULTIPART_MAX_REQUEST_SIZE = "multipart.maxRequestSize";
	
	private static final String MULTIPART_TEMPORARY_FILE_PATH = "multipart.temporaryFilePath";
	
	private static final String MULTIPART_ALLOWED_FILE_EXTENSIONS = "multipart.allowedFileExtensions";
	
	private static final String MULTIPART_DENIED_FILE_EXTENSIONS = "multipart.deniedFileExtensions";
	
	private final Logger logger = LoggerFactory.getLogger(WebActivityImplBak.class);

	private final boolean debugEnabled = logger.isDebugEnabled();
	
	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	public WebActivityImplBak(ActivityContext context, HttpServletRequest request, HttpServletResponse response) {
		super(context);
		
		this.request = request;
		this.response = response;
		
		setTransletInterfaceClass(WebTranslet.class);
		setTransletImplementClass(WebTransletImpl.class);
	}
	
	public void ready(String transletName) throws CoreActivityException {
		super.ready(transletName);
		
		RequestAdapter requestAdapter = new HttpServletRequestAdapter(request);
		ResponseAdapter responseAdapter = new HttpServletResponseAdapter(response);
		SessionAdapter sessionAdapter = new HttpSessionAdapter(request.getSession());

		setRequestAdapter(requestAdapter);
		setResponseAdapter(responseAdapter);
		setSessionAdapter(sessionAdapter);
	}
	
	protected void request(CoreTranslet translet) throws RequestException {
		RequestRule requestRule = getRequestRule();
		ResponseRule responseRule = getResponseRule();
		RequestAdapter requestAdapter = getRequestAdapter();
		ResponseAdapter responseAdapter = getResponseAdapter();
		
		try {
			if(requestAdapter != null) {
				String characterEncoding = requestRule.getCharacterEncoding();
				
				if(characterEncoding == null)
					characterEncoding = (String)getRequestSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);
				
				if(characterEncoding != null)
					requestAdapter.setCharacterEncoding(characterEncoding);
			}
			
			if(responseAdapter != null) {
				String characterEncoding = responseRule.getCharacterEncoding();

				if(characterEncoding == null)
					characterEncoding = (String)getResponseSetting(ResponseRule.CHARACTER_ENCODING_SETTING_NAME);

				if(characterEncoding != null)
					responseAdapter.setCharacterEncoding(characterEncoding);
			}
		
			String method = request.getMethod();
			RequestMethodType methodType = getRequestRule().getMethod();
			
	        if(methodType != null
	        		&& !method.equalsIgnoreCase(methodType.toString()))
	        	return;
	        	
	        String contentType = request.getContentType();
	
	        if(method.equalsIgnoreCase(RequestMethodType.POST.toString())
	        		&& contentType != null
	        		&& contentType.startsWith("multipart/form-data")) {
	        	parseMultipart();
	        }
	
	        ValueObjectMap valueMap = parseParameter();
	        
	        if(valueMap != null)
	        	translet.setDeclaredAttributeMap(valueMap);
        
		} catch(Exception e) {
			throw new RequestException(e);
		}
	}
	
	private boolean isMultipart() {
		String method = request.getMethod();
        String contentType = request.getContentType();
		
        if(method.equalsIgnoreCase(RequestMethodType.POST.toString())
        		&& contentType != null
        		&& contentType.startsWith("multipart/form-data")) {
        	return true;
        }
        
        return false;
	}

	/**
	 * Parses the multipart parameters.
	 */
	private void parseMultipart() throws MultipartRequestException {
		RequestRule requestRule = getRequestRule();

		String multipartMaxRequestSize = (String)getRequestSetting(MULTIPART_MAX_REQUEST_SIZE);
		String multipartTemporaryFilePath = (String)getRequestSetting(MULTIPART_TEMPORARY_FILE_PATH);
		String multipartAllowedFileExtensions = (String)getRequestSetting(MULTIPART_ALLOWED_FILE_EXTENSIONS);
		String multipartDeniedFileExtensions = (String)getRequestSetting(MULTIPART_DENIED_FILE_EXTENSIONS);
		
		MultipartRequestDataParser handler = new MultipartRequestDataParser(request);
		handler.setMaxRequestSize(new Long(multipartMaxRequestSize));
		handler.setTemporaryFilePath(multipartTemporaryFilePath);
		handler.setAllowedFileExtensions(multipartAllowedFileExtensions);
		handler.setDeniedFileExtensions(multipartDeniedFileExtensions);
		handler.parse();
		
		// sets the servlet request wrapper
		MultipartRequestWrapper wrapper = new MultipartRequestWrapper(handler);
		request = wrapper;
		
		RequestAdapter requestAdapter = new HttpServletRequestAdapter(request);
		setRequestAdapter(requestAdapter);
		
		FileItemRuleMap fileItemRuleMap = requestRule.getFileItemRuleMap();
		
		FileItemMap fileItemMap = requestAdapter.touchFileItemMap();

		for(FileItemRule fir : fileItemRuleMap) {
			if(fir.getUnityType() == FileItemUnityType.ARRAY) {
				MultipartFileItem[] multipartFileItems = handler.getMultipartFileItems(fir.getName());
				
				if(multipartFileItems != null) {
					fileItemMap.putFileItem(fir.getName(), multipartFileItems);
				}
			} else {
				MultipartFileItem multipartFileItem = handler.getMultipartFileItem(fir.getName());
				fileItemMap.putFileItem(fir.getName(), multipartFileItem);
			}
		}
		
		requestAdapter.setMaxLengthExceeded(handler.isMaxLengthExceeded());
		
		if(requestRule.isFileItemCanBeAttribute()) {
			for(Map.Entry<String, Object> entry : fileItemMap.entrySet())
				request.setAttribute(entry.getKey(), entry.getValue());
		}
		
		if(debugEnabled) {
			if(requestAdapter.isMaxLengthExceeded()) {
				logger.debug("Max length exceeded. multipart.maxRequestSize: " + multipartMaxRequestSize);
			}

			for(FileItemRule fir : fileItemRuleMap) {
				if(fir.getUnityType() == FileItemUnityType.ARRAY) {
					FileItem[] fileItems = fileItemMap.getFileItems(fir.getName());
					
					for(int i = 0; i < fileItems.length; i++) {
						logger.debug("fileItem[" + i + "] name=" + fir.getName() + " " + fileItems[i]);
					}
				} else {
					FileItem f = fileItemMap.getFileItem(fir.getName());
					logger.debug("fileItem name=" + fir.getName() + " " + f);
				}
			}
		}
	}
	
	/**
	 * Parses the parameter.
	 */
	private ValueObjectMap parseParameter() {
		RequestRule requestRule = getRequestRule();
		
		if(requestRule.getAttributeItemRuleMap() != null) {
			ItemTokenExpressor expressor = new ItemTokenExpression(this);
			ValueObjectMap valueMap = expressor.express(requestRule.getAttributeItemRuleMap());

			if(valueMap != null && valueMap.size() > 0) {
				for(Map.Entry<String, Object> entry : valueMap.entrySet())
					request.setAttribute(entry.getKey(), entry.getValue());
				
				return valueMap;
			}
		}
		
		return null;
	}
	
	public CoreActivity newCoreActivity() {
		WebActivityImplBak webActivity = new WebActivityImplBak(getActivityContext(), request, response);
		return webActivity;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.web.activity.WebTranslet#getHttpServletRequest()
	 */
	public HttpServletRequest getHttpServletRequest() {
		return request;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.web.activity.WebTranslet#getHttpServletResponse()
	 */
	public HttpServletResponse getHttpServletResponse() {
		return response;
	}

}
