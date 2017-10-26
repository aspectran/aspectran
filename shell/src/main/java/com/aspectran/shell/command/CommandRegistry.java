package com.aspectran.shell.command;

import com.aspectran.core.util.ClassUtils;
import com.aspectran.shell.service.ShellAspectranService;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Created: 2017. 10. 25.</p>
 */
public class CommandRegistry {

    private Map<String, Command> commands = new LinkedHashMap<>();

    private CommandLineParser parser = new DefaultParser();

    private ShellAspectranService service;

    public CommandRegistry(ShellAspectranService service) {
        this.service = service;
    }

    public ShellAspectranService getService() {
        return service;
    }

    public CommandLineParser getParser() {
        return parser;
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }

    public void addCommand(String... classNames) {
        if (classNames != null) {
            for (String className : classNames) {
                try {
                    ClassLoader classLoader = service.getAspectranClassLoader();
                    @SuppressWarnings("unchecked")
                    Class<? extends Command> commandClass = (Class<? extends Command>)classLoader.loadClass(className);
                    Command command = ClassUtils.createInstance(commandClass, service);
                    assert command != null;
                    commands.put(command.getDescriptor().getName(), command);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Unable to load Command class: " + className, e);
                }
            }
        }
    }

}
