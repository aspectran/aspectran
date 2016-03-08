package com.aspectran.core.context.message;

/**
 * <p>Created: 2016. 3. 8.</p>
 */
public interface HierarchicalMessageSource {

    void setParentMessageSource(MessageSource parent);

    MessageSource getParentMessageSource();

}
