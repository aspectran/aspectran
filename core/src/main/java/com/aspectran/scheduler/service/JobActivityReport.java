/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

    private final Log log = LogFactory.getLog(JobActivityReport.class);

    private final JobExecutionContext jobExecutionContext;

    private final JobExecutionException jobException;

    public JobActivityReport(JobExecutionContext jobExecutionContext, JobExecutionException jobException) {
        this.jobExecutionContext = jobExecutionContext;
        this.jobException = jobException;
    }

    public void reporting(Activity activity) {
        if (!log.isDebugEnabled()) {
            return;
        }

        try {
            JobDetail jobDetail = jobExecutionContext.getJobDetail();
            JobKey key = jobDetail.getKey();

            String jobName = key.getName();
            String jobGroup = key.getGroup();

            StringBuilder sb = new StringBuilder(720);
            sb.append("Result of the job [").append(jobName).append("]").append(ActivityContext.LINE_SEPARATOR);
            sb.append("-------------------------------------------------------------------------").append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Job Group           : ").append(jobGroup).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Job Name            : ").append(jobName).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Scheduled Fire Time : ").append(jobExecutionContext.getScheduledFireTime()).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Actual Fire Time    : ").append(jobExecutionContext.getFireTime()).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Run Time            : ").append(jobExecutionContext.getJobRunTime()).append(" milliseconds").append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Previous Fire Time  : ").append(jobExecutionContext.getPreviousFireTime()).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Next Fire Time      : ").append(jobExecutionContext.getNextFireTime()).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Recovering          : ").append(jobExecutionContext.isRecovering()).append(ActivityContext.LINE_SEPARATOR);
            sb.append("- Re-fire Count       : ").append(jobExecutionContext.getRefireCount()).append(ActivityContext.LINE_SEPARATOR);

            if (jobException != null) {
                sb.append("- An error occurred running job -----------------------------------------").append(ActivityContext.LINE_SEPARATOR);
                sb.append(jobException).append(ActivityContext.LINE_SEPARATOR);
                sb.append("=========================================================================").append(ActivityContext.LINE_SEPARATOR);
            } else {
                sb.append("-------------------------------------------------------------------------").append(ActivityContext.LINE_SEPARATOR);
            }

            if (activity != null) {
                Writer writer = activity.getResponseAdapter().getWriter();
                String output = writer.toString();
                if (!output.isEmpty()) {
                    sb.append(output).append(ActivityContext.LINE_SEPARATOR);
                    sb.append("=========================================================================").append(ActivityContext.LINE_SEPARATOR);
                }
            }

            log.debug(sb.toString());
        } catch(IOException e) {
            log.warn("Job activity reporting failed", e);
        }
    }

}
