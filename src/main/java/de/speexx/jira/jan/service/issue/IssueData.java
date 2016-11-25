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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Container class for all relavant fetched information from a JIRA issue.
 * The container holds current (actual) field information and the history field
 * information from the JIRA issue changelog.
 */
public final class IssueData {
    
    private static final FieldNamePath CREATEDDATE_FIELDNAME_PATH;
    static {
        final FieldNameService service = new FieldNameService();
        CREATEDDATE_FIELDNAME_PATH = service.createFieldNamePath(service.createFieldName("createddate"));
    }
    
    private final Map<FieldNamePath, Object> currentFieldData = new HashMap<>();
    private final Map<FieldName, List<HistoricalDataEntry>> historicalFieldData = new HashMap<>();

    /**
     * Adds the given issue field value to the list of current 
     * @param currentFieldPath the field name path of the current value. Must not be {@code null}.
     * @param currentValue the value for the field name path. Can be {@code null}.
     * @throws NullPointerException if and only if <em>currentFieldPath</em> is {@code null}.
     * @throws IllegalArgumentException if and only if the field name path is of the core path for
     *                                  <tt>createdDate</tt> and the value is not {@code null} or
     *                                  not an instance of {@link LocalDateTime}.
     */
    public void addCurrentFieldData(final FieldNamePath currentFieldPath, final Object currentValue) {
        assert currentFieldData != null;
        Objects.requireNonNull(currentFieldPath, "fieldNamePath is null");
        if (CREATEDDATE_FIELDNAME_PATH.equals(currentFieldPath)) {
            if (currentValue != null && !(currentValue instanceof LocalDateTime)) {
                throw new IllegalArgumentException("Value for path " + CREATEDDATE_FIELDNAME_PATH 
                                                   + " not of type " + LocalDateTime.class);
            }
            this.currentFieldData.put(currentFieldPath, currentValue);
        } else if (currentValue instanceof String) {
            this.currentFieldData.put(currentFieldPath, ((String) currentValue).intern());
        } else {
            this.currentFieldData.put(currentFieldPath, currentValue);
        }
    }

    /**
     * Adds a new historical field value for a <em>historicalFieldName</em>.
     * The implementation prevents from adding a <em>HistoricalDataEntry</em> with
     * same state twice.
     * @param historicalFieldName the field name of the current value. Must not be {@code null}.
     * @param historicalEntry the value for the field name path. Can be {@code null}.
     * @throws NullPointerException if and only if <em>currentFieldPath</em> is {@code null}.
     */
    public void addHistoricalDataEntry(final FieldName historicalFieldName, final HistoricalDataEntry historicalEntry) {
        assert !Objects.isNull(historicalFieldData);
        Objects.requireNonNull(historicalFieldName, "historicalFieldName is null");

        if (historicalEntry != null && !isAvailabilityInHistoricalData(historicalFieldName, historicalEntry)) {
            List<HistoricalDataEntry> entries = this.historicalFieldData.get(historicalFieldName);
            if (entries == null) {
                entries = new ArrayList<>();
                this.historicalFieldData.put(historicalFieldName, entries);
            }

            entries.add(historicalEntry);
        }
    }
    
    boolean isAvailabilityInHistoricalData(final FieldName historicalFieldName, final HistoricalDataEntry historicalEntry) {
        assert !Objects.isNull(historicalFieldName);
        assert !Objects.isNull(historicalEntry);
        assert !Objects.isNull(this.historicalFieldData);
        
        final List<HistoricalDataEntry> historicalData = this.getHistoricalIssueData(historicalFieldName);
        if (historicalData == null) {
            return false;
        }
        return historicalData.contains(historicalEntry);
    }
    
    public Optional<LocalDateTime> getCreatedDate() {
        assert this.currentFieldData != null;
        return Optional.ofNullable((LocalDateTime) this.currentFieldData.get(CREATEDDATE_FIELDNAME_PATH));
    }
    
    public Object getCurrentIssueData(final FieldNamePath path) {
        assert this.currentFieldData != null;
        return this.currentFieldData.get(path);
    }
    
    public int getHistoricalCount() {
        assert this.historicalFieldData != null;
        return this.historicalFieldData.size();
    }

    public List<HistoricalDataEntry> getHistoricalIssueData(final FieldName fieldName) {
        assert this.historicalFieldData != null;
        final List<HistoricalDataEntry> retval = this.historicalFieldData.get(fieldName);
        if (retval == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(retval);
    }

    @Override
    public String toString() {
        return "IssueData{" + "currentFieldData=" + currentFieldData + ", historyFieldData=" + historicalFieldData + '}';
    }

    public final static class HistoricalDataEntry {
        private final String from;
        private final String to;
        private final LocalDateTime changeDate;

        public HistoricalDataEntry(final String from, final LocalDateTime at, final String to) {
            if (Objects.equals(from, to)) {
                throw new IllegalArgumentException("to and from value are equal: " + String.valueOf(to));
            }
            this.from = from != null ? from.intern() : null;
            this.changeDate = Objects.requireNonNull(at, "Changedate is null");
            this.to = to != null ? to.intern() : null;
        }

        public String getFrom() {
            return this.from;
        }

        public String getTo() {
            return this.to;
        }

        public LocalDateTime getChangeDate() {
            return this.changeDate;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + Objects.hashCode(this.from);
            hash = 79 * hash + Objects.hashCode(this.to);
            hash = 79 * hash + Objects.hashCode(this.changeDate);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HistoricalDataEntry other = (HistoricalDataEntry) obj;
            if (!Objects.equals(this.from, other.from)) {
                return false;
            }
            if (!Objects.equals(this.to, other.to)) {
                return false;
            }
            return Objects.equals(this.changeDate, other.changeDate);
        }

        @Override
        public String toString() {
            return "HistoricalDataEntry{" + "from=" + from + ", at=" + changeDate + ", to=" + to + '}';
        }
    }
}
