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
 * An activity that processes a shell command.
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

    public void preProcedure() throws AdapterException, RequestParseException, ActivityTerminatedException {
        adapt();
        parseRequest();
    }

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
