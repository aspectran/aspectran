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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.locale.LocaleChangeInterceptor;
import com.aspectran.core.context.locale.LocaleResolver;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.web.activity.request.multipart.MultipartFormDataParser;
import com.aspectran.web.activity.request.multipart.MultipartRequestException;
import com.aspectran.web.activity.request.parser.HttpPutFormContentParser;
import com.aspectran.web.adapter.GZipHttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpServletRequestAdapter;
import com.aspectran.web.adapter.HttpServletResponseAdapter;
import com.aspectran.web.adapter.HttpSessionAdapter;
import com.aspectran.web.support.http.HttpHeaders;

/**
 * The Class WebActivity.
 *
 * @since 2008. 4. 28.
 */
public class WebActivity extends CoreActivity {

	private static final String MULTIPART_FORM_DATA_PARSER_SETTING_NAME = "multipartFormDataParser";

	public static final String MULTIPART_FORM_DATA = "multipart/form-data";

	public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
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
	protected void adapt() throws AdapterException {
		try {
			RequestAdapter requestAdapter = new HttpServletRequestAdapter(request);
			requestAdapter.setCharacterEncoding(determineRequestCharacterEncoding());
			setRequestAdapter(requestAdapter);

			String localeResolverId = getRequestSetting(RequestRule.LOCALE_RESOLVER_SETTING_NAME);
			String localeChangeInterceptorId = getRequestSetting(RequestRule.LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME);
			LocaleResolver localeResolver = null;
			if(localeResolverId != null) {
				localeResolver = getBean(localeResolverId, LocaleResolver.class);
				if(localeChangeInterceptorId != null) {
					localeResolver.determineLocale(getTranslet());
					localeResolver.determineTimeZone(getTranslet());
				}
			}
			if(localeChangeInterceptorId != null) {
				LocaleChangeInterceptor localeChangeInterceptor = getBean(localeChangeInterceptorId, LocaleChangeInterceptor.class);
				localeChangeInterceptor.handle(getTranslet(), localeResolver);
			}

			String contentEncoding = getResponseSetting(ResponseRule.CONTENT_ENCODING_SETTING_NAME);
			String acceptEncoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
			if(contentEncoding != null && acceptEncoding != null && acceptEncoding.contains(contentEncoding)) {
				ResponseAdapter responseAdapter = new GZipHttpServletResponseAdapter(response, this);
				setResponseAdapter(responseAdapter);
			} else {
				ResponseAdapter responseAdapter = new HttpServletResponseAdapter(response, this);
				setResponseAdapter(responseAdapter);
			}
		} catch(Exception e) {
			throw new AdapterException("Failed to adapt Web Activity.", e);
		}
	}

	@Override
	public synchronized SessionAdapter getSessionAdapter() {
		if(super.getSessionAdapter() == null) {
			SessionAdapter sessionAdapter = new HttpSessionAdapter(request, getActivityContext());
			super.setSessionAdapter(sessionAdapter);
		}
		return super.getSessionAdapter();
	}

	@Override
	protected void request() {
		String contentType = request.getContentType();

		MethodType requestMethod = getRequestAdapter().getRequestMethod();
		MethodType allowedMethod = getRequestRule().getAllowedMethod();
		if(allowedMethod != null && !allowedMethod.equals(requestMethod)) {
			throw new RequestMethodNotAllowedException(allowedMethod);
		}

		if(contentType != null) {
			if(MethodType.POST.equals(requestMethod) && contentType.startsWith(MULTIPART_FORM_DATA)) {
				parseMultipartFormData();
			} else if((MethodType.PUT.equals(requestMethod) ||
					MethodType.PATCH.equals(requestMethod) ||
					MethodType.DELETE.equals(requestMethod)) &&
					contentType.startsWith(APPLICATION_FORM_URLENCODED)) {
				parseHttpPutFormContent();
			}
		}
		
        parseDeclaredAttributes();
	}
	
	/**
	 * Parse the multipart form data.
	 */
	private void parseMultipartFormData() {
		String multipartFormDataParser = getRequestSetting(MULTIPART_FORM_DATA_PARSER_SETTING_NAME);
		if(multipartFormDataParser == null) {
			throw new MultipartRequestException("The settings name 'multipartFormDataParser' has not been specified in the default request rule.");
		}

		MultipartFormDataParser parser = getBean(multipartFormDataParser);
		if(parser == null) {
			throw new MultipartRequestException("No bean named '" + multipartFormDataParser + "' is defined.");
		}

		parser.parse(getRequestAdapter());
	}

	/**
	 * Parse the HTTP PUT requests.
	 */
	private void parseHttpPutFormContent() {
		HttpPutFormContentParser.parse(getRequestAdapter());
	}
	
	/**
	 * Parse the declared attributes.
	 */
	private void parseDeclaredAttributes() {
		ItemRuleMap attributeItemRuleMap = getRequestRule().getAttributeItemRuleMap();
		if(attributeItemRuleMap != null) {
			ItemEvaluator evaluator = new ItemExpressionParser(this);
			Map<String, Object> valueMap = evaluator.evaluate(attributeItemRuleMap);
			for(ItemRule itemRule : attributeItemRuleMap.values()) {
				String name = itemRule.getName();
				Object value = valueMap.get(name);
				if(value != null) {
					getRequestAdapter().setAttribute(name, value);
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		WebActivity webActivity = new WebActivity(getActivityContext(), request, response);
		return (T)webActivity;
	}

}
