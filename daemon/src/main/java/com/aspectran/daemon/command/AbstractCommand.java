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
package com.aspectran.daemon.command;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.asel.item.ItemEvaluator;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.daemon.service.DaemonService;
import com.aspectran.utils.ExceptionUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Base support class for daemon commands.
 * <p>
 * Implementations receive a {@link CommandRegistry} to access the {@link com.aspectran.daemon.Daemon}
 * and its services. The {@linkplain #isIsolated() isolated} flag indicates that a command should
 * be executed in an isolated activity context when necessary.</p>
 * <p>
 * This class also provides convenience logging helpers (debug/info/warn/error) that return the
 * logged message for easy composition, and factory methods to build {@link CommandResult} instances.</p>
 */
public abstract class AbstractCommand implements Command {

    /** Logger available to subclasses */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommandRegistry registry;

    private final boolean isolated;

    public AbstractCommand(CommandRegistry registry) {
        this(registry, false);
    }

    public AbstractCommand(CommandRegistry registry, boolean isolated) {
        this.registry = registry;
        this.isolated = isolated;
    }

    /**
     * Returns the registry that owns and executes this command.
     * @return the command registry
     */
    public CommandRegistry getCommandRegistry() {
        return registry;
    }

    /**
     * Whether this command should run in an isolated activity context.
     * @return {@code true} if isolated; {@code false} otherwise
     */
    @Override
    public boolean isIsolated() {
        return isolated;
    }

    /**
     * Returns the active daemon service.
     * @return the {@link DaemonService}
     * @throws IllegalStateException if the service is not available or not active
     */
    public DaemonService getDaemonService() {
        DaemonService daemonService = registry.getDaemonService();
        if (daemonService == null || !daemonService.getServiceLifeCycle().isActive()) {
            throw new IllegalStateException("SERVICE NOT AVAILABLE");
        }
        return daemonService;
    }

    /**
     * Returns whether the {@link DaemonService} is currently available and active.
     * @return {@code true} if the service exists and its lifecycle is active
     */
    public boolean isServiceAvailable() {
        return (registry.getDaemonService() != null &&
                registry.getDaemonService().getServiceLifeCycle().isActive());
    }

    /**
     * Returns an available {@link Activity} from the active daemon service.
     * @return the available activity
     */
    protected Activity getAvailableActivity() {
        return getDaemonService().getAvailableActivity();
    }

    /**
     * Evaluates and returns the request parameters from the given {@link CommandParameters}.
     * @param parameters the command parameters
     * @return the evaluated parameter map, or {@code null} if no parameters are specified
     * @throws IllegalRuleException if an error occurs while evaluating the parameter rules
     */
    protected ParameterMap getParameterMap(CommandParameters parameters) throws IllegalRuleException {
        if (parameters == null) {
            return null;
        }
        ItemRuleMap parameterItemRuleMap = parameters.getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = getAvailableActivity().getItemEvaluator();
            return evaluator.evaluateAsParameterMap(parameterItemRuleMap);
        } else {
            return null;
        }
    }

    /**
     * Evaluates and returns the arguments from the given {@link CommandParameters}.
     * @param parameters the command parameters
     * @return an array of evaluated argument values, or {@code null} if no arguments are specified
     * @throws IllegalRuleException if an error occurs while evaluating the argument rules
     */
    protected Object[] getArguments(CommandParameters parameters) throws IllegalRuleException {
        if (parameters == null) {
            return null;
        }
        ItemRuleList itemRuleList = parameters.getArgumentItemRuleList();
        if (itemRuleList != null && !itemRuleList.isEmpty()) {
            ItemEvaluator evaluator = getAvailableActivity().getItemEvaluator();
            Object[] args = new Object[itemRuleList.size()];
            for (int i = 0; i < itemRuleList.size(); i++) {
                args[i] = evaluator.evaluate(itemRuleList.get(i));
            }
            return args;
        } else {
            return null;
        }
    }

    /**
     * Evaluates and returns the request attributes from the given {@link CommandParameters}.
     * @param parameters the command parameters
     * @return the evaluated attribute map, or {@code null} if no attributes are specified
     * @throws IllegalRuleException if an error occurs while evaluating the attribute rules
     */
    protected Map<String, Object> getAttributeMap(CommandParameters parameters) throws IllegalRuleException {
        if (parameters == null) {
            return null;
        }
        ItemRuleMap attributeItemRuleMap = parameters.getAttributeItemRuleMap();
        if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = getAvailableActivity().getItemEvaluator();
            return evaluator.evaluate(attributeItemRuleMap);
        } else {
            return null;
        }
    }

    /**
     * Logs a debug message and returns it for further composition.
     * @param message the message to log
     * @return the same message
     */
    protected String debug(String message) {
        logger.debug(message);
        return message;
    }

    /**
     * Logs an info message and returns it for further composition.
     * @param message the message to log
     * @return the same message
     */
    protected String info(String message) {
        logger.info(message);
        return message;
    }

    /**
     * Logs a warning message and returns it for further composition.
     * @param message the message to log
     * @return the same message
     */
    protected String warn(String message) {
        logger.warn(message);
        return message;
    }

    /**
     * Logs an error message and returns it for further composition.
     * @param message the message to log
     * @return the same message
     */
    protected String error(String message) {
        logger.error(message);
        return message;
    }

    /**
     * Creates a successful {@link CommandResult} with the given message.
     * @param message the success message
     * @return a successful result
     */
    protected CommandResult success(String message) {
        return new CommandResult(true, message);
    }

    /**
     * Creates a failed {@link CommandResult} with the given message.
     * @param message the error message
     * @return a failed result
     */
    protected CommandResult failed(String message) {
        return new CommandResult(false, message);
    }

    /**
     * Logs the throwable with the given message and returns a failed result whose
     * message is stored in the result and stack trace in the error.
     * @param message contextual message to log
     * @param throwable the cause
     * @return a failed result containing the message and stack trace
     */
    protected CommandResult failed(String message, Throwable throwable) {
        logger.error(message, throwable);
        return new CommandResult(false, "[FAILED] " + message,
                ExceptionUtils.getStackTrace(throwable));
    }

    /**
     * Logs the provided throwable and returns a failed result whose error
     * consists of the full stack trace.
     * @param throwable the cause
     * @return a failed result with the throwable's stack trace
     */
    protected CommandResult failed(@NonNull Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null) {
            message = throwable.toString();
        }
        logger.error(throwable.toString(), throwable);
        return new CommandResult(false, "[FAILED] " + message,
                ExceptionUtils.getStackTrace(throwable));
    }

}
