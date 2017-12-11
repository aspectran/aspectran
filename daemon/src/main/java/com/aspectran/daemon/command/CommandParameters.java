package com.aspectran.daemon.command;

import com.aspectran.core.context.rule.params.ItemHolderParameters;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public class CommandParameters extends AbstractParameters {

    public static final ParameterDefinition command;
    public static final ParameterDefinition bean;
    public static final ParameterDefinition method;
    public static final ParameterDefinition arguments;
    public static final ParameterDefinition translet;
    public static final ParameterDefinition template;
    public static final ParameterDefinition parameters;
    public static final ParameterDefinition attributes;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        command = new ParameterDefinition("command", ParameterValueType.STRING);
        bean = new ParameterDefinition("bean", ParameterValueType.STRING);
        method = new ParameterDefinition("method", ParameterValueType.STRING);
        arguments = new ParameterDefinition("arguments", ItemHolderParameters.class);
        translet = new ParameterDefinition("translet", ParameterValueType.STRING);
        template = new ParameterDefinition("template", ParameterValueType.STRING);
        parameters = new ParameterDefinition("parameters", ItemHolderParameters.class);
        attributes = new ParameterDefinition("attributes", ItemHolderParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                command,
                bean,
                method,
                arguments,
                translet,
                template,
                parameters,
                attributes
        };
    }

    public CommandParameters() {
        super(parameterDefinitions);
    }

    public CommandParameters(String text) {
        super(parameterDefinitions, text);
    }

}
