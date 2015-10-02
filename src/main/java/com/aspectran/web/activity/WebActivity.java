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
package com.aspectran.web.activity;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.variable.ValueMap;
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
import com.aspectran.core.util.FileUtils;
import com.aspectran.web.activity.multipart.MultipartFormDataParser;
import com.aspectran.web.activity.multipart.MultipartRequestException;
import com.aspectran.web.activity.multipart.MultipartRequestWrapper;
import com.aspectran.web.adapter.HttpServletRequestAdapter;
import com.aspectran.web.adapter.HttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpSessionAdapter;

/**
 * The Class WebActivity.
 *
 * @since 2008. 04. 28
 */
public class WebActivity extends CoreActivity implements Activity {

	/** The Constant MULTIPART_MAX_REQUEST_SIZE. */
	private static final String MULTIPART_MAX_REQUEST_SIZE = "multipart.maxRequestSize";
	
	/** The Constant MULTIPART_TEMPORARY_FILE_PATH. */
	private static final String MULTIPART_TEMPORARY_FILE_PATH = "multipart.temporaryFilePath";
	
	/** The Constant MULTIPART_ALLOWED_FILE_EXTENSIONS. */
	private static final String MULTIPART_ALLOWED_FILE_EXTENSIONS = "multipart.allowedFileExtensions";
	
	/** The Constant MULTIPART_DENIED_FILE_EXTENSIONS. */
	private static final String MULTIPART_DENIED_FILE_EXTENSIONS = "multipart.deniedFileExtensions";
	
	/** The request rule. */
	private RequestRule requestRule;
	
	/** The response rule. */
	private ResponseRule responseRule;
	
	/** The request. */
	private HttpServletRequest request;
	
	/** The response. */
	private HttpServletResponse response;
	
	/** The multipart request wrapper. */
	private MultipartRequestWrapper multipartRequestWrapper;
	
	/**
	 * Instantiates a new web activity.
	 *
	 * @param context the context
	 * @param request the request
	 * @param response the response
	 */
	public WebActivity(ActivityContext context, HttpServletRequest request, HttpServletResponse response) {
		super(context);

		this.request = request;
		this.response = response;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#adapting(com.aspectran.core.activity.Translet)
	 */
	protected void adapting(Translet translet) {
		requestRule = getRequestRule();
		responseRule = getResponseRule();
		
		determineCharacterEncoding();

		multipartRequestWrapper = getMultipartRequestWrapper();
    	
    	if(multipartRequestWrapper != null)
    		request = multipartRequestWrapper;

		RequestAdapter requestAdapter = new HttpServletRequestAdapter(request);
		setRequestAdapter(requestAdapter);

		ResponseAdapter responseAdapter = new HttpServletResponseAdapter(response);
		setResponseAdapter(responseAdapter);

		SessionAdapter sessionAdapter = new HttpSessionAdapter(request.getSession());
		setSessionAdapter(sessionAdapter);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#request(com.aspectran.core.activity.Translet)
	 */
	protected void request(Translet translet) {
		String method = request.getMethod();
		RequestMethodType requestMethod = requestRule.getRequestMethod();
		
		if(requestMethod != null && !requestMethod.toString().equals(method)) {
			throw new RequestMethodNotAllowedException(requestMethod);
		}
		
		if(multipartRequestWrapper != null) {
			multipartRequestWrapper.parse();
			
			Enumeration<String> names = multipartRequestWrapper.getFileParameterNames();
			
			while(names.hasMoreElements()) {
				String name = names.nextElement();
				getRequestAdapter().setFileParameter(name, multipartRequestWrapper.getFileParameters(name));
			}
			
			getRequestAdapter().setMaxLengthExceeded(multipartRequestWrapper.isMaxLengthExceeded());
		}

        ValueMap valueMap = parseDeclaredParameter(multipartRequestWrapper);
        
        if(valueMap != null)
        	translet.setDeclaredAttributeMap(valueMap);
	}
	
	/**
	 * Determine character encoding.
	 *
	 * @throws ActivityException the activity exception
	 */
	private void determineCharacterEncoding() {
		try {
			String characterEncoding = requestRule.getCharacterEncoding();
			
			if(characterEncoding == null)
				characterEncoding = (String)getRequestSetting(RequestRule.CHARACTER_ENCODING_SETTING_NAME);
			
			if(characterEncoding != null)
				request.setCharacterEncoding(characterEncoding);
		
			characterEncoding = responseRule.getCharacterEncoding();
	
			if(characterEncoding == null)
				characterEncoding = (String)getResponseSetting(ResponseRule.CHARACTER_ENCODING_SETTING_NAME);
	
			if(characterEncoding != null)
				response.setCharacterEncoding(characterEncoding);
		} catch(UnsupportedEncodingException e) {
			throw new ActivityException(e);
		}
	}
	
	/**
	 * Gets the multipart request wrapper.
	 *
	 * @return the multipart request wrapper
	 * @throws MultipartRequestException the multipart request exception
	 */
	private MultipartRequestWrapper getMultipartRequestWrapper() {
		String method = request.getMethod();
		String contentType = request.getContentType();
		
        if(RequestMethodType.POST.toString().equals(method)
        		&& contentType != null
        		&& contentType.startsWith("multipart/form-data")) {

			String multipartMaxRequestSize = (String)getRequestSetting(MULTIPART_MAX_REQUEST_SIZE);
			String multipartTemporaryFilePath = (String)getRequestSetting(MULTIPART_TEMPORARY_FILE_PATH);
			String multipartAllowedFileExtensions = (String)getRequestSetting(MULTIPART_ALLOWED_FILE_EXTENSIONS);
			String multipartDeniedFileExtensions = (String)getRequestSetting(MULTIPART_DENIED_FILE_EXTENSIONS);
	
			long maxRequestSize = FileUtils.formattedSizeToBytes(multipartMaxRequestSize, -1);
			
			MultipartFormDataParser parser = new MultipartFormDataParser(request);
			
			if(maxRequestSize > -1)
				parser.setMaxRequestSize(maxRequestSize);
			
			parser.setTemporaryFilePath(multipartTemporaryFilePath);
			parser.setAllowedFileExtensions(multipartAllowedFileExtensions);
			parser.setDeniedFileExtensions(multipartDeniedFileExtensions);
			
			// sets the servlet request wrapper
			MultipartRequestWrapper requestWrapper = new MultipartRequestWrapper(parser);
			
			return requestWrapper;
        }
        
        return null;
	}
	
	/**
	 * Parses the parameter.
	 *
	 * @param requestWrapper the request wrapper
	 * @return the value map
	 */
	private ValueMap parseDeclaredParameter(MultipartRequestWrapper requestWrapper) {
		ItemRuleMap attributeItemRuleMap = requestRule.getAttributeItemRuleMap();
		
		if(attributeItemRuleMap != null) {
			ItemTokenExpressor expressor = new ItemTokenExpression(this);
			ValueMap valueMap = expressor.express(attributeItemRuleMap);

			for(ItemRule itemRule : attributeItemRuleMap.values()) {
				String name = itemRule.getName();
				
				if(requestWrapper != null) {
					if(itemRule.getValueType() == ItemValueType.MULTIPART_FILE) {
						Object value = requestWrapper.getFileParameter(name, itemRule);
						valueMap.put(name, value);
						request.setAttribute(name, value);
					}
				} else {
					Object value = valueMap.get(name);
					if(value != null) {
						request.setAttribute(name, value);
					}
				}
			}

			if(valueMap.size() > 0)
				return valueMap;
		}
		
		/*
		if(debugEnabled) {
			if(requestAdapter.isMaxLengthExceeded()) {
				logger.debug("Max length exceeded. multipart.maxRequestSize: " + multipartMaxRequestSize);
			}

			for(FileItemRule fir : fileItemRuleMap) {
				if(fir.getUnityType() == FileItemUnityType.ARRAY) {
					FileParameter[] fileItems = fileItemMap.getFileItems(fir.getName());
					
					for(int i = 0; i < fileItems.length; i++) {
						logger.debug("fileItem[" + i + "] name=" + fir.getName() + " " + fileItems[i]);
					}
				} else {
					FileParameter f = fileItemMap.getFileItem(fir.getName());
					logger.debug("fileItem name=" + fir.getName() + " " + f);
				}
			}
		}
		*/
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.activity.CoreActivity#newActivity()
	 */
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		WebActivity webActivity = new WebActivity(getActivityContext(), request, response);
		return (T)webActivity;
	}

}
