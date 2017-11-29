package com.aspectran.core.component.bean;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Qualifier;

/**
 * <p>Created: 2017. 11. 29.</p>
 */
public class TestFieldAutowireBean {

    @Autowired
    @Qualifier("bean.TestFieldValueAutowireBean")
    private TestFieldValueAutowireBean bean1;

    @Autowired(required = false)
    @Qualifier("bean.TestFieldValueAutowireBean22")
    private TestFieldValueAutowireBean bean2;

    public TestFieldValueAutowireBean getBean1() {
        return bean1;
    }
    public TestFieldValueAutowireBean getBean2() {
        return bean2;
    }

}
