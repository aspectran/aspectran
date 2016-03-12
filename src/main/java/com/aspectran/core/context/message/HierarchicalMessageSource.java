package com.aspectran.core.context.message;

/**
 * Sub-interface of MessageSource to be implemented by objects that
 * can resolve messages hierarchically.
 *
 * <p>Created: 2016. 3. 8.</p>
 */
public interface HierarchicalMessageSource extends MessageSource {

    /**
     * Set the parent that will be used to try to resolve messages
     * that this object can't resolve.
     *
     * @param parent the parent MessageSource that will be used to
     * resolve messages that this object can't resolve.
     * May be {@code null}, in which case no further resolution is possible.
     */
    void setParentMessageSource(MessageSource parent);

    /**
     * Return the parent of this MessageSource, or {@code null} if none.
     */
    MessageSource getParentMessageSource();

}
