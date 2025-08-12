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
 * <p>
 * A {@code ShellActivity} is created per shell invocation and wired with shell-specific
 * adapters:
 * <ul>
 *   <li>{@link com.aspectran.shell.adapter.ShellRequestAdapter} to provide request
 *   parameters gathered from the command line</li>
 *   <li>{@link com.aspectran.shell.adapter.ShellResponseAdapter} to render output to the
 *   interactive console (or a redirected {@link java.io.Writer})</li>
 * </ul>
 * It also honours shell features such as procedural prompting for missing inputs, verbose
 * description printing, and async/timeout hints taken from the target {@link TransletRule}.
 * </p>
 *
 * @since 2016. 1. 18.
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
     * @param shellService the {@code ShellService} instance
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

    public boolean isProcedural() {
        return procedural;
    }

    public void setProcedural(boolean procedural) {
        this.procedural = procedural;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public MethodType getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Returns the request name prefixed with the HTTP-like method when available.
     * For example, {@code GET /path}.
     * @return the combined method and request name, or an empty string if none
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

    public ParameterMap getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public Writer getOutputWriter() {
        return outputWriter;
    }

    public void setOutputWriter(Writer outputWriter) {
        this.outputWriter = outputWriter;
    }

    public boolean isAsync() {
        return async;
    }

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
