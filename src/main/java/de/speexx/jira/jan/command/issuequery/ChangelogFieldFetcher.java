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
package de.speexx.jira.jan.command.issuequery;

import de.speexx.jira.jan.service.issue.FieldConfig;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.speexx.jira.jan.Command;
import de.speexx.jira.jan.Config;
import de.speexx.jira.jan.ExecutionContext;
import de.speexx.jira.jan.JiraAnalyzeException;
import de.speexx.jira.jan.service.issue.FieldName;
import de.speexx.jira.jan.service.issue.FieldNamePath;
import de.speexx.jira.jan.service.issue.FieldNameService;
import de.speexx.jira.jan.service.issue.IssueData;
import de.speexx.jira.jan.service.issue.IssueCoreFieldConfig;
import de.speexx.jira.jan.service.issue.IssueFieldService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import static java.util.stream.Collectors.joining;

@Parameters(commandNames = {"issuequery"}, commandDescription = "Fetch the changelog for the required fields. The fields KEY and CreateDate are always fetched.")
public final class ChangelogFieldFetcher implements Command {

    private FieldName issueKeyFieldName;
    private FieldName createdDateFieldName;

    @Inject @Config
    private ExecutionContext execCtx;
    
    @Inject @FieldConfig
    private IssueCoreFieldConfig fieldConfig;

    @Inject
    private IssueFieldService issueFieldService;
    
    @Inject
    private FieldNameService fieldNameService;
    
    @Inject
    private StringToFieldNamePathConverter pathNameConverter;
    
    @Inject
    private CsvCreator csvCreator;
    
    @Parameter(names = {"-l", "--limit"}, hidden = true, description = "Fetch size limit.")
    private int fetchLimit = 100;

    @Parameter(names = {"-q", "-query"},
               required = true,
               variableArity = true,
               description = "The query for the changelog to fetch. "
                           + "The query should be surrounded with quotation marks or apostrophs. "
                           + "Quotation marks inside the query might be escaped/protected "
                           + "by backslash (reversed solidus) character.")
    private List<String> query;

    @Parameter(names = {"-h", "--history"},
               description = "Field name to get the changelog for. "
                           + "The fields \"key\", \"createdDate\", \"creator\""
                           + "and \"reporter\" will be ignored.",
               converter = StringToFieldNameConverter.class,
               variableArity = true)
    private final List<FieldName> historyFieldNames = new ArrayList<>();

    @Parameter(names = {"-c", "--current"},
               description = "List of unique final fields of the issues. "
                           + "These fields are not fetched from the changelog. "
                           + "If the field is also defined with the \"change\" "
                           + "parameter the \"change\" parameter wins. "
                           + "The fields \"key\" and \"createdDate\" will be ignored.",
               converter = StringToFieldNamePathConverter.class,
               variableArity = true)
    private final List<FieldNamePath> currentFieldNames = new ArrayList<>();
    
    static final String TEMPORAL_PARAMETER_NAME_SHORT = "-t";
    static final String TEMPORAL_PARAMETER_NAME_LONG = "--temporal";
    static final String TEMPORAL_VALUE_NONE_LONG = "none";
    static final String TEMPORAL_VALUE_NONE_SHORT = "n";
    static final String TEMPORAL_VALUE_TIME_LONG = "time";
    static final String TEMPORAL_VALUE_TIME_SHORT = "t";
    static final String TEMPORAL_VALUE_DURATION_LONG = "duration";
    static final String TEMPORAL_VALUE_DURATION_SHORT = "d";
    static final String TEMPORAL_VALUE_BOTH_LONG = "both";
    static final String TEMPORAL_VALUE_BOTH_SHORT = "b";
    
    @Parameter(names = {TEMPORAL_PARAMETER_NAME_SHORT, TEMPORAL_PARAMETER_NAME_LONG},
               validateWith = TemporalParameterValidator.class,
               converter = TemporalParameterValidator.class,
               description = "Flag to for output of temporal value of change data. "
                           + "Possible values are '" + TEMPORAL_VALUE_NONE_LONG + "' "
                           + "for no temporal output, '" + TEMPORAL_VALUE_TIME_LONG + "' "
                           + "for datetime, '" + TEMPORAL_VALUE_DURATION_LONG + "' "
                           + "for the duration between changes and "
                           + "'" + TEMPORAL_VALUE_BOTH_LONG + "' for time and duration. "
                           + "Can be abrreviated with first character. "
                           + "E.g. '" + TEMPORAL_VALUE_TIME_SHORT + "' for "
                           + "'" + TEMPORAL_VALUE_TIME_LONG + "'.")
    private TemporalChangeOutput temporalOutput = TemporalChangeOutput.DURATION;

    @Parameter(names = {"-n", "--noheader"}, description = "Noheader output if given.")
    private boolean noHeader = false;

    @Override
    public void execute() {
        createFieldNames();
        correctFields();
        
        int count = 0;
        int startIndex = 0;
        int total = -1;
        
        final AtomicBoolean header = new AtomicBoolean(!this.noHeader);
        try (final JiraRestClient restClient = this.execCtx.newJiraClient()) {
            do {
                final String q = getQuery().orElseThrow(() -> new JiraAnalyzeException("No query given for fetching transitions"));
                final SearchResult searchResult = fetchIssues(restClient, q, startIndex);
                total = total == -1 ? searchResult.getTotal() : total;
                startIndex += getFetchLimit();

                for (final Issue searchResultIssue : searchResult.getIssues()) {
                    count++;
                    final Issue issue = fetchIssueForSearchResult(restClient, searchResultIssue);

                    final IssueData issueData = new IssueData();
                    final IssueData currentIssueData
                            = this.issueFieldService.fetchCurrentIssueData(issue, issueData, this.currentFieldNames);
                    final IssueData withHistoricalIssueData
                            = this.issueFieldService.fetchHistoricalIssueData(issue, currentIssueData, this.historyFieldNames);
                    
                    this.csvCreator.printIssueData(withHistoricalIssueData, this.historyFieldNames, this.currentFieldNames, this.temporalOutput, header);
                }
                this.execCtx.log("total: {} - count: {}", total, count);
            } while (total != count);
        } catch (final IOException e) {
            throw new JiraAnalyzeException(e);
        }
    }

    Issue fetchIssueForSearchResult(final JiraRestClient restClient, final Issue searchResultIssue) {
        final Set<IssueRestClient.Expandos> expandos = new HashSet<>();
        expandos.add(IssueRestClient.Expandos.NAMES);
        expandos.add(IssueRestClient.Expandos.CHANGELOG);
        expandos.add(IssueRestClient.Expandos.OPERATIONS);
        expandos.add(IssueRestClient.Expandos.SCHEMA);
        expandos.add(IssueRestClient.Expandos.TRANSITIONS);

        final IssueRestClient issueClient = restClient.getIssueClient();
        final Promise<Issue> issueResult = issueClient.getIssue(searchResultIssue.getKey(), expandos);
        return issueResult.claim();
    }

   SearchResult fetchIssues(final JiraRestClient restClient, String q, int startIndex) {
        final SearchRestClient searchClient = restClient.getSearchClient();
        final Promise<SearchResult> results = searchClient.searchJql(q, getFetchLimit(), startIndex, null);
        return results.claim();
    }
    
    Optional<String> getQuery() {
        if (this.query == null || this.query.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.query.stream().collect(joining(" ")));
    }

    int getFetchLimit() {
        return this.fetchLimit;
    }
    
    final void correctFields() {
        final List<FieldNamePath> adjustedUnique = new ArrayList<>();
        this.currentFieldNames.stream().map(path -> path.asString().toLowerCase(Locale.ENGLISH))
                                       .forEach((asString) -> {
            adjustedUnique.add(pathNameConverter.convert(asString));
        });
        this.currentFieldNames.clear();
        this.currentFieldNames.addAll(adjustedUnique);

        final List<FieldName> adjustedChange = new ArrayList<>();
        this.historyFieldNames.stream().filter(c -> (c != null))
                              .forEach(c -> {
            this.fieldConfig.isIgnorable(c).ifPresent(ignore -> {
                if (!ignore) {
                    adjustedChange.add(c);
                }
            });
        });
        
        this.historyFieldNames.clear();
        this.historyFieldNames.addAll(adjustedChange);
        
        this.currentFieldNames.removeAll(this.historyFieldNames);
        this.currentFieldNames.add(0, this.fieldNameService.createFieldNamePath(this.createdDateFieldName));
        this.currentFieldNames.add(0, this.fieldNameService.createFieldNamePath(this.issueKeyFieldName));
    }
    
    final List<FieldName> getChangeFieldNames() {
        return this.historyFieldNames;
    }

    final List<FieldNamePath> getUniqueFieldNames() {
        return this.currentFieldNames;
    }
    
    void createFieldNames() {
        this.issueKeyFieldName = this.fieldNameService.createFieldName("issuekey");
        this.createdDateFieldName = this.fieldNameService.createFieldName("createddate");
    }
}
