/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.request.MissingMandatoryAttributesException;
import com.aspectran.core.activity.request.MissingMandatoryParametersException;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.DefaultSessionAdapter;
import com.aspectran.core.lang.NonNull;
import com.aspectran.core.util.OutputStringWriter;
import com.aspectran.shell.adapter.ShellRequestAdapter;
import com.aspectran.shell.adapter.ShellResponseAdapter;
import com.aspectran.shell.command.ShellTransletProcedure;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.service.ShellService;

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

    private ParameterMap parameterMap;

    private Writer outputWriter;

    /**
     * Instantiates a new ShellActivity.
     * @param shellService the {@code ShellService} instance
     */
    public ShellActivity(@NonNull ShellService shellService) {
        super(shellService.getActivityContext());

        this.shellService = shellService;
        this.console = shellService.getConsole();
    }

    public void setProcedural(boolean procedural) {
        this.procedural = procedural;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setParameterMap(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void setOutputWriter(Writer outputWriter) {
        this.outputWriter = outputWriter;
    }

    @Override
    protected void adapt() throws AdapterException {
        try {
            setSessionAdapter(shellService.newSessionAdapter());

            ShellRequestAdapter requestAdapter = new ShellRequestAdapter(getTranslet().getRequestMethod());
            requestAdapter.setEncoding(console.getEncoding());
            if (parameterMap != null) {
                requestAdapter.setParameterMap(parameterMap);
            }
            setRequestAdapter(requestAdapter);

            if (outputWriter == null) {
                outputWriter = new OutputStringWriter();
            }
            ShellResponseAdapter responseAdapter = new ShellResponseAdapter(outputWriter);
            responseAdapter.setEncoding(console.getEncoding());
            setResponseAdapter(responseAdapter);
        } catch (Exception e) {
            throw new AdapterException("Failed to adapt for Shell Activity", e);
        }

        if (getParentActivity() == null && getSessionAdapter() instanceof DefaultSessionAdapter) {
            ((DefaultSessionAdapter)getSessionAdapter()).getSessionAgent().access();
        }

        super.adapt();
    }

    @Override
    protected void parseRequest() throws RequestParseException, ActivityTerminatedException {
        if (getTransletRule().isAsync()) {
            super.parseRequest();
        } else {
            ShellTransletProcedure procedure = new ShellTransletProcedure(
                    shellService, getTransletRule(),
                    getRequestAdapter().getParameterMap(),
                    procedural, verbose);
            procedure.printDescription(getTranslet());
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
    }

    @Override
    protected void release() {
        if (!hasParentActivity() && getSessionAdapter() instanceof DefaultSessionAdapter) {
            ((DefaultSessionAdapter)getSessionAdapter()).getSessionAgent().complete();
        }

        super.release();
    }

}
