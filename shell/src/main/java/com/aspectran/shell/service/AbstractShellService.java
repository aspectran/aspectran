package com.aspectran.shell.service;

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.context.builder.config.SessionConfig;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.TokenExpressionParser;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.adapter.ShellApplicationAdapter;
import com.aspectran.shell.adapter.ShellSessionAdapter;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.console.Console;

import java.io.IOException;

/**
 * <p>Created: 2017. 10. 30.</p>
 */
public abstract class AbstractShellService extends AspectranCoreService implements ShellService {

    private static final Log log = LogFactory.getLog(AbstractShellService.class);

    private SessionManager sessionManager;

    private Console console;

    private String[] commands;

    private CommandRegistry commandRegistry;

    /** If verbose mode is on, a detailed description is printed each time the command is executed. */
    private boolean verbose;

    private String greetings;

    private Token[] greetingsTokens;

    protected AbstractShellService() throws IOException {
        super(new ShellApplicationAdapter());
    }

    @Override
    public void afterContextLoaded() throws Exception {
        sessionManager = new DefaultSessionManager(getActivityContext());
        sessionManager.setGroupName("SHL");
        SessionConfig sessionConfig = getAspectranConfig().getParameters(AspectranConfig.session);
        if (sessionConfig != null) {
            sessionManager.setSessionConfig(sessionConfig);
        }
        sessionManager.initialize();

        if (commands != null) {
            CommandRegistry commandRegistry = new CommandRegistry(this);
            commandRegistry.addCommand(commands);
            setCommandRegistry(commandRegistry);
        }

        parseGreetings();
    }

    @Override
    public void beforeContextDestroy() {
        sessionManager.destroy();
        sessionManager = null;
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        SessionAgent agent = sessionManager.newSessionAgent();
        return new ShellSessionAdapter(agent);
    }

    @Override
    public Console getConsole() {
        return console;
    }

    protected void setConsole(Console console) {
        this.console = console;
    }

    @Override
    public String[] getCommands() {
        return commands;
    }

    protected void setCommands(String[] commands) {
        this.commands = commands;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    private void setCommandRegistry(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    /**
     * Tests if the verbose mode is enabled.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Returns a flag indicating whether to show the description or not.
     *
     * @return true if the verbose mode is enabled
     */
    @Override
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Enables or disables the verbose mode.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Sets a flag indicating whether to show the description or not.
     *
     * @param verbose true to enable the verbose mode; false to disable
     */
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String getGreetings() {
        return greetings;
    }

    @Override
    public void setGreetings(String greetings) {
        this.greetings = greetings;
    }

    @Override
    public void printGreetings() {
        if (greetingsTokens != null) {
            TokenEvaluator evaluator = new TokenExpressionParser(getActivityContext().getDefaultActivity());
            String message = evaluator.evaluateAsString(greetingsTokens);
            console.writeLine(message);
            console.flush();
        } else if (greetings != null) {
            console.writeLine(greetings);
            console.flush();
        }
    }

    private void parseGreetings() {
        if (StringUtils.hasText(greetings)) {
            greetingsTokens = TokenParser.makeTokens(greetings, true);
            if (greetingsTokens != null) {
                try {
                    for (Token token : greetingsTokens) {
                        if (token.getType() == TokenType.BEAN) {
                            if (token.getDirectiveType() == TokenDirectiveType.CLASS) {
                                Class<?> beanClass = getAspectranClassLoader().loadClass(token.getValue());
                                token.setAlternativeValue(beanClass);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    greetingsTokens = null;
                    log.error("Failed to parse greetings", e);
                }
            }
        }
    }

    @Override
    public void printHelp() {
        if (isVerbose() && getActivityContext().getDescription() != null) {
            console.writeLine(getActivityContext().getDescription());
            console.flush();
        }
    }

}
