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
package com.aspectran.shell.command.builtins;

import com.aspectran.core.context.resource.SiblingClassLoader;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
                .desc("Displays JVM system properties")
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
    public void execute(@NonNull ParsedOptions options, ShellConsole console) throws Exception {
        if (options.hasOptions()) {
            Iterator<Option> iter = options.iterator();
            Set<Option> done = new HashSet<>();
            while (iter.hasNext()) {
                Option option = iter.next();
                String name = option.getName();
                if (!done.contains(option)) {
                    if (!done.isEmpty()) {
                        console.writeLine();
                    }
                    if ("h".equals(name)) {
                        printHelp(console);
                    } else if ("props".equals(name)) {
                        printSysProperties(console);
                    } else if ("cp".equals(name)) {
                        printClasspath(console);
                    } else if ("mem".equals(name)) {
                        mem(false, console);
                    } else if ("gc".equals(name)) {
                        mem(true, console);
                    }
                }
                done.add(option);
            }
        } else {
            printSysProperties(console);
            console.writeLine();
            mem(false, console);
            console.writeLine();
            console.writeLine("----------------------------------------------------------------");
            printHelp(console);
            console.writeLine("----------------------------------------------------------------");
        }
    }

    private void printSysProperties(@NonNull ShellConsole console) {
        console.writeLine("---------------------");
        console.writeLine("JVM system properties");
        console.writeLine("---------------------");
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            String value = entry.getValue() != null ? escape(entry.getValue().toString()) : null;
            console.writeLine("%s=%s", entry.getKey(), StringUtils.nullToEmpty(value));
        }
    }

    private void printClasspath(@NonNull ShellConsole console) {
        console.writeLine("-------------------------");
        console.writeLine("JVM classpath information");
        console.writeLine("-------------------------");
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        for (String line : StringUtils.split(bean.getClassPath(), File.pathSeparator)) {
            console.writeLine(line);
        }
        if (isServiceAvailable()) {
            ClassLoader classLoader = getShellService().getServiceClassLoader();
            if (classLoader instanceof SiblingClassLoader scl) {
                Enumeration<URL> urls = scl.getAllResources();
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    console.writeLine(url.toString());
                }
            }
        }
    }

    /**
     * Displays memory usage.
     * @param gc true if performing garbage collection; false otherwise
     */
    private void mem(boolean gc, @NonNull ShellConsole console) {
        long max = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();

        console.writeLine("------------------------------------");
        console.writeLine("Memory information about current JVM");
        console.writeLine("------------------------------------");
        console.writeLine("%-23s %12s", "Available memory", StringUtils.toHumanFriendlyByteSize(max));
        console.writeLine("%-23s %12s", "Total memory", StringUtils.toHumanFriendlyByteSize(total));
        console.writeLine("%-23s %12s", "Used memory", StringUtils.toHumanFriendlyByteSize(total - free));
        console.writeLine("%-23s %12s", "Free memory before GC", StringUtils.toHumanFriendlyByteSize(free));

        if (gc) {
            // Let the finalizer finish its work and remove objects from its queue
            System.gc(); // asynchronous garbage collector might already run
            System.gc(); // to make sure it does a full gc call it twice
            // System.runFinalization(); // deprecated
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }

            long after = Runtime.getRuntime().freeMemory();

            console.writeLine("%-23s %12s", "Free memory after GC", StringUtils.toHumanFriendlyByteSize(after));
            console.writeLine("%-23s %12s", "Memory gained with GC", StringUtils.toHumanFriendlyByteSize(after - free));
        } else {
            console.writeLine("%-23s %12s", "Free memory", StringUtils.toHumanFriendlyByteSize(free));
        }
        console.writeLine("------------------------------------");
    }

    @NonNull
    private String escape(@NonNull String s){
        return s.replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f");
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
        @NonNull
        public String getDescription() {
            return "Displays current JVM runtime information";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
