package com.aspectran.shell.command.builtin;

import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandLineParser;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;

import java.util.Collection;

public class TransletCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "translet";

    private CommandDescriptor descriptor = new CommandDescriptor();

    public TransletCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("l").longOpt("list").desc("Prints all Translets or those retrieved with the given name").build());
        addOption(Option.builder("h").longOpt("help").desc("Display this help").build());
    }

    @Override
    public String execute(String[] args) throws Exception {
        ParsedOptions options = parse(args);

        if (!options.hasOptions() && options.hasArgs()) {
            String commandLine = String.join(" ", options.getArgs());
            CommandLineParser parser = CommandLineParser.parseCommandLine(commandLine);
            getService().execute(parser);
            getConsole().writeLine();
        } else if (options.hasOption("l")) {
            String[] keywords = options.getArgs();
            listTranslets(keywords.length > 0 ? keywords : null);
        } else {
            printUsage();
        }

        return null;
    }

    public void listTranslets(String[] keywords) {
        TransletRuleRegistry transletRuleRegistry = getService().getActivityContext().getTransletRuleRegistry();
        Collection<TransletRule> transletRules = transletRuleRegistry.getTransletRules();

        for (TransletRule transletRule : transletRules) {
            String name = transletRule.getName();
            if (getService().isExposable(name)) {
                if (keywords != null) {
                    boolean exists = false;
                    for (String keyw : keywords) {
                        if (name.contains(keyw)) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        continue;
                    }
                }
                if (transletRule.getAllowedMethods() != null) {
                    getConsole().write("[");
                    for (int i = 0; i < transletRule.getAllowedMethods().length; i++) {
                        if (i > 0) {
                            getConsole().write(", ");
                        }
                        getConsole().write(transletRule.getAllowedMethods()[i].toString());
                    }
                    getConsole().writeLine("] ");
                }
                getConsole().writeLine(name);
            }
        }
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private class CommandDescriptor implements Descriptor {

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
            return "Translet run, or you can find them";
        }

        @Override
        public String getUsage() {
            return "translet [-l] [-s] [translet_name]";
        }

        @Override
        public Collection<Option> getOptions() {
            return options.getOptions();
        }

    }

}
