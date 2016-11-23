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
package de.speexx.jira.jan;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.beust.jcommander.Parameter;
import de.speexx.jira.jan.app.Application;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionContext {
    
    private final static String MAIN_CONFIG_FILENAME = Application.APPLICATION_NAME + ".config";
    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    
    @Parameter(names = {"-p", "--password"}, description = "JIRA connection password.")
    private String password;

    @Parameter(names = {"-u", "--user"}, description = "JIRA connection user.")
    private String user;

    @Parameter(names = {"-j", "--jira"}, description = "JIRA connection URI.")
    private String jiraUri;

    @Parameter(names = {"-h", "--help"}, help = true, description = "Prints the help page.")
    private boolean help = false;

    @Parameter(names = {"-v", "--verbose"}, hidden = true, description = "Prints more support information in case of problems.")
    private boolean verbose = false;

    @Parameter(names = {"-c", "--config"}, hidden = true, description = "Name of the main configuration file.")
    private String mainConfigFilename = MAIN_CONFIG_FILENAME;

    String getPassword() {
        return this.password;
    }

    public String getUser() {
        return this.user;
    }
    
    public String getJiraUri() {
        return this.jiraUri;
    }
    
    public String getMainConfigFilename() {
        return this.mainConfigFilename;
    }

    public boolean isHelp() {
        return this.help;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public void setJiraUri(final String jiraUri) {
        this.jiraUri = jiraUri;
    }
    
    public JiraRestClient newJiraClient() {
        try {
            final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            return factory.createWithBasicHttpAuthentication(new URI(getJiraUri()), getUser(), getPassword());
        } catch (final URISyntaxException ex) {
            throw new JiraAnalyzeException(ex);
        }
    }
    
    public boolean isVerbose() {
        return this.verbose;
    }

    public void log(final String message, final Object... params) {
        if (isVerbose()) {
            LOG.info(message, params);
        }
    }
}
