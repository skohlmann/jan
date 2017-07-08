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

import static de.speexx.jira.jan.service.issue.IssueData.HistoricalDataEntry;
import java.time.LocalDateTime;
import static java.time.LocalDateTime.now;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class HistoricalDataEntryTest {
    
    @Test
    public void test_creation() {
        final LocalDateTime dt = now();
        final HistoricalDataEntry entry = new HistoricalDataEntry("from", dt, "to");
        
        assertEquals("from", entry.getFrom());
        assertEquals("to", entry.getTo());
        assertEquals(dt, entry.getChangeDate());
    }

    @Test
    public void test_creation_with_change_date_null() {
        final Throwable exception = assertThrows(NullPointerException.class, () -> {
            new HistoricalDataEntry("from", null, "to");
        });
        assertEquals("Changedate is null", exception.getMessage());
    }
}
