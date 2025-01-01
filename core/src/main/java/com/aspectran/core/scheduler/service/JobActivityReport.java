/*
 * Copyright (c) 2008-2025 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.scheduler.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * The Class JobActivityReport.
 *
 * <p>Created: 2016. 9. 3.</p>
 *
 * @since 3.0.0
 */
public class JobActivityReport {

    private static final Logger logger = LoggerFactory.getLogger(JobActivityReport.class);

    private final JobExecutionContext jobExecutionContext;

    private final JobExecutionException jobException;

    public JobActivityReport(JobExecutionContext jobExecutionContext, JobExecutionException jobException) {
        this.jobExecutionContext = jobExecutionContext;
        this.jobException = jobException;
    }

    public void reporting() {
        try {
            JobDetail jobDetail = jobExecutionContext.getJobDetail();

            JobKey key = jobDetail.getKey();
            String jobName = key.getName();
            String jobGroup = key.getGroup();

            StringBuilder sb = new StringBuilder(768);
            sb.append("Result of job execution");
            if (jobException != null) {
                sb.append(" (Failed)");
            }
            sb.append(System.lineSeparator());
            sb.append("----------------------------------------------------------------------------").append(System.lineSeparator());
            sb.append("- Job Group           : ").append(jobGroup).append(System.lineSeparator());
            sb.append("- Job Name            : ").append(jobName).append(System.lineSeparator());
            sb.append("- Scheduled Fire Time : ").append(formatDate(jobExecutionContext.getScheduledFireTime())).append(System.lineSeparator());
            sb.append("- Actual Fire Time    : ").append(formatDate(jobExecutionContext.getFireTime())).append(System.lineSeparator());
            sb.append("- Run Time            : ").append(jobExecutionContext.getJobRunTime()).append(" milliseconds").append(System.lineSeparator());
            sb.append("- Previous Fire Time  : ").append(jobExecutionContext.getPreviousFireTime() != null ? formatDate(jobExecutionContext.getPreviousFireTime()) : "N/A").append(System.lineSeparator());
            sb.append("- Next Fire Time      : ").append(jobExecutionContext.getNextFireTime() != null ? formatDate(jobExecutionContext.getNextFireTime()) : "N/A").append(System.lineSeparator());
            sb.append("- Recovering          : ").append(jobExecutionContext.isRecovering()).append(System.lineSeparator());
            sb.append("- Re-fire Count       : ").append(jobExecutionContext.getRefireCount()).append(System.lineSeparator());
            sb.append("----------------------------------------------------------------------------").append(System.lineSeparator());

            Activity activity = (Activity)jobExecutionContext.getResult();
            if (activity != null) {
                String result = activity.getResponseAdapter().getWriter().toString();
                if (StringUtils.hasLength(result)) {
                    sb.append(result).append(System.lineSeparator());
                    sb.append("----------------------------------------------------------------------------").append(System.lineSeparator());
                }
            } else {
                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                ScheduledJobRule jobRule = (ScheduledJobRule)jobDataMap.get(DefaultSchedulerService.JOB_RULE_DATA_KEY);
                if (jobRule.isDisabled()) {
                    sb.append("- Execution of this job is disabled.").append(System.lineSeparator());
                    sb.append("----------------------------------------------------------------------------").append(System.lineSeparator());
                }
            }

            if (jobException != null) {
                String msg = ExceptionUtils.getRootCause(jobException).getMessage();
                sb.append("[ERROR] ").append(msg.trim()).append(System.lineSeparator());
                logger.error(sb.toString().trim(), jobException);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(sb.toString());
                }
            }
        } catch (IOException e) {
            logger.warn("Job activity reporting failed", e);
        }
    }

    @NonNull
    private String formatDate(@NonNull Date date) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ldt);
    }

}
