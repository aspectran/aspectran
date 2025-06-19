/*
 * Copyright (c) 2008-present The Aspectran Project
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

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.shell.activity.ShellActivity;
import com.aspectran.shell.command.OutputRedirection;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Provides an interactive shell that lets you use or control Aspectran directly
 * from the command line.
 *
 * @since 2016. 1. 18.
 */
public class DefaultShellService extends AbstractShellService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellService.class);

    protected volatile long pauseTimeout = -1L;

    DefaultShellService(ShellConsole console) {
        super(console);
    }

    public Translet translate(TransletCommandLine transletCommandLine) throws TransletNotFoundException {
        if (checkPaused()) {
            return null;
        }
        if (transletCommandLine == null) {
            throw new IllegalArgumentException("transletCommandLine must not be null");
        }
        if (!isRequestAcceptable(transletCommandLine.getRequestName())) {
            getConsole().writeError("Unavailable translet: " + transletCommandLine.getRequestName());
            return null;
        }

        final String requestName = transletCommandLine.getRequestName();
        final MethodType requestMethod = (transletCommandLine.getRequestMethod() != null ?
                transletCommandLine.getRequestMethod() : MethodType.GET);
        final ParameterMap parameterMap = transletCommandLine.getParameterMap();
        final boolean procedural = (parameterMap.isEmpty());
        final boolean verbose = (isVerbose() || transletCommandLine.isVerbose());

        final PrintWriter outputWriter;
        List<OutputRedirection> redirectionList = transletCommandLine.getLineParser().getRedirectionList();
        if (redirectionList != null) {
            try {
                outputWriter = OutputRedirection.determineOutputWriter(redirectionList, getConsole());
            } catch (Exception e) {
                getConsole().writeError("Invalid Output Redirection - " + e.getMessage());
                return null;
            }
        } else {
            outputWriter = null;
        }

        ShellActivity activity = new ShellActivity(this);
        activity.setProcedural(procedural);
        activity.setVerbose(verbose);
        activity.setRequestName(requestName);
        activity.setRequestMethod(requestMethod);
        activity.setParameterMap(parameterMap);
        activity.setOutputWriter(outputWriter);
        try {
            activity.prepare();
        } catch (TransletNotFoundException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("No translet mapped for {} {}", requestMethod, requestName);
            }
            throw e;
        } catch (Exception e) {
            serviceError(activity, e);
        }
        if (activity.isAsync()) {
            asyncPerform(activity);
            return null;
        } else {
            return perform(activity);
        }
    }

    private void asyncPerform(@NonNull ShellActivity activity) {
        try {
            activity.preProcedure();
        } catch (ActivityTerminatedException e) {
           return;
        } catch (Exception e) {
            serviceError(activity, e);
        }

        final Runnable performable = () -> perform(activity);
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(performable);
        if (activity.getTimeout() != null) {
            completableFuture.orTimeout(activity.getTimeout(), TimeUnit.MILLISECONDS);
        }
        completableFuture.exceptionally(throwable -> {
            if (!activity.isResponded() && !activity.isExceptionRaised()) {
                activity.setRaisedException(new ActivityTerminatedException("Async Timeout"));
            } else {
                logger.error(throwable.getMessage(), throwable);
            }
            return null;
        });
    }

    private Translet perform(@NonNull ShellActivity activity) {
        Translet translet = null;
        try {
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: {}", e.getMessage());
            }
        } catch (Exception e) {
            serviceError(activity, e);
        } finally {
            if (activity.getOutputWriter() != null) {
                try {
                    activity.getOutputWriter().close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        if (translet != null && activity.getOutputWriter() == null) {
            try {
                String result = translet.getResponseAdapter().getWriter().toString();
                if (StringUtils.hasLength(result)) {
                    if (activity.isAsync()) {
                        getConsole().clearLine();
                    }
                    getConsole().writeLine(result);
                    if (activity.isAsync()) {
                        getConsole().redrawLine();
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to print activity result", e);
            }
        }
        return translet;
    }

    private void serviceError(@NonNull ShellActivity activity, Exception e) {
        getConsole().clearLine();
        getConsole().resetStyle();

        Throwable t;
        if (activity.isExceptionRaised()) {
            t = activity.getRaisedException();
        } else {
            t = e;
        }
        Throwable cause = ExceptionUtils.getRootCause(t);
        throw new CoreServiceException("Error occurred while processing request: " +
                activity.getFullRequestName() + "; Cause: " + ExceptionUtils.getSimpleMessage(cause), t);
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

}
