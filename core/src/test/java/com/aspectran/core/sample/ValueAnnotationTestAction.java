package com.aspectran.core.sample;

import com.aspectran.core.component.bean.annotation.Value;

public class ValueAnnotationTestAction {

    @Value("#{properties^property1}")
    private String property1;

    @Value("#{properties^property2}")
    private String property2;

    @Value("#{properties^property3}")
    private String property3;

    @Value("%{classpath:/test.properties^hello}")
    private String property4;

    public String property1() {
        return property1;
    }

    public String property2() {
        return property2;
    }

    public String property3() {
        return property3;
    }

    public String property4() {
        return property4;
    }

}
