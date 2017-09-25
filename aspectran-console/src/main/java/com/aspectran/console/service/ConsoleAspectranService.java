/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.console.service;

import com.aspectran.console.activity.ConsoleActivity;
import com.aspectran.console.adapter.ConsoleApplicationAdapter;
import com.aspectran.console.adapter.ConsoleSessionAdapter;
import com.aspectran.console.inout.ConsoleInout;
import com.aspectran.console.inout.SystemConsoleInout;
import com.aspectran.console.inout.jline.Jline3ConsoleInout;
import com.aspectran.console.service.command.CommandParser;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.component.translet.TransletNotFoundException;
import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.context.builder.config.AspectranConsoleConfig;
import com.aspectran.core.context.builder.config.AspectranContextConfig;
import com.aspectran.core.context.builder.config.AspectranSessionConfig;
import com.aspectran.core.context.builder.resource.AspectranClassLoader;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.BasicAspectranService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * The Class ConsoleAspectranService.
 *
 * @since 2016. 1. 18.
 */
public class ConsoleAspectranService extends BasicAspectranService {

    private static final Log log = LogFactory.getLog(ConsoleAspectranService.class);

    private static final String DEFAULT_ROOT_CONTEXT = "config/root-config.xml";

    private SessionManager sessionManager;

    private long pauseTimeout = -1L;

    private ConsoleInout consoleInout;

    private boolean showDescription;

    private ConsoleAspectranService() throws IOException {
        super(new ConsoleApplicationAdapter());
    }

    @Override
    public void afterContextLoaded() throws Exception {
        sessionManager = new DefaultSessionManager(getActivityContext());
        sessionManager.setGroupName("CON");

        AspectranSessionConfig sessionConfig = getAspectranConfig().getParameters(AspectranConfig.session);
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
        return new ConsoleSessionAdapter(agent);
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
    public boolean isShowDescription() {
        return showDescription;
    }

    /**
     * Sets a flag indicating whether to show the description or not.
     *
     * @param showDescription true if description should be shown
     */
    public void setShowDescription(boolean showDescription) {
        this.showDescription = showDescription;
    }

    public void showDescription() {
        showDescription(false);
    }

    public void showDescription(boolean force) {
        if (force || isShowDescription()) {
            if (getActivityContext().getDescription() != null) {
                consoleInout.writeLine(getActivityContext().getDescription());
                consoleInout.flush();
            }
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
            activity = new ConsoleActivity(this, redirectionWriters);
            activity.prepare(commandParser.getTransletName(), commandParser.getRequestMethod());
            activity.perform();
        } catch (TransletNotFoundException e) {
            log.info("Unregistered Translet: " + command);
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Translet did not complete and terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("An error occurred while processing an activity on the console service", e);
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
     * @return the console aspectran service
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    public static ConsoleAspectranService create(String aspectranConfigFile)
            throws AspectranServiceException, IOException {
        File file = ResourceUtils.getFile(aspectranConfigFile, AspectranClassLoader.getDefaultClassLoader());
        return create(file);
    }

    /**
     * Returns a new instance of ConsoleAspectranService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the console aspectran service
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    public static ConsoleAspectranService create(File aspectranConfigFile)
            throws AspectranServiceException, IOException {
        AspectranConfig aspectranConfig = new AspectranConfig();
        if (aspectranConfigFile != null) {
            AponReader.parse(aspectranConfigFile, aspectranConfig);
        }

        AspectranContextConfig contextConfig = aspectranConfig.touchAspectranContextConfig();
        String rootContext = contextConfig.getString(AspectranContextConfig.root);
        if (rootContext == null || rootContext.length() == 0) {
            contextConfig.putValue(AspectranContextConfig.root, DEFAULT_ROOT_CONTEXT);
        }

        ConsoleAspectranService consoleAspectranService = new ConsoleAspectranService();
        consoleAspectranService.prepare(aspectranConfig);

        AspectranConsoleConfig consoleConfig = aspectranConfig.getAspectranConsoleConfig();
        if (consoleConfig != null) {
            String consoleMode = consoleConfig.getString(AspectranConsoleConfig.mode);
            String commandPrompt = consoleConfig.getString(AspectranConsoleConfig.prompt);
            ConsoleInout consoleInout;
            if ("jline".equals(consoleMode)) {
                consoleInout = new Jline3ConsoleInout();
            } else {
                consoleInout = new SystemConsoleInout();
            }
            if (commandPrompt != null) {
                consoleInout.setCommandPrompt(commandPrompt);
            }
            consoleAspectranService.setConsoleInout(consoleInout);
            boolean showDescription = BooleanUtils.toBoolean(consoleConfig.getBoolean(AspectranConsoleConfig.showDescription));
            consoleAspectranService.setShowDescription(showDescription);
            consoleAspectranService.setExposals(consoleConfig.getStringArray(AspectranConsoleConfig.exposals));
        } else {
            consoleAspectranService.setConsoleInout(new SystemConsoleInout());
        }

        setServiceStateListener(consoleAspectranService);

        return consoleAspectranService;
    }

    private static void setServiceStateListener(final ConsoleAspectranService aspectranService) {
        aspectranService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                aspectranService.pauseTimeout = 0;
                aspectranService.showDescription();
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
