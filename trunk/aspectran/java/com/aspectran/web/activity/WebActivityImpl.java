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

import java.io.UnsupportedEncodingException;
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
import com.aspectran.core.activity.variable.ValueObjectMap;
import com.aspectran.core.activity.variable.token.ItemTokenExpression;
import com.aspectran.core.activity.variable.token.ItemTokenExpressor;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.web.activity.multipart.MultipartRequestException;
import com.aspectran.web.activity.multipart.MultipartRequestDataParser;
import com.aspectran.web.activity.multipart.MultipartRequestWrapper;
import com.aspectran.web.adapter.HttpServletRequestAdapter;
import com.aspectran.web.adapter.HttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpSessionAdapter;

/**
 * <p>Created: 2008. 04. 28 오전 12:48:48</p>
 */
public class WebActivityImpl extends CoreActivityImpl implements WebActivity {

	private static final String MULTIPART_MAX_REQUEST_SIZE = "multipart.maxRequestSize";
	
	private static final String MULTIPART_TEMPORARY_FILE_PATH = "multipart.temporaryFilePath";
	
	private static final String MULTIPART_ALLOWED_FILE_EXTENSIONS = "multipart.allowedFileExtensions";
	
	private static final String MULTIPART_DENIED_FILE_EXTENSIONS = "multipart.deniedFileExtensions";
	
	private final Logger logger = LoggerFactory.getLogger(WebActivityImpl.class);

	private final boolean debugEnabled = logger.isDebugEnabled();
	
	private RequestRule requestRule;
	
	private ResponseRule responseRule;
	
	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	public WebActivityImpl(ActivityContext context, HttpServletRequest request, HttpServletResponse response) {
		super(context);

		this.request = request;
		this.response = response;
		
		setTransletInterfaceClass(WebTranslet.class);
		setTransletImplementClass(WebTransletImpl.class);
	}
	
	public void ready(String transletName) throws CoreActivityException {
		super.ready(transletName);

		requestRule = getRequestRule();
		responseRule = getResponseRule();
		
		determineCharacterEncoding();
	}
	
	protected void request(CoreTranslet translet) throws RequestException {
		String method = request.getMethod();
		RequestMethodType methodType = requestRule.getMethod();
		
        if(methodType != null && !method.equalsIgnoreCase(methodType.toString()))
        	return;
		
		try {
			MultipartRequestWrapper requestWrapper = null;
			
			String contentType = request.getContentType();
			
	        if(method.equalsIgnoreCase(RequestMethodType.POST.toString())
	        		&& contentType != null
	        		&& contentType.startsWith("multipart/form-data")) {
	        	
	        	requestWrapper = parseMultipartFormData();
	        }

	        ValueObjectMap valueMap = parseParameter(requestWrapper);
	        
	        if(valueMap != null)
	        	translet.setDeclaredAttributeMap(valueMap);
        
		} catch(Exception e) {
			throw new RequestException("Could not parse multipart servlet request.", e);
		}
		
		RequestAdapter requestAdapter = new HttpServletRequestAdapter(request);
		ResponseAdapter responseAdapter = new HttpServletResponseAdapter(response);
		SessionAdapter sessionAdapter = new HttpSessionAdapter(request.getSession());

		setRequestAdapter(requestAdapter);
		setResponseAdapter(responseAdapter);
		setSessionAdapter(sessionAdapter);
	}

	private void determineCharacterEncoding() throws CoreActivityException {
		try {
			String characterEncoding = requestRule.getCharacterEncoding();
			
			if(characterEncoding == null)
				characterEncoding = (String)getRequestSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);
			
			if(characterEncoding != null)
				request.setCharacterEncoding(characterEncoding);
		
			responseRule.getCharacterEncoding();
	
			if(characterEncoding == null)
				characterEncoding = (String)getResponseSetting(ResponseRule.CHARACTER_ENCODING_SETTING_NAME);
	
			if(characterEncoding != null)
				response.setCharacterEncoding(characterEncoding);
		} catch(UnsupportedEncodingException e) {
			throw new CoreActivityException(e);
		}
	}
	
	/**
	 * Parses the multipart parameters.
	 */
	private MultipartRequestWrapper parseMultipartFormData() throws MultipartRequestException {
		String multipartMaxRequestSize = (String)getRequestSetting(MULTIPART_MAX_REQUEST_SIZE);
		String multipartTemporaryFilePath = (String)getRequestSetting(MULTIPART_TEMPORARY_FILE_PATH);
		String multipartAllowedFileExtensions = (String)getRequestSetting(MULTIPART_ALLOWED_FILE_EXTENSIONS);
		String multipartDeniedFileExtensions = (String)getRequestSetting(MULTIPART_DENIED_FILE_EXTENSIONS);
		
		MultipartRequestDataParser parser = new MultipartRequestDataParser(request);
		parser.setMaxRequestSize(new Long(multipartMaxRequestSize));
		parser.setTemporaryFilePath(multipartTemporaryFilePath);
		parser.setAllowedFileExtensions(multipartAllowedFileExtensions);
		parser.setDeniedFileExtensions(multipartDeniedFileExtensions);
		parser.parse();
		
		// sets the servlet request wrapper
		MultipartRequestWrapper requestWrapper = new MultipartRequestWrapper(parser);
		
		return requestWrapper;
	}
	
	/**
	 * Parses the parameter.
	 */
	private ValueObjectMap parseParameter(MultipartRequestWrapper requestWrapper) {
		ItemRuleMap attributeItemRuleMap = requestRule.getAttributeItemRuleMap();
		
		if(attributeItemRuleMap != null) {
			ItemTokenExpressor expressor = new ItemTokenExpression(this);
			ValueObjectMap valueMap = expressor.express(attributeItemRuleMap);

			for(ItemRule itemRule : attributeItemRuleMap.values()) {
				String name = itemRule.getName();
				
				if(requestWrapper != null) {
					if(itemRule.getValueType() == ItemValueType.FILE_ITEM && itemRule.getValue() == null) {
						FileItem fileItem = requestWrapper.getFileItem(name);
					}
				} else {
					Object value = valueMap.get(name);
					if(value != null)
						request.setAttribute(name, value);
				}
			}
			
			if(valueMap != null && valueMap.size() > 0) {
				for(Map.Entry<String, Object> entry : valueMap.entrySet())
					request.setAttribute(entry.getKey(), entry.getValue());
				
				return valueMap;
			}
		}
		
		return null;
	}
	
	public CoreActivity newCoreActivity() {
		WebActivityImpl webActivity = new WebActivityImpl(getActivityContext(), request, response);
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
