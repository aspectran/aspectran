package com.aspectran.web.support.multipart.commons;

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.web.support.http.HttpHeaders;
import org.apache.commons.fileupload.RequestContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * RequestContext needed by Jakarta Commons Upload.
 *
 * <p>Created: 2019-07-31</p>
 *
 * @since 6.3.0
 */
public class CommonsRequestContext implements RequestContext {

    private final RequestAdapter requestAdapter;

    public CommonsRequestContext(RequestAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

    @Override
    public String getCharacterEncoding() {
        return requestAdapter.getEncoding();
    }

    @Override
    public String getContentType() {
        return requestAdapter.getHeader(HttpHeaders.CONTENT_TYPE);
    }

    @Override
    @Deprecated
    public int getContentLength() {
        String contentLength = requestAdapter.getHeader(HttpHeaders.CONTENT_LENGTH);
        return (contentLength != null && !contentLength.isEmpty() ? Integer.parseInt(contentLength) : -1);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return requestAdapter.getInputStream();
    }

}
