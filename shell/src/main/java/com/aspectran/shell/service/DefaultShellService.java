/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.shell.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.ShellConfig;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.shell.activity.ShellActivity;
import com.aspectran.shell.command.OutputRedirection;
import com.aspectran.shell.command.ShellTransletProcedure;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides an interactive shell that lets you use or control Aspectran directly
 * from the command line.
 *
 * @since 2016. 1. 18.
 */
public class DefaultShellService extends AbstractShellService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellService.class);

    private volatile long pauseTimeout = -1L;

    private DefaultShellService(ShellConsole console) {
        super(console);
    }

    public Translet translate(TransletCommandLine transletCommandLine) throws TransletNotFoundException {
        if (transletCommandLine == null) {
            throw new IllegalArgumentException("transletCommandLine must not be null");
        }
        if (!isExposable(transletCommandLine.getRequestName())) {
            getConsole().writeError("Unavailable translet: " + transletCommandLine.getRequestName());
            return null;
        }
        if (checkPaused()) {
            return null;
        }

        String requestName = transletCommandLine.getRequestName();
        MethodType requestMethod = transletCommandLine.getRequestMethod();
        if (requestMethod == null) {
            requestMethod = MethodType.GET;
        }
        TransletRule transletRule = getActivityContext()
                .getTransletRuleRegistry()
                .getTransletRule(requestName, requestMethod);
        if (transletRule == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No translet mapped for " + requestMethod + " " + requestName);
            }
            throw new TransletNotFoundException(requestName, requestMethod);
        }

        PrintWriter outputWriter = null;
        List<OutputRedirection> redirectionList = transletCommandLine.getLineParser().getRedirectionList();
        if (redirectionList != null) {
            try {
                outputWriter = OutputRedirection.determineOutputWriter(redirectionList, getConsole());
            } catch (Exception e) {
                getConsole().writeError("Invalid Output Redirection - " + e.getMessage());
                return null;
            }
        }

        ParameterMap parameterMap = transletCommandLine.getParameterMap();
        boolean procedural = (parameterMap == null);
        boolean verbose = (isVerbose() || transletCommandLine.isVerbose());

        if (transletRule.isAsync()) {
            asyncPerform(outputWriter, procedural, verbose, parameterMap,
                    requestName, requestMethod, transletRule);
            return null;
        } else {
            return perform(outputWriter, procedural, verbose, parameterMap,
                    requestName, requestMethod, transletRule, null);
        }
    }

    private void asyncPerform(@Nullable PrintWriter outputWriter,
                              boolean procedural, boolean verbose,
                              @Nullable ParameterMap parameterMap, String requestName,
                              MethodType requestMethod, TransletRule transletRule) {
        final ParameterMap finalParameterMap;
        if (parameterMap != null) {
            finalParameterMap = parameterMap;
        } else {
            finalParameterMap = new ParameterMap();
        }
        ShellTransletProcedure procedure = new ShellTransletProcedure(
                this, transletRule, finalParameterMap, procedural, verbose);
        procedure.printDescription(transletRule);
        try {
            procedure.proceed();
        } catch (MissingMandatoryParametersException e) {
            procedure.printSomeMandatoryParametersMissing(e.getItemRules());
            return;
        }

        final AtomicReference<Activity> activityReference = new AtomicReference<>();
        Runnable performable = () ->
                perform(outputWriter, procedural, verbose, finalParameterMap,
                        requestName, requestMethod, transletRule, activityReference);

        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(performable);
        if (transletRule.getTimeout() != null) {
            completableFuture.orTimeout(transletRule.getTimeout(), TimeUnit.MILLISECONDS);
        }
        completableFuture.exceptionally(throwable -> {
            Activity activity = activityReference.get();
            if (activity != null && !activity.isCommitted() && !activity.isExceptionRaised()) {
                activity.setRaisedException(new ActivityTerminatedException("Async Timeout"));
            } else {
                logger.error("Async Timeout ", throwable);
            }
            return null;
        });
    }

    private Translet perform(@Nullable PrintWriter outputWriter,
                             boolean procedural, boolean verbose,
                             @Nullable ParameterMap parameterMap, String requestName,
                             MethodType requestMethod, TransletRule transletRule,
                             AtomicReference<Activity> activityReference) {
        ShellActivity activity = null;
        Translet translet = null;
        try {
            activity = new ShellActivity(this);
            if (activityReference != null) {
                activityReference.set(activity);
            }
            activity.setProcedural(procedural);
            activity.setVerbose(verbose);
            activity.setParameterMap(parameterMap);
            activity.setOutputWriter(outputWriter);
            activity.prepare(requestName, requestMethod, transletRule);
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            getConsole().clearLine();
            getConsole().resetStyle();

            Throwable t;
            if (activity != null && activity.getRaisedException() != null) {
                t = activity.getRaisedException();
            } else {
                t = e;
            }
            Throwable cause = ExceptionUtils.getRootCause(t);
            throw new AspectranServiceException("Error occurred while processing request: " +
                Activity.makeFullRequestName(requestMethod, requestName) + "; Cause: " +
                ExceptionUtils.getSimpleMessage(cause), t);
        } finally {
            if (outputWriter != null) {
                outputWriter.close();
            }
        }
        if (translet != null && outputWriter == null) {
            try {
                String result = translet.getResponseAdapter().getWriter().toString();
                if (StringUtils.hasLength(result)) {
                    if (transletRule.isAsync()) {
                        getConsole().clearLine();
                    }
                    getConsole().writeLine(result);
                    if (transletRule.isAsync()) {
                        getConsole().redrawLine();
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to print activity result", e);
            }
        }
        return translet;
    }

    private boolean checkPaused() {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (pauseTimeout == -1L) {
                    getConsole().writeLine(getServiceName() + " has been paused");
                } else {
                    long remains = pauseTimeout - System.currentTimeMillis();
                    if (remains > 0L) {
                        getConsole().writeLine(getServiceName() + " has been paused and will resume after "
                                + remains + " ms");
                    } else {
                        getConsole().writeLine(getServiceName() + " has been paused and will soon resume");
                    }
                }
                return true;
            } else {
                pauseTimeout = 0L;
            }
        }
        return false;
    }

    /**
     * Returns a new instance of {@code DefaultShellService}.
     * @param aspectranConfig the aspectran configuration
     * @param console the {@code Console} instance
     * @return the instance of {@code DefaultShellService}
     */
    @NonNull
    public static DefaultShellService create(@NonNull AspectranConfig aspectranConfig, ShellConsole console) {
        DefaultShellService shellService = new DefaultShellService(console);
        ShellConfig shellConfig = aspectranConfig.getShellConfig();
        if (shellConfig != null) {
            configure(shellService, shellConfig);
        }
        shellService.configure(aspectranConfig);
        setServiceStateListener(shellService);
        return shellService;
    }

    private static void configure(@NonNull DefaultShellService shellService, @NonNull ShellConfig shellConfig) {
        shellService.setVerbose(shellConfig.isVerbose());
        shellService.setGreetings(shellConfig.getGreetings());
        ExposalsConfig exposalsConfig = shellConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            shellService.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(@NonNull final DefaultShellService shellService) {
        shellService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                CoreServiceHolder.hold(shellService);
                shellService.createSessionManager();
                shellService.pauseTimeout = 0L;
                shellService.printGreetings();
                shellService.printHelp();
            }

            @Override
            public void restarted() {
                started();
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    shellService.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0");
                }
            }

            @Override
            public void paused() {
                shellService.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                shellService.pauseTimeout = 0L;
            }

            @Override
            public void stopped() {
                paused();
                shellService.destroySessionManager();
                CoreServiceHolder.release(shellService);
            }
        });
    }

}
