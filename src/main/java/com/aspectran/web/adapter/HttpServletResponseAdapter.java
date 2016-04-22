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
package com.aspectran.web.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.context.expr.ItemExpression;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.RedirectResponseRule;

/**
 * The Class HttpServletResponseAdapter.
 * 
 * @since 2011. 3. 13.
 * @author Juho Jeong
 */
public class HttpServletResponseAdapter extends AbstractResponseAdapter {

	private static final char QUESTION_CHAR = '?';

	private static final char AMPERSAND_CHAR = '&';

	private static final char EQUAL_CHAR = '=';

	private final Activity activity;

	/**
	 * Instantiates a new HttpServletResponseAdapter.
	 *
	 * @param response the HTTP response
	 * @param activity the activity
	 */
	public HttpServletResponseAdapter(HttpServletResponse response, Activity activity) {
		super(response);
		this.activity = activity;
	}

	@Override
	public String getCharacterEncoding() {
		return ((HttpServletResponse)adaptee).getCharacterEncoding();
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
		((HttpServletResponse)adaptee).setCharacterEncoding(characterEncoding);
	}

	@Override
	public String getContentType() {
		return ((HttpServletResponse)adaptee).getContentType();
	}

	@Override
	public void setContentType(String contentType) {
		((HttpServletResponse)adaptee).setContentType(contentType);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return ((HttpServletResponse)adaptee).getOutputStream();
	}

	@Override
	public Writer getWriter() throws IOException {
		return ((HttpServletResponse)adaptee).getWriter();
	}

	@Override
	public void redirect(String target) throws IOException {
		((HttpServletResponse)adaptee).sendRedirect(target);
	}

	@Override
	public String redirect(RedirectResponseRule redirectResponseRule) throws IOException {
		String characterEncoding = ((HttpServletResponse)adaptee).getCharacterEncoding();
		String target = redirectResponseRule.getTarget(activity);
		int questionPos = -1;

		StringBuilder sb = new StringBuilder(256);

		if(target != null) {
			sb.append(target);
			questionPos = target.indexOf(QUESTION_CHAR);
		}
		
		if(redirectResponseRule.getParameterItemRuleMap() != null) {
			ItemEvaluator evaluator = new ItemExpression(activity);
			Map<String, Object> valueMap = evaluator.evaluate(redirectResponseRule.getParameterItemRuleMap());

			if(valueMap != null && valueMap.size() > 0) {
				if(questionPos == -1)
					sb.append(QUESTION_CHAR);

				String name = null;
				Object value;
				
				for(Map.Entry<String, Object> entry : valueMap.entrySet()) {
					if(name != null)
						sb.append(AMPERSAND_CHAR);

					name = entry.getKey();
					value = entry.getValue();

					if(!redirectResponseRule.isExcludeNullParameter() || value != null) {
						sb.append(name).append(EQUAL_CHAR);

						if(value != null) {
							value = URLEncoder.encode(value.toString(), characterEncoding);
							sb.append(value.toString());
						}
					}
				}
			}
		}

		target = sb.toString();
		redirect(target);
		
		return target;
	}

}
