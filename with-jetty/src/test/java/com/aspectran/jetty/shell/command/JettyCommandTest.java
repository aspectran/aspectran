package com.aspectran.jetty.shell.command;

import com.aspectran.shell.command.CommandInterpreter;
import com.aspectran.shell.console.Console;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.aspectran.core.util.PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY;

/**
 * <p>Created: 2021/05/16</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JettyCommandTest {

    private final CommandInterpreter interpreter = new TestCommandInterpreter();

    private Console getConsole() {
        return interpreter.getConsole();
    }

    @BeforeAll
    void saveProperties() {
        // System default
        System.setProperty(ENCRYPTION_PASSWORD_KEY, "encryption-password-for-test");
    }

    @Test
    void testJettyCommand() {
        JettyCommand command = new JettyCommand(interpreter.getCommandRegistry());
        //getConsole().writeLine(command.getDescriptor().getDescription());
        command.printHelp(getConsole());
    }

}
