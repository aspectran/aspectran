/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.shell.command.builtin;

import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collection;
import java.util.Map;

public class SysInfoCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "sysinfo";

    private CommandDescriptor descriptor = new CommandDescriptor();

    public SysInfoCommand(CommandRegistry registry) {
        super(registry);

        addOption(new Option("props", "Displays the JVM's system properties"));
        addOption(Option.builder("cp").longOpt("classpath").desc("Displays JVM classpath information").build());
        addOption(Option.builder("mem").longOpt("memory").desc("Displays memory information about current JVM").build());
        addOption(new Option("gc", "Performs garbage collection"));
    }

    @Override
    public String execute(String[] args) throws Exception {
        ParsedOptions options = parse(args);
        if (options.hasOption("props")) {
            printSysProperties();
        } else if (options.hasOption("cp")) {
            printClasspath();
        } else if (options.hasOption("mem")) {
            mem(false);
        } else if (options.hasOption("gc")) {
            mem(true);
        } else {
            printUsage();
        }
        return null;
    }

    private void printSysProperties() {
        for(Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            getConsole().writeLine("%1$30s   %2$s", entry.getKey(), entry.getValue());
        }
    }

    private void printClasspath() {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        for (String line : StringUtils.split(bean.getClassPath(), File.pathSeparator)) {
            getConsole().writeLine(line);
        }
    }

    /**
     * Displays memory usage.
     *
     * @param gc true if performing garbage collection; false otherwise
     */
    private void mem(boolean gc) {
        long total = Runtime.getRuntime().totalMemory();
        long before = Runtime.getRuntime().freeMemory();

        getConsole().setStyle("yellow");
        getConsole().write("Total memory: ");
        getConsole().setStyle("fg:off");
        getConsole().writeLine(StringUtils.convertToHumanFriendlyByteSize(total));
        getConsole().setStyle("yellow");
        getConsole().write("Used memory: ");
        getConsole().setStyle("fg:off");
        getConsole().writeLine(StringUtils.convertToHumanFriendlyByteSize(total - before));
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

            getConsole().setStyle("yellow");
            getConsole().write("Free memory before GC: ");
            getConsole().setStyle("fg:off");
            getConsole().writeLine(StringUtils.convertToHumanFriendlyByteSize(before));
            getConsole().setStyle("yellow");
            getConsole().write("Free memory after GC: ");
            getConsole().setStyle("fg:off");
            getConsole().writeLine(StringUtils.convertToHumanFriendlyByteSize(after));
            getConsole().setStyle("yellow");
            getConsole().write("Memory gained with GC: ");
            getConsole().setStyle("fg:off");
            getConsole().writeLine(StringUtils.convertToHumanFriendlyByteSize(after - before));
        } else {
            getConsole().setStyle("yellow");
            getConsole().write("Free memory: ");
            getConsole().setStyle("fg:off");
            getConsole().writeLine(StringUtils.convertToHumanFriendlyByteSize(before));
        }
        getConsole().offStyle();
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private class CommandDescriptor implements Descriptor {

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

        @Override
        public Collection<Option> getOptions() {
            return options.getOptions();
        }

    }

}
