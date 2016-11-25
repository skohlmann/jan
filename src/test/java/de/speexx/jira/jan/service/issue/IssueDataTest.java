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

import  java.time.LocalDateTime;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;
import java.time.Month;
import static org.junit.jupiter.api.Assertions.expectThrows;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class IssueDataTest {
    
    private static final FieldName FIELDNAME_CREATEDDATE = new FieldNameService().createFieldName("createdDate");
    private static final FieldName FIELDNAME_SIMPLE = new FieldNameService().createFieldName("simple");
    private static final FieldNamePath FIELDNAME_PATH_CREATEDDATE = new FieldNameService().createFieldNamePath(FIELDNAME_CREATEDDATE);
    private static final FieldNamePath FIELDNAME_PATH_SIMPLE = new FieldNameService().createFieldNamePath(FIELDNAME_SIMPLE);

    @Test
    public void test_simple_addCurrentFieldData() {
        final IssueData data = new IssueData();
        final String simpleValue = "simple string";
        data.addCurrentFieldData(FIELDNAME_PATH_SIMPLE, simpleValue);
        
        assertSame(simpleValue, data.getCurrentIssueData(FIELDNAME_PATH_SIMPLE));
    }

    @Test
    public void test_simple_addCurrentFieldData_twice_with_different_string_instances() {
        final IssueData data = new IssueData();
        final String simpleValue = "simple string";
        data.addCurrentFieldData(FIELDNAME_PATH_SIMPLE, simpleValue);
        data.addCurrentFieldData(FIELDNAME_PATH_SIMPLE, "simple string");
        
        assertSame(simpleValue, data.getCurrentIssueData(FIELDNAME_PATH_SIMPLE));
    }

    @Test
    public void test_simple_addCurrentFieldData_twice_with_different_LocalDateTime_instances() {
        final IssueData data = new IssueData();
        final LocalDateTime simpleValue = of(2016, Month.DECEMBER, 24, 12, 13);
        data.addCurrentFieldData(FIELDNAME_PATH_CREATEDDATE, simpleValue);
        data.addCurrentFieldData(FIELDNAME_PATH_CREATEDDATE, of(2016, Month.DECEMBER, 24, 12, 13));
        
        assertNotSame(simpleValue, data.getCurrentIssueData(FIELDNAME_PATH_CREATEDDATE));
        assertTrue(data.getCreatedDate().isPresent());
    }
    
    @Test
    public void test_simple_addCurrentFieldData_with_null_value() {
        final IssueData data = new IssueData();
        data.addCurrentFieldData(FIELDNAME_PATH_CREATEDDATE, null);
        
        assertNull(data.getCurrentIssueData(FIELDNAME_PATH_CREATEDDATE));
        assertFalse(data.getCreatedDate().isPresent());
    }
    
    @Test
    public void test_simple_addCurrentFieldData_with_null_key() {
        final IssueData data = new IssueData();
        final Throwable exception = expectThrows(NullPointerException.class, () -> {
            data.addCurrentFieldData(null, data);
        });
        assertEquals("fieldNamePath is null", exception.getMessage());
    }

    @Test
    public void test_simple_addCurrentFieldData_with_key_createdDate_but_illegal_value_type() {
        final IssueData data = new IssueData();
        final Throwable exception = expectThrows(IllegalArgumentException.class, () -> {
            data.addCurrentFieldData(FIELDNAME_PATH_CREATEDDATE, new Object());
        });
        assertTrue(exception.getMessage().startsWith("Value for path "));
    }
    
    @Test
    public void test_addHistoricalDataEntry() {
        final IssueData data = new IssueData();
        final IssueData.HistoricalDataEntry entry = new IssueData.HistoricalDataEntry("from", LocalDateTime.now(), "to");
        data.addHistoricalDataEntry(FIELDNAME_SIMPLE, entry);

        assertSame(entry, data.getHistoricalIssueData(FIELDNAME_SIMPLE).get(0));
    }

    @Test
    public void test_addHistoricalDataEntry_twice() {
        final IssueData data = new IssueData();
        final LocalDateTime now = now();
        final IssueData.HistoricalDataEntry entryFirst = new IssueData.HistoricalDataEntry("from", now, "to");
        final IssueData.HistoricalDataEntry entrySecond = new IssueData.HistoricalDataEntry("from", now, "to");
        data.addHistoricalDataEntry(FIELDNAME_SIMPLE, entryFirst);
        data.addHistoricalDataEntry(FIELDNAME_SIMPLE, entrySecond);

        assertTrue(1 == data.getHistoricalIssueData(FIELDNAME_SIMPLE).size());
        assertSame(entryFirst, data.getHistoricalIssueData(FIELDNAME_SIMPLE).get(0));
    }

    @Test
    public void test_addHistoricalDataEntry_two() {
        final IssueData data = new IssueData();
        final IssueData.HistoricalDataEntry entryFirst = new IssueData.HistoricalDataEntry("from", now(), "to");
        final IssueData.HistoricalDataEntry entrySecond = new IssueData.HistoricalDataEntry("to", now(), "from");
        data.addHistoricalDataEntry(FIELDNAME_SIMPLE, entryFirst);
        data.addHistoricalDataEntry(FIELDNAME_SIMPLE, entrySecond);

        assertTrue(2 == data.getHistoricalIssueData(FIELDNAME_SIMPLE).size());
    }

    @Test
    public void test_simple_addHistoricalDataEntry_with_null_key() {
        final IssueData data = new IssueData();
        final Throwable exception = expectThrows(NullPointerException.class, () -> {
            final IssueData.HistoricalDataEntry entry = new IssueData.HistoricalDataEntry("from", now(), "to");
            data.addHistoricalDataEntry(null, entry);
        });
        assertEquals("historicalFieldName is null", exception.getMessage());
    }

    @Test
    public void test_addHistoricalDataEntry_with_two_different_fieldnames() {
        final IssueData data = new IssueData();
        final LocalDateTime now = now();
        final IssueData.HistoricalDataEntry entryFirst = new IssueData.HistoricalDataEntry("from", now, "to");
        final IssueData.HistoricalDataEntry entrySecond = new IssueData.HistoricalDataEntry("from", now, "to");
        data.addHistoricalDataEntry(FIELDNAME_SIMPLE, entryFirst);
        data.addHistoricalDataEntry(FIELDNAME_CREATEDDATE, entrySecond);

        assertTrue(2 == data.getHistoricalCount());
    }

}
