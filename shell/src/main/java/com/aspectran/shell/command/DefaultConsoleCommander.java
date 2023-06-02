/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ShellConfig;
import com.aspectran.core.context.config.ShellStyleConfig;
import com.aspectran.core.lang.NonNull;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.SystemUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.shell.command.builtins.QuitCommand;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.CommandReadFailedException;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.console.ShellConsoleClosedException;
import com.aspectran.shell.console.ShellConsoleWrapper;
import com.aspectran.shell.service.DefaultShellService;
import com.aspectran.shell.service.ShellService;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public ShellConsole getConsole() {
        return console;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public ShellService getShellService() {
        return shellService;
    }

    public void prepare(@Nullable String basePath, File aspectranConfigFile) throws Exception {
        AspectranConfig aspectranConfig = new AspectranConfig();
        try {
            AponReader.parse(aspectranConfigFile, aspectranConfig);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse aspectran config file: " +
                    aspectranConfigFile, e);
        }
        if (basePath != null) {
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

        if (aspectranConfig.hasContextConfig()) {
            shellService = DefaultShellService.create(aspectranConfig, console);
            shellService.start();
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

        File workingDir = determineWorkingDir();
        if (workingDir != null) {
            console.setWorkingDir(workingDir);
        }

        String historyFile = shellConfig.getHistoryFile();
        if (!StringUtils.isEmpty(historyFile)) {
            if (basePath != null) {
                historyFile = new File(basePath, historyFile).getCanonicalPath();
            } else {
                historyFile = new File(historyFile).getCanonicalPath();
            }
            console.setCommandHistoryFile(historyFile);
        }

        console.setCommandRunner(this);
    }

    public void perform() {
        try {
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
                    break;
                } catch (CommandReadFailedException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Command read failed", e.getCause());
                    }
                } catch (Throwable e) {
                    logger.error("Error executing shell command", e);
                }
            }
        } finally {
            if (logger.isDebugEnabled()) {
                if (shellService != null && shellService.getServiceController().isActive()) {
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
            console.setStyle(console.getDangerStyle());
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
            shellService.stop();
            shellService = null;
        }
    }

    private File determineWorkingDir() {
        String workPath = SystemUtils.getProperty(WORK_PATH_PROPERTY_NAME);
        if (workPath != null) {
            return new File(workPath);
        }
        String userHome = SystemUtils.getUserHome();
        if (userHome != null) {
            return new File(userHome);
        }
        String userDir = SystemUtils.getUserDir();
        if (userDir != null) {
            return new File(userDir);
        }
        return null;
    }

}
