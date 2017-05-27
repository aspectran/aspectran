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

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import com.aspectran.console.activity.ConsoleActivity;
import com.aspectran.console.adapter.ConsoleApplicationAdapter;
import com.aspectran.console.adapter.ConsoleSessionAdapter;
import com.aspectran.console.inout.ConsoleInout;
import com.aspectran.console.inout.ConsoleTerminatedException;
import com.aspectran.console.inout.jline.Jline3ConsoleInout;
import com.aspectran.console.inout.SystemConsoleInout;
import com.aspectran.console.service.command.CommandParser;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.context.builder.config.AspectranConsoleConfig;
import com.aspectran.core.context.builder.config.AspectranContextConfig;
import com.aspectran.core.context.builder.resource.AspectranClassLoader;
import com.aspectran.core.context.translet.TransletNotFoundException;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.AspectranServiceLifeCycleListener;
import com.aspectran.core.service.BasicAspectranService;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class ConsoleAspectranService.
 *
 * @since 2016. 1. 18.
 */
public class ConsoleAspectranService extends BasicAspectranService {

    private static final Log log = LogFactory.getLog(ConsoleAspectranService.class);

    private static final String DEFAULT_ROOT_CONTEXT = "config/aspectran-config.xml";

    private SessionAdapter sessionAdapter;

    private SessionScopeAdvisor sessionScopeAdvisor;

    private long pauseTimeout;

    private ConsoleInout consoleInout;

    private boolean showDescription;

    private ConsoleAspectranService() throws IOException {
        super(new ConsoleApplicationAdapter());
    }

    @Override
    public void afterContextLoaded() {
        sessionAdapter = new ConsoleSessionAdapter();
        sessionScopeAdvisor = SessionScopeAdvisor.newInstance(getActivityContext(), sessionAdapter);
        if (sessionScopeAdvisor != null) {
            sessionScopeAdvisor.executeBeforeAdvice();
        }
    }

    @Override
    public void beforeContextDestroy() {
        if (sessionScopeAdvisor != null) {
            sessionScopeAdvisor.executeAfterAdvice();
        }
        if (sessionAdapter != null) {
            Scope sessionScope = sessionAdapter.getSessionScope();
            sessionScope.destroy();
        }
    }

    public SessionAdapter getSessionAdapter() {
        return sessionAdapter;
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

    protected void showDescription() {
        showDescription(false);
    }

    protected void showDescription(boolean force) {
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
    protected void service(String command) {
        if (!isExposable(command)) {
            log.info("Unexposable translet name: " + command);
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
                log.warn("Invalid redirection: " + CommandParser.serialize(commandParser.getRedirectionList()), e);
                return;
            }
        }

        Activity activity = null;

        try {
            activity = new ConsoleActivity(this, redirectionWriters);
            activity.prepare(commandParser.getTransletName(), commandParser.getRequestMethod());
            activity.perform();
        } catch (TransletNotFoundException e) {
            log.info("Unknown translet name: " + command);
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Translet activity was terminated");
            }
        } catch (Exception e) {
            log.error("Console activity failed to perform", e);
        } finally {
            if (redirectionWriters != null) {
                for (Writer writer : redirectionWriters) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        log.error("Failed to close writer: " + e.getMessage(), e);
                    }
                }
            }
            if (activity != null) {
                activity.finish();
            }
        }
    }

    public void service() {
        try {
            loop:
            while (true) {
                String command = consoleInout.readCommand();

                if (command == null) {
                    continue;
                }
                command = command.trim();
                if (command.isEmpty()) {
                    continue;
                }

                switch (command) {
                    case "restart":
                        log.info("Restarting the Aspectran Service...");
                        restart();
                        break;
                    case "pause":
                        log.info("Pausing the Aspectran Service...");
                        pause();
                        break;
                    case "resume":
                        log.info("Resuming the Aspectran Service...");
                        resume();
                        break;
                    case "desc on":
                        log.info("Descripton On");
                        setShowDescription(true);
                        break;
                    case "desc off":
                        log.info("Descripton Off");
                        setShowDescription(false);
                        break;
                    case "help":
                        showDescription(true);
                        break ;
                    case "quit":
                        log.info("Goodbye");
                        break loop;
                    default:
                        service(command);
                        consoleInout.writeLine();
                }
            }
        } catch (ConsoleTerminatedException e) {
            // Do Nothing
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isActive()) {
                log.info("Do not terminate this application while destroying all scoped beans");
                shutdown();
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

        Parameters contextParameters = aspectranConfig.touchAspectranContextConfig();
        String rootContext = contextParameters.getString(AspectranContextConfig.root);
        if (rootContext == null || rootContext.length() == 0) {
            contextParameters.putValue(AspectranContextConfig.root, DEFAULT_ROOT_CONTEXT);
        }

        ConsoleAspectranService consoleAspectranService = new ConsoleAspectranService();
        consoleAspectranService.initialize(aspectranConfig);

        Parameters consoleParameters = aspectranConfig.getAspectranConsoleConfig();
        if (consoleParameters != null) {
            String consoleMode = consoleParameters.getString(AspectranConsoleConfig.mode);
            String commandPrompt = consoleParameters.getString(AspectranConsoleConfig.prompt);
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
            boolean showDescription = BooleanUtils.toBoolean(consoleParameters.getBoolean(AspectranConsoleConfig.showDescription));
            consoleAspectranService.setShowDescription(showDescription);
            consoleAspectranService.setExposals(consoleParameters.getStringArray(AspectranConsoleConfig.exposals));
        } else {
            consoleAspectranService.setConsoleInout(new SystemConsoleInout());
        }

        setAspectranServiceLifeCycleListener(consoleAspectranService);

        consoleAspectranService.startup();

        return consoleAspectranService;
    }

    private static void setAspectranServiceLifeCycleListener(final ConsoleAspectranService aspectranService) {
        aspectranService.setAspectranServiceLifeCycleListener(new AspectranServiceLifeCycleListener() {
            @Override
            public void started() {
                aspectranService.pauseTimeout = 0;
                aspectranService.showDescription();
            }

            @Override
            public void restarted(boolean hardReload) {
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
                started();
            }

            @Override
            public void stopped() {
                paused();
            }
        });
    }

}
