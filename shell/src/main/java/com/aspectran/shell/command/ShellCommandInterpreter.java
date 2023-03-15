/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
import com.aspectran.core.lang.NonNull;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.shell.command.builtins.QuitCommand;
import com.aspectran.shell.command.option.OptionParserException;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.CommandReadFailedException;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.console.ConsoleTerminatedException;
import com.aspectran.shell.console.ConsoleWrapper;
import com.aspectran.shell.service.DefaultShellService;
import com.aspectran.shell.service.ShellService;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The Shell Command Interpreter.
 *
 * <p>Created: 2017. 6. 3.</p>
 */
public class ShellCommandInterpreter implements CommandInterpreter {

    private static final Logger logger = LoggerFactory.getLogger(ShellCommandInterpreter.class);

    private final Console console;

    private ShellCommandRegistry commandRegistry;

    private DefaultShellService service;

    private PrintStream originalSystemOut;

    private PrintStream originalSystemErr;

    public ShellCommandInterpreter(Console console) {
        if (console == null) {
            throw new IllegalArgumentException("console must not be null");
        }
        this.console = console;
    }

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
        return service;
    }

    public void init(@Nullable String basePath, File aspectranConfigFile) throws Exception {
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
        String commandPrompt = shellConfig.getPrompt();
        if (commandPrompt != null) {
            console.setCommandPrompt(commandPrompt);
        }

        if (aspectranConfig.hasContextConfig()) {
            service = DefaultShellService.create(aspectranConfig, console);
            service.start();
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

        File workingDir = determineWorkingDir(basePath, shellConfig.getWorkingDir());
        console.setWorkingDir(workingDir);

        String historyFile = shellConfig.getHistoryFile();
        if (!StringUtils.isEmpty(historyFile)) {
            if (basePath != null) {
                historyFile = new File(basePath, historyFile).getCanonicalPath();
            } else {
                historyFile = new File(historyFile).getCanonicalPath();
            }
            console.setCommandHistoryFile(historyFile);
        }

        console.setInterpreter(this);

        originalSystemOut = System.out;
        originalSystemErr = System.err;
        PrintStream out = new PrintStreamWrapper(originalSystemOut, console);
        PrintStream err = new PrintStreamWrapper(originalSystemErr, console);
        System.setOut(out);
        System.setErr(err);
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
                    } else if (service != null) {
                        TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
                        execute(transletCommandLine);
                    } else {
                        console.writeLine("No command mapped to '" + lineParser.getCommandName() + "'");
                    }
                } catch (ConsoleTerminatedException e) {
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
            if (service != null && service.getServiceController().isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Do not terminate this application while releasing all resources");
                }
            }
        }
    }

    /**
     * Executes a command built into Aspectran Shell.
     *
     * @param command an instance of the built-in command to be executed
     * @param lineParser the command line parser
     */
    private void execute(Command command, CommandLineParser lineParser) {
        ConsoleWrapper wrappedConsole = new ConsoleWrapper(console);
        PrintWriter outputWriter = null;
        try {
            ParsedOptions options = lineParser.parseOptions(command.getOptions());
            outputWriter = OutputRedirection.determineOutputWriter(lineParser.getRedirectionList(), wrappedConsole);
            wrappedConsole.setWriter(outputWriter);
            command.execute(options, wrappedConsole);
        } catch (ConsoleTerminatedException e) {
            throw e;
        } catch (OptionParserException e) {
            wrappedConsole.writeError(e.getMessage());
            command.printHelp(wrappedConsole);
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
     *
     * @param transletCommandLine the {@code TransletCommandLine} instance
     */
    private void execute(TransletCommandLine transletCommandLine) {
        if (transletCommandLine.getRequestName() != null) {
            try {
                service.translate(transletCommandLine, console);
            } catch (TransletNotFoundException e) {
                console.writeError("No command or translet mapped to '" + e.getTransletName() + "'");
            } catch (ConsoleTerminatedException e) {
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
        if (originalSystemOut != null) {
            System.setOut(originalSystemOut);
            originalSystemOut = null;
        }
        if (originalSystemErr != null) {
            System.setErr(originalSystemErr);
            originalSystemErr = null;
        }
        if (service != null) {
            service.stop();
            service = null;
        }
    }

    private File determineWorkingDir(String baseDir, String workingDir) {
        File dir = null;
        if (baseDir != null && workingDir != null) {
            Path basePath = Paths.get(baseDir);
            Path workingPath = Paths.get(workingDir);
            if (workingPath.startsWith(basePath) && workingPath.isAbsolute()) {
                dir = workingPath.toFile();
            } else {
                dir = new File(baseDir, workingDir);
            }
        } else if (workingDir != null) {
            dir = new File(workingDir);
        } else {
            String userDir = System.getProperty("user.dir");
            if (userDir != null) {
                dir = new File(userDir);
            }
        }
        if (dir != null) {
            dir.mkdirs();
        }
        return dir;
    }

    private class PrintStreamWrapper extends PrintStream {

        private final Console console;

        PrintStreamWrapper(PrintStream parent, Console console) {
            super(parent);
            this.console = console;
        }

        @Override
        public void write(int b) {
            write(new byte[] { (byte)b }, 0, 1);
        }

        @Override
        public void write(@NonNull byte[] buf, int off, int len) {
            if (service != null && service.isActive()) {
                console.clearLine();
            }
            try {
                console.write(new String(buf, off, len, console.getEncoding()));
            } catch (IOException e) {
                // ignore
            }
            if (service != null && service.isActive()) {
                console.redrawLine();
            }
        }

    }

}
