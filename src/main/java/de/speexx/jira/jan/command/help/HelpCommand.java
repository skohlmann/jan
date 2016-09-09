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
package de.speexx.jira.jan.command.help;

import de.speexx.jira.jan.ExecutionContext;
import de.speexx.jira.jan.Config;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.speexx.jira.jan.Command;
import de.speexx.jira.jan.app.Application;
import java.io.Console;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;

@Parameters(commandDescription = "Prints help support on the console.", commandNames = "help")
public class HelpCommand implements Command {

    @Inject @Config
    private ExecutionContext execCtx;

    @Parameter(description = "The name of the command to get the help for.")
    private String commandName;
    private JCommander jCommander;
    private boolean onlyCommandNames = false;

    public HelpCommand() {
        this.jCommander = null;
    }

    public HelpCommand(final JCommander jc) {
        this.jCommander =jc;
        this.onlyCommandNames = true;
    }
    
    @Override
    public void execute() {
        if (getJCommander() == null) {
            System.console().printf("No commands configured. Unable to print any help.");
            return;
        }
        if (isOnlyCommandNames()) {
            printCommandNamesOnly();
            return;
        }
        printHelp();
    }
    
    void printCommandNamesOnly() {
        final Console cons = System.console();
        cons.printf("%s commands:%n", Application.APPLICATION_NAME);
        getCommands().keySet().stream().forEach((command) -> {
            cons.printf("    %s%n", command);
        });
    }
    
    Map<String, JCommander> getCommands() {
        final TreeMap<String, JCommander> cmds = new TreeMap<>((String k1, String k2) -> k2.compareTo(k1));
        cmds.putAll(getJCommander().getCommands());
        return cmds;
    }
    
    void printHelp() {
        final String cmdName = getCommandName();
        if (cmdName != null) {
            getJCommander().usage(cmdName);
            return;
        }
        getJCommander().usage();
    }
    
    public void setJCommander(final JCommander jc) {
        this.jCommander = jc;
    }
    
    public void setOnlyCommandNames(final boolean onlyCommandNames) {
        this.onlyCommandNames = onlyCommandNames;
    }

    public void setCommandName(final String commandName) {
        this.commandName = commandName;
    }

    public JCommander getJCommander() {
        return jCommander;
    }

    public boolean isOnlyCommandNames() {
        return onlyCommandNames;
    }

    public String getCommandName() {
        return commandName;
    }
}
