package com.aspectran.web.service;

import java.net.URL;
import java.net.URLClassLoader;

public class WebServiceClassLoader extends URLClassLoader {

    public WebServiceClassLoader(ClassLoader parent) {
        super(new URL[]{}, parent != null ? parent
                : (Thread.currentThread().getContextClassLoader() != null ? Thread.currentThread().getContextClassLoader()
                : (WebServiceClassLoader.class.getClassLoader() != null ? WebServiceClassLoader.class.getClassLoader()
                : ClassLoader.getSystemClassLoader())));
    }

}
