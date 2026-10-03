/*
 * Copyright (c) 2008-present The Aspectran Project
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A utility class for setting HTTP status codes and headers on a {@link Translet}.
 */
public abstract class HttpStatusSetter {

    private HttpStatusSetter() {
    }

    /**
     * Sets the HTTP status code on the response.
     * @param httpStatus the HTTP status to set
     * @param translet the current translet
     */
    public static void setStatus(@NonNull HttpStatus httpStatus, @NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(httpStatus.value());
    }

    /**
     * Sets the HTTP status code on the response.
     * @param statusCode the HTTP status code to set
     * @param translet the current translet
     */
    public static void setStatus(int statusCode, @NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(statusCode);
    }

    /**
     * Sets the HTTP status to {@code 200 OK}.
     * <p>The request has succeeded. The information returned with the response
     * is dependent on the method used in the request. For example:
     * <ul>
     *   <li>GET: an entity corresponding to the requested resource is sent in the response.</li>
     *   <li>HEAD: the entity-header fields corresponding to the requested resource are sent
     *     in the response without any message-body.</li>
     *   <li>POST: an entity describing or containing the result of the action.</li>
     *   <li>TRACE: an entity containing the request message as received by the end server.</li>
     * </ul>
     * @param translet the current translet
     */
    public static void ok(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.OK.value());
    }

    /**
     * Sets the HTTP status to {@code 201 Created}.
     * <p>The request has succeeded and a new resource has been created as a result of it.
     * This is typically the response sent after a PUT request.
     * @param translet the current translet
     */
    public static void created(@NonNull Translet translet) {
        created(translet, null);
    }

    /**
     * Sets the HTTP status to {@code 201 Created}.
     * <p>The request has succeeded and a new resource has been created as a result of it.
     * This is typically the response sent after a PUT request.
     * The newly created resource can be referenced by the URI(s) returned in the entity
     * of the response, with the most specific URI for the resource given by
     * a Location header field.
     * @param translet the current translet
     * @param location a location header set to the given URI
     */
    public static void created(@NonNull Translet translet, @Nullable String location) {
        translet.getResponseAdapter().setStatus(HttpStatus.CREATED.value());
        if (location != null) {
            translet.getResponseAdapter().setHeader(HttpHeaders.LOCATION, location);
        }
    }

    /**
     * Sets the HTTP status to {@code 202 Accepted}.
     * <p>The request has been received but not yet acted upon. It is non-committal,
     * meaning that there is no way in HTTP to later send an asynchronous response
     * indicating the outcome of processing the request. It is intended for cases
     * where another process or server handles the request, or for batch processing.
     * @param translet the current translet
     */
    public static void accepted(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.ACCEPTED.value());
    }

    /**
     * Sets the HTTP status to {@code 204 No Content}.
     * <p>The server successfully processed the request and is not returning any content.
     * The 204 response MUST NOT include a message-body, and thus is always terminated
     * by the first empty line after the header fields.
     * @param translet the current translet
     */
    public static void noContent(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.NO_CONTENT.value());
    }

    /**
     * Sets the HTTP status to {@code 301 Moved Permanently}.
     * <p>This response code means that URI of requested resource has been changed.
     * Any future references to this resource SHOULD use one of the returned URIs.
     * @param translet the current translet
     */
    public static void movedPermanently(@NonNull Translet translet) {
        movedPermanently(translet, null);
    }

    /**
     * Sets the HTTP status to {@code 301 Moved Permanently}.
     * <p>This response code means that URI of requested resource has been changed.
     * Any future references to this resource SHOULD use one of the returned URIs.
     * @param translet the current translet
     * @param location a location header set to the given URI
     */
    public static void movedPermanently(@NonNull Translet translet, @Nullable String location) {
        translet.getResponseAdapter().setStatus(HttpStatus.MOVED_PERMANENTLY.value());
        if (location != null) {
            translet.getResponseAdapter().setHeader(HttpHeaders.LOCATION, location);
        }
    }

    /**
     * Sets the HTTP status to {@code 302 Found}.
     * <p>The target resource resides temporarily under a different URI.
     * Since the redirection might be altered on occasion, the client ought to
     * continue to use the effective request URI for future requests.
     * @param translet the current translet
     */
    public static void found(@NonNull Translet translet) {
        found(translet, null);
    }

    /**
     * Sets the HTTP status to {@code 302 Found}.
     * <p>The target resource resides temporarily under a different URI.
     * Since the redirection might be altered on occasion, the client ought to
     * continue to use the effective request URI for future requests.
     * @param translet the current translet
     * @param location a location header set to the given URI
     */
    public static void found(@NonNull Translet translet, @Nullable String location) {
        translet.getResponseAdapter().setStatus(HttpStatus.FOUND.value());
        if (location != null) {
            translet.getResponseAdapter().setHeader(HttpHeaders.LOCATION, location);
        }
    }

    /**
     * Sets the HTTP status to {@code 303 See Other}.
     * <p>The response to the request can be found under a different URI and SHOULD be retrieved
     * using a GET method on that resource. This method exists primarily to allow the output
     * of a POST-activated script to redirect the user agent to a selected resource.
     * The new URI is not a substitute reference for the originally requested resource.
     * The 303 response MUST NOT be cached, but the response to the second (redirected)
     * request might be cacheable.
     * @param translet the current translet
     */
    public static void seeOther(@NonNull Translet translet) {
        seeOther(translet, null);
    }

    /**
     * Sets the HTTP status to {@code 303 See Other}.
     * <p>The response to the request can be found under a different URI and SHOULD be retrieved
     * using a GET method on that resource.
     * @param translet the current translet
     * @param location a location header set to the given URI
     */
    public static void seeOther(@NonNull Translet translet, @Nullable String location) {
        translet.getResponseAdapter().setStatus(HttpStatus.SEE_OTHER.value());
        if (location != null) {
            translet.getResponseAdapter().setHeader(HttpHeaders.LOCATION, location);
        }
    }

    /**
     * Sets the HTTP status to {@code 304 Not Modified}.
     * <p>This is used for caching purposes. It is telling to client that response has not been modified.
     * So, client can continue to use same cached version of response.
     * @param translet the current translet
     */
    public static void notModified(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.NOT_MODIFIED.value());
    }

    /**
     * Sets the HTTP status to {@code 307 Temporary Redirect}.
     * <p>The target resource resides temporarily under a different URI and the user agent
     * MUST NOT change the request method if it performs an automatic redirection to that URI.
     * @param translet the current translet
     */
    public static void temporaryRedirect(@NonNull Translet translet) {
        temporaryRedirect(translet, null);
    }

    /**
     * Sets the HTTP status to {@code 307 Temporary Redirect}.
     * <p>The target resource resides temporarily under a different URI and the user agent
     * MUST NOT change the request method if it performs an automatic redirection to that URI.
     * @param translet the current translet
     * @param location a location header set to the given URI
     */
    public static void temporaryRedirect(@NonNull Translet translet, @Nullable String location) {
        translet.getResponseAdapter().setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
        if (location != null) {
            translet.getResponseAdapter().setHeader(HttpHeaders.LOCATION, location);
        }
    }

    /**
     * Sets the HTTP status to {@code 308 Permanent Redirect}.
     * <p>The target resource has been assigned a new permanent URI and any future references
     * to this resource should use one of the returned URIs.
     * @param translet the current translet
     */
    public static void permanentRedirect(@NonNull Translet translet) {
        permanentRedirect(translet, null);
    }

    /**
     * Sets the HTTP status to {@code 308 Permanent Redirect}.
     * <p>The target resource has been assigned a new permanent URI and any future references
     * to this resource should use one of the returned URIs.
     * @param translet the current translet
     * @param location a location header set to the given URI
     */
    public static void permanentRedirect(@NonNull Translet translet, @Nullable String location) {
        translet.getResponseAdapter().setStatus(HttpStatus.PERMANENT_REDIRECT.value());
        if (location != null) {
            translet.getResponseAdapter().setHeader(HttpHeaders.LOCATION, location);
        }
    }

    /**
     * Sets the HTTP status to {@code 400 Bad Request}.
     * <p>The server cannot or will not process the request due to something that is perceived
     * to be a client error (e.g., malformed request syntax, invalid request message framing,
     * or deceptive request routing).
     * @param translet the current translet
     */
    public static void badRequest(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Sets the HTTP status to {@code 401 Unauthorized}.
     * <p>The request was a legal request, but the server is refusing to respond to it.
     * For use when authentication is possible but has failed or not yet been provided.
     * @param translet the current translet
     */
    public static void unauthorized(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Sets the HTTP status to {@code 403 Forbidden}.
     * <p>The request was a legal request, but the server is refusing to respond to it.
     * @param translet the current translet
     */
    public static void forbidden(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.FORBIDDEN.value());
    }

    /**
     * Sets the HTTP status to {@code 404 Not Found}.
     * <p>The server has not found anything matching the Request-URI.
     * The requested resource could not be found but may be available again
     * in the future. Subsequent requests by the client are permissible.
     * @param translet the current translet
     */
    public static void notFound(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.NOT_FOUND.value());
    }

    /**
     * Sets the HTTP status to {@code 405 Method Not Allowed}.
     * <p>The request method is known by the server but has been disabled
     * and cannot be used. The two mandatory methods, GET and HEAD,
     * must never be disabled and should not return this error code.
     * @param translet the current translet
     */
    public static void methodNotAllowed(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    /**
     * Sets the HTTP status to {@code 406 Not Acceptable}.
     * <p>The requested resource is capable of generating only content not
     * acceptable according to the Accept headers sent in the request.
     * @param translet the current translet
     */
    public static void notAcceptable(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    }

    /**
     * Sets the HTTP status to {@code 409 Conflict}.
     * <p>The request could not be completed because of a conflict.
     * @param translet the current translet
     */
    public static void conflict(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.CONFLICT.value());
    }

    /**
     * Sets the HTTP status to {@code 412 Precondition failed}.
     * <p>The server does not meet one of the preconditions that
     * the requester put on the request.
     * @param translet the current translet
     */
    public static void preconditionFailed(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.PRECONDITION_FAILED.value());
    }

    /**
     * Sets the HTTP status to {@code 415 Unsupported Media Type}.
     * <p>The server is refusing to service the request because
     * the entity of the request is in a format not supported by
     * the requested resource for the requested method.
     * @param translet the current translet
     */
    public static void unsupportedMediaType(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
    }

    /**
     * Sets the HTTP status to {@code 422 Unprocessable Entity}.
     * <p>The server understands the content type of the request entity,
     * and the syntax of the request entity is correct, but it was unable
     * to process the contained instructions.
     * @param translet the current translet
     */
    public static void unprocessableEntity(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    /**
     * Sets the HTTP status to {@code 429 Too Many Requests}.
     * <p>The user has sent too many requests in a given amount of time ("rate limiting").
     * @param translet the current translet
     */
    public static void tooManyRequests(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    /**
     * Sets the HTTP status to {@code 500 Internal Server Error}.
     * <p>The server encountered an unexpected condition which
     * prevented it from fulfilling the request.
     * @param translet the current translet
     */
    public static void internalServerError(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * Sets the HTTP status to {@code 502 Bad Gateway}.
     * <p>The server, while acting as a gateway or proxy, received an invalid
     * response from the inbound server it accessed while attempting to fulfill
     * the request.
     * @param translet the current translet
     */
    public static void badGateway(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.BAD_GATEWAY.value());
    }

    /**
     * Sets the HTTP status to {@code 503 Service Unavailable}.
     * <p>The server is currently unable to handle the request due to a temporary
     * overloading or maintenance of the server.
     * @param translet the current translet
     */
    public static void serviceUnavailable(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
    }

    /**
     * Sets the HTTP status to {@code 504 Gateway Timeout}.
     * <p>The server, while acting as a gateway or proxy, did not receive a timely
     * response from the upstream server specified by the URI.
     * @param translet the current translet
     */
    public static void gatewayTimeout(@NonNull Translet translet) {
        translet.getResponseAdapter().setStatus(HttpStatus.GATEWAY_TIMEOUT.value());
    }

}
