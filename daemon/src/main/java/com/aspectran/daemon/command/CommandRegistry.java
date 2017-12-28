package com.aspectran.daemon.command;

import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.daemon.Daemon;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    private final Daemon daemon;

    public CommandRegistry(Daemon daemon) {
        this.daemon = daemon;
    }

    public Daemon getDaemon() {
        return daemon;
    }

    public void init(String[] classNames) throws Exception {
        try {
            addCommand(classNames);
        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon commander", e);
        }
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }

    public void addCommand(String... classNames) {
        if (classNames != null) {
            for (String className : classNames) {
                try {
                    ClassLoader classLoader = AspectranClassLoader.getDefaultClassLoader();
                    @SuppressWarnings("unchecked")
                    Class<? extends Command> commandClass = (Class<? extends Command>)classLoader.loadClass(className);
                    addCommand(commandClass);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Unable to load Command class: " + className, e);
                }
            }
        }
    }

    public void addCommand(Class<? extends Command> commandClass) {
        Command command = ClassUtils.createInstance(commandClass, this);
        commands.put(command.getDescriptor().getName(), command);
    }

    public Collection<Command> getAllCommands() {
        return commands.values();
    }

}
