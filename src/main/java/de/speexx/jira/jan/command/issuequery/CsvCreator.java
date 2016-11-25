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

import de.speexx.jira.jan.JiraAnalyzeException;
import de.speexx.jira.jan.service.issue.FieldName;
import de.speexx.jira.jan.service.issue.FieldNamePath;
import de.speexx.jira.jan.service.issue.IssueData;
import de.speexx.jira.jan.service.issue.IssueData.HistoricalDataEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import static de.speexx.jira.jan.command.issuequery.TemporalChangeOutput.BOTH;
import static de.speexx.jira.jan.command.issuequery.TemporalChangeOutput.NONE;
import static de.speexx.jira.jan.command.issuequery.TemporalChangeOutput.TIME;
import static de.speexx.jira.jan.command.issuequery.TemporalChangeOutput.DURATION;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import static org.apache.commons.csv.CSVFormat.RFC4180;
import org.apache.commons.csv.CSVPrinter;


class CsvCreator {
    
    static final String EMPTY = "";
    static final String HOSTORICAL_TO_PREFIX = "to_";
    static final String HOSTORICAL_CHANGE_DATETIME_PREFIX = "at_";
    static final String HOSTORICAL_FROM_PREFIX = "from_";
    static final String HOSTORICAL_DURATION_PREFIX = "duration_";
    static final String FIELDNAMEPATH_DELIMITER_REPALCEMENT = "_";
    static final int MILLIS = 1000;

    public void printIssueData(final IssueData issueData,
                               final List<FieldName> historyFieldNames,
                               final List<FieldNamePath> currentFieldNames,
                               final TemporalChangeOutput temporalOutput,
                               final AtomicBoolean header) {
        checkParameter(issueData, historyFieldNames, currentFieldNames, temporalOutput, header);

        if (header.get()) {
            printHeader(currentFieldNames, historyFieldNames, temporalOutput);
            header.set(false);
        }
        
        printIssueData(issueData, currentFieldNames, historyFieldNames, temporalOutput);
    }

    void checkParameter(final IssueData issueData,
                        final List<FieldName> historyFieldNames,
                        final List<FieldNamePath> currentFieldNames,
                        final TemporalChangeOutput temporalOutput,
                        final AtomicBoolean header) {
        Objects.requireNonNull(issueData, "issueData is null");
        Objects.requireNonNull(historyFieldNames, "historyFieldNames is null");
        Objects.requireNonNull(currentFieldNames, "currentFieldNames is null");
        Objects.requireNonNull(temporalOutput, "temporalOutput is null");
        Objects.requireNonNull(header, "header is null");
    }
    
    int calculateHistoricalFieldSize(final TemporalChangeOutput temporalOutput) {
        assert temporalOutput != null;
        switch (temporalOutput) {
            case DURATION: return 2 + 1;
            case TIME: return 2 + 1;
            case NONE: return 2;
            case BOTH: return 2 + 2;
            default: throw new IllegalStateException("Unsupported TemporalChangeOutput: " + temporalOutput);
        }
    }
    
    void printIssueData(final IssueData issueData,
                        final List<FieldNamePath> currentFieldNames,
                        final List<FieldName> historyFieldNames,
                        final TemporalChangeOutput temporalOutput) {
        assert !Objects.isNull(issueData);
        assert !Objects.isNull(currentFieldNames);
        assert !Objects.isNull(historyFieldNames);
        assert !Objects.isNull(temporalOutput);
        
        final List<String> currentFieldEntries = fetchCurrentFieldEntries(issueData, currentFieldNames);

        try {
            final CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), RFC4180);

            if (issueData.getHistoricalCount() == 0) {
                final int fieldsPerChangeEntry = calculateHistoricalFieldSize(temporalOutput);
                final int max = historyFieldNames.size() * fieldsPerChangeEntry;
                final List<String> out = new ArrayList(currentFieldEntries);
                addEmptyChangeData(out, max);
                csvPrinter.printRecord(out);
                
            } else {
                final int fieldsPerChangeEntry = calculateHistoricalFieldSize(temporalOutput);
                final int historyFieldNamesSize = historyFieldNames.size();

                for (int idx = 0; idx < historyFieldNamesSize; idx++) {
                    final FieldName fieldName = historyFieldNames.get(idx);

                    final List<HistoricalDataEntry> historicalData = issueData.getHistoricalIssueData(fieldName);
                    LocalDateTime lastChangeDate =
                            issueData.getCreatedDate().orElseThrow(() -> new IllegalStateException("No createdDate available"));

                    for (final HistoricalDataEntry entry : historicalData) {
                        final List<String> out = new ArrayList();
                        for (int i = 0; i < historyFieldNamesSize; i++) {
                            if (i != idx) {
                                addEmptyChangeData(out, fieldsPerChangeEntry);
                            } else {
                                lastChangeDate = addChangeData(out, entry, temporalOutput, lastChangeDate);
                            }
                        }
                        final List<String> outList = new ArrayList<>(currentFieldEntries);
                        outList.addAll(out);
                        csvPrinter.printRecord(outList.toArray());
                    }
                }
            }

            csvPrinter.flush();
        } catch (final IOException e) {
            throw new JiraAnalyzeException(e);
        }
    }

    LocalDateTime addChangeData(final List<String> out, final HistoricalDataEntry entry, final TemporalChangeOutput temporalOutput, final LocalDateTime lastChangeDate) {
        out.add(entry.getFrom());
        final LocalDateTime changeDate = entry.getChangeDate();
        if (temporalOutput == TIME || temporalOutput == BOTH) {
            out.add(localDateTimeToString(entry.getChangeDate()));
        }
        if (temporalOutput == DURATION || temporalOutput == BOTH) {
            final Duration duration = Duration.between(lastChangeDate, changeDate);
            final String dur = String.valueOf(duration.toMillis() /*/ MILLIS*/);
            out.add(dur);
        }
        out.add(entry.getTo());
        return changeDate;
    }

    void addEmptyChangeData(final List<String> out, final int fieldsPerChangeEntry) {
        assert fieldsPerChangeEntry >= 0;
        assert out != null;
        for (int c = 0; c < fieldsPerChangeEntry; c++) {
            out.add(EMPTY);
        }
    }
    
    List<String> fetchCurrentFieldEntries(final IssueData issueData, final List<FieldNamePath> currentFieldNames) {
        assert !Objects.isNull(issueData);
        assert !Objects.isNull(currentFieldNames);
        
        final List<String> retval = new ArrayList<>();
        
        currentFieldNames.stream().map(path -> issueData.getCurrentIssueData(path)).forEach(value -> {
            retval.add(objectToString(value));
        });
        
        return Collections.unmodifiableList(retval);
    }

    String objectToString(final Object o) {
        if (o == null) {
            return EMPTY;
        } else if (o instanceof Double || o instanceof Float) {
            return String.format("#.########", o);
        } else if (o instanceof LocalDateTime) {
            return localDateTimeToString((LocalDateTime) o);
        }
        return String.valueOf(o);
    }
    
    String localDateTimeToString(final LocalDateTime dt) {
        assert dt != null;
        return DateTimeFormatter.ISO_DATE_TIME.format(dt);
    }
        
    void printHeader(final List<FieldNamePath> currentFieldNames,
                     final List<FieldName> historyFieldNames,
                     final TemporalChangeOutput temporalOutput) {
        assert !Objects.isNull(currentFieldNames);
        assert !Objects.isNull(historyFieldNames);
        assert !Objects.isNull(temporalOutput);
        
        final List<String> headerNames = new ArrayList<>();
        currentFieldNames.stream().map(FieldNamePath::asString)
                                  .map(name -> name.replaceAll(FieldNamePath.DELIMITER, FIELDNAMEPATH_DELIMITER_REPALCEMENT))
                                  .forEach(name -> headerNames.add(name));
        historyFieldNames.stream().map(FieldName::asString)
                                  .forEach(name -> {
                                      headerNames.add(HOSTORICAL_FROM_PREFIX + name);
                                      if (temporalOutput != NONE) {
                                          if (temporalOutput == BOTH || temporalOutput == TIME) {
                                              headerNames.add(HOSTORICAL_CHANGE_DATETIME_PREFIX + name);
                                          }
                                          if (temporalOutput == BOTH || temporalOutput == DURATION) {
                                              headerNames.add(HOSTORICAL_DURATION_PREFIX + name);
                                          }
                                      }
                                      headerNames.add(HOSTORICAL_TO_PREFIX + name);
                                  });
        try {
            final CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), RFC4180);
            csvPrinter.printRecord(headerNames.toArray());
            csvPrinter.flush();
        } catch (final IOException e) {
            throw new JiraAnalyzeException(e);
        }
    }
}
