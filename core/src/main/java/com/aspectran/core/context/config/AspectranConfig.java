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
package com.aspectran.core.context.config;

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.SystemUtils;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.VariableParameters;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The root configuration class for the Aspectran framework.
 * <p>This class acts as a container for all modular configuration sections.
 * It serves as the main entry point for loading configuration from a file or
 * other sources. The main configuration sections are:
 * <ul>
 *   <li>{@code system}: Configures system-level properties.</li>
 *   <li>{@code context}: The core and mandatory configuration for the ActivityContext.</li>
 *   <li>{@code scheduler}: Configures the built-in job scheduler.</li>
 *   <li>{@code web}: Web-specific settings for running as a web application.</li>
 *   <li>{@code daemon}: Settings for running as a non-interactive background service.</li>
 *   <li>{@code shell}: Settings for running as an interactive command-line interface.</li>
 *   <li>{@code embed}: Settings for running in an embedded mode within another application.</li>
 * </ul>
 */
public class AspectranConfig extends DefaultParameters {

    /** The name of the system property that specifies the base path. */
    public static final String BASE_PATH_PROPERTY_NAME = "aspectran.basePath";

    /** The name of the system property that specifies the temporary path. */
    public static final String TEMP_PATH_PROPERTY_NAME = "aspectran.tempPath";

    /** The name of the system property that specifies the working path. */
    public static final String WORK_PATH_PROPERTY_NAME = "aspectran.workPath";

    /** The default name of the Aspectran configuration file. */
    public static final String DEFAULT_ASPECTRAN_CONFIG_FILE = "aspectran-config.apon";

    private static final ParameterKey system;
    private static final ParameterKey context;
    private static final ParameterKey scheduler;
    private static final ParameterKey embed;
    private static final ParameterKey shell;
    private static final ParameterKey daemon;
    private static final ParameterKey web;

    private static final ParameterKey[] parameterKeys;

    static {
        system = new ParameterKey("system", SystemConfig.class);
        context = new ParameterKey("context", ContextConfig.class);
        scheduler = new ParameterKey("scheduler", SchedulerConfig.class);
        embed = new ParameterKey("embed", EmbedConfig.class);
        shell = new ParameterKey("shell", ShellConfig.class);
        daemon = new ParameterKey("daemon", DaemonConfig.class);
        web = new ParameterKey("web", WebConfig.class);

        parameterKeys = new ParameterKey[] {
                system,
                context,
                scheduler,
                embed,
                shell,
                daemon,
                web
        };
    }

    /**
     * Instantiates a new AspectranConfig.
     */
    public AspectranConfig() {
        super(parameterKeys);
    }

    /**
     * Instantiates a new AspectranConfig and reads the configuration from the given APON text.
     * @param apon the APON text
     * @throws AponParseException if the APON text is invalid
     */
    public AspectranConfig(String apon) throws AponParseException {
        this();
        readFrom(apon);
    }

    /**
     * Instantiates a new AspectranConfig and reads the configuration from the given parameters.
     * @param parameters the parameters
     * @throws AponParseException if the parameters are invalid
     */
    public AspectranConfig(VariableParameters parameters) throws AponParseException {
        this();
        readFrom(parameters);
    }

    /**
     * Instantiates a new AspectranConfig and reads the configuration from the given file.
     * @param configFile the configuration file
     * @throws AponParseException if the file content is invalid
     */
    public AspectranConfig(File configFile) throws AponParseException {
        this();
        readFrom(configFile);
    }

    /**
     * Instantiates a new AspectranConfig and reads the configuration from the given reader.
     * @param reader the reader
     * @throws AponParseException if the content is invalid
     */
    public AspectranConfig(Reader reader) throws AponParseException {
        this();
        readFrom(reader);
    }

    /**
     * Returns the system configuration section.
     * @return the {@code SystemConfig} instance, or {@code null} if not defined
     */
    public SystemConfig getSystemConfig() {
        return getParameters(system);
    }

    /**
     * Creates a new system configuration section.
     * @return the new {@code SystemConfig} instance
     */
    public SystemConfig newSystemConfig() {
        return attachParameters(system);
    }

    /**
     * Returns the existing system configuration section or creates a new one if it does not exist.
     * @return a non-null {@code SystemConfig} instance
     */
    public SystemConfig touchSystemConfig() {
        return touchParameters(system);
    }

    /**
     * Returns whether the system configuration section exists.
     * @return true if the system configuration section exists, otherwise false
     */
    public boolean hasSystemConfig() {
        return hasValue(system);
    }

    /**
     * Returns the core context configuration section.
     * @return the {@code ContextConfig} instance, or {@code null} if not defined
     */
    public ContextConfig getContextConfig() {
        return getParameters(context);
    }

    /**
     * Returns the existing core context configuration section or creates a new one if it does not exist.
     * @return a non-null {@code ContextConfig} instance
     */
    public ContextConfig touchContextConfig() {
        return touchParameters(context);
    }

    /**
     * Returns whether the core context configuration section exists.
     * @return true if the core context configuration section exists, otherwise false
     */
    public boolean hasContextConfig() {
        return hasValue(context);
    }

    /**
     * Returns the scheduler configuration section.
     * @return the {@code SchedulerConfig} instance, or {@code null} if not defined
     */
    public SchedulerConfig getSchedulerConfig() {
        return getParameters(scheduler);
    }

    /**
     * Returns the existing scheduler configuration section or creates a new one if it does not exist.
     * @return a non-null {@code SchedulerConfig} instance
     */
    public SchedulerConfig touchSchedulerConfig() {
        return touchParameters(scheduler);
    }

    /**
     * Returns whether the scheduler configuration section exists.
     * @return true if the scheduler configuration section exists, otherwise false
     */
    public boolean hasSchedulerConfig() {
        return hasValue(scheduler);
    }

    /**
     * Returns the embed configuration section.
     * @return the {@code EmbedConfig} instance, or {@code null} if not defined
     */
    public EmbedConfig getEmbedConfig() {
        return getParameters(embed);
    }

    /**
     * Returns the existing embed configuration section or creates a new one if it does not exist.
     * @return a non-null {@code EmbedConfig} instance
     */
    public EmbedConfig touchEmbedConfig() {
        return touchParameters(embed);
    }

    /**
     * Returns the shell configuration section.
     * @return the {@code ShellConfig} instance, or {@code null} if not defined
     */
    public ShellConfig getShellConfig() {
        return getParameters(shell);
    }

    /**
     * Sets the shell configuration section.
     * @param shellConfig the shell configuration
     */
    public void setShellConfig(ShellConfig shellConfig) {
        putValue(AspectranConfig.shell, shellConfig);
    }

    /**
     * Returns the existing shell configuration section or creates a new one if it does not exist.
     * @return a non-null {@code ShellConfig} instance
     */
    public ShellConfig touchShellConfig() {
        return touchParameters(shell);
    }

    /**
     * Returns the daemon configuration section.
     * @return the {@code DaemonConfig} instance, or {@code null} if not defined
     */
    public DaemonConfig getDaemonConfig() {
        return getParameters(daemon);
    }

    /**
     * Sets the daemon configuration section.
     * @param daemonConfig the daemon configuration
     */
    public void setDaemonConfig(DaemonConfig daemonConfig) {
        putValue(AspectranConfig.daemon, daemonConfig);
    }

    /**
     * Returns the existing daemon configuration section or creates a new one if it does not exist.
     * @return a non-null {@code DaemonConfig} instance
     */
    public DaemonConfig touchDaemonConfig() {
        return touchParameters(daemon);
    }

    /**
     * Returns whether the web configuration section exists.
     * @return true if the web configuration section exists, otherwise false
     */
    public boolean hasWebConfig() {
        return hasValue(web);
    }

    /**
     * Returns the web configuration section.
     * @return the {@code WebConfig} instance, or {@code null} if not defined
     */
    public WebConfig getWebConfig() {
        return getParameters(web);
    }

    /**
     * Returns the existing web configuration section or creates a new one if it does not exist.
     * @return a non-null {@code WebConfig} instance
     */
    public WebConfig touchWebConfig() {
        return touchParameters(web);
    }

    /**
     * Determines the base path from the given arguments or system properties.
     * @param args the command line arguments
     * @return the canonical base path, or {@code null} if not found
     */
    public static String determineBasePath(@Nullable String[] args) {
        String basePath;
        if (args == null || args.length < 2) {
            basePath = SystemUtils.getProperty(BASE_PATH_PROPERTY_NAME);
        } else {
            basePath = args[0];
        }
        if (StringUtils.hasText(basePath)) {
            try {
                basePath = new File(basePath).getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to determine the base path", e);
            }
        } else {
            basePath = null;
        }
        return basePath;
    }

    /**
     * Determines the Aspectran configuration file from the given arguments or system properties.
     * @param args the command line arguments
     * @return the canonical configuration file
     * @throws IllegalArgumentException if the configuration file cannot be determined
     */
    @NonNull
    public static File determineAspectranConfigFile(@Nullable String[] args) {
        File file;
        if (args == null || args.length == 0) {
            String basePath = SystemUtils.getProperty(BASE_PATH_PROPERTY_NAME);
            if (StringUtils.hasText(basePath)) {
                file = new File(basePath, DEFAULT_ASPECTRAN_CONFIG_FILE);
            } else {
                file = new File(DEFAULT_ASPECTRAN_CONFIG_FILE);
            }
            if (!file.exists()) {
                throw new IllegalArgumentException("No Aspectran Configuration file provided");
            }
        } else if (args.length == 1) {
            String baseDir = SystemUtils.getProperty(BASE_PATH_PROPERTY_NAME);
            if (baseDir != null) {
                Path basePath = Paths.get(baseDir);
                Path filePath = Paths.get(args[0]);
                if (filePath.startsWith(basePath) && filePath.isAbsolute()) {
                    file = filePath.toFile();
                } else {
                    file = new File(baseDir, args[0]);
                }
            } else {
                file = new File(args[0]);
            }
        } else {
            file = new File(args[0], args[1]);
        }
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to determine the aspectran config file", e);
        }
    }

}
