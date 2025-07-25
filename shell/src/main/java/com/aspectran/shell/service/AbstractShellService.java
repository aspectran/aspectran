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

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.config.AcceptableConfig;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.context.config.ShellConfig;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.core.service.DefaultCoreService;
import com.aspectran.core.service.RequestAcceptor;
import com.aspectran.shell.adapter.ShellSessionAdapter;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@code ShellService} implementations.
 *
 * <p>Created: 2017. 10. 30.</p>
 */
public abstract class AbstractShellService extends DefaultCoreService implements ShellService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractShellService.class);

    private final ShellConsole console;

    private DefaultSessionManager sessionManager;

    private SessionAgent sessionAgent;

    /** If verbose mode is on, a detailed description is printed each time the command is executed. */
    private boolean verbose;

    private String greetings;

    private Token[] greetingsTokens;

    AbstractShellService(ShellConsole console) {
        super();

        Assert.notNull(console, "console must not be null");
        this.console = console;
    }

    @Override
    public void afterContextLoaded() throws Exception {
        super.afterContextLoaded();
        parseGreetings();
    }

    @Override
    public ShellConsole getConsole() {
        return console;
    }

    /**
     * Tests if the verbose mode is enabled.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Returns a flag indicating whether to show the description or not.
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
     * @param verbose true to enable the verbose mode; false to disable
     */
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String getGreetings() {
        return greetings;
    }

    @Override
    public void setGreetings(String greetings) {
        this.greetings = greetings;
    }

    @Override
    public void printGreetings() {
        if (greetingsTokens != null) {
            TokenEvaluator evaluator = getDefaultActivity().getTokenEvaluator();
            String message = evaluator.evaluateAsString(greetingsTokens);
            if (console.isReading()) {
                console.clearLine();
            }
            console.writeLine(message);
        } else if (greetings != null) {
            if (console.isReading()) {
                console.clearLine();
            }
            console.writeLine(greetings);
        }
    }

    private void parseGreetings() {
        if (StringUtils.hasText(greetings)) {
            greetingsTokens = TokenParser.makeTokens(greetings, true);
            if (greetingsTokens != null) {
                try {
                    for (Token token : greetingsTokens) {
                        Token.resolveValueProvider(token, getServiceClassLoader());
                    }
                } catch (Exception e) {
                    greetingsTokens = null;
                    logger.error("Failed to parse greetings", e);
                }
            }
        }
    }

    @Override
    public void printHelp() {
        if (isVerbose()) {
            String description = getActivityContext().getDescription();
            if (description != null) {
                console.writeLine(description);
            }
        }
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        if (sessionAgent != null) {
            return new ShellSessionAdapter(sessionAgent);
        } else {
            return null;
        }
    }

    protected void createSessionManager() {
        Assert.state(this.sessionManager == null,
                "Session Manager is already exists for " + getServiceName());
        ShellConfig shellConfig = getAspectranConfig().getShellConfig();
        if (shellConfig != null) {
            SessionManagerConfig sessionManagerConfig = shellConfig.getSessionManagerConfig();
            if (sessionManagerConfig != null && sessionManagerConfig.isEnabled()) {
                try {
                    DefaultSessionManager sessionManager = new DefaultSessionManager();
                    sessionManager.setActivityContext(getActivityContext());
                    sessionManager.setSessionManagerConfig(sessionManagerConfig);
                    sessionManager.initialize();
                    this.sessionManager = sessionManager;
                    this.sessionAgent = new SessionAgent(sessionManager);
                } catch (Exception e) {
                    throw new CoreServiceException("Failed to create session manager for " + getServiceName(), e);
                }
            }
        }
    }

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

    @Override
    public void restart(String message) throws Exception {
        if (StringUtils.hasText(message)) {
            console.setStyle(console.getDangerStyle());
            console.writeAbove(message);
            console.resetStyle();
        }
        if (!isBusy() && console.confirmRestart()) {
            try {
                super.restart(message);
            } catch (Exception e) {
                logger.error("Shell restart failed", e);
                console.setStyle(console.getDangerStyle());
                console.writeAbove("Shell restart failed!");
                console.resetStyle();
            }
            if (console.isReading()) {
                console.redrawLine();
            }
        }
    }

    @Override
    public boolean isBusy() {
        return console.isReading();
    }

    @Override
    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        ShellConfig shellConfig = aspectranConfig.getShellConfig();
        if (shellConfig != null) {
            configure(shellConfig);
        }
        super.configure(aspectranConfig);
    }

    private void configure(@NonNull ShellConfig shellConfig) {
        setVerbose(shellConfig.isVerbose());
        setGreetings(shellConfig.getGreetings());
        AcceptableConfig acceptableConfig = shellConfig.getAcceptableConfig();
        if (acceptableConfig != null) {
            setRequestAcceptor(new RequestAcceptor(acceptableConfig));
        }
    }

}
