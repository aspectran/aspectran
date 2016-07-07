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

import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.Translet;

/**
 * HTTP Status Codes for REST.
 * 
 * <pre>
 *  - 200("OK"): 일반적인 요청 성공을 나타내는 데 사용해야 한다. 응답 바디에 에러를 전송하는 데 사용해서는 안 된다.
 *  - 201("Created"): 성공적으로 리소스를 생성했을 때 사용해야 한다.
 *  - 202("Accepted"): 비동기 처리가 성공적으로 시작되었음을 알릴 때 사용해야 한다.
 *  - 204("No Content"): 응답 바디에 의도적으로 아무것도 포함하지 않을 때 사용한다.
 *  - 301("Moved Permanently"): 리소스를 이동시켰을 때 사용한다.
 *  - 303("See Other"): 다른 URI를 참조하라고 알려줄 때 사용한다.
 *  - 304("Not Modified"): 대역폭을 절약할 때 사용한다.
 *  - 307("Temporary Redirect"): 클라이언트가 다른 URI로 요청을 다시 보내게 할 때 사용해야 한다.
 *  - 400("Bad Request"): 일반적인 요청 실패에 사용해야 한다.
 *  - 401("Unauthorized"): 클라이언트 인증에 문제가 있을 때 사용해야 한다.
 *  - 403("Forbidden"): 인증 상태에 상관없이 액세스를 금지할 때 사용해야 한다.
 *  - 404("Not Found"): 요청 URI에 해당하는 리소스가 없을 때 사용해야 한다.
 *  - 405("Method Not Allowed"): HTTP 메서드가 지원되지 않을 때 사용해야 한다.
 *  - 406("Not Acceptable"): 요청된 리소스 미디어 타입을 제공하지 못할 때 사용해야 한다.
 *  - 409("Conflict"): 리소스 상태에 위반되는 행위를 했을 때 사용해야 한다.
 *  - 412("Precondition Failed"): 조건부 연산을 지원할 때 사용한다.
 *  - 415("Unsupported Media Type"): 요청의 페이로드에 있는 미디어 타입이 처리되지 못했을 때 사용해야 한다.
 *  - 500("Internal Server Error"): API가 잘못 작동할 때 사용해야 한다.
 */
public class HttpStatusCodeSetter {

	/**
	 * 200("OK"): 일반적인 요청 성공을 나타내는 데 사용해야 한다. 응답 바디에 에러를 전송하는 데 사용해서는 안 된다.
	 *
	 * @param translet the Translet
	 */
	public static void setOk(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_OK);
	}
	
	/**
	 * 201("Created"): 성공적으로 리소스를 생성했을 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setCreated(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_CREATED);
	}
	
	/**
	 * 202("Accepted"): 비동기 처리가 성공적으로 시작되었음을 알릴 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setAccepted(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_ACCEPTED);
	}
	
	/**
	 * 204("No Content"): 응답 바디에 의도적으로 아무것도 포함하지 않을 때 사용한다.
	 *
	 * @param translet the Translet
	 */
	public static void setNoContent(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}
	
	/**
	 * 301("Moved Permanently"): 리소스를 이동시켰을 때 사용한다.
	 *
	 * @param translet the Translet
	 */
	public static void setMovedPermanently(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
	}
	
	/**
	 * 303("See Other"): 다른 URI를 참조하라고 알려줄 때 사용한다.
	 *
	 * @param translet the Translet
	 */
	public static void setSeeOther(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_SEE_OTHER);
	}
	
	/**
	 * 304("Not Modified"): 대역폭을 절약할 때 사용한다.
	 *
	 * @param translet the Translet
	 */
	public static void setNotModified(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
	}
	
	/**
	 * 307("Temporary Redirect"): 클라이언트가 다른 URI로 요청을 다시 보내게 할 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setTemporaryRedirect(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
	}
	
	/**
	 * 400("Bad Request"): 일반적인 요청 실패에 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setBadRequest(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}
	
	/**
	 * 401("Unauthorized"): 클라이언트 인증에 문제가 있을 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setUnauthorized(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}
	
	/**
	 * 403("Forbidden"): 인증 상태에 상관없이 액세스를 금지할 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setForbidden(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_FORBIDDEN);
	}
	
	/**
	 * 404("Not Found"): 요청 URI에 해당하는 리소스가 없을 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setNotFound(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
	
	/**
	 * 405("Method Not Allowed"): HTTP 메서드가 지원되지 않을 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setMethodNotAllowed(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	/**
	 * 406("Not Acceptable"): 요청된 리소스 미디어 타입을 제공하지 못할 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setNotAcceptable(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
	}
	
	/**
	 * 409("Conflict"): 리소스 상태에 위반되는 행위를 했을 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setConflict(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_CONFLICT);
	}
	
	/**
	 * 412("Precondition Failed"): 조건부 연산을 지원할 때 사용한다.
	 *
	 * @param translet the Translet
	 */
	public static void setPreconditionFailed(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
	}
	
	/**
	 * 415("Unsupported Media Type"): 요청의 페이로드에 있는 미디어 타입이 처리되지 못했을 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setUnsupportedMediaType(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
	}
	
	/**
	 * 500("Internal Server Error"): API가 잘못 작동할 때 사용해야 한다.
	 *
	 * @param translet the Translet
	 */
	public static void setInternalServerError(Translet translet) {
		HttpServletResponse res = translet.getResponseAdaptee();
		res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
	
}
