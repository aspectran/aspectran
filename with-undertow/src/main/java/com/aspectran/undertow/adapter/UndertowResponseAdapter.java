package com.aspectran.undertow.adapter;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.context.rule.RedirectRule;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;

/**
 * <p>Created: 2019-07-27</p>
 */
public class UndertowResponseAdapter extends AbstractResponseAdapter {

    private final Activity activity;

    public UndertowResponseAdapter(HttpServerExchange exchange, Activity activity) {
        super(exchange);
        this.activity = activity;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public void setHeader(String name, String value) {

    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {

    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void setContentType(String contentType) {

    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public Writer getWriter() throws IOException {
        return null;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void redirect(String path) throws IOException {

    }

    @Override
    public String redirect(RedirectRule redirectRule) throws IOException {
        return null;
    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public void setStatus(int status) {

    }

}
