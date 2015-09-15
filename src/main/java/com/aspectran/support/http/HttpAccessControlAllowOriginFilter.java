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
package com.aspectran.support.http;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;

public class HttpAccessControlAllowOriginFilter {
	
	private boolean withCredentials;
	
	private Set<String> origins;

	public void setWithCredentials(boolean withCredentials) {
		this.withCredentials = withCredentials;
	}

	public void setOrigins(Set<String> origins) {
		this.origins = origins;
	}

	public void checkAccessControlAllowCredentials(Translet translet) throws IOException {
		if(withCredentials) {
			RequestAdapter requestAdapter = translet.getRequestAdapter();
			HttpServletRequest req = requestAdapter.getAdaptee();
	
			String origin = req.getHeader("origin");
			boolean allowed = origins.contains(origin);
			
			if(allowed) {
				ResponseAdapter responseAdapter = translet.getResponseAdapter();
				HttpServletResponse res = responseAdapter.getAdaptee();
				res.setHeader("Access-Control-Allow-Credentials", "true");
			    res.addHeader("Access-Control-Allow-Origin", origin);
			}
		}		
	}
}
