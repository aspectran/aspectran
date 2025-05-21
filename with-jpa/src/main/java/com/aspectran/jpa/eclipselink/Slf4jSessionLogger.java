/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.jpa.eclipselink;

import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This is a wrapper class for SLF4J. It is used when messages need to be logged
 * through SLF4J.
 * </p>
 * <p>
 * Use the following configuration for using SLF4J with EclipseLink
 * <code>eclipselink.logging.logger</code> and the value
 * <code>org.eclipse.persistence.logging.Slf4jSessionLogger</code>
 * </p>
 * <p>
 * Use the following categories from EclipseLink
 * (eclipselink.logging.timestamp, eclipselink.logging.thread,
 * eclipselink.logging.session, eclipselink.logging.connection
 * y eclipselink.logging.parameters).
 * <p>
 * Logging categories available are:
 * <p>
 * <ul>
 * <li>org.eclipse.persistence.logging.default
 * <li>org.eclipse.persistence.logging.sql
 * <li>org.eclipse.persistence.logging.transaction
 * <li>org.eclipse.persistence.logging.event
 * <li>org.eclipse.persistence.logging.connection
 * <li>org.eclipse.persistence.logging.query
 * <li>org.eclipse.persistence.logging.cache
 * <li>org.eclipse.persistence.logging.propagation
 * <li>org.eclipse.persistence.logging.sequencing
 * <li>org.eclipse.persistence.logging.ejb
 * <li>org.eclipse.persistence.logging.ejb_or_metadata
 * <li>org.eclipse.persistence.logging.weaver
 * <li>org.eclipse.persistence.logging.properties
 * <li>org.eclipse.persistence.logging.server
 * </ul>
 * </p>
 * <p>
 * Mapping of Java Log Level to SLF4J Log Level:
 * </p>
 * <ul>
 * <li>ALL,FINER,FINEST -> TRACE
 * <li>FINE -> DEBUG
 * <li>CONFIG,INFO -> INFO
 * <li>WARNING -> WARN
 * <li>SEVERE -> ERROR
 * </ul>
 * </p>
 * <p>
 */
public class Slf4jSessionLogger extends AbstractSessionLog {

    public static final String ECLIPSELINK_NAMESPACE = "org.eclipse.persistence.logging";

    public static final String DEFAULT_CATEGORY = "default";

    public static final String DEFAULT_ECLIPSELINK_NAMESPACE = ECLIPSELINK_NAMESPACE + "." + DEFAULT_CATEGORY;

    private static final Map<Integer, LogLevel> MAP_LEVELS = new HashMap<>();

    private final Map<String, Logger> categoryLoggers = new HashMap<>();

    static {
        MAP_LEVELS.put(SessionLog.ALL, LogLevel.TRACE);
        MAP_LEVELS.put(SessionLog.FINEST, LogLevel.TRACE);
        MAP_LEVELS.put(SessionLog.FINER, LogLevel.TRACE);
        MAP_LEVELS.put(SessionLog.FINE, LogLevel.DEBUG);
        MAP_LEVELS.put(SessionLog.CONFIG, LogLevel.INFO);
        MAP_LEVELS.put(SessionLog.INFO, LogLevel.INFO);
        MAP_LEVELS.put(SessionLog.WARNING, LogLevel.WARN);
        MAP_LEVELS.put(SessionLog.SEVERE, LogLevel.ERROR);
    }

    public Slf4jSessionLogger() {
        super();
        createCategoryLoggers();
    }

    @Override
    public void log(@NonNull SessionLogEntry entry) {
        if (!shouldLog(entry.getLevel(), entry.getNameSpace())) {
            return;
        }
        StringBuilder message = getSupplementDetailStringBuilder(entry);
        if (message != null) {
            message.append(formatMessage(entry));
            Logger logger = getLogger(entry.getNameSpace());
            LogLevel logLevel = getLogLevel(entry.getLevel());
            switch (logLevel) {
                case TRACE -> logger.trace(message.toString());
                case DEBUG -> logger.debug(message.toString());
                case INFO -> logger.info(message.toString());
                case WARN -> logger.warn(message.toString());
                case ERROR -> logger.error(message.toString());
            }
        }
    }

    @Override
    public boolean shouldLog(int level, String category) {
        Logger logger = getLogger(category);
        LogLevel logLevel = getLogLevel(level);
        return switch (logLevel) {
            case TRACE -> logger.isTraceEnabled();
            case DEBUG -> logger.isDebugEnabled();
            case INFO -> logger.isInfoEnabled();
            case WARN -> logger.isWarnEnabled();
            case ERROR -> logger.isErrorEnabled();
            default -> true;
        };
    }

    @Override
    public boolean shouldLog(int level) {
        return shouldLog(level, "default");
    }

    /**
     * Return true if SQL logging should log visible bind parameters. If the
     * shouldDisplayData is not set, return false.
     */
    @Override
    public boolean shouldDisplayData() {
        return BooleanUtils.toBoolean(shouldDisplayData);
    }

    /**
     * Initialize loggers eagerly
     */
    private void createCategoryLoggers() {
        for (String category : SessionLog.loggerCatagories) {
            addLogger(category, ECLIPSELINK_NAMESPACE + "." + category);
        }
        // Logger default para cuando no hay categoría.
        addLogger(DEFAULT_CATEGORY, DEFAULT_ECLIPSELINK_NAMESPACE);
    }

    /**
     * INTERNAL: Add Logger to the categoryLoggers.
     */
    private void addLogger(String loggerCategory, String loggerNameSpace) {
        categoryLoggers.put(loggerCategory, LoggerFactory.getLogger(loggerNameSpace));
    }

    /**
     * INTERNAL: Return the Logger for the given category
     */
    private Logger getLogger(String category) {
        if (StringUtils.isEmpty(category) || !categoryLoggers.containsKey(category)) {
            category = DEFAULT_CATEGORY;
        }
        return categoryLoggers.get(category);
    }

    /**
     * Return the corresponding Slf4j Level for a given EclipseLink level.
     */
    private LogLevel getLogLevel(Integer level) {
        LogLevel logLevel = MAP_LEVELS.get(level);
        if (logLevel == null) {
            logLevel = LogLevel.OFF;
        }
        return logLevel;
    }

    protected StringBuilder getSupplementDetailStringBuilder(SessionLogEntry entry) {
        if (!shouldLogExceptionStackTrace() && entry.hasException()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        if (shouldPrintSession() && (entry.getSession() != null)) {
            builder.append(this.getSessionString(entry.getSession()));
            builder.append("--");
        }
        if (entry.hasException()) {
            builder.append(entry.getException());
        } else {
            if (shouldPrintConnection() && (entry.getConnection() != null)) {
                builder.append(this.getConnectionString(entry.getConnection()));
                builder.append("--");
            }
            if (entry.getSourceClassName() != null) {
                builder.append(entry.getSourceClassName());
                builder.append("--");
            }
            if (entry.getSourceMethodName() != null) {
                builder.append(entry.getSourceMethodName());
                builder.append("--");
            }
        }
        return builder;
    }

    /**
     * SLF4J log levels.
     */
    enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, OFF
    }

}
