package com.aspectran.web.adapter;

import com.aspectran.core.component.bean.scope.SessionScope;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;

public class HttpSessionScope extends SessionScope implements HttpSessionBindingListener, Serializable {

    public HttpSessionScope() {
        super();
    }

    @Override
    public  void valueBound(HttpSessionBindingEvent event) {
        // Servlet 3.x backward compatibility
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        destroy();
    }

}
