package com.aspectran.core.service;

/**
 * <p>Created: 2025-02-13</p>
 */
public interface ServiceHoldingListener {

    default void afterServiceHolding(CoreService service) {
    }

    default void beforeServiceRelease(CoreService service) {
    }

}
