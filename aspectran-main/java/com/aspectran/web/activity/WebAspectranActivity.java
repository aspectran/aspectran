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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aspectran.core.activity.AbstractAspectranActivity;
import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.AspectranContext;
import com.aspectran.core.rule.FileItemRule;
import com.aspectran.core.rule.FileItemRuleMap;
import com.aspectran.core.rule.RequestRule;
import com.aspectran.core.token.expression.ItemTokenExpression;
import com.aspectran.core.token.expression.ItemTokenExpressor;
import com.aspectran.core.type.FileItemUnityType;
import com.aspectran.core.type.RequestMethodType;
import com.aspectran.core.var.FileItem;
import com.aspectran.core.var.FileItemMap;
import com.aspectran.core.var.ValueMap;
import com.aspectran.web.activity.multipart.MultipartFileItem;
import com.aspectran.web.activity.multipart.MultipartRequestException;
import com.aspectran.web.activity.multipart.MultipartRequestHandler;
import com.aspectran.web.activity.multipart.MultipartRequestWrapper;
import com.aspectran.web.adapter.HttpServletRequestAdapter;
import com.aspectran.web.adapter.HttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpSessionAdapter;

/**
 * <p>Created: 2008. 04. 28 오전 12:48:48</p>
 */
public class WebAspectranActivity extends AbstractAspectranActivity implements AspectranActivity {

	private final Log log = LogFactory.getLog(WebAspectranActivity.class);

	private final boolean debugEnabled = log.isDebugEnabled();
	
	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	public WebAspectranActivity(AspectranContext context, HttpServletRequest request, HttpServletResponse response) {
		super(context);
		this.request = request;
		this.response = response;

		RequestAdapter requestAdapter = new HttpServletRequestAdapter(request);
		ResponseAdapter responseAdapter = new HttpServletResponseAdapter(response);
		//RequestDispatcherAdapter requestDispatcherAdapter = new HttpRequestDispatcherAdapter(requestAdapter, responseAdapter);
		//requestAdapter.setRequestDispatcherAdapter(requestDispatcherAdapter);
		SessionAdapter sessionAdapter = new HttpSessionAdapter(request.getSession());

		setRequestAdapter(requestAdapter);
		setResponseAdapter(responseAdapter);
		setSessionAdapter(sessionAdapter);
		
		setTransletInterfaceClass(WebTranslet.class);
		setTransletInstanceClass(AspectranWebTranslet.class);
	}
	
	public void request(String transletName) throws RequestException {
		super.request(transletName);

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

        parseParameter();
	}

	/**
	 * Parses the multipart parameters.
	 */
	private void parseMultipart() throws MultipartRequestException {
		try {
			RequestRule requestRule = getRequestRule();
			
			MultipartRequestHandler handler = new MultipartRequestHandler(request);
			handler.setMaxRequestSize(requestRule.getMaxMultipartRequestSize());
			handler.setTemporaryFilePath(requestRule.getMultipartTemporaryFilePath());
			handler.parse();
			
			// sets the servlet request wrapper
			MultipartRequestWrapper wrapper = new MultipartRequestWrapper(handler);
			request = wrapper;
			
			RequestAdapter requestAdapter = new HttpServletRequestAdapter(request);
			setRequestAdapter(requestAdapter);
			
			FileItemRuleMap fileItemRuleMap = requestRule.getFileItemRuleMap();
			
			FileItemMap fileItemMap = requestAdapter.getFileItemMap();
			
			if(fileItemMap == null) {
				fileItemMap = new FileItemMap();
				requestAdapter.setFileItemMap(fileItemMap);
			}
			
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
			
			if(debugEnabled) {
				if(requestAdapter.isMaxLengthExceeded()) {
					log.debug("Max length exceeded. MaxMultipartRequestSize: " + requestRule.getMaxMultipartRequestSize());
				}

				for(FileItemRule fir : fileItemRuleMap) {
					if(fir.getUnityType() == FileItemUnityType.ARRAY) {
						FileItem[] fileItems = fileItemMap.getFileItems(fir.getName());
						
						for(int i = 0; i < fileItems.length; i++) {
							log.debug("FileItem[" + i + "] name=" + fir.getName() + " " + fileItems[i]);
						}
					} else {
						FileItem f = fileItemMap.getFileItem(fir.getName());
						log.debug("FileItem name=" + fir.getName() + " " + f);
					}
				}

				for(Map.Entry<String, Object> entry : fileItemMap.entrySet())
					request.setAttribute(entry.getKey(), entry.getValue());
				
			}
		} catch(MultipartRequestException e) {
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * Parses the parameter.
	 */
	private void parseParameter() {
		RequestRule requestRule = getRequestRule();
		
		if(requestRule.getAttributeItemRuleMap() != null) {
			ItemTokenExpressor expressor = new ItemTokenExpression(this);
			ValueMap valueMap = expressor.express(requestRule.getAttributeItemRuleMap());

			if(valueMap != null && valueMap.size() > 0) {
				for(Map.Entry<String, Object> entry : valueMap.entrySet())
					request.setAttribute(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public AspectranActivity newAspectranActivity() {
		WebAspectranActivity activity = new WebAspectranActivity(getContext(), request, response);
		activity.setRequestAdapter(getRequestAdapter());
		activity.setResponseAdapter(getResponseAdapter());
		activity.setSessionAdapter(getSessionAdapter());
		
		return activity;
	}
	
}
