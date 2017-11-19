package com.aspectran.shell.command;

import com.aspectran.shell.command.builtin.HelpCommand;
import com.aspectran.shell.command.builtin.VerboseCommand;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.console.DefaultConsole;
import org.junit.Test;

/**
 * <p>Created: 2017. 11. 19.</p>
 */
public class CommandTest {

    @Test
    public void testVerboseCommand() {
        Console console = new DefaultConsole();
        Command command = new VerboseCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
    }

    @Test
    public void testHelpCommand() {
        Console console = new DefaultConsole();
        Command command = new HelpCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
    }

}
