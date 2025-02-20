/*
 * Copyright (c) 2008-2025 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.shell.command;

import com.aspectran.core.AboutMe;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ShellConfig;
import com.aspectran.core.context.config.ShellStyleConfig;
import com.aspectran.shell.command.builtins.QuitCommand;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.CommandReadFailedException;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.console.ShellConsoleClosedException;
import com.aspectran.shell.console.ShellConsoleWrapper;
import com.aspectran.shell.service.DefaultShellService;
import com.aspectran.shell.service.DefaultShellServiceBuilder;
import com.aspectran.shell.service.ShellService;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.SystemUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.AponParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOError;
import java.io.PrintWriter;

import static com.aspectran.core.context.config.AspectranConfig.WORK_PATH_PROPERTY_NAME;

/**
 * The Shell Command Runner.
 *
 * <p>Created: 2017. 6. 3.</p>
 */
public class DefaultConsoleCommander implements ConsoleCommander {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConsoleCommander.class);

    private final ShellConsole console;

    private ShellCommandRegistry commandRegistry;

    private DefaultShellService shellService;

    public DefaultConsoleCommander(@NonNull ShellConsole console) {
        this.console = console;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ShellConsole> T getConsole() {
        return (T)console;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public ShellService getShellService() {
        return shellService;
    }

    protected void consoleReady() {
        AboutMe.printPretty(console.getOutput());
        console.getOutput().flush();
    }

    public void configure(@Nullable String basePath, @NonNull File aspectranConfigFile) throws Exception {
        AspectranConfig aspectranConfig;
        try {
            aspectranConfig = new AspectranConfig(aspectranConfigFile);
        } catch (AponParseException e) {
            throw new IllegalArgumentException("Failed to parse aspectran config file: " +
                    aspectranConfigFile, e);
        }

        if (StringUtils.hasText(basePath)) {
            aspectranConfig.touchContextConfig().setBasePath(basePath);
        }

        ShellConfig shellConfig = aspectranConfig.touchShellConfig();
        ShellStyleConfig shellStyleConfig = shellConfig.getShellStyleConfig();
        if (shellStyleConfig != null) {
            console.setShellStyleConfig(shellStyleConfig);
        }
        String commandPrompt = shellConfig.getPrompt();
        if (commandPrompt != null) {
            console.setCommandPrompt(commandPrompt);
        }

        String historyFile = shellConfig.getHistoryFile();
        if (StringUtils.hasLength(historyFile)) {
            if (StringUtils.hasText(basePath)) {
                historyFile = new File(basePath, historyFile).getCanonicalPath();
            } else {
                historyFile = new File(historyFile).getCanonicalPath();
            }
            console.setCommandHistoryFile(historyFile);
        }

        consoleReady();

        if (aspectranConfig.hasContextConfig()) {
            shellService = DefaultShellServiceBuilder.build(aspectranConfig, console);
            shellService.start();
            File workingDir = determineWorkingDir();
            console.setWorkingDir(workingDir);
        } else {
            String greetings = shellConfig.getGreetings();
            if (StringUtils.hasText(greetings)) {
                console.writeLine(greetings);
            }
        }

        commandRegistry = new ShellCommandRegistry(this);
        commandRegistry.addCommand(shellConfig.getCommands());
        if (commandRegistry.getCommand(QuitCommand.class) == null) {
            commandRegistry.addCommand(QuitCommand.class);
        }
    }

    public void run() {
        try {
            console.setConsoleCommander(this);

            for (;;) {
                try {
                    String commandLine = console.readCommandLine();
                    if (!StringUtils.hasLength(commandLine)) {
                        continue;
                    }

                    CommandLineParser lineParser = new CommandLineParser(commandLine);
                    if (lineParser.getCommandName() == null) {
                        continue;
                    }

                    Command command = null;
                    if (commandRegistry != null) {
                        command = commandRegistry.getCommand(lineParser.getCommandName());
                    }
                    if (command != null) {
                        execute(command, lineParser);
                    } else if (shellService != null) {
                        TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
                        execute(transletCommandLine);
                    } else {
                        console.writeLine("No command mapped to '" + lineParser.getCommandName() + "'");
                    }
                } catch (ShellConsoleClosedException e) {
                    if (StringUtils.hasText(e.getMessage())) {
                        console.writeLine(e.getMessage());
                    }
                    break;
                } catch (CommandReadFailedException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Command read failed", e.getCause());
                    }
                } catch (IOError e) {
                    console.clearLine();
                    break;
                } catch (Throwable e) {
                    logger.error("Error executing shell command", e);
                }
            }
        } finally {
            console.setConsoleCommander(null);

            if (logger.isDebugEnabled()) {
                if (shellService != null && shellService.isActive()) {
                    logger.debug("Do not terminate this application while releasing all resources");
                }
            }
        }
    }

    /**
     * Executes a command built into Aspectran Shell.
     * @param command an instance of the built-in command to be executed
     * @param lineParser the command line parser
     */
    private void execute(Command command, CommandLineParser lineParser) {
        ShellConsoleWrapper wrappedConsole = new ShellConsoleWrapper(console);
        PrintWriter outputWriter = null;
        try {
            ParsedOptions options = lineParser.parseOptions(command.getOptions());
            outputWriter = OutputRedirection.determineOutputWriter(lineParser.getRedirectionList(), wrappedConsole);
            wrappedConsole.setWriter(outputWriter);
            command.execute(options, wrappedConsole);
        } catch (ShellConsoleClosedException e) {
            throw e;
        } catch (OptionParserException e) {
            wrappedConsole.writeError(e.getMessage());
            command.printHelp(wrappedConsole);
        } catch (ShellCommandExecutionException e) {
            logger.error("Failed to execute command: " + lineParser.getCommandLine(), e.getCause());
            console.dangerStyle();
            console.writeAbove(e.getMessage());
            console.resetStyle();
        } catch (Exception e) {
            logger.error("Failed to execute command: " + lineParser.getCommandLine(), e);
        } finally {
            if (outputWriter != null) {
                outputWriter.close();
            }
        }
    }

    /**
     * Executes a Translet defined in Aspectran.
     * @param transletCommandLine the {@code TransletCommandLine} instance
     */
    private void execute(TransletCommandLine transletCommandLine) {
        if (shellService == null) {
            throw new IllegalStateException("Shell service not available");
        }
        if (transletCommandLine.getRequestName() != null) {
            try {
                shellService.translate(transletCommandLine);
            } catch (TransletNotFoundException e) {
                console.writeError("No command or translet mapped to '" + e.getTransletName() + "'");
            } catch (ShellConsoleClosedException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Failed to execute command: " +
                        transletCommandLine.getLineParser().getCommandLine(), e);
            }
        } else {
            console.writeError("No command or translet mapped to '" +
                    transletCommandLine.getLineParser().getCommandLine() + "'");
        }
    }

    public void release() {
        if (shellService != null) {
            if (shellService.isActive()) {
                shellService.stop();
            }
            shellService.withdraw();
            shellService = null;
        }
    }

    @Nullable
    private File determineWorkingDir() {
        String workPath = SystemUtils.getProperty(WORK_PATH_PROPERTY_NAME);
        if (StringUtils.hasText(workPath)) {
            return new File(workPath);
        } else {
            return null;
        }
    }

}
