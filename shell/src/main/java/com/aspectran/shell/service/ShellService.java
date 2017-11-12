package com.aspectran.shell.service;

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.shell.command.CommandLineParser;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.console.Console;

import java.io.File;
import java.io.IOException;

/**
 * The Interface ShellService.
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface ShellService extends CoreService {

    SessionAdapter newSessionAdapter();

    Console getConsole();

    String[] getCommands();

    CommandRegistry getCommandRegistry();

    /**
     * Tests if the verbose mode is enabled.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Returns a flag indicating whether to show the description or not.
     *
     * @return true if the verbose mode is enabled
     */
    boolean isVerbose();

    /**
     * Enables or disables the verbose mode.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Sets a flag indicating whether to show the description or not.
     *
     * @param verbose true to enable the verbose mode; false to disable
     */
    void setVerbose(boolean verbose);

    String getGreetings();

    void setGreetings(String greetings);

    /**
     * Prints welcome message.
     */
    void printGreetings();

    /**
     * Prints help information.
     */
    void printHelp();

    /**
     * Process the actual dispatching to the activity.
     *
     * @param command the translet name mapped to the command
     */
    void serve(String command);

    /**
     * Process the actual dispatching to the activity.
     *
     * @param commandLineParser the command line parser
     */
    void serve(CommandLineParser commandLineParser);

    /**
     * Returns a new instance of ShellService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the instance of ShellService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    static ShellService create(String aspectranConfigFile)
            throws AspectranServiceException, IOException {
        return AspectranShellService.create(aspectranConfigFile);
    }

    /**
     * Returns a new instance of ShellService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @param console the console
     * @return the instance of ShellService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    static ShellService create(String aspectranConfigFile, Console console)
            throws AspectranServiceException, IOException {
        return AspectranShellService.create(aspectranConfigFile, console);
    }

    /**
     * Returns a new instance of ShellService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the instance of ShellService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
     static ShellService create(File aspectranConfigFile)
            throws AspectranServiceException, IOException {
        return AspectranShellService.create(aspectranConfigFile);
    }

    /**
     * Returns a new instance of ShellService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @param console the console
     * @return the instance of ShellService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    static ShellService create(File aspectranConfigFile, Console console)
            throws AspectranServiceException, IOException {
        return AspectranShellService.create(aspectranConfigFile, console);
    }

}
