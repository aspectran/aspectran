/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.web.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.transform.JsonTransformResponse;
import com.aspectran.core.activity.response.transform.apon.ContentsAponConverter;
import com.aspectran.core.activity.response.transform.json.ContentsJsonWriter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.util.apon.AponConverter;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.json.JsonWriter;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>Created: 2019-06-16</p>
 */
public abstract class AbstractRestResponse<T> {

    private static final String APPLICATION_JSON = "application/json";

    private static final String APPLICATION_APON = "application/apon";

    private int status;

    private String location;

    private boolean prettyPrint = true;

    public AbstractRestResponse() {
    }

    @SuppressWarnings("unchecked")
    public T ok() {
        this.status = HttpStatus.OK.value();
        return (T)this;
    }

    public T created() {
        return created(null);
    }

    @SuppressWarnings("unchecked")
    public T created(String location) {
        this.status = HttpStatus.CREATED.value();
        this.location = location;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T accepted() {
        this.status = HttpStatus.ACCEPTED.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T noContent() {
        this.status = HttpStatus.NO_CONTENT.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T movedPermanently() {
        this.status = HttpStatus.MOVED_PERMANENTLY.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T seeOther() {
        this.status = HttpStatus.SEE_OTHER.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T notModified() {
        this.status = HttpStatus.NOT_MODIFIED.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T temporaryRedirect() {
        this.status = HttpStatus.TEMPORARY_REDIRECT.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T badRequest() {
        this.status = HttpStatus.BAD_REQUEST.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T unauthorized() {
        this.status = HttpStatus.UNAUTHORIZED.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T forbidden() {
        this.status = HttpStatus.FORBIDDEN.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T notFound() {
        this.status = HttpStatus.NOT_FOUND.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T methodNotAllowed() {
        this.status = HttpStatus.METHOD_NOT_ALLOWED.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T notAcceptable() {
        this.status = HttpStatus.NOT_ACCEPTABLE.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T conflict() {
        this.status = HttpStatus.CONFLICT.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T preconditionFailed() {
        this.status = HttpStatus.PRECONDITION_FAILED.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T unsupportedMediaType() {
        this.status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T internalServerError() {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T prettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return (T)this;
    }

    protected int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    protected String getLocation() {
        return location;
    }

    protected void toJSON(Activity activity, Object data) throws IOException {
        RequestAdapter requestAdapter = activity.getRequestAdapter();
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        Writer writer = responseAdapter.getWriter();

        // support for jsonp
        String callback = requestAdapter.getParameter(JsonTransformResponse.CALLBACK_PARAM_NAME);
        if (callback != null) {
            writer.write(callback + JsonTransformResponse.ROUND_BRACKET_OPEN);
        }
        if (data != null) {
            JsonWriter jsonWriter;
            if (prettyPrint) {
                String indentString = activity.getSetting("indentString");
                jsonWriter = new JsonWriter(writer, indentString);
            } else {
                jsonWriter = new JsonWriter(writer, false);
            }
            jsonWriter.write(data);
        } else {
            JsonWriter jsonWriter;
            if (prettyPrint) {
                String indentString = activity.getSetting("indentString");
                jsonWriter = new ContentsJsonWriter(writer, indentString);
            } else {
                jsonWriter = new ContentsJsonWriter(writer, false);
            }
            jsonWriter.write(activity.getProcessResult());
        }
        if (callback != null) {
            writer.write(JsonTransformResponse.ROUND_BRACKET_CLOSE);
        }
    }

    protected void toAPON(Activity activity, Object data) throws IOException {
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        Writer writer = responseAdapter.getWriter();

        AponWriter aponWriter = new AponWriter(writer);
        if (data != null) {
            Parameters parameters = AponConverter.from(data);
            if (prettyPrint) {
                String indentString = activity.getSetting("indentString");
                if (indentString != null) {
                    aponWriter.setIndentString(indentString);
                }
            } else {
                aponWriter.setIndentString(null);
            }
            aponWriter.write(parameters);
        } else {
            Parameters parameters = ContentsAponConverter.from(activity.getProcessResult());
            if (prettyPrint) {
                String indentString = activity.getSetting("indentString");
                if (indentString != null) {
                    aponWriter.setIndentString(indentString);
                }
            } else {
                aponWriter.setIndentString(null);
            }
            aponWriter.write(parameters);
        }
    }

    protected static String determineAcceptContentType(ResponseAdapter responseAdapter) {
        String acceptType = getAcceptType(responseAdapter);
        if (acceptType == null) {
            return getContentType(responseAdapter);
        } else {
            return null;
        }
    }

    protected static String getAcceptType(ResponseAdapter responseAdapter) {
        // String acceptType = responseAdapter.getHeader(HttpHeaders.ACCEPT);
        // TODO
        return null;
    }

    protected static String getContentType(ResponseAdapter responseAdapter) {
        String contentType = responseAdapter.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType.startsWith(APPLICATION_JSON)) {
            return APPLICATION_JSON;
        } else if (contentType.startsWith(APPLICATION_APON)) {
            return APPLICATION_APON;
        } else {
            return null;
        }
    }

}
