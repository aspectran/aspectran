/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.component.translet.TransletNotFoundException;
import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.context.builder.config.ContextConfig;
import com.aspectran.core.context.builder.config.SessionConfig;
import com.aspectran.core.context.builder.config.ShellConfig;
import com.aspectran.core.context.builder.resource.AspectranClassLoader;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.activity.ShellActivity;
import com.aspectran.shell.adapter.ShellApplicationAdapter;
import com.aspectran.shell.adapter.ShellSessionAdapter;
import com.aspectran.shell.command.CommandLineParser;
import com.aspectran.shell.command.CommandRegistry;
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
class AspectranShellService extends AspectranCoreService implements ShellService {

    private static final Log log = LogFactory.getLog(AspectranShellService.class);

    private static final String DEFAULT_ROOT_CONTEXT = "/config/aspectran/root-config.xml";

    private SessionManager sessionManager;

    private long pauseTimeout = -1L;

    private Console console;

    private String[] commands;

    private CommandRegistry commandRegistry;

    /** If verbose mode is on, a detailed description is printed each time the command is executed. */
    private boolean verbose;

    private String welcomeMessage;

    private AspectranShellService() throws IOException {
        super(new ShellApplicationAdapter());
    }

    @Override
    public void afterContextLoaded() throws Exception {
        if (commands != null) {
            CommandRegistry commandRegistry = new CommandRegistry(this);
            commandRegistry.addCommand(commands);
            setCommandRegistry(commandRegistry);
        }

        sessionManager = new DefaultSessionManager(getActivityContext());
        sessionManager.setGroupName("SHL");
        SessionConfig sessionConfig = getAspectranConfig().getParameters(AspectranConfig.session);
        if (sessionConfig != null) {
            sessionManager.setSessionConfig(sessionConfig);
        }
        sessionManager.initialize();
    }

    @Override
    public void beforeContextDestroy() {
        sessionManager.destroy();
        sessionManager = null;
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        SessionAgent agent = sessionManager.newSessionAgent();
        return new ShellSessionAdapter(agent);
    }

    @Override
    public Console getConsole() {
        return console;
    }

    private void setConsole(Console console) {
        this.console = console;
    }

    @Override
    public String[] getCommands() {
        return commands;
    }

    private void setCommands(String[] commands) {
        this.commands = commands;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    private void setCommandRegistry(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    /**
     * Tests if the verbose mode is enabled.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Returns a flag indicating whether to show the description or not.
     *
     * @return true if the verbose mode is enabled
     */
    @Override
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Enables or disables the verbose mode.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Sets a flag indicating whether to show the description or not.
     *
     * @param verbose true to enable the verbose mode; false to disable
     */
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    @Override
    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    @Override
    public void printWelcomeMessage() {
        if (StringUtils.hasText(welcomeMessage)) {
            console.writeLine(welcomeMessage);
            console.flush();
        }
    }

    @Override
    public void printHelp() {
        if (isVerbose() && getActivityContext().getDescription() != null) {
            console.writeLine(getActivityContext().getDescription());
            console.flush();
        }
    }

    /**
     * Process the actual dispatching to the activity.
     *
     * @param command the translet name mapped to the command
     */
    @Override
    public void serve(String command) {
        CommandLineParser commandLineParser = CommandLineParser.parseCommandLine(command);
        serve(commandLineParser);
    }

    /**
     * Process the actual dispatching to the activity.
     *
     * @param commandLineParser the command line parser
     */
    @Override
    public void serve(CommandLineParser commandLineParser) {
        if (!isExposable(commandLineParser.getCommand())) {
            log.info("Unexposable Translet: " + commandLineParser.getCommand());
            return;
        }

        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (pauseTimeout == -1L) {
                    log.info("ShellService has been paused");
                } else {
                    long remains = pauseTimeout - System.currentTimeMillis();
                    if (remains > 0L) {
                        log.info("ShellService has been paused and will resume after " + remains + " ms");
                    } else {
                        log.info("ShellService has been paused and will soon resume");
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
                redirectionWriters = commandLineParser.getRedirectionWriters(console);
            } catch (Exception e) {
                log.warn("Invalid Redirection: " + CommandLineParser.serialize(commandLineParser.getRedirectionList()), e);
                return;
            }
        }

        Activity activity = null;

        try {
            activity = new ShellActivity(this, redirectionWriters);
            activity.prepare(commandLineParser.getCommand(), commandLineParser.getRequestMethod());
            activity.perform();
        } catch (TransletNotFoundException e) {
            console.writeLine("No translet mapped to the command [" + commandLineParser.getCommand() + "]");
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Activity terminated without completion: " + e.getMessage());
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
            AponReader.parse(aspectranConfigFile, aspectranConfig);
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
            service.setWelcomeMessage(shellConfig.getString(ShellConfig.welcome));
            service.setExposals(shellConfig.getStringArray(ShellConfig.exposals));
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
                service.printWelcomeMessage();
            }

            @Override
            public void restarted() {
                started();
            }

            @Override
            public void paused(long millis) {
                if (millis < 0L) {
                    throw new IllegalArgumentException("Pause timeout in milliseconds needs to be set to a value of greater than 0");
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
