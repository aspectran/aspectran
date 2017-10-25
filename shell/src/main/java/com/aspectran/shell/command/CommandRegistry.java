package com.aspectran.shell.command;

import com.aspectran.core.util.ClassUtils;
import com.aspectran.shell.service.ShellAspectranService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Created: 2017. 10. 25.</p>
 */
public class CommandRegistry {

    private Map<String, Command> commands = new LinkedHashMap<>();

    private ShellAspectranService service;

    public CommandRegistry(ShellAspectranService service) {
        this.service = service;
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
