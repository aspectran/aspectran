package com.aspectran.shell.command.builtin;

import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;

import java.util.Collection;

public class ClearCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "clear";

    private PauseCommandDescriptor descriptor = new PauseCommandDescriptor();

    public ClearCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public String execute(String[] args) throws Exception {
        getConsole().clearScreen();
        return null;
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private class PauseCommandDescriptor implements Descriptor {

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
            return "Clear the screen";
        }

        @Override
        public String getUsage() {
            return "Type 'clear'";
        }

        @Override
        public Collection<Option> getOptions() {
            return null;
        }

    }

}
