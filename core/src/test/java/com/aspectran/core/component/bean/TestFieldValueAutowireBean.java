package com.aspectran.core.component.bean;

import com.aspectran.core.component.bean.annotation.Value;

public class TestFieldValueAutowireBean {

    @Value("#{properties^property1}")
    private String property1;

    @Value("#{properties^property2}")
    private String property2;

    @Value("#{properties^property3}")
    private String property3;

    @Value("%{classpath:test.properties^hello}")
    private String property4;

    public String getProperty1() {
        return property1;
    }

    public String getProperty2() {
        return property2;
    }

    public String getProperty3() {
        return property3;
    }

    public String getProperty4() {
        return property4;
    }

}
