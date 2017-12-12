package com.aspectran.core.context.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

/**
 * <p>Created: 2017. 12. 12.</p>
 *
 * @since 5.1.0
 */
public class DaemonConfig extends AbstractParameters {

    public static final ParameterDefinition commander;
    public static final ParameterDefinition commands;
    public static final ParameterDefinition pollingIntervalSeconds;
    public static final ParameterDefinition exposals;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        commander = new ParameterDefinition("commander", ParameterValueType.STRING);
        commands = new ParameterDefinition("commands", ParameterValueType.STRING, true);
        pollingIntervalSeconds = new ParameterDefinition("pollingIntervalSeconds", ParameterValueType.INT);
        exposals = new ParameterDefinition("exposals", ExposalsConfig.class);

        parameterDefinitions = new ParameterDefinition[] {
                commander,
                commands,
                pollingIntervalSeconds,
                exposals
        };
    }

    public DaemonConfig() {
        super(parameterDefinitions);
    }

}
