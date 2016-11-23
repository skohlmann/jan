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
package de.speexx.jira.jan.command.issuefieldanalyzer;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.speexx.jira.jan.Command;
import de.speexx.jira.jan.Config;
import de.speexx.jira.jan.ExecutionContext;
import de.speexx.jira.jan.JiraAnalyzeException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import static java.util.stream.Collectors.joining;

@Parameters(commandNames = {"issueanalyze"}, commandDescription = "Prints a human readable structure of all fields of the issues fetched for the given JQL query.")
public class IssueFieldAnalyzer implements Command {
    
    static final int JSON_PRINT_INDENT = 4;

    static final String JSON_VALUETYPE_UNKNOWN = "unknown";
    static final String JSON_VALUETYPE_STRING = "String";

    static final String JSON_KEY_VALUE_TYPE = "valuetype";
    static final String JSON_KEY_ID = "id";
    static final String JSON_KEY_FIELDS = "fields";
    static final String JSON_KEY_TYPE = "type";
    static final String JSON_KEY_PROJECT = "project";
    static final String JSON_KEY_NAME = "name";

    @Inject @Config
    private ExecutionContext execCtx;
    
    @Parameter(names = {"-l", "--limit"}, hidden = true, description = "Fetch size limit.")
    private int fetchLimit = 100;

    @Parameter(description = "The query for the issues to get the issue field information for. "
                           + "The query should be surrounded with quotation marks or apostrophs. "
                           + "Quotation marks inside the query might be escaped/protected "
                           + "by backslash (reversed solidus) character.")
    private List<String> query;

    @Override
    public void execute() {
        int count = 0;
        int startIndex = 0;
        int total = -1;
        
        try (final JiraRestClient restClient = this.execCtx.newJiraClient()) {
            do {
                final String q = getQuery().orElseThrow(() -> new JiraAnalyzeException("No query given for fetching transitions"));
                final SearchResult searchResult = fetchIssues(restClient, q, startIndex);
                total = total == -1 ? searchResult.getTotal() : total;
                startIndex += getFetchLimit();

                System.out.format("{[%n");
                count += handleIssueSearchResult(searchResult, restClient);
                System.out.format("]}%n");
                this.execCtx.log("total: {} - count: {}", total, count);
            } while (total != count);
        } catch (final IOException e) {
            throw new JiraAnalyzeException(e);
        }
    }

    int handleIssueSearchResult(final SearchResult searchResult, final JiraRestClient restClient) {
        int count = 0;
        for (final Issue searchResultIssue : searchResult.getIssues()) {
            count++;
            final Issue issue = fetchIssueForSearchResult(restClient, searchResultIssue);
            
            try {
                final JSONObject issueDescription = handleIssue(issue);
                System.out.format("%s,%n", issueDescription.toString(JSON_PRINT_INDENT));
            } catch (final JsonRuntimeException | JSONException e) {
                this.execCtx.log("Failure in JSON handling of issue {}: {}", issue.getKey(), e.getMessage());
            }
        }
        return count;
    }

    JSONObject handleIssue(final Issue issue) throws JSONException {
        final JSONObject issueDescription = new JSONObject();
        headerDataForIssue(issueDescription, issue);
        final JSONArray fieldDescriptions = new JSONArray();

        issue.getFields().forEach(field -> {
            try {
                final JSONObject fieldDescription = handleIssueField(field);
                fieldDescriptions.put(fieldDescription);
            } catch (final JSONException e) {
                throw new JsonRuntimeException(e);
            }
        });

        issueDescription.put(JSON_KEY_FIELDS, fieldDescriptions);
        return issueDescription;
    }

    JSONObject handleIssueField(final IssueField field) throws JSONException {
        final JSONObject fieldDescription = new JSONObject();
        final String id = field.getId();
        fieldDescription.put(JSON_KEY_ID, id);
        final String fieldName = field.getName();
        fieldDescription.put(JSON_KEY_NAME, fieldName);
        final Object value = field.getValue();
        handleFieldValue(value, fieldDescription);
        return fieldDescription;
    }

    void headerDataForIssue(final JSONObject issueDescription, final Issue issue) throws JSONException {
        issueDescription.put(JSON_KEY_PROJECT, issue.getProject().getName());
        issueDescription.put(JSON_KEY_TYPE, issue.getIssueType().getName());
    }

    void handleFieldValue(final Object value, final JSONObject fieldDescription) throws JSONException {
        if (value instanceof JSONObject) {
            fieldDescription.put(JSON_KEY_VALUE_TYPE, JSONObject.class.getCanonicalName());
            final JSONArray f = handleJSONObjectValue((JSONObject) value);
            fieldDescription.put(JSON_KEY_FIELDS, f);
        } else if (value instanceof JSONArray) {
            fieldDescription.put(JSON_KEY_VALUE_TYPE, JSONArray.class.getCanonicalName());
            final JSONArray f = handleJSONArrayValue((JSONArray) value);
            fieldDescription.put(JSON_KEY_FIELDS, f);
        } else if (value != null) {
            fieldDescription.put(JSON_KEY_VALUE_TYPE, value.getClass().getCanonicalName());
        } else {
            fieldDescription.put(JSON_KEY_VALUE_TYPE, JSON_VALUETYPE_UNKNOWN);
        }
    }
    
    JSONArray handleJSONObjectValue(final JSONObject json) throws JSONException {
        final JSONArray fieldDescriptions = new JSONArray();
        json.keys().forEachRemaining(o -> {
            final JSONObject fieldDescription = new JSONObject();
            assert o instanceof String;

            final String name = (String) o;
            try {
                fieldDescription.put(JSON_KEY_NAME, name);
                final Object value = json.get(name);
                handleFieldValue(value, fieldDescription);
                fieldDescriptions.put(fieldDescription);
            } catch (final JSONException e) {
                throw new JsonRuntimeException(e);
            }
        });
        return fieldDescriptions;
    }

    JSONArray handleJSONArrayValue(final JSONArray json) throws JSONException {
        final JSONArray fieldDescriptions = new JSONArray();
        final int length = json.length();

        for (int idx = 0; idx < length; idx++) {
            final JSONObject entryDescription = new JSONObject();
            final Object value = json.get(idx);
            handleFieldValue(value, entryDescription);
            fieldDescriptions.put(entryDescription);
        }
        

        return fieldDescriptions;
    }

    Issue fetchIssueForSearchResult(final JiraRestClient restClient, final Issue searchResultIssue) {
        final Set<IssueRestClient.Expandos> expandos = new HashSet<>();
        expandos.add(IssueRestClient.Expandos.NAMES);
        expandos.add(IssueRestClient.Expandos.SCHEMA);

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
}
