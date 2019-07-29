package com.aspectran.undertow.adapter;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpression;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.CanonicalPathUtils;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.URLUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Created: 2019-07-27</p>
 */
public class UndertowResponseAdapter extends AbstractResponseAdapter {

    private static final Log log = LogFactory.getLog(UndertowResponseAdapter.class);

    private static final char QUESTION_CHAR = '?';

    private static final char AMPERSAND_CHAR = '&';

    private static final char EQUAL_CHAR = '=';

    private final Activity activity;

    private String contentType;

    private String charset;

    private Writer writer;

    private ResponseState responseState = ResponseState.NONE;

    private boolean responseDone;

    public UndertowResponseAdapter(HttpServerExchange exchange, Activity activity) {
        super(exchange);
        this.activity = activity;
    }

    @Override
    public String getHeader(String name) {
        return getHttpServerExchange().getResponseHeaders().getFirst(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return getHttpServerExchange().getResponseHeaders().get(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return getHttpServerExchange().getResponseHeaders()
                .getHeaderNames()
                .stream()
                .map(HttpString::toString)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean containsHeader(String name) {
        return getHttpServerExchange().getResponseHeaders().contains(name);
    }

    @Override
    public void setHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        setHeader(HttpString.tryFromString(name), value);
    }

    public void setHeader(HttpString name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        if (name.equals(Headers.CONTENT_TYPE)) {
            setContentType(value);
        } else {
            getHttpServerExchange().getResponseHeaders().put(name, value);
        }
    }

    @Override
    public void addHeader(String name, String value) {
        addHeader(HttpString.tryFromString(name), value);
    }

    public void addHeader(final HttpString name, final String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        if (name.equals(Headers.CONTENT_TYPE) &&
                !getHttpServerExchange().getResponseHeaders().contains(Headers.CONTENT_TYPE)) {
            setContentType(value);
        } else {
            getHttpServerExchange().getResponseHeaders().add(name, value);
        }
    }

    @Override
    public String getEncoding() {
        if (charset != null) {
            return charset;
        }
        return StandardCharsets.ISO_8859_1.name();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        this.charset = encoding;
        if (contentType != null) {
            getHttpServerExchange().getResponseHeaders().put(Headers.CONTENT_TYPE, getContentType());
        }
    }

    @Override
    public String getContentType() {
        if (contentType != null) {
            if (charset != null) {
                return contentType + ";charset=" + charset;
            } else {
                return contentType;
            }
        }
        return null;
    }

    @Override
    public void setContentType(String contentType) {
        if (contentType == null) {
            return;
        }

        MediaType mediaType = MediaType.parseMediaType(contentType);
        String type = mediaType.getType();
        String charset = mediaType.getParameter("charset");

        this.contentType = type;
        if (charset != null) {
            this.charset = charset;
        }

        if (this.charset != null) {
            getHttpServerExchange().getResponseHeaders().put(Headers.CONTENT_TYPE,
                    type + "; charset=" + this.charset);
        } else {
            getHttpServerExchange().getResponseHeaders().put(Headers.CONTENT_TYPE, type);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (responseState == ResponseState.WRITER) {
            throw new IllegalStateException("Cannot call getOutputStream(), getWriter() already called");
        }
        responseState = ResponseState.STREAM;
        return getHttpServerExchange().getOutputStream();
    }

    @Override
    public Writer getWriter() throws IOException {
        if (writer == null) {
            if (responseState == ResponseState.STREAM) {
                throw new IllegalStateException("Cannot call getWriter(), getOutputStream() already called");
            }
            responseState = ResponseState.WRITER;
            writer = new OutputStreamWriter(getHttpServerExchange().getOutputStream(), getEncoding());
        }
        return writer;
    }

    @Override
    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        } else {
            getHttpServerExchange().getOutputStream().flush();
        }
    }

    @Override
    public void redirect(String path) throws IOException {
        setStatus(HttpStatus.FOUND.value());
        if (URLUtils.isAbsoluteUrl(path)) { //absolute url
            getHttpServerExchange().getResponseHeaders().put(Headers.LOCATION, path);
        } else {
            String realPath;
            if (path.startsWith("/")) {
                realPath = path;
            } else {
                realPath = CanonicalPathUtils.canonicalize(path);
            }
            String url = getHttpServerExchange().getRequestScheme() + "://" + getHttpServerExchange().getHostAndPort() + realPath;
            getHttpServerExchange().getResponseHeaders().put(Headers.LOCATION, url);
        }
        responseDone();
    }

    @Override
    public String redirect(RedirectRule redirectRule) throws IOException {
        if (redirectRule == null) {
            throw new IllegalArgumentException("redirectRule must not be null");
        }

        String path = redirectRule.getPath(activity);
        int questionPos = -1;

        StringBuilder sb = new StringBuilder(256);
        if (path != null) {
            sb.append(path);
            questionPos = path.indexOf(QUESTION_CHAR);
        }

        ItemRuleMap parameterItemRuleMap = redirectRule.getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = new ItemExpression(activity);
            Map<String, Object> valueMap = evaluator.evaluate(parameterItemRuleMap);
            if (valueMap != null && !valueMap.isEmpty()) {
                if (questionPos == -1) {
                    sb.append(QUESTION_CHAR);
                }

                String name = null;
                Object value;
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    if (name != null) {
                        sb.append(AMPERSAND_CHAR);
                    }

                    name = entry.getKey();
                    value = entry.getValue();
                    String stringValue = (value != null ? value.toString() : null);
                    if (redirectRule.isExcludeEmptyParameters() &&
                            stringValue != null && !stringValue.isEmpty()) {
                        sb.append(name).append(EQUAL_CHAR);
                    } else if (redirectRule.isExcludeNullParameters() && stringValue != null) {
                        sb.append(name).append(EQUAL_CHAR);
                    } else {
                        sb.append(name).append(EQUAL_CHAR);
                    }
                    if (stringValue != null) {
                        stringValue = URLEncoder.encode(stringValue, getEncoding());
                        sb.append(stringValue);
                    }
                }
            }
        }

        path = sb.toString();
        redirect(path);
        return path;
    }

    @Override
    public int getStatus() {
        return getHttpServerExchange().getStatusCode();
    }

    @Override
    public void setStatus(int status) {
        getHttpServerExchange().setStatusCode(status);
    }

    public void responseDone() {
        if (responseDone) {
            return;
        }
        responseDone = true;
        try {
            closeStreamAndWriter();
        } catch (IOException e) {
            log.debug("An IOException occurred", e);
        }
    }

    public void closeStreamAndWriter() throws IOException {
        if (writer != null) {
            writer.close();
        } else {
            getHttpServerExchange().getOutputStream().close();
        }
    }

    private HttpServerExchange getHttpServerExchange() {
        return (HttpServerExchange)getAdaptee();
    }

    public enum ResponseState {
        NONE,
        STREAM,
        WRITER
    }

}
