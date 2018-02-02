/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.web.support.http;

import com.aspectran.core.activity.Translet;

/**
 * HTTP Status Codes for REST.
 */
public class HttpStatusSetter {

    /**
     * Sets the status code.
     *
     * @param httpStatus the http status code
     * @param translet the Translet
     */
    public static void setStatus(HttpStatus httpStatus, Translet translet) {
        translet.getResponseAdapter().setStatus(httpStatus.value());
    }

    /**
     * Sets the status code.
     *
     * @param statusCode the http status code
     * @param translet the Translet
     */
    public static void setStatus(int statusCode, Translet translet) {
        translet.getResponseAdapter().setStatus(statusCode);
    }

    /**
     * {@code 200 OK}.
     * The request has succeeded.
     * The information returned with the response is dependent on
     * the method used in the request, for example:
     * <ul>
     *   <li>GET an entity corresponding to the requested resource
     *     is sent in the response;</li>
     *   <li>HEAD the entity-header fields corresponding to the requested
     *     resource are sent in the response without any message-body;</li>
     *   <li>POST an entity describing or containing the result of the action;</li>
     *   <li>TRACE an entity containing the request message as received
     *     by the end server.</li>
     * </ul>
     *
     * @param translet the Translet
     */
    public static void ok(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.OK.value());
    }

    /**
     * {@code 201 Created}.
     * The request has succeeded and a new resource has been created as a result of it.
     * This is typically the response sent after a PUT request.
     *
     * @param translet the Translet
     */
    public static void created(Translet translet) {
        created(translet, null);
    }

    /**
     * {@code 201 Created}.
     * The request has succeeded and a new resource has been created as a result of it.
     * This is typically the response sent after a PUT request.
     * The newly created resource can be referenced by the URI(s) returned in the entity
     * of the response,with the most specific URI for the resource given by
     * a Location header field.
     *
     * @param translet the Translet
     * @param location a location header set to the given URI
     */
    public static void created(Translet translet, String location) {
        translet.getResponseAdapter().setStatus(HttpStatus.CREATED.value());
        if (location != null) {
            translet.getResponseAdapter().setHeader(HttpHeaders.LOCATION, location);
        }
    }

    /**
     * {@code 202 Accepted}.
     * The request has been received but not yet acted upon.
     * It is non-committal, meaning that there is no way in HTTP to later send
     * an asynchronous response indicating the outcome of processing the request.
     * It is intended for cases where another process or server handles the request,
     * or for batch processing.
     *
     * @param translet the Translet
     */
    public static void accepted(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.ACCEPTED.value());
    }

    /**
     * {@code 204 No Content}.
     * The server successfully processed the request and is not returning any content.
     * The 204 response MUST NOT include a message-body, and thus is always terminated
     * by the first empty line after the header fields.
     *
     * @param translet the Translet
     */
    public static void noContent(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.NO_CONTENT.value());
    }

    /**
     * {@code 301 Moved Permanently}.
     * This response code means that URI of requested resource has been changed.
     * Any future references to this resource SHOULD use one of the returned URIs.
     *
     * @param translet the Translet
     */
    public static void movedPermanently(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.MOVED_PERMANENTLY.value());
    }

    /**
     * {@code 303 See Other}.
     * The response to the request can be found under a different URI and SHOULD be retrieved
     * using a GET method on that resource. This method exists primarily to allow the output
     * of a POST-activated script to redirect the user agent to a selected resource.
     * The new URI is not a substitute reference for the originally requested resource.
     * The 303 response MUST NOT be cached, but the response to the second (redirected)
     * request might be cacheable.
     *
     * @param translet the Translet
     */
    public static void seeOther(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.SEE_OTHER.value());
    }

    /**
     * {@code 304 Not Modified}.
     * This is used for caching purposes.
     * It is telling to client that response has not been modified.
     * So, client can continue to use same cached version of response.
     *
     * @param translet the Translet
     */
    public static void notModified(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.NOT_MODIFIED.value());
    }

    /**
     * {@code 307 Temporary Redirect}.
     * The target resource resides temporarily under a different URI and the user agent
     * MUST NOT change the request method if it performs an automatic redirection to that URI.
     *
     * @param translet the Translet
     */
    public static void temporaryRedirect(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
    }

    /**
     * {@code 400 Bad Request}.
     * The server cannot or will not process the request due to something that is perceived
     * to be a client error (e.g., malformed request syntax, invalid request message framing,
     * or deceptive request routing).
     *
     * @param translet the Translet
     */
    public static void badRequest(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * {@code 401 Unauthorized}.
     * The request was a legal request, but the server is refusing to respond to it.
     * For use when authentication is possible but has failed or not yet been provided.
     *
     * @param translet the Translet
     */
    public static void unauthorized(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * {@code 403 Forbidden}.
     * The request was a legal request, but the server is refusing to respond to it.
     *
     * @param translet the Translet
     */
    public static void forbidden(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.FORBIDDEN.value());
    }

    /**
     * {@code 404 Not Found}.
     * The server has not found anything matching the Request-URI.
     * The requested resource could not be found but may be available again
     * in the future. Subsequent requests by the client are permissible.
     *
     * @param translet the Translet
     */
    public static void notFound(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.NOT_FOUND.value());
    }

    /**
     * {@code 405 Method Not Allowed}.
     * The request method is known by the server but has been disabled
     * and cannot be used. The two mandatory methods, GET and HEAD,
     * must never be disabled and should not return this error code.
     *
     * @param translet the Translet
     */
    public static void methodNotAllowed(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    /**
     * {@code 406 Not Acceptable}.
     * The requested resource is capable of generating only content not
     * acceptable according to the Accept headers sent in the request.
     *
     * @param translet the Translet
     */
    public static void notAcceptable(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    }

    /**
     * {@code 409 Conflict}.
     * The request could not be completed because of a conflict.
     *
     * @param translet the Translet
     */
    public static void conflict(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.CONFLICT.value());
    }

    /**
     * {@code 412 Precondition failed}.
     * The server does not meet one of the preconditions that
     * the requester put on the request.
     *
     * @param translet the Translet
     */
    public static void preconditionFailed(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.PRECONDITION_FAILED.value());
    }

    /**
     * {@code 415 Unsupported Media Type}.
     * The server is refusing to service the request because
     * the entity of the request is in a format not supported by
     * the requested resource for the requested method.
     *
     * @param translet the Translet
     */
    public static void unsupportedMediaType(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
    }

    /**
     * {@code 500 Internal Server Error}.
     * The server encountered an unexpected condition which
     * prevented it from fulfilling the request.
     *
     * @param translet the Translet
     */
    public static void internalServerError(Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

}
