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
package de.speexx.jira.jan.command.transition;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import de.speexx.jira.jan.Command;
import de.speexx.jira.jan.Config;
import de.speexx.jira.jan.ExecutionContext;
import de.speexx.jira.jan.JiraAnalyzeException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import org.joda.time.DateTime;
import static org.apache.commons.csv.CSVFormat.RFC4180;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.joining;


@Parameters(commandNames = {"transitions"}, commandDescription = "Fetch all transition changes of issues form JIRA and exports into a normalized structure.")
public class IssueTransitionFetcher implements Command {

    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    static final String CREATED_STAGE = "created";
    static final String STATUS_CHANGELOG_ENTRY = "status";

    @Inject @Config
    private ExecutionContext execCtx;

    @Parameter(names = {"-l", "--limit"}, hidden = true, description = "Fetch size limit.")
    private int fetchLimit = 100;

    @Parameter(description = "The query for the transitions. "
                           + "The query should be surrounded with quotation marks or apostrophs. "
                           + "Quotation marks inside the query might be escaped/protected "
                           + "by backslash (reversed solidus) charakter.")
    private List<String> query;

    @Override
    public void execute() {
        
        int count = 0;
        int startIndex = 0;
        int total = -1;
        
        final List<IssueInfo> issueInfos = new ArrayList<>();

        final AtomicBoolean header = new AtomicBoolean(false);
        try (final JiraRestClient restClient = this.execCtx.newJiraClient()) {
            do {
                final String q = getQuery().orElseThrow(() -> new JiraAnalyzeException("No query given for fetching transitions"));
                final SearchResult searchResult = fetchIssues(restClient, q, startIndex);
                total = total == -1 ? searchResult.getTotal() : total;
                startIndex += getFetchLimit();

                for (final Issue searchResultIssue : searchResult.getIssues()) {
                    count++;
                    final Issue issue = fetchIssueForSearchResult(restClient, searchResultIssue);
                    final Iterable<ChangelogGroup> changeLogs = issue.getChangelog();

                    final Optional<IssueInfo> issueInfo = handleChangeLog(changeLogs, issue);
                    issueInfo.ifPresent(info -> {
                            info.issueType = fetchIssueType(issue);
                            info.key = issue.getKey();
                            info.resolution = fetchResolution(issue);
                            info.priority = fetchPriority(issue);
                            info.created = fetchCreationDateTime(issue);
                            issueInfos.add(info);
                            this.execCtx.log("ISSUE INFO: {}", info);
                    });
                }
                this.execCtx.log("total: {} - count: {}", total, count);
            } while (total != count);
        } catch (final IOException e) {
            throw new JiraAnalyzeException(e);
        }
        exportAsCsv(issueInfos, header);
    }

    LocalDateTime fetchCreationDateTime(final Issue issue) {
        assert Objects.nonNull(issue);
        return createLocalDateTime(issue.getCreationDate());
    }
    
    String fetchPriority(final Issue issue) {
        assert Objects.nonNull(issue);
        return issue.getPriority().getName();
    }

    Optional<IssueInfo> handleChangeLog(final Iterable<ChangelogGroup> changeLogs, final Issue issue) {
        assert Objects.nonNull(issue);

        if (changeLogs != null) {
            final IssueInfo info = new IssueInfo();
            for (final ChangelogGroup changeLog : changeLogs) {
                for (final ChangelogItem item : changeLog.getItems()) {
                    final String fieldName = item.getField();
                    
                    if (STATUS_CHANGELOG_ENTRY.equalsIgnoreCase(fieldName)) {
                        final StageInfo si = new StageInfo();

                        final String name = item.getToString();
                        if (name != null) {
                            si.stageName = name;
                            si.stageStart = extractChangeLogCreateDateToAsDateTime(changeLog);
                            info.stageInfos.add(si);
                        }
                    }
                }
            }
            return Optional.of(info);
        } else {
            LOG.debug("No change log in issue {}", issue.getKey());
        }
        return Optional.empty();
    }

    void exportAsCsv(final List<IssueInfo> issues, final AtomicBoolean doHeader) {
        try (final CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), RFC4180)) {
                        
            final String[] header = new String[] {"issue-key", "type", "issue-creation-datetime", "priority", "resolution", "stage", "stage-enter-datetime", "stage-duration"};
            
            if (!doHeader.get()) {
                csvPrinter.printRecord((Object[]) header);
                doHeader.set(true);
            }
            
            issues.forEach(info -> {
                info.stageInfoAsDuration().forEach(stageDuration -> {

                    final String[] values = new String[header.length];
                    values[0] = info.key;
                    values[1] = info.issueType;
                    values[2] = DateTimeFormatter.ISO_DATE_TIME.format(info.created);
                    values[3] = info.priority;
                    values[4] = resolutionAdjustment(info);

                    values[5] = "" + stageDuration.stageName;
                    values[6] = DateTimeFormatter.ISO_DATE_TIME.format(stageDuration.stageStart);
                    values[7] = "" + stageDuration.getDurationSeconds();

                    try {
                        csvPrinter.printRecord((Object[]) values);
                    } catch (final IOException e) {
                       throw new JiraAnalyzeException(e);
                    }
                });
            });
            
        } catch (final IOException e) {
           throw new JiraAnalyzeException(e);
        }
    } 

    static String resolutionAdjustment(final IssueInfo info) {
        return info.resolution != null ? info.resolution : "";
    }

    LocalDateTime extractChangeLogCreateDateToAsDateTime(final ChangelogGroup changeLog) {
        final DateTime dt = changeLog.getCreated();
        return createLocalDateTime(dt);
    }

    Issue fetchIssueForSearchResult(final JiraRestClient restClient, final Issue searchResultIssue) {
        final Set<IssueRestClient.Expandos> expandos = new HashSet<>();
        expandos.add(IssueRestClient.Expandos.NAMES);
        expandos.add(IssueRestClient.Expandos.CHANGELOG);

        final IssueRestClient issueClient = restClient.getIssueClient();
        final Promise<Issue> issueResult = issueClient.getIssue(searchResultIssue.getKey(), expandos);
        return issueResult.claim();
    }

    SearchResult fetchIssues(final JiraRestClient restClient, String q, int startIndex) {
        final SearchRestClient searchClient = restClient.getSearchClient();
        final Promise<SearchResult> results = searchClient.searchJql(q, getFetchLimit(), startIndex, null);
        return results.claim();
    }
    
    int getFetchLimit() {
        return this.fetchLimit;
    }
    
    Optional<String> getQuery() {
        if (this.query == null || this.query.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.query.stream().collect(joining(" ")));
    }

    LocalDate createLocalDate(final DateTime dt) {
        final int year = dt.getYear();
        final int month = dt.getMonthOfYear();
        final int day = dt.getDayOfMonth();
        return LocalDate.of(year, month, day);
    }
    
    LocalDateTime createLocalDateTime(final DateTime dt) {
        final int year = dt.getYear();
        final int month = dt.getMonthOfYear();
        final int day = dt.getDayOfMonth();
        final int hour = dt.getHourOfDay();
        final int minute = dt.getMinuteOfHour();
        final int second = dt.getSecondOfMinute();
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    String fetchIssueType(final Issue issue) {
        if (issue != null) {
            final IssueType type = issue.getIssueType();
            if (type != null) {
                return type.getName().toLowerCase(Locale.ENGLISH).intern();
            }
        }
        return null;
    }

    String fetchResolution(final Issue issue) {
        if (issue != null) {
            final Resolution resolution = issue.getResolution();
            if (resolution != null) {
                return resolution.getName().toLowerCase(Locale.ENGLISH).intern();
            }
        }
        return null;
    }
    
    static final class IssueInfo {
        String issueType;
        String key;
        String priority;
        String resolution;
        LocalDateTime created;
        List<StageInfo> stageInfos = new ArrayList<>();

        @Override
        public String toString() {
            return "IssueInfo{" + "issueType=" + issueType + ", key=" + key + ", priority=" + priority + ", resolution=" + resolution + ", created=" + created + ", stageInfos=" + stageInfos + '}';
        }

        public List<StageDuration> stageInfoAsDuration() {
            final List<StageInfo> workingList = new ArrayList<>(this.stageInfos);
            final List<StageDuration> retval = new ArrayList<>(workingList.size());

            if (!workingList.isEmpty()) {
                workingList.sort((final StageInfo si1, final StageInfo si2) -> si1.stageStart.compareTo(si2.stageStart));
                LocalDateTime lastStart = this.created;

                for (final StageInfo si : workingList) {
                    final StageDuration sd = new StageDuration();
                    sd.stageName = si.stageName;
                    sd.stageStart = si.stageStart;
                    sd.stageDuration = Duration.between(lastStart, si.stageStart);
                    lastStart = si.stageStart;
                    retval.add(sd);
                }
            }
            return retval;
        }
    }
    
    static class StageDuration extends StageInfo {
        Duration stageDuration;

        @Override
        public String toString() {
            return "StageDuration{" + "stageStart=" + stageStart + ", stageName=" + stageName + ", stageDuration=" + this.stageDuration + "}";
        }
        
        public long getDurationSeconds() {
            return this.stageDuration.toMillis() / 1_000;
        }
    }
    
    static class StageInfo {
        LocalDateTime stageStart;
        String stageName;

        @Override
        public String toString() {
            return "StageInfo{" + "stageStart=" + stageStart + ", stageName=" + stageName + '}';
        }
    }
}
