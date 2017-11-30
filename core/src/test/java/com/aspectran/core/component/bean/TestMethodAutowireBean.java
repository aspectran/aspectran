package com.aspectran.core.component.bean;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Qualifier;

/**
 * <p>Created: 2017. 11. 29.</p>
 */
public class TestMethodAutowireBean {

    private TestFieldValueAutowireBean bean1;

    private TestFieldValueAutowireBean bean2;

    @Autowired
    @Qualifier("bean.TestFieldValueAutowireBean")
    public void setBean1(TestFieldValueAutowireBean bean) {
        this.bean1 = bean;
    }

    public TestFieldValueAutowireBean getBean1() {
        return bean1;
    }
    @Autowired(required = false)
    @Qualifier("bean.TestFieldValueAutowireBean3")
    public void setBean2(TestFieldValueAutowireBean bean) {
        this.bean2 = bean;
    }

    public TestFieldValueAutowireBean getBean2() {
        return bean2;
    }

}
