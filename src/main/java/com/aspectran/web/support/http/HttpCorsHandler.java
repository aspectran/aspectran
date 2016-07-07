/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.web.support.http;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public class HttpCorsHandler {
	
	private static final Log log = LogFactory.getLog(HttpCorsHandler.class);
	
	private Set<String> allowedOrigins;

	private String allowedMethods;
	
	private String allowedHeaders;
	
	private String exposedHeaders;
	
	private boolean allowCredentials;
	
	private String maxAgeSeconds;

	public void setAllowedOrigins(Set<String> allowedOrigins) {
		if(allowedOrigins != null && !allowedOrigins.isEmpty()) {
			this.allowedOrigins = allowedOrigins;
		} else {
			this.allowedOrigins = null;
		}
	}

	public void setAllowedMethods(Set<String> allowedMethods) {
		if(allowedMethods != null && !allowedMethods.isEmpty()) {
			this.allowedMethods = StringUtils.joinCommaDelimitedList(allowedMethods);
		} else {
			this.allowedMethods = null;
		}
	}
	
	public void setAllowedMethods(String allowedMethods) {
		this.allowedMethods = allowedMethods;
	}
	
	public void setAllowedHeaders(Set<String> allowedHeaders) {
		if(allowedHeaders != null && !allowedHeaders.isEmpty()) {
			this.allowedHeaders = StringUtils.joinCommaDelimitedList(allowedHeaders);
		} else {
			this.allowedHeaders = null;
		}
	}

	public void setAllowedHeaders(String allowedHeaders) {
		this.allowedHeaders = allowedHeaders;
	}
	
	public void setExposedHeaders(Set<String> exposedHeaders) {
		if(exposedHeaders != null && !exposedHeaders.isEmpty()) {
			this.exposedHeaders = StringUtils.joinCommaDelimitedList(exposedHeaders);
		} else {
			this.exposedHeaders = null;
		}
	}

	public void setExposedHeaders(String exposedHeaders) {
		this.exposedHeaders = exposedHeaders;
	}
	
	public void setAllowCredentials(boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
	}
	
	public void setMaxAgeSeconds(int maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds == -1 ? null : String.valueOf(maxAgeSeconds);
	}
	
	public void handle(Translet translet) throws IOException {
		HttpServletRequest req = translet.getRequestAdaptee();
		HttpServletResponse res = translet.getResponseAdaptee();

		String origin = req.getHeader("origin");
		
		if(origin != null) { 
			if(isValidOrigin(origin)) {
			    if(allowCredentials) {
			    	// Must be exact origin (not '*') in case of credentials
			    	res.setHeader("Access-Control-Allow-Credentials", "true");
			    	res.addHeader("Access-Control-Allow-Origin", origin);
			    } else {
			    	res.addHeader("Access-Control-Allow-Origin", origin);
			    }
			    
			    if(allowedMethods != null) {
			    	res.setHeader("Access-Control-Allow-Methods", allowedMethods);
			    }
			    
			    if(allowedHeaders != null) {
			    	res.setHeader("Access-Control-Allow-Headers", allowedHeaders);
			    }
			    
			    if(exposedHeaders != null) {
			    	res.setHeader("Access-Control-Expose-Headers", exposedHeaders);
			    }
			    
			    if(maxAgeSeconds != null) {
			    	res.setHeader("Access-Control-Max-Age", maxAgeSeconds);
			    }
			} else {
				res.setStatus(HttpServletResponse.SC_FORBIDDEN);
				log.debug("Cross-Origin Request Blocked: Invalid origin: " + origin);
			}
		}
	}
	
	private boolean isValidOrigin(String origin) {
		if(allowedOrigins == null || allowedOrigins.isEmpty()) {
			return false;
		}
		
		return allowedOrigins.contains(origin);
	}
	
}
