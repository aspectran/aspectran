package com.aspectran.shell.command.builtins;

import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.service.ShellAspectranService;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.LinkedHashMap;
import java.util.Map;

public class DescCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "desc";

    private static final Map<String, String> arguments;

    static {
        arguments = new LinkedHashMap<>();
        arguments.put("-on", "Turn on the ability to print a description of the command");
        arguments.put("-off", "Turn off the ability to print a description of the command");
    }

    public DescCommand(ShellAspectranService service) {
        super(service);
    }

    @Override
    public String execute(String[] args) throws Exception {
        DescOptions descOptions = new DescOptions();
        JCommander.newBuilder()
                .addObject(descOptions)
                .build()
                .parse(args);

        if (descOptions.on) {
            getService().setDescriptable(true);
        } else if (descOptions.off) {
            getService().setDescriptable(false);
        }

        return null;
    }

    @Override
    public Descriptor getDescriptor() {
        return new Descriptor() {

            @Override
            public String getNamespace() {
                return NAMESPACE;
            }

            @Override
            public String getName() {
                return COMMAND_NAME;
            }

            @Override
            public String getDescription() {
                return "Turn on or off the ability to print a description of the command";
            }

            @Override
            public String getUsage() {
                return "Type 'desc [-on] [-off]'";
            }

            @Override
            public Map<String, String> getArguments() {
                return arguments;
            }

        };
    }

    private class DescOptions {

        @Parameter(names = "-on", required = false, description = "Turn on the ability to print a description of the command")
        public boolean on = false;

        @Parameter(names = "-off", required = false, description = "Turn off the ability to print a description of the command")
        public boolean off = false;

    }

}
