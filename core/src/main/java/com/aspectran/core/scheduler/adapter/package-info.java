/**
 * Provides adapter classes for integrating the Quartz scheduler with Aspectran's activity model.
 * <p>These classes adapt Quartz's {@link org.quartz.JobExecutionContext} to Aspectran's
 * standard {@link com.aspectran.core.adapter.RequestAdapter} and
 * {@link com.aspectran.core.adapter.ResponseAdapter} interfaces, allowing scheduled jobs
 * to be treated like any other request within the framework.</p>
 */
package com.aspectran.core.scheduler.adapter;
