package com.aspectran.core.context.bean;

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.TransletRule;

/**
 * The Interface AnnotatedConfigRelater.
 *
 * <p>Created: 2016. 2. 21.</p>
 */
public interface AnnotatedConfigRelater {

    public void relay(Class<?> targetBeanClass, BeanRule beanRule);

    public void relay(TransletRule transletRule);

}
