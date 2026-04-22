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
package com.aspectran.daemon.service;

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.context.config.AcceptableConfig;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.core.context.config.DaemonExecutorConfig;
import com.aspectran.core.context.config.DaemonPollingConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.core.service.DefaultCoreService;
import com.aspectran.core.service.RequestAcceptor;
import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.adapter.DaemonSessionAdapter;
import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.DaemonCommandRegistry;
import com.aspectran.daemon.command.builtins.QuitCommand;
import com.aspectran.daemon.command.polling.DefaultFileCommander;
import com.aspectran.daemon.command.polling.FileCommander;
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link DaemonService} implementations.
 *
 * <p>This class extends {@link DefaultCoreService} to provide specialized wiring for
 * daemon environments. It manages the core administrative components of a daemon,
 * including:
 * <ul>
 *   <li>{@link CommandExecutor}: Handles asynchronous execution of administrative commands.</li>
 *   <li>{@link FileCommander}: Monitors the filesystem for command request files (polling).</li>
 *   <li>{@link CommandRegistry}: Maintains the set of available daemon commands.</li>
 * </ul>
 *
 * <p>It also provides support for session management in a non-web context and handles
 * daemon-specific configuration defined in {@link DaemonConfig}.</p>
 *
 * <p>This class supports a "derived" mode, where it can act as a child service
 * to another {@link CoreService} (e.g., a WebService). In derived mode, it reuses
 * the parent's {@code ActivityContext} while providing additional daemon-specific
 * capabilities.</p>
 *
 * @since 5.1.0
 */
public abstract class AbstractDaemonService extends DefaultCoreService implements DaemonService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDaemonService.class);

    private Daemon daemon;

    private DefaultSessionManager sessionManager;

    private SessionAgent sessionAgent;

    private CommandExecutor commandExecutor;

    private FileCommander fileCommander;

    private CommandRegistry commandRegistry;

    private Thread pollingThread;

    private volatile boolean polling;

    /**
     * Instantiates a new AbstractDaemonService.
     */
    AbstractDaemonService() {
        super();
    }

    /**
     * Instantiates a new AbstractDaemonService with a parent service.
     * @param parentService the parent service
     * @param derived whether this service is derived from the parent
     */
    AbstractDaemonService(CoreService parentService, boolean derived) {
        super(parentService, derived);
    }

    @Override
    public Daemon getDaemon() {
        return daemon;
    }

    @Override
    public void setDaemon(Daemon daemon) {
        this.daemon = daemon;
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        if (sessionAgent != null) {
            return new DaemonSessionAdapter(sessionAgent);
        } else {
            return null;
        }
    }

    @Override
    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    @Override
    public FileCommander getFileCommander() {
        return fileCommander;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    /**
     * Creates and initializes a session manager for the daemon service.
     * The session manager is configured based on the {@link SessionManagerConfig}
     * within the daemon configuration.
     * @throws CoreServiceException if the session manager fails to initialize
     */
    protected void createSessionManager() {
        Assert.state(this.sessionManager == null,
                "Session Manager already exists for " + getServiceName());
        DaemonConfig daemonConfig = getAspectranConfig().getDaemonConfig();
        if (daemonConfig != null) {
            SessionManagerConfig sessionManagerConfig = daemonConfig.getSessionManagerConfig();
            if (sessionManagerConfig != null && sessionManagerConfig.isEnabled()) {
                try {
                    DefaultSessionManager sessionManager = new DefaultSessionManager();
                    sessionManager.setActivityContext(getActivityContext());
                    sessionManager.setSessionManagerConfig(sessionManagerConfig);
                    sessionManager.initialize();
                    this.sessionManager = sessionManager;
                    this.sessionAgent = new SessionAgent(sessionManager);
                } catch (Exception e) {
                    throw new CoreServiceException("Failed to create a session manager", e);
                }
            }
        }
    }

    /**
     * Destroys the session manager and releases its resources.
     */
    protected void destroySessionManager() {
        if (sessionAgent != null) {
            sessionAgent.invalidate();
            sessionAgent = null;
        }
        if (sessionManager != null) {
            sessionManager.destroy();
            sessionManager = null;
        }
    }

    /**
     * Configures the service using the provided {@link AspectranConfig}.
     * <p>
     * If the service is not derived, it first performs the standard core service
     * configuration. Then, it initializes the daemon-specific components
     * (executor, commander, registry) based on the {@link DaemonConfig}.
     * </p>
     * @param aspectranConfig the service configuration (never {@code null})
     */
    @Override
    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        if (!isDerived()) {
            super.configure(aspectranConfig);
        } else {
            setBasePath(getParentService().getBasePath());
        }

        DaemonConfig daemonConfig = aspectranConfig.touchDaemonConfig();
        configure(daemonConfig);

        try {
            DaemonExecutorConfig executorConfig = daemonConfig.touchExecutorConfig();
            this.commandExecutor = new CommandExecutor(this, executorConfig);

            DaemonPollingConfig pollingConfig = daemonConfig.touchPollingConfig();
            if (pollingConfig.isEnabled()) {
                this.fileCommander = new DefaultFileCommander(this, pollingConfig);
            }

            DaemonCommandRegistry commandRegistry = new DaemonCommandRegistry(this);
            commandRegistry.addCommand(daemonConfig.getCommands());
            if (commandRegistry.getCommand(QuitCommand.class) == null) {
                commandRegistry.addCommand(QuitCommand.class);
            }
            this.commandRegistry = commandRegistry;
        } catch (Exception e) {
            throw new CoreServiceException("Failed to initialize daemon components", e);
        }
    }

    /**
     * Applies {@link DaemonConfig}-specific settings, such as the {@link RequestAcceptor}
     * based on the daemon's acceptable request rules.
     * @param daemonConfig the daemon-specific configuration (never {@code null})
     */
    private void configure(@NonNull DaemonConfig daemonConfig) {
        AcceptableConfig acceptableConfig = daemonConfig.getAcceptableConfig();
        if (acceptableConfig != null) {
            setRequestAcceptor(new RequestAcceptor(acceptableConfig));
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (fileCommander != null) {
            fileCommander.requeue();
            polling = true;
            startPollingThread();
        }
    }

    @Override
    protected void doStop() {
        polling = false;
        stopPollingThread();
        if (commandExecutor != null) {
            commandExecutor.shutdown();
        }
        super.doStop();
    }

    /**
     * Starts a background thread to poll for command files if {@link FileCommander}
     * is active.
     */
    private void startPollingThread() {
        if (pollingThread == null) {
            Runnable runnable = () -> {
                logger.info("Polling thread for File Commander started");
                while (polling) {
                    try {
                        fileCommander.polling();
                        Thread.sleep(fileCommander.getPollingInterval());
                    } catch (InterruptedException ie) {
                        break;
                    } catch (Exception e) {
                        logger.error("Error occurred while polling for command files", e);
                    }
                }
                logger.info("Polling thread for File Commander finished");
            };
            pollingThread = new Thread(runnable, getServiceName() + "-polling");
            pollingThread.start();
        }
    }

    /**
     * Interrupts and waits for the polling thread to terminate.
     */
    private void stopPollingThread() {
        if (pollingThread != null) {
            pollingThread.interrupt();
            try {
                pollingThread.join(3000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            pollingThread = null;
        }
    }

}
