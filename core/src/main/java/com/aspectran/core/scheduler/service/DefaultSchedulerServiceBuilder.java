package com.aspectran.core.scheduler.service;

import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.SchedulerConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

public class DefaultSchedulerServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSchedulerServiceBuilder.class);

    @NonNull
    public static DefaultSchedulerService build(CoreService parentService, SchedulerConfig schedulerConfig) {
        Assert.notNull(parentService, "parentService must not be null");
        Assert.notNull(schedulerConfig, "schedulerConfig must not be null");

        DefaultSchedulerService schedulerService = new DefaultSchedulerService(parentService);

        int startDelaySeconds = schedulerConfig.getStartDelaySeconds();
        if (startDelaySeconds == -1) {
            startDelaySeconds = 3;
            if (logger.isDebugEnabled()) {
                logger.debug("Scheduler option 'startDelaySeconds' is not specified, defaulting to 3 seconds");
            }
        }

        boolean waitOnShutdown = schedulerConfig.isWaitOnShutdown();
        if (waitOnShutdown) {
            schedulerService.setWaitOnShutdown(true);
        }
        schedulerService.setStartDelaySeconds(startDelaySeconds);

        ExposalsConfig exposalsConfig = schedulerConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            schedulerService.setExposals(includePatterns, excludePatterns);
        }

        setServiceStateListener(schedulerService);
        return schedulerService;
    }

    private static void setServiceStateListener(@NonNull final DefaultSchedulerService schedulerService) {
        schedulerService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
            }

            @Override
            public void stopped() {
            }

            @Override
            public void paused(long millis) {
                logger.warn(schedulerService.getServiceName() + " does not support pausing for a certain period of time");
            }

            @Override
            public void paused() {
                schedulerService.pauseAll();
            }

            @Override
            public void resumed() {
                schedulerService.resumeAll();
            }
        });
    }

}
