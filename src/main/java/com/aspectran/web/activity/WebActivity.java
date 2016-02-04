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
package com.aspectran.web.activity;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.ItemTokenExpression;
import com.aspectran.core.context.expr.ItemTokenExpressor;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.RequestMethodType;
import com.aspectran.core.context.variable.ValueMap;
import com.aspectran.web.activity.request.multipart.MultipartRequestException;
import com.aspectran.web.activity.request.multipart.MultipartRequestWrapper;
import com.aspectran.web.activity.request.multipart.MultipartRequestWrapperResolver;
import com.aspectran.web.adapter.GZipHttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpServletRequestAdapter;
import com.aspectran.web.adapter.HttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpSessionAdapter;

/**
 * The Class WebActivity.
 *
 * @since 2008. 04. 28
 */
public class WebActivity extends CoreActivity implements Activity {

	private static final String MULTIPART_REQUEST_WRAPPER_RESOLVER = "multipartRequestWrapperResolver";
	
	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	private MultipartRequestWrapper requestWrapper;
	
	/**
	 * Instantiates a new WebActivity.
	 *
	 * @param context the current ActivityContext
	 * @param request the HTTP request
	 * @param response the HTTP response
	 */
	public WebActivity(ActivityContext context, HttpServletRequest request, HttpServletResponse response) {
		super(context);

		this.request = request;
		this.response = response;
	}

	@Override
	protected void adapting(Translet translet) {
		RequestAdapter requestAdapter = new HttpServletRequestAdapter(request);
		setRequestAdapter(requestAdapter);

		String acceptEncoding = request.getHeader("Accept-Encoding");
		if(acceptEncoding != null && acceptEncoding.indexOf("gzip") > -1) {
			ResponseAdapter responseAdapter = new GZipHttpServletResponseAdapter(response);
			setResponseAdapter(responseAdapter);
		} else {
			ResponseAdapter responseAdapter = new HttpServletResponseAdapter(response);
			setResponseAdapter(responseAdapter);
		}
	}

	@Override
	public synchronized SessionAdapter getSessionAdapter() {
		if(super.getSessionAdapter() == null) {
			SessionAdapter sessionAdapter = new HttpSessionAdapter(request.getSession(), getActivityContext());
			super.setSessionAdapter(sessionAdapter);
		}
		return super.getSessionAdapter();
	}

	@Override
	protected void request(Translet translet) {
		String method = request.getMethod();
		RequestMethodType requestMethod = getRequestRule().getRequestMethod();
		
		if(requestMethod != null && !requestMethod.toString().equals(method)) {
			throw new RequestMethodNotAllowedException(requestMethod);
		}

		requestWrapper = getMultipartRequestWrapper();
    	
		if(requestWrapper != null) {
			request = requestWrapper;

			RequestAdapter requestAdapter = getRequestAdapter();
			requestAdapter.setAdaptee(requestWrapper);
			
			Enumeration<String> names = requestWrapper.getFileParameterNames();
			
			while(names.hasMoreElements()) {
				String name = names.nextElement();
				getRequestAdapter().setFileParameter(name, requestWrapper.getFileParameters(name));
			}
			
			requestAdapter.setMaxLengthExceeded(requestWrapper.isMaxLengthExceeded());
		}

        ValueMap valueMap = parseDeclaredParameter(requestWrapper);
        
        if(valueMap != null)
        	translet.setDeclaredAttributeMap(valueMap);
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

			String multipartRequestWrapperResolver = getRequestSetting(MULTIPART_REQUEST_WRAPPER_RESOLVER);
			if(multipartRequestWrapperResolver == null) {
				throw new MultipartRequestException("'multipartRequestWrapperResolver' was not specified.");
			}

			MultipartRequestWrapperResolver resolver = getBean(multipartRequestWrapperResolver);
			if(resolver == null) {
				throw new MultipartRequestException("No bean named 'multipartRequestWrapperResolver' is defined");
			}
				
			return resolver.getMultipartRequestWrapper(getTranslet());
        }
        
        return null;
	}
	
	/**
	 * Parses the parameter.
	 *
	 * @param requestWrapper the multipart request wrapper
	 * @return the value map
	 */
	private ValueMap parseDeclaredParameter(MultipartRequestWrapper requestWrapper) {
		ItemRuleMap attributeItemRuleMap = getRequestRule().getAttributeItemRuleMap();
		
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

			if(!valueMap.isEmpty())
				return valueMap;
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		WebActivity webActivity = new WebActivity(getActivityContext(), request, response);
		return (T)webActivity;
	}

}
