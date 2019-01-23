package com.aspectran.shell.command;

import com.aspectran.shell.console.Console;
import com.aspectran.shell.console.DefaultConsole;
import com.aspectran.shell.service.ShellService;

public class TestCommandInterpreter implements CommandInterpreter {

    private final Console console = new DefaultConsole();

    private final CommandRegistry commandRegistry = new ShellCommandRegistry(this);

    @Override
    public Console getConsole() {
        return console;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public ShellService getService() {
        return null;
    }

}
