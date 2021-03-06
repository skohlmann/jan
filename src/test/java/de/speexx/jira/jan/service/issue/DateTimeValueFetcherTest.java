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
import org.joda.time.DateTime;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class DateTimeValueFetcherTest {
    
    @Test
    public void test_simple_get() {
        assertEquals(LocalDateTime.class, new DateTimeValueFetcher().getValue(new DateTime()).get().getClass());
    }
    
    @Test
    public void test_null() {
        assertFalse(new DateTimeValueFetcher().getValue(null).isPresent());
    }
    
    @Test
    public void test_wrong_type() {
        final Throwable exception = assertThrows(UnsupportedSourceTypeException.class, () -> {
            new DateTimeValueFetcher().getValue("No Joda DateTime");
        });
        assertEquals("Value type class java.lang.String not supported by de.speexx.jira.jan.service.issue.DateTimeValueFetcher", exception.getMessage());
    }
}
