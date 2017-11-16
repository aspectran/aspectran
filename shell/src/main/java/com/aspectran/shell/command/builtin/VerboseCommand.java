package com.aspectran.shell.command.builtin;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;

import java.util.Collection;

public class VerboseCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(VerboseCommand.class);

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "verbose";

    private VerboseCommandDescriptor descriptor = new VerboseCommandDescriptor();

    public VerboseCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("h").longOpt("help").desc("Display this help").build());
        addOption(new Option("on", "Enable verbose output"));
        addOption(new Option("off", "Disable verbose output"));
    }

    @Override
    public String execute(String[] args) throws Exception {
        ParsedOptions options = parse(args);

        if (options.hasOption("on")) {
            getService().setVerbose(true);
            getConsole().writeLine("Enabled verbose mode");
        } else if (options.hasOption("off")) {
            getService().setVerbose(false);
            getConsole().writeLine("Disabled verbose mode");
        } else if (options.hasOption("help")) {
            printUsage();
        } else {
            printUsage();
        }

        return null;
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private class VerboseCommandDescriptor implements Descriptor {

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
            return "Turns verbose mode on or off";
        }

        @Override
        public String getUsage() {
            return "verbose [-h|--help] [-on] [-off]";
        }

        @Override
        public Collection<Option> getOptions() {
            return options.getOptions();
        }

    }

}
