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
package de.speexx.jira.jan.service.issue;

import com.atlassian.jira.rest.client.api.ExpandableProperty;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.OperationGroup;
import com.atlassian.jira.rest.client.api.domain.Operations;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;


public class IssueFieldServiceTest {

    private WeldContainer weldContainer;
    private IssueFieldService issueFieldService;
    private FieldNameService fieldNameService;
    
    @Test
    public void test_issue_data_fetching() throws JSONException {
        final Issue issue = createFullBlownIssue();
        
        final FieldNamePath report = this.fieldNameService.createFieldNamePath(this.fieldNameService.createFieldName("Reporter"));
        final FieldNamePath key = this.fieldNameService.createFieldNamePath(this.fieldNameService.createFieldName("key"));
        final FieldNamePath timer = this.fieldNameService.createFieldNamePath(this.fieldNameService.createFieldName("TimeR"));
        final FieldNamePath json3 = this.fieldNameService.createFieldNamePath(this.fieldNameService.createFieldName("js3"));
        final FieldNamePath json4 = this.fieldNameService.createFieldNamePath(this.fieldNameService.createFieldName("js4::first"));
        final FieldNamePath json5 = this.fieldNameService.createFieldNamePath(this.fieldNameService.createFieldName("JSON 5"));
        final FieldNamePath json6 = this.fieldNameService.createFieldNamePath(this.fieldNameService.createFieldName("json 6"));
        final FieldNamePath empty = this.fieldNameService.createFieldNamePath(this.fieldNameService.createFieldName("null"));

        final FieldName chl1name = this.fieldNameService.createFieldName("abc");
        final FieldName emptyName = this.fieldNameService.createFieldName("empty");

        try {
            final IssueData data = new IssueData();
            final IssueData withCurrent = this.issueFieldService.fetchCurrentIssueData(issue, data, Arrays.asList(key, report, timer, json3, json4, json5, json6, empty));
            final IssueData withHistory = this.issueFieldService.fetchHistoricalIssueData(issue, withCurrent, Arrays.asList(chl1name, emptyName));

            System.out.format("ISSUE DATA: %s%n", withHistory);
        } catch (final Throwable t) {
            t.printStackTrace(System.out);
            throw t;
        }
    }

    Issue createFullBlownIssue() throws JSONException {
        final Map<String, URI> avatarUris = new HashMap<>();
        avatarUris.put(User.S16_16, URI.create("http://exmple.com/user/user/avartar/uri/16x16"));
        avatarUris.put(User.S48_48, URI.create("http://exmple.com/user/user/avartar/uri/48x48"));

        final ChangelogGroup clg1 = new ChangelogGroup(
                new User(URI.create("http://exmple.com/user/changer"), "g", "Gabi", "gabi@example.com", new ExpandableProperty(0), avatarUris, "UTC"),
                new DateTime(123L),
                new ArrayList<>(Arrays.asList(new ChangelogItem(FieldType.JIRA, "abc", "Nothing", "Nothing S", "Something", "Something S"),
                                              new ChangelogItem(FieldType.CUSTOM, "xyz", null, null, "core", "Core S"))));
        final ChangelogGroup clg2 = new ChangelogGroup(
                new User(URI.create("http://exmple.com/user/changer"), "j", "Johanna", "johanna@example.com", new ExpandableProperty(0), avatarUris, "UTC"),
                new DateTime(456L),
                new ArrayList<>(Arrays.asList(new ChangelogItem(FieldType.JIRA, "abc", "Done", "Done S", "Fixed", "fixed S"),
                                              new ChangelogItem(FieldType.CUSTOM, "test 2", "master", "Master S", "puppets", "Puppets S"))));
        
        final IssueField if1 = new IssueField("id1", "id", null, "simpel String");
        final IssueField if2 = new IssueField("t1", "timer", null, new DateTime(1001L));

        final JSONObject json3 = new JSONObject();
        json3.put("name", "some JSON name");
        final IssueField if3 = new IssueField("js3", "json 3", null, json3);

        final JSONObject json4 = new JSONObject();
        final JSONObject json41 = new JSONObject();
        json41.put("second", "second hierarchy");
        json4.put("first", json4);
        final IssueField if4 = new IssueField("js4", "json 4", null, json4);

        final JSONObject json51 = new JSONObject();
        json51.put("second 5", "second hierarchy 5");
        final JSONArray jsonArray = new JSONArray();
        jsonArray.put(json51);
        final IssueField if5 = new IssueField("js5", "json 5", null, jsonArray);

        final IssueField if6 = new IssueField("js6", "json 6", null, new JSONArray());
        
        return new Issue(
                "Summary",
                URI.create("http://exmple.com/issue"),
                "TEST-123",
                123L,
                new BasicProject(URI.create("http://exmple.com/basicproject"), "TEST", 234L, "Basic Project"),
                new IssueType(URI.create("http://exmple.com/issuetype"), 345L, "Test", false, "Simple test issue type", URI.create("http://exmple.com/issuetype/icon")),
                new Status(URI.create("http://exmple.com/status"), 456L, "Closed", "Closed issue", URI.create("http://exmple.com/status/icon")),
                "Simple Description",
                new BasicPriority(URI.create("http://exmple.com/priority"), 567L, "Major"),
                new Resolution(URI.create("http://exmple.com/resolution"), 678L, "Fixed", "Issue is fixed"),
                Collections.emptyList(),
                new User(URI.create("http://exmple.com/user/reporter"), "sascha", "Sascha Kohlmann", "sascha.kohlmann@example.com", new ExpandableProperty(0), avatarUris, "UTC"),
                new User(URI.create("http://exmple.com/user/assignee"), "someone", "Some One", "someone@example.com", new ExpandableProperty(0), avatarUris, "UTC"),
                new DateTime(123L),
                new DateTime(456L),
                new DateTime(789L),
                new ArrayList<Version>(),
                new ArrayList<Version>(),
                new HashSet<BasicComponent>(),
                new TimeTracking(123, 456, 567),
                Arrays.asList(if1, if2, if3, if4, if5, if6),
                new HashSet<Comment>(),
                URI.create("http://exmple.com/user/transitions"),
                new HashSet<IssueLink>(),
                new BasicVotes(URI.create("http://exmple.com/user/basicvotes"), 789, true),
                new ArrayList<Worklog>(),
                new BasicWatchers(URI.create("http://exmple.com/user/basicwatchers"), true, 13),
                new HashSet<String>(), // expandos
                new HashSet<Subtask>(),
                new ArrayList<ChangelogGroup>(Arrays.asList(clg1, clg2)),
                new Operations(new HashSet<OperationGroup>()),
                new HashSet<String>()
        );
    }
    
    @BeforeEach
    public void setupCdi() {
        this.weldContainer = new Weld().initialize();
        this.issueFieldService = this.weldContainer.instance().select(IssueFieldService.class).get();
        this.fieldNameService = this.weldContainer.instance().select(FieldNameService.class).get();
    }
    
    @AfterEach
    public void teardownCdi() {
        this.weldContainer.close();
    }
}
