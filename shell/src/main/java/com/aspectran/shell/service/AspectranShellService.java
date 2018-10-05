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
package com.aspectran.shell.service;

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.component.translet.TransletNotFoundException;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.ShellConfig;
import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.activity.ShellActivity;
import com.aspectran.shell.command.CommandLineParser;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.console.DefaultConsole;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * The Class AspectranShellService.
 *
 * @since 2016. 1. 18.
 */
class AspectranShellService extends AbstractShellService {

    private static final Log log = LogFactory.getLog(AspectranShellService.class);

    private static final String DEFAULT_ROOT_CONTEXT = "/config/aspectran/root-config.xml";

    private long pauseTimeout = -1L;

    private AspectranShellService() throws IOException {
        super();
    }

    @Override
    public void execute(String commandLine) {
        CommandLineParser commandLineParser = CommandLineParser.parse(commandLine, true);
        if (!commandLineParser.hasParameters()) {
            commandLineParser = CommandLineParser.parse(commandLine, false);
        }
        execute(commandLineParser);
    }

    @Override
    public void execute(CommandLineParser commandLineParser) {
        if (!isExposable(commandLineParser.getCommandName())) {
            getConsole().writeLine("Unexposable translet: " + commandLineParser.getCommandName());
            return;
        }

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
                return;
            } else {
                pauseTimeout = 0L;
            }
        }

        Writer[] redirectionWriters = null;
        if (commandLineParser.getRedirectionList() != null) {
            try {
                redirectionWriters = commandLineParser.getRedirectionWriters(getConsole());
            } catch (Exception e) {
                getConsole().writeLine("Invalid Redirection: " +
                        CommandLineParser.serialize(commandLineParser.getRedirectionList()), e);
                return;
            }
        }

        ShellActivity activity = null;
        try {
            activity = new ShellActivity(this);
            activity.setProcedural(!commandLineParser.hasParameters());
            activity.setParameterMap(commandLineParser.extractParameters());
            activity.setRedirectionWriters(redirectionWriters);
            activity.prepare(commandLineParser.getCommandName(), commandLineParser.getRequestMethod());
            activity.perform();
        } catch (TransletNotFoundException e) {
            if (log.isTraceEnabled()) {
                log.trace("Unknown translet: " + commandLineParser.getCommandName());
            }
            throw e;
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Activity terminated: Cause: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("An error occurred while processing the command", e);
        } finally {
            if (redirectionWriters != null) {
                for (Writer writer : redirectionWriters) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        log.error("Redirection writer close failed: " + e.getMessage(), e);
                    }
                }
            }
            if (activity != null) {
                activity.finish();
            }
        }
    }

    /**
     * Returns a new instance of {@code ShellService}.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the instance of {@code ShellService}
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    protected static ShellService create(String aspectranConfigFile)
            throws AspectranServiceException, IOException {
        return create(aspectranConfigFile, null);
    }

    /**
     * Returns a new instance of {@code ShellService}.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @param console the console
     * @return the instance of {@code ShellService}
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    protected static ShellService create(String aspectranConfigFile, Console console)
            throws AspectranServiceException, IOException {
        File file = ResourceUtils.getFile(aspectranConfigFile, AspectranClassLoader.getDefaultClassLoader());
        return create(file, console);
    }

    /**
     * Returns a new instance of {@code ShellService}.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the instance of {@code ShellService}
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    protected static ShellService create(File aspectranConfigFile)
            throws AspectranServiceException, IOException {
        return create(aspectranConfigFile, null);
    }

    /**
     * Returns a new instance of {@code ShellService}.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @param console the console
     * @return the instance of {@code ShellService}
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    protected static ShellService create(File aspectranConfigFile, Console console)
            throws AspectranServiceException, IOException {
        AspectranConfig aspectranConfig = new AspectranConfig();
        if (aspectranConfigFile != null) {
            try {
                AponReader.parse(aspectranConfigFile, aspectranConfig);
            } catch (Exception e) {
                throw new AspectranServiceException("Failed to parse aspectran config file: " +
                        aspectranConfigFile, e);
            }
        }

        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        String rootConfigLocation = contextConfig.getString(ContextConfig.root);
        if (rootConfigLocation == null || rootConfigLocation.length() == 0) {
            contextConfig.putValue(ContextConfig.root, DEFAULT_ROOT_CONTEXT);
        }

        AspectranShellService service = new AspectranShellService();
        service.prepare(aspectranConfig);
        ShellConfig shellConfig = aspectranConfig.getShellConfig();
        if (shellConfig != null) {
            if (console != null) {
                service.setConsole(console);
            } else {
                service.setConsole(new DefaultConsole());
            }
            String commandPrompt = shellConfig.getString(ShellConfig.prompt);
            if (commandPrompt != null) {
                service.getConsole().setCommandPrompt(commandPrompt);
            }
            String[] commands = shellConfig.getStringArray(ShellConfig.commands);
            if (commands != null && commands.length > 0) {
                service.setCommands(commands);
            }
            service.setVerbose(BooleanUtils.toBoolean(shellConfig.getBoolean(ShellConfig.verbose)));
            service.setGreetings(shellConfig.getString(ShellConfig.greetings));
            ExposalsConfig exposalsConfig = shellConfig.getExposalsConfig();
            if (exposalsConfig != null) {
                String[] includePatterns = exposalsConfig.getStringArray(ExposalsConfig.plus);
                String[] excludePatterns = exposalsConfig.getStringArray(ExposalsConfig.minus);
                service.setExposals(includePatterns, excludePatterns);
            }
        } else {
            service.setConsole(new DefaultConsole());
        }

        setServiceStateListener(service);
        return service;
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
