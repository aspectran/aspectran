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
package com.aspectran.web.activity.request.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.util.StringUtils;

/**
 * Support for HTTP request methods like PUT/PATCH.
 *
 * <p>The Servlet spec requires form data to be available for HTTP POST but
 * not for HTTP PUT or PATCH requests. This parser intercepts HTTP PUT and PATCH
 * requests where content type is {@code 'application/x-www-form-urlencoded'},
 * reads form encoded content from the body of the request.
 *
 * @since 2.3.0
 */
public class HttpPutFormContentParser {

	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private static final int BUFFER_SIZE = 4096;

	public static void parse(RequestAdapter requestAdapter) {
		try {
			HttpServletRequest request = requestAdapter.getAdaptee();
			String requestEncoding = requestAdapter.getCharacterEncoding();
			Charset charset = (requestEncoding != null) ? Charset.forName(requestEncoding) : DEFAULT_CHARSET;
			String body = copyToString(request.getInputStream(), charset);

			String[] pairs = StringUtils.tokenize(body, "&");
			Map<String, List<String>> parameterListMap = new HashMap<String, List<String>>();

			for(String pair : pairs) {
				int idx = pair.indexOf('=');
				if(idx == -1) {
					String name = URLDecoder.decode(pair, charset.name());
					putParameter(name, null, parameterListMap);
				} else {
					String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
					String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
					putParameter(name, value, parameterListMap);
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
		} catch(Exception e) {
			throw new RequestParsingException("Could not parse multipart servlet request.", e);
		}
	}

	private static String copyToString(InputStream in, Charset charset) throws IOException {
		StringBuilder out = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(in, charset);
		char[] buffer = new char[BUFFER_SIZE];
		int bytesRead = -1;
		while((bytesRead = reader.read(buffer)) != -1) {
			out.append(buffer, 0, bytesRead);
		}
		return out.toString();
	}

	private static void putParameter(String name, String value, Map<String, List<String>> parameterListMap) {
		List<String> list = parameterListMap.get(name);
		if(list == null) {
			list = new LinkedList<String>();
			parameterListMap.put(name, list);
		}
		list.add(value);
	}

}
