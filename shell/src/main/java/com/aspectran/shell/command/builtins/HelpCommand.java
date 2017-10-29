package com.aspectran.shell.command.builtins;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;

import java.util.Collection;

public class HelpCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(HelpCommand.class);

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "verbose";

    public HelpCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("a").longOpt("all").desc("Prints all the available commands").build());
        addOption(new Option("on", "Enable verbose output"));
        addOption(new Option("off", "Disable verbose output"));
    }

    @Override
    public String execute(String[] args) throws Exception {
        ParsedOptions options = parse(args);

        if (options.hasOption("on")) {
            log.info("Enabled verbose mode");
            getService().setVerbose(true);
        } else if (options.hasOption("off")) {
            log.info("Disabled verbose mode");
            getService().setVerbose(false);
        } else if (options.hasOption("help")) {
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
                return "Display help information about Aspectran Shell";
            }

            @Override
            public String getUsage() {
                return "Type 'help [-h|--help] [-a|--all]'";
            }

            @Override
            public Collection<Option> getOptions() {
                return options.getOptions();
            }

        };
    }

}
