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
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.BasicAspectranService;
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
import com.aspectran.shell.inout.ConsoleInout;
import com.aspectran.shell.inout.SystemConsoleInout;
import com.aspectran.shell.service.command.CommandParser;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * The Class ShellAspectranService.
 *
 * @since 2016. 1. 18.
 */
public class ShellAspectranService extends BasicAspectranService {

    private static final Log log = LogFactory.getLog(ShellAspectranService.class);

    private static final String DEFAULT_ROOT_CONTEXT = "config/root-config.xml";

    private SessionManager sessionManager;

    private long pauseTimeout = -1L;

    private ConsoleInout consoleInout;

    private boolean descriptable;

    private String usage;

    private ShellAspectranService() throws IOException {
        super(new ShellApplicationAdapter());
    }

    @Override
    public void afterContextLoaded() throws Exception {
        sessionManager = new DefaultSessionManager(getActivityContext());
        sessionManager.setGroupName("CON");

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

    public SessionAdapter newSessionAdapter() {
        SessionAgent agent = sessionManager.newSessionAgent();
        return new ShellSessionAdapter(agent);
    }

    public ConsoleInout getConsoleInout() {
        return consoleInout;
    }

    private void setConsoleInout(ConsoleInout consoleInout) {
        this.consoleInout = consoleInout;
    }

    /**
     * Returns a flag indicating whether to show the description or not.
     *
     * @return true if description should be shown
     */
    public boolean isDescriptable() {
        return descriptable;
    }

    /**
     * Sets a flag indicating whether to show the description or not.
     *
     * @param descriptable true if description should be shown
     */
    public void setDescriptable(boolean descriptable) {
        this.descriptable = descriptable;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public void printUsage() {
        if (StringUtils.hasText(usage)) {
            consoleInout.writeLine(usage);
            consoleInout.flush();
        }
        if (isDescriptable() && getActivityContext().getDescription() != null) {
            consoleInout.writeLine(getActivityContext().getDescription());
            consoleInout.flush();
        }
    }

    /**
     * Process the actual dispatching to the activity.
     *
     * @param command the translet name
     */
    public void serve(String command) {
        if (!isExposable(command)) {
            log.info("Unexposable Translet: " + command);
            return;
        }

        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (pauseTimeout == -1L) {
                    log.info("AspectranService has been paused");
                } else {
                    long remains = pauseTimeout - System.currentTimeMillis();
                    if (remains > 0L) {
                        log.info("AspectranService has been paused and will resume after " + remains + " ms");
                    } else {
                        log.info("AspectranService has been paused and will soon resume");
                    }
                }
                return;
            } else {
                pauseTimeout = 0L;
            }
        }

        CommandParser commandParser = CommandParser.parseCommand(command);
        Writer[] redirectionWriters = null;

        if (commandParser.getRedirectionList() != null) {
            try {
                redirectionWriters = commandParser.getRedirectionWriters(consoleInout);
            } catch (Exception e) {
                log.warn("Invalid Redirection: " + CommandParser.serialize(commandParser.getRedirectionList()), e);
                return;
            }
        }

        Activity activity = null;

        try {
            activity = new ShellActivity(this, redirectionWriters);
            activity.prepare(commandParser.getTransletName(), commandParser.getRequestMethod());
            activity.perform();
        } catch (TransletNotFoundException e) {
            log.info("translet: " + command + ": command not found");
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Translet did not complete and terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("An error occurred while processing an activity on the shell service", e);
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
     * Returns a new instance of ConsoleAspectranService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the shell aspectran service
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    public static ShellAspectranService create(String aspectranConfigFile)
            throws AspectranServiceException, IOException {
        return create(aspectranConfigFile, null);
    }

    /**
     * Returns a new instance of ConsoleAspectranService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the shell aspectran service
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    public static ShellAspectranService create(String aspectranConfigFile, ConsoleInout consoleInout)
            throws AspectranServiceException, IOException {
        File file = ResourceUtils.getFile(aspectranConfigFile, AspectranClassLoader.getDefaultClassLoader());
        return create(file, consoleInout);
    }

    /**
     * Returns a new instance of ConsoleAspectranService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the shell aspectran service
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    public static ShellAspectranService create(File aspectranConfigFile)
            throws AspectranServiceException, IOException {
        return create(aspectranConfigFile, null);
    }

    /**
     * Returns a new instance of ConsoleAspectranService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the shell aspectran service
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    public static ShellAspectranService create(File aspectranConfigFile, ConsoleInout consoleInout)
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

        ShellAspectranService consoleAspectranService = new ShellAspectranService();
        consoleAspectranService.prepare(aspectranConfig);

        if (consoleInout == null) {
            consoleInout = new SystemConsoleInout();
        }

        ShellConfig consoleConfig = aspectranConfig.getConsoleConfig();
        if (consoleConfig != null) {
            String commandPrompt = consoleConfig.getString(ShellConfig.prompt);
            if (commandPrompt != null) {
                consoleInout.setCommandPrompt(commandPrompt);
            }
            consoleAspectranService.setConsoleInout(consoleInout);
            consoleAspectranService.setDescriptable(BooleanUtils.toBoolean(consoleConfig.getBoolean(ShellConfig.descriptable)));
            consoleAspectranService.setUsage(consoleConfig.getString(ShellConfig.usage));
            consoleAspectranService.setExposals(consoleConfig.getStringArray(ShellConfig.exposals));
        } else {
            consoleAspectranService.setConsoleInout(new SystemConsoleInout());
        }

        setServiceStateListener(consoleAspectranService);

        return consoleAspectranService;
    }

    private static void setServiceStateListener(final ShellAspectranService aspectranService) {
        aspectranService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                aspectranService.pauseTimeout = 0;
                aspectranService.printUsage();
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
                aspectranService.pauseTimeout = System.currentTimeMillis() + millis;
            }

            @Override
            public void paused() {
                aspectranService.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                aspectranService.pauseTimeout = 0;
            }

            @Override
            public void stopped() {
                paused();
            }
        });
    }

}
