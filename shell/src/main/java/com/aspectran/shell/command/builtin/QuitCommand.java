package com.aspectran.shell.command.builtin;

import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.ConsoleTerminatedException;
import com.aspectran.shell.command.option.Option;

import java.util.Collection;

public class QuitCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "quit";

    private QuitCommandDescriptor descriptor = new QuitCommandDescriptor();

    public QuitCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public String execute(String[] args) throws Exception {
        throw new ConsoleTerminatedException();
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private class QuitCommandDescriptor implements Descriptor {

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
            return "Releases all resources and Exits";
        }

        @Override
        public String getUsage() {
            return null;
        }

        @Override
        public Collection<Option> getOptions() {
            return null;
        }

    }

}
