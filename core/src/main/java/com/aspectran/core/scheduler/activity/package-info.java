/**
 * Provides the Activity implementation for scheduled jobs.
 * <p>This package contains the necessary classes to bridge the gap between a scheduler's
 * job execution (e.g., from Quartz) and Aspectran's activity lifecycle. When a scheduled
 * trigger fires, the classes in this package are responsible for initiating an
 * {@link com.aspectran.core.activity.Activity} that runs the specified translet.</p>
 */
package com.aspectran.core.scheduler.activity;
