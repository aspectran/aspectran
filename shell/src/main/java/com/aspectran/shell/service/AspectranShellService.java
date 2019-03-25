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
package com.aspectran.shell.service;

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.ShellConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.activity.ShellActivity;
import com.aspectran.shell.command.OutputRedirection;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.console.Console;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Provides an interactive shell that lets you use or control Aspectran directly
 * from the command line.
 *
 * @since 2016. 1. 18.
 */
public class AspectranShellService extends AbstractShellService {

    private static final Log log = LogFactory.getLog(AspectranShellService.class);

    private static final String DEFAULT_APP_CONFIG_ROOT_FILE = "/config/app-config.xml";

    private long pauseTimeout = -1L;

    private AspectranShellService(Console console) {
        super(console);
    }

    @Override
    public Translet translate(TransletCommandLine transletCommandLine, Console console) {
        if (transletCommandLine == null) {
            throw new IllegalArgumentException("transletCommandLine must not be null");
        }
        if (console == null) {
            throw new IllegalArgumentException("console must not be null");
        }
        if (!isExposable(transletCommandLine.getRequestName())) {
            console.writeError("Unexposable translet: " + transletCommandLine.getRequestName());
            return null;
        }
        if (checkPaused(console)) {
            return null;
        }

        PrintWriter outputWriter = null;
        List<OutputRedirection> redirectionList = transletCommandLine.getLineParser().getRedirectionList();
        if (redirectionList != null) {
            try {
                outputWriter = OutputRedirection.determineOutputWriter(redirectionList, console);
            } catch (Exception e) {
                console.writeError("Invalid Output Redirection - " + e.getMessage());
                return null;
            }
        }

        boolean procedural = (transletCommandLine.getParameterMap() == null);
        ParameterMap parameterMap = transletCommandLine.getParameterMap();
        String transletName = transletCommandLine.getRequestName();
        MethodType requestMethod = transletCommandLine.getRequestMethod();

        ShellActivity activity = null;
        Translet translet = null;
        try {
            activity = new ShellActivity(this, console);
            activity.setProcedural(procedural);
            activity.setParameterMap(parameterMap);
            activity.setOutputWriter(outputWriter);
            activity.prepare(transletName, requestMethod);
            activity.perform();
            translet = activity.getTranslet();
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Activity terminated: " + e.getMessage());
            }
        } catch (TransletNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new AspectranServiceException("An error occurred while processing translet: " + transletName, e);
        } finally {
            if (activity != null) {
                activity.finish();
            }
            if (outputWriter != null) {
                outputWriter.close();
            }
        }
        if (translet != null && outputWriter == null) {
            try {
                String result = translet.getResponseAdapter().getWriter().toString();
                if (StringUtils.hasLength(result)) {
                    console.writeLine(result);
                }
            } catch (IOException e) {
                log.warn("Failed to print activity result", e);
            }
        }
        return translet;
    }

    private boolean checkPaused(Console console) {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (pauseTimeout == -1L) {
                    console.writeLine(getServiceName() + " has been paused");
                } else {
                    long remains = pauseTimeout - System.currentTimeMillis();
                    if (remains > 0L) {
                        console.writeLine(getServiceName() + " has been paused and will resume after "
                                + remains + " ms");
                    } else {
                        console.writeLine(getServiceName() + " has been paused and will soon resume");
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
     * Returns a new instance of {@code AspectranShellService}.
     *
     * @param aspectranConfig the aspectran configuration
     * @param console the {@code Console} instance
     * @return the instance of {@code AspectranShellService}
     */
    public static AspectranShellService create(AspectranConfig aspectranConfig, Console console) {
        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        String rootFile = contextConfig.getRootFile();
        if (!StringUtils.hasText(rootFile) && !contextConfig.hasAspectranParameters()) {
            contextConfig.setRootFile(DEFAULT_APP_CONFIG_ROOT_FILE);
        }

        AspectranShellService service = new AspectranShellService(console);
        ShellConfig shellConfig = aspectranConfig.getShellConfig();
        if (shellConfig != null) {
            applyShellConfig(service, shellConfig);
        }
        service.prepare(aspectranConfig);
        setServiceStateListener(service);
        return service;
    }

    private static void applyShellConfig(AspectranShellService service, ShellConfig shellConfig) {
        service.setVerbose(shellConfig.isVerbose());
        service.setGreetings(shellConfig.getGreetings());
        ExposalsConfig exposalsConfig = shellConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            service.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(final AspectranShellService service) {
        service.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                service.pauseTimeout = 0;
                service.printGreetings();
                service.printHelp();
            }

            @Override
            public void restarted() {
                started();
            }

            @Override
            public void paused(long millis) {
                if (millis < 0L) {
                    throw new IllegalArgumentException("Pause timeout in milliseconds needs to be " +
                            "set to a value of greater than 0");
                }
                service.pauseTimeout = System.currentTimeMillis() + millis;
            }

            @Override
            public void paused() {
                service.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                service.pauseTimeout = 0;
            }

            @Override
            public void stopped() {
                paused();
            }
        });
    }

}
