package com.aspectran.shell.command.builtins;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.Collection;

public class DescCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(DescCommand.class);

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "desc";

    public DescCommand(CommandRegistry registry) {
        super(registry);

        addOption(new Option("help", "Print this message"));
        addOption(new Option("on", "Turn on the ability to print a description of the command"));
        addOption(new Option("off", "Turn off the ability to print a description of the command"));
    }

    @Override
    public String execute(String[] args) throws Exception {
        CommandLine line = parse(args);

        if (line.hasOption("on")) {
            log.info("Description On");
            getService().setDescriptable(true);
        } else if (line.hasOption("off")) {
            log.info("Description Off");
            getService().setDescriptable(false);
        } else if (line.hasOption("help")) {
            printUsage();
        } else {
            printUsage();
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
            public Collection<Option> getOptions() {
                return options.getOptions();
            }

        };
    }

}
