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
package de.speexx.jira.jan.command.count;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.speexx.jira.jan.Command;
import de.speexx.jira.jan.Config;
import de.speexx.jira.jan.ExecutionContext;
import de.speexx.jira.jan.JiraAnalyzeException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import static java.util.stream.Collectors.joining;


@Parameters(commandNames = {"count"}, commandDescription = "Prints the count of the given JQL query.")
public class JqlResultCounter implements Command {
    
    @Inject @Config
    private ExecutionContext execCtx;

    @Parameter(description = "The query to count the results for.")
    private List<String> query;

    @Override
    public void execute() {
        try (final JiraRestClient restClient = this.execCtx.newJiraClient()) {
            final SearchRestClient searchClient = restClient.getSearchClient();
            final String q = getQuery().orElseThrow(() -> new JiraAnalyzeException("No query given for fetching transitions"));
            final Promise<SearchResult> results = searchClient.searchJql(q);
            final SearchResult result = results.claim();
            final int total = result.getTotal();
            System.out.println(total);
        } catch (final IOException e) {
            throw new JiraAnalyzeException(e);
        }
    }

    Optional<String> getQuery() {
        if (this.query == null || this.query.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.query.stream().collect(joining(" ")));
    }
}
