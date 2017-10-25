package com.aspectran.shell.command.builtins;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.service.ShellAspectranService;
import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DescCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(DescCommand.class);

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "desc";

    private static final Map<String, String> options;

    static {
        options = new LinkedHashMap<>();
        options.put("-on", "Turn on the ability to print a description of the command");
        options.put("-off", "Turn off the ability to print a description of the command");
    }

    public DescCommand(ShellAspectranService service) {
        super(service);
    }

    @Override
    public String execute(String[] args) throws Exception {
        DescOptions descOptions = parse(args, new DescOptions());

        if (descOptions.on) {
            log.info("Description On");
            getService().setDescriptable(true);
        } else if (descOptions.off) {
            log.info("Description Off");
            getService().setDescriptable(false);
        } else if (descOptions.help) {
            printUsage();
        } else {
            printUsage();
        }

        return null;
    }

    @Override
    public Descriptor getDescriptor() {
        return new Descriptor() {

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
                return "Turn on or off the ability to print a description of the command";
            }

            @Override
            public String getUsage() {
                return "Type 'desc [-on] [-off]'";
            }

            @Override
            public Map<String, String> getOptions() {
                return options;
            }

        };
    }

    private class DescOptions {

        @Parameter
        private List<String> dummy = new ArrayList<>();

        @Parameter(names = "-help", help = true, description = "Print this message")
        public boolean help;

        @Parameter(names = "-on", description = "Turn on the ability to print a description of the command")
        public boolean on = false;

        @Parameter(names = "-off", description = "Turn off the ability to print a description of the command")
        public boolean off = false;

    }

}
