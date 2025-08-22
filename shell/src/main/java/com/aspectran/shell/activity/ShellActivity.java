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
package com.aspectran.shell.activity;

import com.aspectran.core.activity.ActivityPrepareException;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.MissingMandatoryAttributesException;
import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.shell.adapter.ShellRequestAdapter;
import com.aspectran.shell.adapter.ShellResponseAdapter;
import com.aspectran.shell.command.TransletPreProcedure;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.service.ShellService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.OutputStringWriter;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Writer;

/**
 * Activity implementation used by Aspectran Shell to execute a translet from the console.
 * <p>This class extends {@link CoreActivity} and adapts the core Aspectran processing
 * pipeline to an interactive command-line environment. It provides shell-specific
 * request and response adapters, handles procedural prompting for missing inputs,
 * and supports features like verbose output and output redirection.
 *
 * @since 2016-01-18
 */
public class ShellActivity extends CoreActivity {

    private final ShellService shellService;

    private final ShellConsole console;

    private boolean procedural;

    private boolean verbose;

    private String requestName;

    private MethodType requestMethod;

    private ParameterMap parameterMap;

    private Writer outputWriter;

    private boolean async;

    private Long timeout;

    /**
     * Instantiates a new ShellActivity.
     * @param shellService the {@link ShellService} instance
     */
    public ShellActivity(@NonNull ShellService shellService) {
        super(shellService.getActivityContext());

        this.shellService = shellService;
        this.console = shellService.getConsole();
    }

    @Override
    public Mode getMode() {
        return Mode.SHELL;
    }

    /**
     * Returns whether procedural prompting for missing inputs is enabled.
     * @return {@code true} if procedural prompting is enabled, {@code false} otherwise
     */
    public boolean isProcedural() {
        return procedural;
    }

    /**
     * Sets whether procedural prompting for missing inputs should be enabled.
     * @param procedural {@code true} to enable, {@code false} to disable
     */
    public void setProcedural(boolean procedural) {
        this.procedural = procedural;
    }

    /**
     * Returns whether verbose mode is enabled.
     * @return {@code true} if verbose mode is enabled, {@code false} otherwise
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets whether verbose mode is enabled.
     * @param verbose {@code true} to enable, {@code false} to disable
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Returns the name of the request being processed by this activity.
     * @return the request name
     */
    public String getRequestName() {
        return requestName;
    }

    /**
     * Sets the name of the request to be processed by this activity.
     * @param requestName the request name
     */
    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    /**
     * Returns the method type of the request being processed by this activity.
     * @return the request method type
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Sets the method type of the request to be processed by this activity.
     * @param requestMethod the request method type
     */
    public void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Returns a human-readable representation of the request, combining
     * method and name when both are available (e.g., "GET foo/bar").
     * @return the combined method and request name, or just the name if no method is set
     */
    public String getFullRequestName() {
        if (requestMethod != null && requestName != null) {
            return requestMethod + " " + requestName;
        } else if (requestName != null) {
            return requestName;
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns the parameter map associated with this activity.
     * @return the parameter map
     */
    public ParameterMap getParameterMap() {
        return parameterMap;
    }

    /**
     * Sets the parameter map for this activity.
     * @param parameterMap the parameter map
     */
    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    /**
     * Returns the output writer for this activity.
     * @return the output writer
     */
    public Writer getOutputWriter() {
        return outputWriter;
    }

    /**
     * Sets the output writer for this activity.
     * @param outputWriter the output writer
     */
    public void setOutputWriter(Writer outputWriter) {
        this.outputWriter = outputWriter;
    }

    /**
     * Returns whether this activity is configured for asynchronous execution.
     * @return {@code true} if asynchronous execution is enabled, {@code false} otherwise
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Returns the timeout for asynchronous execution in milliseconds.
     * @return the timeout in milliseconds, or {@code null} if no timeout is set
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * Prepares this activity to execute the target translet.
     * <p>Both {@link #requestName} and {@link #requestMethod} must be set prior to calling.</p>
     * @throws TransletNotFoundException if no translet is mapped to the requested name
     * @throws ActivityPrepareException if preparation fails
     */
    public void prepare() throws TransletNotFoundException, ActivityPrepareException {
        Assert.state(requestName != null, "requestName is not set");
        Assert.state(requestMethod != null, "requestMethod is not set");
        prepare(requestName, requestMethod);
    }

    @Override
    protected void prepare(String requestName, MethodType requestMethod, @NonNull TransletRule transletRule)
            throws ActivityPrepareException {
        this.async = transletRule.isAsync();
        this.timeout = transletRule.getTimeout();
        super.prepare(requestName, requestMethod, transletRule);
    }

    /**
     * Performs shell-specific pre-procedure steps: adapts the request/response and parses input.
     * @throws AdapterException if adapters cannot be initialized
     * @throws RequestParseException if parsing fails
     * @throws ActivityTerminatedException if the activity is terminated during validation
     */
    public void preProcedure() throws AdapterException, RequestParseException, ActivityTerminatedException {
        adapt();
        parseRequest();
    }

    /**
     * Sets up shell-specific adapters and delegates to the base {@link CoreActivity}.
     * Initializes session adapter, request/response adapters, and configures locale and flash maps.
     * @throws AdapterException if any adapter fails to initialize
     */
    @Override
    protected void adapt() throws AdapterException {
        try {
            setSessionAdapter(shellService.newSessionAdapter());

            ShellRequestAdapter requestAdapter = new ShellRequestAdapter(getTranslet().getRequestMethod());
            requestAdapter.setEncoding(console.getEncoding());
            if (getParameterMap() != null) {
                requestAdapter.setParameterMap(getParameterMap());
            }
            setRequestAdapter(requestAdapter);

            Writer outputWriter = (getOutputWriter() != null ? getOutputWriter() : new OutputStringWriter());
            ShellResponseAdapter responseAdapter = new ShellResponseAdapter(console, outputWriter);
            responseAdapter.setEncoding(console.getEncoding());
            setResponseAdapter(responseAdapter);
        } catch (Exception e) {
            throw new AdapterException("Failed to adapt for the shell activity", e);
        }

        setFlashMapManager(shellService.getFlashMapManager());
        setLocaleResolver(shellService.getLocaleResolver());

        super.adapt();
    }

    /**
     * Parses the request from the console context, prompting for missing values when allowed.
     * Prints helpful messages for missing mandatory inputs and terminates the activity gracefully.
     * @throws RequestParseException if parsing fails
     * @throws ActivityTerminatedException if the activity is terminated due to validation errors
     */
    @Override
    protected void parseRequest() throws RequestParseException, ActivityTerminatedException {
        TransletPreProcedure procedure = new TransletPreProcedure(
                console, getTransletRule(), getParameterMap(), isProcedural());
        procedure.printDescription(this);
        try {
            procedure.proceed();
            super.parseRequest();
        } catch (MissingMandatoryParametersException e) {
            procedure.printSomeMandatoryParametersMissing(e.getItemRules());
            terminate("Some mandatory parameters are missing");
        } catch (MissingMandatoryAttributesException e) {
            procedure.printSomeMandatoryAttributesMissing(e.getItemRules());
            terminate("Some mandatory attributes are missing");
        }
    }

    @Override
    protected void saveCurrentActivity() {
        super.saveCurrentActivity();

        if (isOriginalActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().access();
        }
    }

    @Override
    protected void removeCurrentActivity() {
        if (isOriginalActivity() && hasSessionAdapter() &&
                getSessionAdapter() instanceof DefaultSessionAdapter sessionAdapter) {
            sessionAdapter.getSessionAgent().complete();
        }

        super.removeCurrentActivity();
    }

}
