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
package com.aspectran.core.scheduler.activity;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.scheduler.service.DefaultSchedulerService;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * The Class ActivityJobReporter.
 *
 * <p>Created: 2016. 9. 3.</p>
 *
 * @since 3.0.0
 */
public class ActivityJobReporter {

    private static final Logger logger = LoggerFactory.getLogger(ActivityJobReporter.class);

    public static void jobToBeExecuted(@NonNull JobExecutionContext context, boolean vetoed) {
        JobDetail jobDetail = context.getJobDetail();
        JobKey key = jobDetail.getKey();
        String jobName = key.getName();
        String jobGroup = key.getGroup();

        Activity activity = resolveActivity(context);

        String title = (vetoed ? "VETOED" : "START");
        ToStringBuilder tsb = new ToStringBuilder(title);
        tsb.append("group", jobGroup);
        tsb.append("name", jobName);
        tsb.append("scheduledFireTime", toLocalDateTime(context.getScheduledFireTime()));
        tsb.append("fireTime", toLocalDateTime(context.getFireTime()));
        tsb.append("previousFireTime", toLocalDateTime(context.getPreviousFireTime()));
        tsb.append("nextFireTime", toLocalDateTime(context.getNextFireTime()));
        tsb.append("recovering", context.isRecovering());
        if (context.getRefireCount() > 0) {
            tsb.append("refireCount", context.getRefireCount());
        }

        if (activity == null) {
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            ScheduledJobRule jobRule = (ScheduledJobRule)jobDataMap.get(DefaultSchedulerService.JOB_RULE_DATA_KEY);
            SchedulerService service = (SchedulerService)jobDataMap.get(DefaultSchedulerService.SERVICE_DATA_KEY);
            if (!service.isActive()) {
                tsb.append("service", "inactive");
            }
            if (jobRule.isDisabled()) {
                tsb.append("job", "disabled");
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(tsb.toString());
        }
    }

    public static void jobWasExecuted(@NonNull JobExecutionContext context, JobExecutionException jobException) {
        try {
            JobDetail jobDetail = context.getJobDetail();
            JobKey key = jobDetail.getKey();
            String jobName = key.getName();
            String jobGroup = key.getGroup();

            Activity activity = resolveActivity(context);

            String title = (jobException == null ? "SUCCESS" : "FAILURE");
            ToStringBuilder tsb = new ToStringBuilder(title);
            tsb.append("group", jobGroup);
            tsb.append("name", jobName);
            tsb.append("jobRunTime", context.getJobRunTime());

            if (activity != null) {
                String response = activity.getResponseAdapter().getWriter().toString();
                if (StringUtils.hasLength(response)) {
                    tsb.append("response", response);
                }
            }

            if (jobException != null) {
                tsb.append("error", ExceptionUtils.getRootCause(jobException).getMessage());
                logger.error(tsb.toString(), jobException);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(tsb.toString());
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to report job activity", e);
        }
    }

    private static Activity resolveActivity(@NonNull JobExecutionContext context) {
        Activity activity = (Activity)context.getResult();
        StringifyContext stringifyContext;
        if (activity != null && activity.hasStringifyContext()) {
            stringifyContext = activity.getStringifyContext().clone();
        } else {
            stringifyContext = new StringifyContext();
        }
        if (stringifyContext.getDateTimeFormat() == null || stringifyContext.getDateTimeFormatter() == null) {
            stringifyContext.setDateTimeFormatter(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        return activity;
    }

    private static LocalDateTime toLocalDateTime(Date date) {
        if (date != null) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } else {
            return null;
        }
    }

}
