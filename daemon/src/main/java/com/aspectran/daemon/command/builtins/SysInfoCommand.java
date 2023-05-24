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
package com.aspectran.daemon.command.builtins;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.resource.SiblingsClassLoader;
import com.aspectran.core.util.StringUtils;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

public class SysInfoCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "sysinfo";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public SysInfoCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        try  {
            ParameterMap parameterMap = parameters.getParameterMap();
            String[] options = parameterMap.getParameterValues("options");
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            for (String option : options) {
                if ("props".equals(option)) {
                    printSysProperties(printWriter);
                } else if ("cp".equals(option)) {
                    printClasspath(printWriter);
                } else if ("mem".equals(option)) {
                    mem(false, printWriter);
                } else if ("gc".equals(option)) {
                    mem(true, printWriter);
                }
            }
            return success(writer.toString());
        } catch (Exception e) {
            return failed(e);
        }
    }

    private void printSysProperties(PrintWriter printWriter) {
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            printWriter.format("%1$30s   %2$s", entry.getKey(), entry.getValue()).println();
        }
    }

    private void printClasspath(PrintWriter printWriter) {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        for (String line : StringUtils.split(bean.getClassPath(), File.pathSeparator)) {
            printWriter.println(line);
        }
        if (isServiceAvailable()) {
            ClassLoader classLoader = getDaemonService().getActivityContext().getApplicationAdapter().getClassLoader();
            if (classLoader instanceof SiblingsClassLoader) {
                SiblingsClassLoader scl = (SiblingsClassLoader)classLoader;
                Enumeration<URL> urls = scl.getAllResources();
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    printWriter.println(url.toString());
                }
            }
        }
    }

    /**
     * Prints memory usage.
     * @param gc true if performing garbage collection; false otherwise
     */
    private void mem(boolean gc, PrintWriter printWriter) {
        long total = Runtime.getRuntime().totalMemory();
        long before = Runtime.getRuntime().freeMemory();

        printWriter.format("%-24s %12s", "Total memory", StringUtils.convertToHumanFriendlyByteSize(total));
        printWriter.format("%-24s %12s", "Used memory", StringUtils.convertToHumanFriendlyByteSize(total - before));

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

            printWriter.format("%-24s %12s", "Free memory before GC", StringUtils.convertToHumanFriendlyByteSize(before));
            printWriter.format("%-24s %12s", "Free memory after GC", StringUtils.convertToHumanFriendlyByteSize(after));
            printWriter.format("%-24s %12s", "Memory gained with GC", StringUtils.convertToHumanFriendlyByteSize(after - before));
        } else {
            printWriter.format("%-24s %12s", "Free memory", StringUtils.convertToHumanFriendlyByteSize(before));
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
            return "Prints current JVM runtime information";
        }

    }

}
