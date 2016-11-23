/* A tool to extract transition information from JIRA for analyzis (jan).
 *
 * Copyright (C) 2016 Sascha Kohlmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.speexx.jira.jan.app;

import de.speexx.jira.jan.command.help.HelpCommand;
import de.speexx.jira.jan.Config;
import de.speexx.jira.jan.ExecutionContext;
import com.beust.jcommander.JCommander;
import de.speexx.jira.jan.Command;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    public static final String APPLICATION_NAME = "jan";
    private final static ExecutionContext MAIN_CONTEXT = new ExecutionContext();

    @Inject
    private Instance<Command> commands;

    @Produces @Config
    private ExecutionContext context() {
        return MAIN_CONTEXT;
    }

    public static final void main(final String... args) {        
        final Weld weld = new Weld();
        try (final WeldContainer container = weld.initialize()) {
            container.select(Application.class).get().run(args);
        } catch (final Throwable t) {
            if (!MAIN_CONTEXT.isVerbose()) {
                LOG.error("Unexpected end of application: {}", t.getMessage());
                final StackTraceElement[] st = t.getStackTrace();
                LOG.error("Location - class: {} - method: {} - line: {}", st[0].getClassName(), st[0].getMethodName(), st[0].getLineNumber());
            } else {
                LOG.error("Unexpected end of application:", t);
            }
            System.exit(1);
        }
        System.exit(0);
    }
    
    void run(final String... args) {
        final JCommander jc = new JCommander(context());
        this.commands.forEach(cmd -> jc.addCommand(cmd));

        jc.parse(args);
        
        if (context().isHelp()) {
            jc.setProgramName(APPLICATION_NAME);
            jc.setColumnSize(USAGE_COLUMN_SIZE);
            jc.usage();
            return;
        }

        final Optional<Command> cmd = findCommand(jc);
        cmd.orElse(new HelpCommand(jc)).execute();
    }
    static final int USAGE_COLUMN_SIZE = 80;
    
    Optional<Command> findCommand(final JCommander jc) {
        final String parsedCommand = jc.getParsedCommand();

        for (final Map.Entry<String, JCommander> cmdEntry : jc.getCommands().entrySet()) {
            final String name = cmdEntry.getKey();
            if (name != null && name.equals(parsedCommand)) {
                return Optional.of((Command) cmdEntry.getValue().getObjects().get(0));
            }
        }
        
        return Optional.empty();
    }
}
