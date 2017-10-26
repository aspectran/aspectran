package com.aspectran.shell.command;

import com.aspectran.shell.service.ShellAspectranService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public abstract class AbstractCommand implements Command {

    private final CommandRegistry registry;

    protected final Options options = new Options();

    public AbstractCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    public ShellAspectranService getService() {
        return registry.getService();
    }

    protected void addOption(Option option) {
        options.addOption(option);
    }

    protected CommandLine parse(String[] args) throws ParseException {
        return registry.getParser().parse(options, args);
    }

    protected void printUsage() {
    }

}
