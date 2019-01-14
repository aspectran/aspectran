/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.scheduler.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.ExceptionUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import java.io.IOException;
import java.io.Writer;

/**
 * The Class JobActivityReport.
 *
 * <p>Created: 2016. 9. 3.</p>
 *
 * @since 3.0.0
 */
public class JobActivityReport {

    private static final Log log = LogFactory.getLog(JobActivityReport.class);

    private final JobExecutionContext jobExecutionContext;

    private final JobExecutionException jobException;

    public JobActivityReport(JobExecutionContext jobExecutionContext, JobExecutionException jobException) {
        this.jobExecutionContext = jobExecutionContext;
        this.jobException = jobException;
    }

    public void reporting(Activity activity) {
        try {
            JobDetail jobDetail = jobExecutionContext.getJobDetail();
            JobKey key = jobDetail.getKey();

            String jobName = key.getName();
            String jobGroup = key.getGroup();

            StringBuilder sb = new StringBuilder(720);
            sb.append("Result of job execution");
            if (jobException != null) {
                sb.append(" (Failed)");
            }
            sb.append(ActivityContext.LINE_SEPARATOR);
            sb.append("----------------------------------------------------------------------------").append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Job Group           : ").append(jobGroup).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Job Name            : ").append(jobName).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Scheduled Fire Time : ").append(jobExecutionContext.getScheduledFireTime()).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Actual Fire Time    : ").append(jobExecutionContext.getFireTime()).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Run Time            : ").append(jobExecutionContext.getJobRunTime()).append(" milliseconds").append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Previous Fire Time  : ").append(jobExecutionContext.getPreviousFireTime() != null ? jobExecutionContext.getPreviousFireTime() : "N/A").append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Next Fire Time      : ").append(jobExecutionContext.getNextFireTime() != null ? jobExecutionContext.getNextFireTime() : "N/A").append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Recovering          : ").append(jobExecutionContext.isRecovering()).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Re-fire Count       : ").append(jobExecutionContext.getRefireCount()).append(ActivityContext.LINE_SEPARATOR);
            sb.append("----------------------------------------------------------------------------").append(ActivityContext.LINE_SEPARATOR);

            if (activity != null) {
                Writer writer = activity.getResponseAdapter().getWriter();
                String output = writer.toString();
                if (!StringUtils.hasLength(output)) {
                    sb.append(output).append(ActivityContext.LINE_SEPARATOR);
                    sb.append("----------------------------------------------------------------------------").append(ActivityContext.LINE_SEPARATOR);
                }
            }

            if (jobException != null) {
                String msg = ExceptionUtils.getRootCause(jobException).getMessage();
                sb.append("[ERROR] ").append(msg.trim()).append(ActivityContext.LINE_SEPARATOR);
                log.error(sb.toString().trim(), jobException);
            } else {
                log.debug(sb.toString());
            }
        } catch(IOException e) {
            log.warn("Job activity reporting failed", e);
        }
    }

}
