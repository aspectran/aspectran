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
package com.aspectran.web.support.cors;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.util.StringUtils;

/**
 * Handles incoming cross-origin (CORS) requests.
 * Encapsulates the CORS processing logic as specified by the
 * <a href="http://www.w3.org/TR/2013/CR-cors-20130129/">W3C candidate
 * recommendation</a> from 2013-01-29.
 *
 * @author Juho Jeong
 * @since 2016.07.07.
 */
public class CorsRequestHandler {
	
	/**
	 * "Origin" header name.
	 */
	private static final String ORIGIN = "Origin";

	/**
	 * "Access-Control-Request-Method" header name.
	 */
	private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

	/**
	 * "Access-Control-Request-Headers" header name.
	 */
	private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

	/**
	 * "Access-Control-Allow-Origin" header name.
	 */
	private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	/**
	 * "Access-Control-Allow-Methods" header name.
	 */
	private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	/**
	 * "Access-Control-Allow-Headers" header name.
	 */
	private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

	/**
	 * "Access-Control-Allow-Credentials" header name.
	 */
	private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

	/**
	 * "Access-Control-Expose-Headers" header name.
	 */
	private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

	/**
	 * "Access-Control-Max-Age" header name.
	 */
	private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

	/**
	 * "Vary" header name.
	 */
	private static final String VARY = "Vary";

	/**
	 * ""CORS.HTTP_STATUS_CODE"" attribute name.
	 */
	private static final String CORS_HTTP_STATUS_CODE = "CORS.HTTP_STATUS_CODE";

	/**
	 * "CORS.MESSAGE" attribute name.
	 */
	private static final String CORS_MESSAGE = "CORS.MESSAGE";

	/**
	 * Origins that the CORS filter must allow. Requests from origins not
	 * included here must be refused with a HTTP 403 "Forbidden" response.
	 */
	private Set<String> allowedOrigins;

	/**
	 * The supported HTTP methods. Requests for methods not included here
	 * must be refused by the CORS filter with a HTTP 405 "Method not
	 * allowed" response.
	 */
	private Set<String> allowedMethods;

	/**
	 * Pre-computed string of the CORS supported methods.
	 */
	private String allowedMethodsString;

	/**
	 * The names of the supported author request headers.
	 */
	private Set<String> allowedHeaders;

	/**
	 * Pre-computed string of the CORS supported headers.
	 */
	private String allowedHeadersString;

	/**
	 * The non-simple response headers that the web browser should expose
	 * to the author of the CORS request.
	 */
	private Set<String> exposedHeaders;

	/**
	 * Pre-computed string of the CORS exposed headers.
	 */
	private String exposedHeadersString;

	/**
	 * Indicates whether user credentials, such as cookies, HTTP
	 * authentication or client-side certificates, are supported.
	 */
	private boolean allowCredentials;

	/**
	 * Indicates how long the results of a preflight request can be cached
	 * by the web client, in seconds. If {@code -1} unspecified.
	 */
	private int maxAgeSeconds = -1;

	public void setAllowedOrigins(Set<String> allowedOrigins) {
		if(allowedOrigins != null && !allowedOrigins.isEmpty()) {
			boolean allowAnyOrigin = allowedOrigins.contains("*");
			this.allowedOrigins = allowAnyOrigin ? null : allowedOrigins;
		} else {
			this.allowedOrigins = null;
		}
	}

	public String[] getAllowedOrigins() {
		if(allowedOrigins == null)
			return null;
		
		return allowedOrigins.toArray(new String[allowedOrigins.size()]);
	}
	
	public void setAllowedOrigins(String[] allowedOrigins) {
		Set<String> set = new HashSet<>();
		if(allowedOrigins != null) {
			for(String origin : allowedOrigins) {
				set.add(origin);
			}
		}
		setAllowedOrigins(set);
	}

	public void setAllowedOrigins(String allowedOrigins) {
		String[] origins = StringUtils.splitCommaDelimitedString(allowedOrigins);
		setAllowedOrigins(origins);
	}

	public void setAllowedMethods(Set<String> allowedMethods) {
		if(allowedMethods != null && !allowedMethods.isEmpty()) {
			boolean allowAnyMethod = allowedMethods.contains("*");
			if(allowAnyMethod) {
				this.allowedMethods = null;
				this.allowedMethodsString = null;
			} else {
				this.allowedMethods = allowedMethods;
				this.allowedMethodsString = StringUtils.joinCommaDelimitedList(allowedMethods);
			}
		} else {
			this.allowedMethods = null;
			this.allowedMethodsString = null;
		}
	}

	public String[] getAllowedMethods() {
		if(allowedMethods == null)
			return null;
		
		return allowedMethods.toArray(new String[allowedMethods.size()]);
	}
	
	public void setAllowedMethods(String[] allowedMethods) {
		Set<String> set = new HashSet<>();
		if(allowedMethods != null) {
			for(String method : allowedMethods) {
				set.add(method);
			}
		}
		setAllowedMethods(set);
	}

	public void setAllowedMethods(String allowedMethods) {
		String[] methods = StringUtils.splitCommaDelimitedString(allowedMethods);
		setAllowedMethods(methods);
	}

	public void setAllowedHeaders(Set<String> allowedHeaders) {
		if(allowedHeaders != null && !allowedHeaders.isEmpty()) {
			boolean allowAnyHeader = allowedHeaders.contains("*");
			if(allowAnyHeader) {
				this.allowedHeaders = null;
				this.allowedHeadersString = null;
			} else {
				this.allowedHeaders = allowedHeaders;
				this.allowedHeadersString = StringUtils.joinCommaDelimitedList(allowedHeaders);
			}
		} else {
			this.allowedHeaders = null;
			this.allowedHeadersString = null;
		}
	}

	public String[] getAllowedHeaders() {
		if(allowedHeaders == null)
			return null;
		
		return allowedHeaders.toArray(new String[allowedHeaders.size()]);
	}
	
	public void setAllowedHeaders(String[] allowedHeaders) {
		Set<String> set = new HashSet<>();
		if(allowedHeaders != null) {
			for(String header : allowedHeaders) {
				set.add(header);
			}
		}
		setAllowedHeaders(set);
	}

	public void setAllowedHeaders(String allowedHeaders) {
		String[] headers = StringUtils.splitCommaDelimitedString(allowedHeaders);
		setAllowedHeaders(headers);
	}

	public void setExposedHeaders(Set<String> exposedHeaders) {
		if(exposedHeaders != null && !exposedHeaders.isEmpty()) {
			boolean allowAnyHeader = exposedHeaders.contains("*");
			if(allowAnyHeader) {
				this.exposedHeaders = null;
				this.exposedHeadersString = null;
			} else {
				this.exposedHeaders = exposedHeaders;
				this.exposedHeadersString = StringUtils.joinCommaDelimitedList(exposedHeaders);
			}
		} else {
			this.exposedHeaders = null;
			this.exposedHeadersString = null;
		}
	}

	public String[] getExposedHeaders() {
		if(exposedHeaders == null)
			return null;
		
		return exposedHeaders.toArray(new String[exposedHeaders.size()]);
	}

	public void setExposedHeaders(String[] exposedHeaders) {
		Set<String> set = new HashSet<>();
		if(exposedHeaders != null) {
			for(String header : exposedHeaders) {
				set.add(header);
			}
		}
		setExposedHeaders(set);
	}

	public void setExposedHeaders(String exposedHeaders) {
		String[] headers = StringUtils.splitCommaDelimitedString(exposedHeaders);
		setExposedHeaders(headers);
	}

	public boolean getAllowCredentials() {
		return allowCredentials;
	}
	
	public void setAllowCredentials(boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
	}
	
	public int getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	public void setMaxAgeSeconds(int maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}

	/**
	 * Handles a simple or actual CORS request.
	 *
	 * @param translet the translet
	 * @throws CorsException if the request is invalid or denied.
	 */
	public void handleActualRequest(Translet translet) throws CorsException {
		HttpServletRequest req = translet.getRequestAdaptee();
		HttpServletResponse res = translet.getResponseAdaptee();
		
		if(!isAllowedMethod(req.getMethod())) {
			translet.setAttribute(CORS_HTTP_STATUS_CODE, CorsException.UNSUPPORTED_METHOD.getHttpStatusCode());
			translet.setAttribute(CORS_MESSAGE, CorsException.UNSUPPORTED_METHOD.getMessage());
			throw CorsException.UNSUPPORTED_METHOD;
		}

		String origin = req.getHeader(ORIGIN);

		if(!isAllowedOrigin(origin)) {
			translet.setAttribute(CORS_HTTP_STATUS_CODE, CorsException.ORIGIN_DENIED.getHttpStatusCode());
			translet.setAttribute(CORS_MESSAGE, CorsException.ORIGIN_DENIED.getMessage());
			throw CorsException.ORIGIN_DENIED;
		}

		if(allowCredentials) {
			// Must be exact origin (not '*') in case of credentials
			res.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
			res.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			res.addHeader(VARY, "Origin");
		} else {
			res.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigins == null ? "*" : origin);
			res.addHeader(VARY, "Origin");
		}

		if(exposedHeadersString != null) {
			res.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeadersString);
		}
	}

	/**
	 * Handles a preflight CORS request.
	 *
	 * <p>CORS specification:
	 * <a href="http://www.w3.org/TR/2013/CR-cors-20130129/#resource-preflight-requests">PreflightRequest</a>
	 *
	 * @param translet the translet
	 * @throws CorsException if the request is invalid or denied.
	 */
	public void handlePreflightRequest(Translet translet) throws CorsException, IOException {
		HttpServletRequest req = translet.getRequestAdaptee();
		HttpServletResponse res = translet.getResponseAdaptee();

		if(!"OPTIONS".equals(req.getMethod())) {
			translet.setAttribute(CORS_HTTP_STATUS_CODE, CorsException.INVALID_PREFLIGHT_REQUEST.getHttpStatusCode());
			translet.setAttribute(CORS_MESSAGE, CorsException.INVALID_PREFLIGHT_REQUEST.getMessage());
			throw CorsException.INVALID_PREFLIGHT_REQUEST;
		}

		String requestedMethod = req.getHeader(ACCESS_CONTROL_REQUEST_METHOD);
		if(requestedMethod != null) {
			if(!isAllowedMethod(requestedMethod)) {
				translet.setAttribute(CORS_HTTP_STATUS_CODE, CorsException.UNSUPPORTED_METHOD.getHttpStatusCode());
				translet.setAttribute(CORS_MESSAGE, CorsException.UNSUPPORTED_METHOD.getMessage());
				throw CorsException.UNSUPPORTED_METHOD;
			}
		}
		
		String rawRequestHeadersString = req.getHeader(ACCESS_CONTROL_REQUEST_HEADERS);
		if(rawRequestHeadersString != null) {
			String[] requestHeaders = StringUtils.splitCommaDelimitedString(rawRequestHeadersString);
			if(allowedHeaders != null && requestHeaders.length > 0) {
				for(String requestHeader : requestHeaders) {
					if(!allowedHeaders.contains(requestHeader)) {
						translet.setAttribute(CORS_HTTP_STATUS_CODE, CorsException.UNSUPPORTED_REQUEST_HEADER.getHttpStatusCode());
						translet.setAttribute(CORS_MESSAGE, CorsException.UNSUPPORTED_REQUEST_HEADER.getMessage());
						throw CorsException.UNSUPPORTED_REQUEST_HEADER;
					}
				}
			}
		}
		
		String origin = req.getHeader(ORIGIN);
		if(origin != null) {
			if(allowCredentials) {
				// Must be exact origin (not '*') in case of credentials
				res.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
				res.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
				res.addHeader(VARY, "Origin");
			} else {
				res.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigins == null ? "*" : origin);
				res.addHeader(VARY, "Origin");
			}
		}

		if(maxAgeSeconds > 0) {
			res.addHeader(ACCESS_CONTROL_MAX_AGE, Integer.toString(maxAgeSeconds));
		}

		if(allowedMethodsString != null) {
			res.addHeader(ACCESS_CONTROL_ALLOW_METHODS, allowedMethodsString);
		}

		if(allowedHeadersString != null) {
			res.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, allowedHeadersString);
		} else if(rawRequestHeadersString != null) {
			res.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, rawRequestHeadersString);
		}
	}

	public void sendError(Translet translet) throws CorsException, IOException {
		Throwable t = translet.getOriginRaisedException();

		if(t instanceof CorsException) {
			CorsException corsException = (CorsException)t;

			HttpServletResponse res = translet.getResponseAdaptee();
			res.sendError(corsException.getHttpStatusCode(), corsException.getMessage());
		}
	}

	/**
	 * Helper method to check whether requests from the specified origin must be allowed.
	 *
	 * @param origin The origin as reported by the web client (browser), {@code null} if unknown.
	 * @return {@code true} if the origin is allowed, else {@code false}.
	 */
	private boolean isAllowedOrigin(String origin) {
		return (allowedOrigins == null || allowedOrigins.contains(origin));

	}

	/**
	 * Helper method to check whether the specified HTTP method is
	 * supported. This is done by looking up {@link #allowedMethods}.
	 *
	 * @param method The HTTP method.
	 * @return {@code true} if the method is supported, else {@code false}.
	 */
	private boolean isAllowedMethod(String method) {
		return (allowedMethods == null || allowedMethods.contains(method));

	}
	
}
