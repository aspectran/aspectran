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
package com.aspectran.shell.command.builtins;

import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * Displays current JVM runtime information.
 */
public class SysInfoCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "sysinfo";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public SysInfoCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("props")
                .longName("system-properties")
                .desc("Displays the JVM's system properties")
                .build());
        addOption(Option.builder("cp")
                .longName("class-path")
                .desc("Displays JVM classpath information")
                .build());
        addOption(Option.builder("mem")
                .longName("memory-usage")
                .desc("Displays memory information about current JVM")
                .build());
        addOption(Option.builder("gc")
                .longName("garbage-collection")
                .desc("Performs garbage collection")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());
    }

    @Override
    public void execute(ParsedOptions options, ShellConsole console) throws Exception {
        if (options.hasOption("help")) {
            printHelp(console);
        } else if (options.hasOption("props")) {
            printSysProperties(console);
        } else if (options.hasOption("cp")) {
            printClasspath(console);
        } else if (options.hasOption("mem")) {
            mem(false, console);
        } else if (options.hasOption("gc")) {
            mem(true, console);
        } else {
            printQuickHelp(console);
        }
    }

    private void printSysProperties(ShellConsole console) {
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            console.writeLine("%1$30s   %2$s", entry.getKey(), entry.getValue());
        }
    }

    private void printClasspath(ShellConsole console) {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        for (String line : StringUtils.split(bean.getClassPath(), File.pathSeparator)) {
            console.writeLine(line);
        }
    }

    /**
     * Displays memory usage.
     * @param gc true if performing garbage collection; false otherwise
     */
    private void mem(boolean gc, ShellConsole console) {
        long total = Runtime.getRuntime().totalMemory();
        long before = Runtime.getRuntime().freeMemory();

        console.writeLine("%-24s %12s", "Total memory", StringUtils.convertToHumanFriendlyByteSize(total));
        console.writeLine("%-24s %12s", "Used memory", StringUtils.convertToHumanFriendlyByteSize(total - before));

        if (gc) {
            // Let the finalizer finish its work and remove objects from its queue
            System.gc(); // asynchronous garbage collector might already run
            System.gc(); // to make sure it does a full gc call it twice
            System.runFinalization();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }

            long after = Runtime.getRuntime().freeMemory();

            console.writeLine("%-24s %12s", "Free memory before GC", StringUtils.convertToHumanFriendlyByteSize(before));
            console.writeLine("%-24s %12s", "Free memory after GC", StringUtils.convertToHumanFriendlyByteSize(after));
            console.writeLine("%-24s %12s", "Memory gained with GC", StringUtils.convertToHumanFriendlyByteSize(after - before));
        } else {
            console.writeLine("%-24s %12s", "Free memory", StringUtils.convertToHumanFriendlyByteSize(before));
        }
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private static class CommandDescriptor implements Descriptor {

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
            return "Displays current JVM runtime information";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
