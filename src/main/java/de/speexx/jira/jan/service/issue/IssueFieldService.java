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

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import de.speexx.jira.jan.JiraAnalyzeException;
import de.speexx.jira.jan.service.time.TimeConverterService;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public final class IssueFieldService {
    
    @Inject
    private TimeConverterService timeConverter;

    @Inject
    private FieldNameService fieldNameService;

    @Inject @FieldConfig
    private IssueCoreFieldConfig fieldConfig;

    public IssueData fetchHistoricalIssueData(final Issue issue,
                                              final IssueData issueData,
                                              final Collection<FieldName> historyFieldNames) {
        Objects.requireNonNull(issue, "Issue is null");
        Objects.requireNonNull(issueData, "IssueData is null");
        Objects.requireNonNull(historyFieldNames, "FieldNames is null");

        for (final ChangelogGroup changeLog : issue.getChangelog()) {
            for (final ChangelogItem cli : changeLog.getItems()) {
                final FieldName field = new FieldName(cli.getField());
                if (historyFieldNames.contains(field)) {
                    final IssueData.HistoricalDataEntry changeEntry = 
                            new IssueData.HistoricalDataEntry(cli.getFromString(),
                                                          this.timeConverter.jodaDateTimeToJava8LocalDateTime(changeLog.getCreated()),
                                                          cli.getToString());
                    issueData.addHistoricalDataEntry(field, changeEntry);
                }
            }
        }
        return issueData;
    }
    
    public IssueData fetchCurrentIssueData(final Issue issue,
                                           final IssueData issueData,
                                           final Collection<FieldNamePath> currentFieldNames) {
        
        Objects.requireNonNull(issue, "Issue is null");
        Objects.requireNonNull(issueData, "IssueData is null");
        Objects.requireNonNull(currentFieldNames, "FieldNames is null");
        
        final AtomicReference<FieldNamePath> pathRef = new AtomicReference();
        currentFieldNames.stream()
                         .filter(path -> path.length() == 1)
                         .peek(path -> pathRef.set(path))
                         .map(path -> path.iterator().next())
                         .forEach(fieldName -> {
                             this.fieldConfig.getIssueGetter(fieldName).ifPresent(getter -> {
                                 try {
                                     final Object source = getter.invoke(issue);
                                     if (source != null) {
                                         this.fieldConfig.getValueFetcher(fieldName).ifPresent(fetcher -> {
                                             fetcher.getValue(source).ifPresent(value -> {
                                                 issueData.addCurrentFieldData(pathRef.get(), value);
                                             });
                                         });
                                     }
                                 } catch (final IllegalAccessException
                                              | IllegalArgumentException 
                                              | InvocationTargetException ex) {
                                     throw new JiraAnalyzeException(ex);
                                 }
                             });
                         });
        return fetchCurrentDataFromIssueFields(issue, issueData, currentFieldNames);
    }

    IssueData fetchCurrentDataFromIssueFields(final Issue issue,
                                              final IssueData issueData,
                                              final Collection<FieldNamePath> currentFieldNames) {
        assert issue != null;
        assert issueData != null;
        assert currentFieldNames != null;
        
        final Map<FieldName, FieldNamePath> byRoot = new HashMap<>();
        currentFieldNames.forEach(fieldNamePath -> byRoot.putIfAbsent(fieldNamePath.getRootElement(), fieldNamePath));

        issue.getFields().forEach(issueField -> {
            final FieldName id = new FieldName(issueField.getId());
            final FieldName name = new FieldName(issueField.getName());
            if (byRoot.containsKey(name) || byRoot.containsKey(id)) {
                final Object value = issueField.getValue();
                if (!(value instanceof JSONArray) && !(value instanceof JSONObject)) {
                    issueData.addCurrentFieldData(this.fieldNameService.createFieldNamePath(name), value);
                } else {
                    final FieldNamePath original = fetchFieldNamePath(byRoot, id, name);
                    if (original != null) {
                        final FieldNamePath withoutRoot = original.getPathWithoutRoot();
                        if (withoutRoot.length() == 0) {
                            return;
                        }
                        if (value instanceof JSONObject) {
                            final JSONObject obj = (JSONObject) value;
                            final Object objValue = fetchFromJsonObject(withoutRoot, obj);
                            issueData.addCurrentFieldData(original, objValue);
                        } else {
                            assert value instanceof JSONArray;
                            final JSONArray obj = (JSONArray) value;
                            final Object objValue = fetchFromJsonArray(withoutRoot, obj);
                            issueData.addCurrentFieldData(original, objValue);
                        }
                    } else {
                        issueData.addCurrentFieldData(this.fieldNameService.createFieldNamePath(name), value);
                    }
                }
            }
        });

        return issueData;
    }

    Object fetchFromJsonObject(final FieldNamePath withoutRoot, final JSONObject json) {
        assert json != null;
        assert withoutRoot != null;
        if (withoutRoot.length() == 0) {
            // Later: try to fetch most relevant value. (e.g. by name 'id' or 'name' or 'value'
            return null;
        }

        final FieldName fieldName = withoutRoot.iterator().next();
        assert fieldName != null;
        
        final JSONArray names = json.names();
        final int namesSize = names.length();
        for (int idx = 0; idx < namesSize; idx++) {
            try {
                final String name = names.getString(idx);
                if (name != null && new FieldName(name).equals(fieldName)) {
                    final Object o = json.get(name);
                    if (!(o instanceof JSONArray) && !(o instanceof JSONObject)) {
                        // Later: try to check type. E.g if is String try to parse Datetime.
                        return o;
                    }
                    final FieldNamePath wRoot = withoutRoot.getPathWithoutRoot();
                    if (o instanceof JSONObject) {
                        return fetchFromJsonObject(wRoot, (JSONObject) o);
                    }
                    return fetchFromJsonArray(wRoot, (JSONArray) o);
                }
            } catch (final JSONException ex) {
                return null;
            }
        }
        
        return null;
    }

    Object fetchFromJsonArray(final FieldNamePath withoutRoot, final JSONArray json) {
        assert json != null;
        if (json.length() > 0) {
            try {
                final Object o = json.get(0);
                if (o instanceof JSONObject) {
                    return fetchFromJsonObject(withoutRoot, (JSONObject) o);
                }
                return null;
            } catch (final JSONException ex) {
                return null;
            }

        }
        return null;
    }
    
    FieldNamePath fetchFieldNamePath(final Map<FieldName, FieldNamePath> byRoot, final FieldName id, final FieldName name) {
        assert byRoot != null;
        final FieldNamePath original = byRoot.get(name);
        if (name == null) {
            return byRoot.get(id);
        }
        return original;
    }

    @Override
    public String toString() {
        return "IssueFieldService{" + "timeConverter=" + timeConverter + ", fieldNameService=" + fieldNameService + ", fieldConfig=" + fieldConfig + '}';
    }
}
